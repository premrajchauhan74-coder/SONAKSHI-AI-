package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.AssistantConfig
import com.example.data.local.AutomationMacroEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.PersonaMode
import com.example.data.local.PhoneActionType
import com.example.data.local.SonakshiDatabase
import com.example.data.local.SonakshiRepository
import com.example.util.DeviceDiagnostics
import com.example.util.PhoneControlExecutor
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SonakshiScreen {
    ASSISTANT_HUB,
    PHONE_CONTROL,
    MACROS_STUDIO,
    FULL_SETUP
}

data class PendingGuardAction(
    val messageId: Int? = null,
    val actionType: PhoneActionType,
    val targetPayload: String,
    val description: String
)

class SonakshiViewModel(
    application: Application,
    private val repository: SonakshiRepository,
    private val aiRepository: AiRepository
) : AndroidViewModel(application) {

    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val macros: StateFlow<List<AutomationMacroEntity>> = repository.allMacros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val config: StateFlow<AssistantConfig> = repository.assistantConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssistantConfig())

    private val _currentScreen = MutableStateFlow(SonakshiScreen.ASSISTANT_HUB)
    val currentScreen: StateFlow<SonakshiScreen> = _currentScreen.asStateFlow()

    private val _isProcessingAi = MutableStateFlow(false)
    val isProcessingAi: StateFlow<Boolean> = _isProcessingAi.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _pendingGuardAction = MutableStateFlow<PendingGuardAction?>(null)
    val pendingGuardAction: StateFlow<PendingGuardAction?> = _pendingGuardAction.asStateFlow()

    private val _diagnostics = MutableStateFlow(
        PhoneControlExecutor.collectDiagnostics(application, false)
    )
    val diagnostics: StateFlow<DeviceDiagnostics> = _diagnostics.asStateFlow()

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    val isFirebaseConnected: Boolean
        get() = aiRepository.isFirebaseConnected

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        initTextToSpeech(application)
        refreshDiagnostics()
    }

    private fun initTextToSpeech(context: Context) {
        runCatching {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val hiLocale = Locale.forLanguageTag("hi-IN")
                    val result = tts?.setLanguage(hiLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                    }
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                    })
                    _isTtsReady.value = true
                    refreshDiagnostics()
                }
            }
        }
    }

    fun refreshDiagnostics() {
        _diagnostics.value = PhoneControlExecutor.collectDiagnostics(
            getApplication(),
            _isTtsReady.value
        )
    }

    fun selectScreen(screen: SonakshiScreen) {
        _currentScreen.value = screen
        refreshDiagnostics()
    }

    fun dismissStatusBanner() {
        _statusBanner.value = null
    }

    fun postStatus(message: String) {
        _statusBanner.value = message
    }

    fun speakText(rawText: String) {
        val cleanText = rawText
            .replace("[Sample AI Response]", "")
            .replace("[Sample AI Tip]", "")
            .trim()
        if (cleanText.isBlank()) return

        val currentConfig = config.value
        val engine = tts
        if (engine != null && _isTtsReady.value) {
            val targetLocale = when (currentConfig.personaMode) {
                PersonaMode.SHUDH_HINDI, PersonaMode.COMPANION_HINGLISH -> Locale.forLanguageTag("hi-IN")
                else -> Locale.US
            }
            val langResult = engine.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.US)
            }
            engine.setSpeechRate(currentConfig.speechRate.coerceIn(0.5f, 2.0f))
            engine.setPitch(currentConfig.voicePitch.coerceIn(0.5f, 2.0f))
            _isSpeaking.value = true
            engine.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "sonakshi_utt_${System.currentTimeMillis()}")
        } else {
            _statusBanner.value = "Voice preview: \"$cleanText\""
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun startVoiceListening(context: Context) {
        refreshDiagnostics()
        if (!_diagnostics.value.hasMicPermission) {
            _statusBanner.value = "Microphone permission required for live voice input."
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _statusBanner.value = "Speech Recognition service unavailable on this emulator — use quick voice chips or type below."
            return
        }

        runCatching {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _liveTranscript.value = "Listening for \"${config.value.wakeWord}\"..."
                }

                override fun onBeginningOfSpeech() {
                    _liveTranscript.value = "Capturing voice command..."
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _liveTranscript.value = ""
                    _statusBanner.value = "Voice capture finished or no speech detected. Tap a quick voice chip or try again."
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull().orEmpty()
                    _liveTranscript.value = ""
                    if (spoken.isNotBlank()) {
                        sendUserCommand(spoken)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    if (partial.isNotBlank()) {
                        _liveTranscript.value = partial
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val localeTag = if (config.value.personaMode == PersonaMode.SHUDH_HINDI) "hi-IN" else "en-IN"
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            recognizer.startListening(intent)
        }.onFailure { err ->
            _isListening.value = false
            _statusBanner.value = "Could not start microphone listener: ${err.localizedMessage}"
        }
    }

    fun stopVoiceListening() {
        runCatching { speechRecognizer?.stopListening() }
        _isListening.value = false
        _liveTranscript.value = ""
    }

    fun sendUserCommand(rawInput: String) {
        val text = rawInput.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            val currentCfg = config.value
            repository.insertMessage(
                ChatMessageEntity(
                    isUser = true,
                    senderName = "You",
                    messageText = text,
                    isSampleAi = false
                )
            )

            _isProcessingAi.value = true
            try {
                // Check if user command matches a custom enabled Macro trigger phrase first
                val matchedMacro = macros.value.firstOrNull { macro ->
                    macro.isEnabled && (
                        text.equals(macro.triggerPhrase, ignoreCase = true) ||
                            text.equals(macro.title, ignoreCase = true)
                        )
                }

                if (matchedMacro != null) {
                    val actionEnum = runCatching { PhoneActionType.valueOf(matchedMacro.actionType) }
                        .getOrDefault(PhoneActionType.NONE)
                    val msgId = repository.insertMessage(
                        ChatMessageEntity(
                            isUser = false,
                            senderName = "${currentCfg.assistantName} AI (Macro)",
                            messageText = matchedMacro.spokenResponse,
                            secondaryHint = "Triggered Macro: ${matchedMacro.title}",
                            actionType = actionEnum.name,
                            actionTarget = matchedMacro.actionPayload,
                            actionLabel = "Execute ${actionEnum.displayTitle}",
                            isSampleAi = false
                        )
                    ).toInt()
                    repository.updateMacro(
                        matchedMacro.copy(
                            runCount = matchedMacro.runCount + 1,
                            lastRunTimestamp = System.currentTimeMillis()
                        )
                    )
                    if (currentCfg.autoSpeakResponses) {
                        speakText(matchedMacro.spokenResponse)
                    }
                    if (actionEnum != PhoneActionType.NONE && currentCfg.autoExecuteSafeIntents) {
                        requestOrExecutePhoneAction(msgId, actionEnum, matchedMacro.actionPayload)
                    }
                } else {
                    val reply = aiRepository.generateAssistantResponse(text, currentCfg)
                    val insertedId = repository.insertMessage(
                        ChatMessageEntity(
                            isUser = false,
                            senderName = "${currentCfg.assistantName} AI",
                            messageText = reply.spokenText,
                            secondaryHint = reply.secondaryNote,
                            actionType = reply.detectedAction.name,
                            actionTarget = reply.actionTarget,
                            actionLabel = reply.actionButtonLabel,
                            isSampleAi = reply.isPlaceholderSample
                        )
                    ).toInt()

                    if (currentCfg.autoSpeakResponses) {
                        speakText(reply.spokenText)
                    }
                    if (reply.detectedAction != PhoneActionType.NONE && currentCfg.autoExecuteSafeIntents) {
                        requestOrExecutePhoneAction(insertedId, reply.detectedAction, reply.actionTarget)
                    }
                }
            } finally {
                _isProcessingAi.value = false
            }
        }
    }

    fun requestOrExecutePhoneAction(
        messageId: Int? = null,
        actionType: PhoneActionType,
        targetPayload: String
    ) {
        val currentCfg = config.value
        val requiresGuard = currentCfg.touchGuardEnabled &&
            (actionType == PhoneActionType.DIAL_PHONE || actionType == PhoneActionType.OPEN_ACCESSIBILITY)

        if (requiresGuard) {
            _pendingGuardAction.value = PendingGuardAction(
                messageId = messageId,
                actionType = actionType,
                targetPayload = targetPayload,
                description = "${currentCfg.assistantName} Touch-Guard: Confirm launching ${actionType.displayTitle} (${targetPayload.ifBlank { "System" }})?"
            )
        } else {
            performActionImmediately(messageId, actionType, targetPayload)
        }
    }

    fun confirmPendingGuardAction() {
        val pending = _pendingGuardAction.value ?: return
        _pendingGuardAction.value = null
        performActionImmediately(pending.messageId, pending.actionType, pending.targetPayload)
    }

    fun cancelPendingGuardAction() {
        _pendingGuardAction.value = null
        _statusBanner.value = "Action cancelled by Touch-Guard."
    }

    private fun performActionImmediately(
        messageId: Int?,
        actionType: PhoneActionType,
        targetPayload: String
    ) {
        viewModelScope.launch {
            val result = PhoneControlExecutor.executeAction(
                getApplication(),
                actionType,
                targetPayload
            )
            if (messageId != null) {
                repository.markActionExecuted(messageId)
            }
            _statusBanner.value = result.getOrElse { "Could not launch action: ${it.localizedMessage}" }
        }
    }

    fun runMacro(macro: AutomationMacroEntity) {
        viewModelScope.launch {
            repository.updateMacro(
                macro.copy(
                    runCount = macro.runCount + 1,
                    lastRunTimestamp = System.currentTimeMillis()
                )
            )
            speakText(macro.spokenResponse)
            val actionType = runCatching { PhoneActionType.valueOf(macro.actionType) }
                .getOrDefault(PhoneActionType.NONE)
            if (actionType != PhoneActionType.NONE) {
                requestOrExecutePhoneAction(null, actionType, macro.actionPayload)
            } else {
                _statusBanner.value = "Ran macro: ${macro.title}"
            }
        }
    }

    fun toggleMacroEnabled(macro: AutomationMacroEntity) {
        viewModelScope.launch {
            repository.updateMacro(macro.copy(isEnabled = !macro.isEnabled))
        }
    }

    fun addCustomMacro(
        title: String,
        triggerPhrase: String,
        spokenResponse: String,
        actionType: PhoneActionType,
        actionPayload: String,
        category: String
    ) {
        viewModelScope.launch {
            repository.insertMacro(
                AutomationMacroEntity(
                    title = title.trim(),
                    triggerPhrase = triggerPhrase.trim(),
                    spokenResponse = spokenResponse.trim(),
                    actionType = actionType.name,
                    actionPayload = actionPayload.trim(),
                    category = category.ifBlank { "Custom" },
                    isEnabled = true
                )
            )
            _statusBanner.value = "Saved custom voice macro \"$title\""
        }
    }

    fun deleteMacro(macroId: Int) {
        viewModelScope.launch {
            repository.deleteMacroById(macroId)
            _statusBanner.value = "Macro removed."
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearAllMessages()
            _statusBanner.value = "Conversation reset to welcome state."
        }
    }

    fun updateAssistantConfig(transform: (AssistantConfig) -> AssistantConfig) {
        viewModelScope.launch {
            repository.updateConfig(transform, config.value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching {
            tts?.stop()
            tts?.shutdown()
            speechRecognizer?.destroy()
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = SonakshiDatabase.getInstance(application)
            val repo = SonakshiRepository(application, db.sonakshiDao())
            val aiRepo = PlaceholderAiRepository()
            return SonakshiViewModel(application, repo, aiRepo) as T
        }
    }
}
