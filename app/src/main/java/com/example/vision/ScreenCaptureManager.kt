package com.example.vision

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenCaptureManager(private val context: Context) {

    private val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val _isScreenCaptureActive = MutableStateFlow(false)
    val isScreenCaptureActive: StateFlow<Boolean> = _isScreenCaptureActive.asStateFlow()

    @Volatile
    var latestBitmap: Bitmap? = null
        private set

    fun createScreenCaptureIntent(): Intent? {
        return projectionManager?.createScreenCaptureIntent()
    }

    fun startCapture(resultCode: Int, data: Intent) {
        if (resultCode != Activity.RESULT_OK) {
            Log.w("ScreenCaptureManager", "User denied screen capture permission")
            return
        }

        try {
            // Start foreground service first
            val serviceIntent = Intent(context, ScreenCaptureService::class.java).apply {
                action = ScreenCaptureService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            mediaProjection = projectionManager?.getMediaProjection(resultCode, data)
            if (mediaProjection == null) {
                Log.e("ScreenCaptureManager", "Failed to obtain MediaProjection")
                return
            }

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)

            val width = (metrics.widthPixels / 2).coerceAtLeast(360)
            val height = (metrics.heightPixels / 2).coerceAtLeast(640)
            val density = metrics.densityDpi

            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "TitonoxVisionDisplay",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                Handler(Looper.getMainLooper())
            )

            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                if (image != null) {
                    try {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        val bmp = Bitmap.createBitmap(
                            width + rowPadding / pixelStride,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        bmp.copyPixelsFromBuffer(buffer)
                        latestBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height)
                    } catch (e: Exception) {
                        Log.e("ScreenCaptureManager", "Error processing screen frame", e)
                    } finally {
                        image.close()
                    }
                }
            }, Handler(Looper.getMainLooper()))

            _isScreenCaptureActive.value = true
            Log.i("ScreenCaptureManager", "Screen capture successfully initiated")
        } catch (e: Exception) {
            Log.e("ScreenCaptureManager", "Failed to start screen capture", e)
            stopCapture()
        }
    }

    fun stopCapture() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            mediaProjection?.stop()
            mediaProjection = null

            val serviceIntent = Intent(context, ScreenCaptureService::class.java).apply {
                action = ScreenCaptureService.ACTION_STOP
            }
            context.startService(serviceIntent)
        } catch (e: Exception) {
            Log.e("ScreenCaptureManager", "Error stopping screen capture", e)
        } finally {
            _isScreenCaptureActive.value = false
            latestBitmap = null
        }
    }
}
