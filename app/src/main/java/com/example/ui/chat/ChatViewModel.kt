package com.example.ui.chat

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.KainatAiRepository
import com.example.ui.components.OrbState
import com.google.firebase.ai.Chat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale
import java.util.UUID

private const val TAG = "ChatViewModel"

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            isUser = false,
            text = "Kainat AI command core initialized. کائنات اے آئی آن لائن ہے۔ State your directive or engage the voice orb."
        )
    ),
    val inputText: String = "",
    val orbState: OrbState = OrbState.IDLE,
    val audioRmsLevel: Float = 0f,
    val isAutoSpeakEnabled: Boolean = true,
    val isTtsReady: Boolean = false,
    val errorMessage: String? = null,
    val speakingMessageId: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KainatAiRepository()
    private var chatSession: Chat = repository.createChatSession()
    private var activeGenerationJob: Job? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    engine.language = Locale.US
                    // Authoritative command-center cadence: slightly deeper pitch, measured rate
                    engine.setPitch(0.88f)
                    engine.setSpeechRate(0.96f)
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _uiState.update {
                                it.copy(
                                    orbState = OrbState.SPEAKING,
                                    speakingMessageId = utteranceId
                                )
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            _uiState.update { state ->
                                val nextOrb = if (state.orbState == OrbState.SPEAKING) OrbState.IDLE else state.orbState
                                state.copy(orbState = nextOrb, speakingMessageId = null)
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _uiState.update { state ->
                                val nextOrb = if (state.orbState == OrbState.SPEAKING) OrbState.IDLE else state.orbState
                                state.copy(orbState = nextOrb, speakingMessageId = null)
                            }
                        }
                    })
                    _uiState.update { it.copy(isTtsReady = true) }
                }
            } else {
                Log.w(TAG, "TextToSpeech initialization failed with status=$status")
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun toggleAutoSpeak() {
        val next = !_uiState.value.isAutoSpeakEnabled
        if (!next) {
            stopSpeaking()
        }
        _uiState.update { it.copy(isAutoSpeakEnabled = next) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSession() {
        activeGenerationJob?.cancel()
        stopListening()
        stopSpeaking()
        chatSession = repository.createChatSession()
        _uiState.update {
            ChatUiState(
                isAutoSpeakEnabled = it.isAutoSpeakEnabled,
                isTtsReady = it.isTtsReady
            )
        }
    }

    fun sendMessage(overridePrompt: String? = null) {
        val prompt = (overridePrompt ?: _uiState.value.inputText).trim()
        if (prompt.isEmpty()) return

        stopListening()
        stopSpeaking()

        val userMsg = ChatMessage(isUser = true, text = prompt)
        val assistantMsgId = UUID.randomUUID().toString()
        val placeholderAssistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "",
            isStreaming = true
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg + placeholderAssistantMsg,
                inputText = if (overridePrompt == null) "" else it.inputText,
                orbState = OrbState.THINKING,
                errorMessage = null
            )
        }

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            val builder = StringBuilder()
            try {
                withTimeout(60_000L) {
                    repository.sendChatMessageStream(chatSession, prompt).collect { chunk ->
                        builder.append(chunk)
                        val partial = builder.toString()
                        _uiState.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    if (msg.id == assistantMsgId) msg.copy(text = partial) else msg
                                }
                            )
                        }
                    }
                }

                val finalResponse = builder.toString().trim()
                if (finalResponse.isNotEmpty()) {
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMsgId) {
                                    msg.copy(text = finalResponse, isStreaming = false)
                                } else {
                                    msg
                                }
                            },
                            orbState = OrbState.IDLE
                        )
                    }
                    if (_uiState.value.isAutoSpeakEnabled) {
                        speakText(finalResponse, assistantMsgId)
                    }
                } else {
                    removeMessageAndShowError(assistantMsgId, "Model returned an empty response.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat response timed out", e)
                removeMessageAndShowError(
                    assistantMsgId,
                    "Response timed out after 60 seconds. Please try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat inference failed", e)
                removeMessageAndShowError(
                    assistantMsgId,
                    e.localizedMessage ?: "Failed to generate response."
                )
            }
        }
    }

    private fun removeMessageAndShowError(assistantMsgId: String, message: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.filterNot { it.id == assistantMsgId && it.text.isBlank() }
                    .map { if (it.id == assistantMsgId) it.copy(isStreaming = false) else it },
                orbState = OrbState.IDLE,
                errorMessage = message
            )
        }
    }

    fun speakText(text: String, messageId: String = UUID.randomUUID().toString()) {
        val engine = tts ?: return
        if (!_uiState.value.isTtsReady) return

        // Strip markdown asterisks/backticks so voice synthesis reads naturally
        val cleanSpeech = text
            .replace(Regex("```[\\s\\S]*?```"), " code block ")
            .replace(Regex("[*_`#>]"), "")
            .trim()
        if (cleanSpeech.isEmpty()) return

        // Switch locale dynamically if Urdu script is predominant
        val hasUrdu = cleanSpeech.any { it in '\u0600'..'\u06FF' }
        engine.language = if (hasUrdu) Locale("ur", "PK") else Locale.US
        engine.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _uiState.update { state ->
            val nextOrb = if (state.orbState == OrbState.SPEAKING) OrbState.IDLE else state.orbState
            state.copy(orbState = nextOrb, speakingMessageId = null)
        }
    }

    fun startListening() {
        val context = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _uiState.update {
                it.copy(
                    errorMessage = "Speech recognition service is unavailable on this device. Use text input below."
                )
            }
            return
        }

        stopSpeaking()
        if (_uiState.value.orbState == OrbState.LISTENING) {
            stopListening()
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _uiState.update { it.copy(orbState = OrbState.LISTENING, errorMessage = null) }
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _uiState.update { it.copy(audioRmsLevel = normalized) }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _uiState.update { it.copy(audioRmsLevel = 0f) }
                    }

                    override fun onError(error: Int) {
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Voice input timed out."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for voice input."
                            else -> "Voice capture unavailable (code $error)."
                        }
                        _uiState.update {
                            it.copy(
                                orbState = OrbState.IDLE,
                                audioRmsLevel = 0f,
                                errorMessage = msg
                            )
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull().orEmpty().trim()
                        _uiState.update { it.copy(orbState = OrbState.IDLE, audioRmsLevel = 0f) }
                        if (spoken.isNotEmpty()) {
                            sendMessage(overridePrompt = spoken)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        if (partial.isNotBlank()) {
                            _uiState.update { it.copy(inputText = partial) }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            _uiState.update { it.copy(orbState = OrbState.LISTENING, errorMessage = null) }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SpeechRecognizer", e)
            _uiState.update {
                it.copy(
                    orbState = OrbState.IDLE,
                    errorMessage = "Could not start voice input: ${e.localizedMessage ?: "unknown error"}"
                )
            }
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping speech recognizer", e)
        }
        _uiState.update { state ->
            val nextOrb = if (state.orbState == OrbState.LISTENING) OrbState.IDLE else state.orbState
            state.copy(orbState = nextOrb, audioRmsLevel = 0f)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}
