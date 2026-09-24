package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.floating.VoicePersona
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceAssistantManager(
    private val context: Context,
    private val onVoicePowerOff: (() -> Unit)? = null,
    private val onVoiceResult: (String) -> Unit
) : TextToSpeech.OnInitListener {

    constructor(context: Context, onVoiceResult: (String) -> Unit) : this(context, null, onVoiceResult)

    private val TAG = "VoiceAssistantManager"
    private val managerScope = CoroutineScope(Dispatchers.Main + Job())

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var speechModulationJob: Job? = null

    private var currentPersona: VoicePersona = VoicePersona.CORE
    private var currentSpeechRate: Float = 1.05f
    private var currentSpeechPitch: Float = 1.0f

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRms.value = rmsdB.coerceIn(0f, 10f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioRms.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioRms.value = 0f
                        Log.w(TAG, "SpeechRecognizer error: $error")
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioRms.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty()) {
                            val lower = text.lowercase()
                            if (lower.contains("wake off") || lower.contains("turn yourself off") || lower.contains("stop listening") || lower.contains("stop titonox")) {
                                speak("TITONOX powering down. Assistant off.")
                                onVoicePowerOff?.invoke()
                            } else {
                                onVoiceResult(text)
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening(languageLocale: String = "en-US") {
        if (_isSpeaking.value) {
            stopSpeaking()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageLocale)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop listening", e)
        } finally {
            _isListening.value = false
            _audioRms.value = 0f
        }
    }

    fun setVoicePersona(persona: VoicePersona, rate: Float? = null, pitch: Float? = null, localeTag: String? = null) {
        currentPersona = persona
        currentSpeechRate = rate ?: persona.defaultRate
        currentSpeechPitch = pitch ?: persona.defaultPitch

        if (isTtsInitialized) {
            tts?.setSpeechRate(currentSpeechRate)
            tts?.setPitch(currentSpeechPitch)
            if (!localeTag.isNullOrBlank()) {
                val loc = try {
                    Locale.forLanguageTag(localeTag)
                } catch (e: Exception) {
                    Locale.US
                }
                tts?.language = loc
            }
        }
    }

    fun testVoicePersona(persona: VoicePersona) {
        setVoicePersona(persona)
        speak("TITONOX ${persona.displayName} voice synthesizer calibrated and operational.")
    }

    fun speak(text: String, flush: Boolean = true) {
        if (_isMuted.value || !isTtsInitialized || text.isBlank()) return

        val mode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = "titonox_${System.currentTimeMillis()}"

        tts?.speak(text, mode, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        speechModulationJob?.cancel()
        _audioRms.value = 0f
    }

    fun toggleMute(): Boolean {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            stopSpeaking()
        }
        return _isMuted.value
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.language = Locale.US
            tts?.setSpeechRate(currentSpeechRate)
            tts?.setPitch(currentSpeechPitch)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startSpeechAudioModulation()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    speechModulationJob?.cancel()
                    _audioRms.value = 0f
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    speechModulationJob?.cancel()
                    _audioRms.value = 0f
                }
            })
        }
    }

    private fun startSpeechAudioModulation() {
        speechModulationJob?.cancel()
        speechModulationJob = managerScope.launch {
            while (isActive && _isSpeaking.value) {
                val time = System.currentTimeMillis()
                // Natural speech formant envelope modulation
                val mod = 3.5f + (kotlin.math.sin(time * 0.018) * 2.2f + kotlin.math.cos(time * 0.035) * 1.5f).toFloat()
                _audioRms.value = mod.coerceIn(1.0f, 9.5f)
                delay(60)
            }
            _audioRms.value = 0f
        }
    }

    fun destroy() {
        try {
            speechModulationJob?.cancel()
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying VoiceAssistantManager", e)
        }
    }
}
