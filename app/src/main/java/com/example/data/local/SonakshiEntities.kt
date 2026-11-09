package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PhoneActionType(val displayTitle: String, val hindiTitle: String) {
    DIAL_PHONE("Phone Call Dialer", "फ़ोन कॉल डायलर"),
    SET_ALARM("Set System Alarm", "सिस्टम अलार्म सेट करें"),
    SET_TIMER("Start Countdown Timer", "काउंटडाउन टाइमर"),
    WEB_SEARCH("Web Search Query", "वेब सर्च"),
    OPEN_WIFI_SETTINGS("Wi-Fi & Network Settings", "वाई-फ़ाई सेटिंग्स"),
    OPEN_ACCESSIBILITY("Accessibility UI Control", "एक्सेसिबिलिटी कंट्रोल"),
    OPEN_VOICE_ASSISTANT("Default Voice Assistant", "डिफ़ॉल्ट वॉइस असिस्टेंट"),
    OPEN_BLUETOOTH("Bluetooth Settings", "ब्लूटूथ सेटिंग्स"),
    OPEN_APP_SETTINGS("App Permissions & Info", "ऐप अनुमतियाँ"),
    SHARE_NOTE("Share Text / Broadcast", "टेक्स्ट शेयर करें"),
    NONE("Conversational Response", "वार्तालाप उत्तर")
}

enum class PersonaMode(
    val title: String,
    val subtitle: String,
    val badge: String,
    val defaultGreeting: String
) {
    COMPANION_HINGLISH(
        title = "Sonakshi Companion (Hinglish)",
        subtitle = "Warm bilingual Hindi + English assistant for daily phone control",
        badge = "BILINGUAL",
        defaultGreeting = "Namaste! Main Sonakshi hoon, aapki AI Voice & Phone Control assistant. Aaj main aapke phone mein kya madad karoon?"
    ),
    EXECUTIVE_PRO(
        title = "Sonakshi Executive Pro",
        subtitle = "Crisp, ultra-fast productivity & hands-free task automation",
        badge = "PRO",
        defaultGreeting = "Hello! I am Sonakshi, your executive AI phone controller. Ready to schedule alarms, place calls, or launch system workflows."
    ),
    SHUDH_HINDI(
        title = "सोनाक्षी (शुद्ध हिंदी)",
        subtitle = "पूर्णतः हिंदी में वॉइस कमांड और फ़ोन नियंत्रण",
        badge = "हिंदी",
        defaultGreeting = "नमस्ते! मैं सोनाक्षी हूँ। आप मुझे कॉल करने, अलार्म लगाने, टाइमर शुरू करने या फ़ोन सेटिंग्स खोलने का आदेश दे सकते हैं।"
    ),
    SECURITY_GUARDIAN(
        title = "Sonakshi Security Guardian",
        subtitle = "Strict Touch-Guard verification & privacy-first phone automation",
        badge = "GUARD",
        defaultGreeting = "Sonakshi Guardian active. Touch-Guard confirmation is enforced for all outbound calls and system settings changes."
    )
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val isUser: Boolean,
    val senderName: String,
    val messageText: String,
    val secondaryHint: String = "",
    val actionType: String = PhoneActionType.NONE.name,
    val actionTarget: String = "",
    val actionLabel: String = "",
    val isSampleAi: Boolean = false,
    val wasActionExecuted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "automation_macros")
data class AutomationMacroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val triggerPhrase: String,
    val spokenResponse: String,
    val actionType: String,
    val actionPayload: String,
    val category: String,
    val isEnabled: Boolean = true,
    val runCount: Int = 0,
    val lastRunTimestamp: Long? = null
)

data class AssistantConfig(
    val assistantName: String = "Sonakshi",
    val wakeWord: String = "Hey Sonakshi",
    val personaMode: PersonaMode = PersonaMode.COMPANION_HINGLISH,
    val speechRate: Float = 1.0f,
    val voicePitch: Float = 1.05f,
    val autoSpeakResponses: Boolean = true,
    val touchGuardEnabled: Boolean = true,
    val autoExecuteSafeIntents: Boolean = false
)
