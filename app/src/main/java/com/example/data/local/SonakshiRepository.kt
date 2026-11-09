package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sonakshiDataStore by preferencesDataStore(name = "sonakshi_preferences")

class SonakshiRepository(
    private val context: Context,
    private val dao: SonakshiDao
) {
    private object Keys {
        val ASSISTANT_NAME = stringPreferencesKey("assistant_name")
        val WAKE_WORD = stringPreferencesKey("wake_word")
        val PERSONA_MODE = stringPreferencesKey("persona_mode")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val VOICE_PITCH = floatPreferencesKey("voice_pitch")
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak")
        val TOUCH_GUARD = booleanPreferencesKey("touch_guard")
        val AUTO_EXECUTE_SAFE = booleanPreferencesKey("auto_execute_safe")
    }

    val allMessages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()
    val allMacros: Flow<List<AutomationMacroEntity>> = dao.getAllMacros()

    val assistantConfigFlow: Flow<AssistantConfig> = context.sonakshiDataStore.data.map { prefs ->
        val personaName = prefs[Keys.PERSONA_MODE] ?: PersonaMode.COMPANION_HINGLISH.name
        val persona = runCatching { PersonaMode.valueOf(personaName) }
            .getOrDefault(PersonaMode.COMPANION_HINGLISH)

        AssistantConfig(
            assistantName = prefs[Keys.ASSISTANT_NAME] ?: "Sonakshi",
            wakeWord = prefs[Keys.WAKE_WORD] ?: "Hey Sonakshi",
            personaMode = persona,
            speechRate = prefs[Keys.SPEECH_RATE] ?: 1.0f,
            voicePitch = prefs[Keys.VOICE_PITCH] ?: 1.05f,
            autoSpeakResponses = prefs[Keys.AUTO_SPEAK] ?: true,
            touchGuardEnabled = prefs[Keys.TOUCH_GUARD] ?: true,
            autoExecuteSafeIntents = prefs[Keys.AUTO_EXECUTE_SAFE] ?: false
        )
    }

    suspend fun updateConfig(transform: (AssistantConfig) -> AssistantConfig, current: AssistantConfig) {
        val updated = transform(current)
        context.sonakshiDataStore.edit { prefs ->
            prefs[Keys.ASSISTANT_NAME] = updated.assistantName
            prefs[Keys.WAKE_WORD] = updated.wakeWord
            prefs[Keys.PERSONA_MODE] = updated.personaMode.name
            prefs[Keys.SPEECH_RATE] = updated.speechRate
            prefs[Keys.VOICE_PITCH] = updated.voicePitch
            prefs[Keys.AUTO_SPEAK] = updated.autoSpeakResponses
            prefs[Keys.TOUCH_GUARD] = updated.touchGuardEnabled
            prefs[Keys.AUTO_EXECUTE_SAFE] = updated.autoExecuteSafeIntents
        }
    }

    suspend fun insertMessage(message: ChatMessageEntity): Long = dao.insertMessage(message)

    suspend fun markActionExecuted(messageId: Int) = dao.markActionExecuted(messageId)

    suspend fun clearAllMessages() {
        dao.clearAllMessages()
        seedWelcomeMessagesIfEmpty()
    }

    suspend fun insertMacro(macro: AutomationMacroEntity): Long = dao.insertMacro(macro)

    suspend fun updateMacro(macro: AutomationMacroEntity) = dao.updateMacro(macro)

    suspend fun deleteMacroById(macroId: Int) = dao.deleteMacroById(macroId)

    suspend fun ensureSeeded() {
        seedWelcomeMessagesIfEmpty()
        seedDefaultMacrosIfEmpty()
    }

    private suspend fun seedWelcomeMessagesIfEmpty() {
        if (dao.getMessageCount() == 0) {
            val now = System.currentTimeMillis()
            dao.insertMessage(
                ChatMessageEntity(
                    isUser = false,
                    senderName = "Sonakshi AI",
                    messageText = "Namaste! Main Sonakshi hoon — aapki personal AI Voice & Phone Control Companion. Aap mujhse Hindi ya English mein baat kar sakte hain, call laga sakte hain, alarm/timer set kar sakte hain, aur system settings control kar sakte hain!",
                    secondaryHint = "On-Device TTS Voice + Android Phone Control Intents Ready • Sample AI Mode (Firebase Required for Live Gemini)",
                    actionType = PhoneActionType.OPEN_VOICE_ASSISTANT.name,
                    actionTarget = "voice_assistant",
                    actionLabel = "Set Sonakshi as Voice Assistant",
                    isSampleAi = true,
                    timestamp = now - 2000L
                )
            )
            dao.insertMessage(
                ChatMessageEntity(
                    isUser = false,
                    senderName = "Sonakshi AI",
                    messageText = "[Sample AI Tip] Try tapping the Mic button or quick chips below: \"Call 9876543210\", \"Set alarm for 6:30 AM\", \"Start 10 minute timer\", or \"Open Accessibility Settings\".",
                    secondaryHint = "Tap any Phone Action Card to execute real Android system intents.",
                    actionType = PhoneActionType.SET_ALARM.name,
                    actionTarget = "7:0",
                    actionLabel = "Quick Test: Set 07:00 Alarm",
                    isSampleAi = true,
                    timestamp = now - 1000L
                )
            )
        }
    }

    private suspend fun seedDefaultMacrosIfEmpty() {
        if (dao.getMacroCount() == 0) {
            val defaults = listOf(
                AutomationMacroEntity(
                    title = "Subah ki Shuruat (Morning Briefing)",
                    triggerPhrase = "Hey Sonakshi, Good Morning",
                    spokenResponse = "Suprabhat! Main Sonakshi hoon. Aapka 6:30 AM ka alarm aur subah ka schedule tayyar hai.",
                    actionType = PhoneActionType.SET_ALARM.name,
                    actionPayload = "6:30",
                    category = "Daily Routine",
                    isEnabled = true,
                    runCount = 3
                ),
                AutomationMacroEntity(
                    title = "Focus & Study Pomodoro (25m)",
                    triggerPhrase = "Hey Sonakshi, Focus Mode",
                    spokenResponse = "Focus mode active! Sonakshi ne 25 minute ka deep work countdown timer shuru kar diya hai.",
                    actionType = PhoneActionType.SET_TIMER.name,
                    actionPayload = "1500",
                    category = "Productivity",
                    isEnabled = true,
                    runCount = 5
                ),
                AutomationMacroEntity(
                    title = "Hands-Free Emergency / Quick Dial",
                    triggerPhrase = "Hey Sonakshi, Speed Dial Home",
                    spokenResponse = "Sonakshi Touch-Guard active: Ghar ke number par call lagane ke liye dialer khola ja raha hai.",
                    actionType = PhoneActionType.DIAL_PHONE.name,
                    actionPayload = "+919876543210",
                    category = "Phone Control",
                    isEnabled = true,
                    runCount = 2
                ),
                AutomationMacroEntity(
                    title = "UI Screen Automation Setup",
                    triggerPhrase = "Hey Sonakshi, Enable Screen Control",
                    spokenResponse = "Sonakshi agentic phone control ke liye Android Accessibility Settings kholi ja rahi hain.",
                    actionType = PhoneActionType.OPEN_ACCESSIBILITY.name,
                    actionPayload = "accessibility",
                    category = "System Control",
                    isEnabled = true,
                    runCount = 1
                ),
                AutomationMacroEntity(
                    title = "Smart Connectivity Check",
                    triggerPhrase = "Hey Sonakshi, Check Wi-Fi",
                    spokenResponse = "Wi-Fi aur Internet network settings check karne ke liye panel khul raha hai.",
                    actionType = PhoneActionType.OPEN_WIFI_SETTINGS.name,
                    actionPayload = "wifi",
                    category = "System Control",
                    isEnabled = true,
                    runCount = 4
                )
            )
            defaults.forEach { dao.insertMacro(it) }
        }
    }
}
