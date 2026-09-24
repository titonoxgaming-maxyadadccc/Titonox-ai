package com.example.floating

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class FloatingOrbService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var floatingView: ComposeView? = null
    private var params: WindowManager.LayoutParams? = null

    private lateinit var settingsRepo: OrbSettingsRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var voiceManager: VoiceAssistantManager? = null

    // Touch & Drag state
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var lastTapTime = 0L

    // Long press 4-second shutdown timer
    private var powerOffJob: Job? = null
    private val countdownFlow = MutableStateFlow<Int?>(null)
    private val isQuickPanelOpen = MutableStateFlow(false)

    companion object {
        const val CHANNEL_ID = "titonox_floating_assistant"
        const val NOTIFICATION_ID = 4040

        private val _isAppInForeground = MutableStateFlow(false)
        val isAppInForeground = _isAppInForeground.asStateFlow()

        private val _activeTaskBanner = MutableStateFlow<String?>(null)
        val activeTaskBanner = _activeTaskBanner.asStateFlow()

        fun setAppForegroundState(inForeground: Boolean) {
            _isAppInForeground.value = inForeground
        }

        fun updateTaskBanner(task: String?) {
            _activeTaskBanner.value = task
        }

        fun startService(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, FloatingOrbService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FloatingOrbService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        settingsRepo = OrbSettingsRepository.getInstance(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        voiceManager = VoiceAssistantManager(this) { spokenText ->
            handleSpokenCommand(spokenText)
        }

        setupOverlay()
        observeAppForeground()
    }

    private fun observeAppForeground() {
        serviceScope.launch {
            _isAppInForeground.collect { inForeground ->
                floatingView?.let { view ->
                    val shouldHide = inForeground || settingsRepo.isPowerOff.value || !settingsRepo.settings.value.isAssistantEnabled
                    view.visibility = if (shouldHide) View.GONE else View.VISIBLE
                }
            }
        }
        serviceScope.launch {
            settingsRepo.isPowerOff.collect { powerOff ->
                floatingView?.let { view ->
                    val shouldHide = _isAppInForeground.value || powerOff || !settingsRepo.settings.value.isAssistantEnabled
                    view.visibility = if (shouldHide) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private data class SafeBounds(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int)

    private fun calculateSafeBounds(viewWidth: Int, viewHeight: Int): SafeBounds {
        val wm = windowManager ?: return SafeBounds(16, 800, 48, 1600)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = wm.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.statusBars() or
                WindowInsets.Type.navigationBars() or
                WindowInsets.Type.displayCutout()
            )
            val bounds = metrics.bounds
            val minX = insets.left + 16
            val maxX = (bounds.width() - insets.right - viewWidth - 16).coerceAtLeast(minX)
            val minY = insets.top + 16
            val maxY = (bounds.height() - insets.bottom - viewHeight - 16).coerceAtLeast(minY)
            SafeBounds(minX, maxX, minY, maxY)
        } else {
            val dm = resources.displayMetrics
            val statusResId = resources.getIdentifier("status_bar_height", "dimen", "android")
            val statusH = if (statusResId > 0) resources.getDimensionPixelSize(statusResId) else 72
            val navResId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            val navH = if (navResId > 0) resources.getDimensionPixelSize(navResId) else 96
            val minX = 16
            val maxX = (dm.widthPixels - viewWidth - 16).coerceAtLeast(minX)
            val minY = statusH + 16
            val maxY = (dm.heightPixels - navH - viewHeight - 16).coerceAtLeast(minY)
            SafeBounds(minX, maxX, minY, maxY)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOverlay() {
        if (!Settings.canDrawOverlays(this)) return

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val initialSize = (settingsRepo.settings.value.sizeDp * resources.displayMetrics.density).toInt()
        val bounds = calculateSafeBounds(initialSize, initialSize)
        val posX = settingsRepo.lastPosX.value.coerceIn(bounds.minX, bounds.maxX)
        val posY = settingsRepo.lastPosY.value.coerceIn(bounds.minY, bounds.maxY)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = posX
            y = posY
        }

        floatingView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingOrbService)
            setViewTreeSavedStateRegistryOwner(this@FloatingOrbService)

            setContent {
                val customization by settingsRepo.settings.collectAsState()
                val isListening by voiceManager?.isListening?.collectAsState() ?: remember { mutableStateOf(false) }
                val isSpeaking by voiceManager?.isSpeaking?.collectAsState() ?: remember { mutableStateOf(false) }
                val isMuted by voiceManager?.isMuted?.collectAsState() ?: remember { mutableStateOf(false) }
                val audioRms by voiceManager?.audioRms?.collectAsState() ?: remember { mutableStateOf(0f) }
                val countdown by countdownFlow.collectAsState()
                val showQuickPanel by isQuickPanelOpen.collectAsState()
                val taskBanner by _activeTaskBanner.collectAsState()

                val orbState = when {
                    customization.isPowerOff -> OrbState.OFF
                    isListening -> OrbState.LISTENING
                    isSpeaking -> OrbState.SPEAKING
                    taskBanner != null -> OrbState.EXECUTING
                    else -> OrbState.IDLE
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentSize()
                ) {
                    if (showQuickPanel) {
                        FloatingQuickPanel(
                            state = orbState,
                            isListening = isListening,
                            isMuted = isMuted,
                            activeTaskText = taskBanner,
                            onToggleMic = {
                                if (isListening) voiceManager?.stopListening() else voiceManager?.startListening()
                            },
                            onToggleMute = {
                                voiceManager?.toggleMute()
                            },
                            onAbortTask = {
                                voiceManager?.stopSpeaking()
                                voiceManager?.stopListening()
                                updateTaskBanner(null)
                            },
                            onOpenApp = {
                                openMainApp()
                                isQuickPanelOpen.value = false
                            },
                            onOpenOrbStudio = {
                                openOrbStudio()
                                isQuickPanelOpen.value = false
                            },
                            onClose = {
                                isQuickPanelOpen.value = false
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(customization.sizeDp.dp)
                    ) {
                        OrbMasterCanvas(
                            customization = customization,
                            state = orbState,
                            audioLevel = audioRms / 10f,
                            countdownSeconds = countdown,
                            modifier = Modifier.size(customization.sizeDp.dp)
                        )
                    }
                }
            }

            setOnTouchListener { _, event ->
                handleTouchEvent(event)
            }
        }

        try {
            windowManager?.addView(floatingView, params)
            val shouldHide = _isAppInForeground.value || settingsRepo.isPowerOff.value || !settingsRepo.settings.value.isAssistantEnabled
            floatingView?.visibility = if (shouldHide) View.GONE else View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val wm = windowManager ?: return false
        val lp = params ?: return false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        val metrics = resources.displayMetrics
        val orbSizePx = (settingsRepo.settings.value.sizeDp * metrics.density).toInt()
        val bounds = calculateSafeBounds(orbSizePx, orbSizePx)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = lp.x
                initialY = lp.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false

                startPowerOffHoldTimer()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - initialTouchX).toInt()
                val dy = (event.rawY - initialTouchY).toInt()

                if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                    if (!isDragging) {
                        isDragging = true
                        cancelPowerOffHoldTimer()
                    }
                    lp.x = (initialX + dx).coerceIn(bounds.minX, bounds.maxX)
                    lp.y = (initialY + dy).coerceIn(bounds.minY, bounds.maxY)
                    wm.updateViewLayout(floatingView, lp)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                cancelPowerOffHoldTimer()

                if (!isDragging) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < 350) {
                        // Double Tap -> Toggle listening
                        if (voiceManager?.isListening?.value == true) {
                            voiceManager?.stopListening()
                        } else {
                            voiceManager?.startListening()
                        }
                    } else {
                        // Single Tap -> Toggle quick panel
                        isQuickPanelOpen.value = !isQuickPanelOpen.value
                    }
                    lastTapTime = currentTime
                } else {
                    // Finished dragging - Snap to nearest edge if enabled
                    if (settingsRepo.settings.value.snapToEdge) {
                        val midX = (bounds.minX + bounds.maxX) / 2
                        val targetX = if (lp.x + orbSizePx / 2 < midX) bounds.minX else bounds.maxX
                        lp.x = targetX
                        wm.updateViewLayout(floatingView, lp)
                    }
                    settingsRepo.savePosition(lp.x, lp.y)
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelPowerOffHoldTimer()
                return true
            }
        }
        return false
    }

    private fun startPowerOffHoldTimer() {
        powerOffJob?.cancel()
        powerOffJob = serviceScope.launch {
            delay(500) // Initial delay before countdown starts
            for (sec in 4 downTo 1) {
                countdownFlow.value = sec
                vibrateTick()
                delay(1000)
            }
            // 4 SECONDS COMPLETED -> TRIGGER TITONOX POWER OFF
            countdownFlow.value = null
            triggerPowerOff()
        }
    }

    private fun cancelPowerOffHoldTimer() {
        powerOffJob?.cancel()
        powerOffJob = null
        countdownFlow.value = null
    }

    private fun triggerPowerOff() {
        vibratePowerOff()
        voiceManager?.stopListening()
        voiceManager?.stopSpeaking()
        settingsRepo.setPowerOff(true)
        floatingView?.visibility = View.GONE
    }

    private fun handleSpokenCommand(spokenText: String) {
        val lower = spokenText.trim().lowercase()

        // Voice Power-Off check
        if (lower.contains("wake off") || lower.contains("turn yourself off") || lower.contains("stop listening") || lower.contains("stop titonox")) {
            voiceManager?.speak("TITONOX powering down. Assistant off.")
            serviceScope.launch {
                delay(1500)
                triggerPowerOff()
            }
            return
        }

        // Direct app opening
        if (lower.contains("open titonox") || lower.contains("open assistant")) {
            openMainApp()
            return
        }

        // Default quick voice command feedback
        _activeTaskBanner.value = "Executing: $spokenText"
        voiceManager?.speak("Received: $spokenText. Processing in TITONOX.")
        serviceScope.launch {
            delay(4000)
            _activeTaskBanner.value = null
        }
    }

    private fun openMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun openOrbStudio() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_ORB_STUDIO", true)
        }
        startActivity(intent)
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun vibrateTick() {
        val v = getVibrator()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v?.vibrate(50)
        }
    }

    private fun vibratePowerOff() {
        val v = getVibrator()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 300), -1))
        } else {
            @Suppress("DEPRECATION")
            v?.vibrate(400)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TITONOX Floating Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "System-wide floating AI assistant overlay"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TITONOX Assistant Online")
            .setContentText("Floating AI assistant running. Tap to open command center.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        voiceManager?.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
