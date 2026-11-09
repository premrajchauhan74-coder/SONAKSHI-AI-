package com.example.data.ai

import com.example.data.local.AssistantConfig
import com.example.data.local.PersonaMode
import com.example.data.local.PhoneActionType
import kotlinx.coroutines.delay

data class AiAssistantReply(
    val spokenText: String,
    val secondaryNote: String,
    val detectedAction: PhoneActionType = PhoneActionType.NONE,
    val actionTarget: String = "",
    val actionButtonLabel: String = "",
    val isPlaceholderSample: Boolean = true
)

/**
 * Single AI Repository interface for all generative/conversational requests in Sonakshi AI.
 *
 * Since Firebase setup was declined, [PlaceholderAiRepository] provides clearly labeled
 * sample AI responses alongside a real on-device Phone Control Intent parser so all
 * Android device control commands (calls, alarms, timers, web search, system settings)
 * work genuinely on the device.
 */
interface AiRepository {
    val isFirebaseConnected: Boolean
    suspend fun generateAssistantResponse(
        userInput: String,
        config: AssistantConfig
    ): AiAssistantReply
}

class PlaceholderAiRepository : AiRepository {
    override val isFirebaseConnected: Boolean = false

    override suspend fun generateAssistantResponse(
        userInput: String,
        config: AssistantConfig
    ): AiAssistantReply {
        delay(260L) // Brief natural conversational pacing
        val trimmed = userInput.trim()
        val lower = trimmed.lowercase()
        val name = config.assistantName.ifBlank { "Sonakshi" }

        // 1. Detect Phone Call / Dial commands (English & Hindi/Hinglish)
        val phoneRegex = Regex("""(\+?\d[\d\s\-]{5,14}\d)""")
        val phoneMatch = phoneRegex.find(trimmed)
        if (lower.contains("call") || lower.contains("dial") || lower.contains("कॉल") || lower.contains("फोन लगाओ") || phoneMatch != null) {
            val number = phoneMatch?.value?.replace(Regex("""[\s\-]"""), "") ?: "112"
            val spoken = when (config.personaMode) {
                PersonaMode.SHUDH_HINDI ->
                    "[Sample AI Response] नमस्ते, मैं $name हूँ। नंबर $number पर कॉल करने के लिए डायलर तैयार है।"
                PersonaMode.COMPANION_HINGLISH ->
                    "[Sample AI Response] Zaroor! Main $name hoon — $number par call lagane ke liye Android Dialer ready hai. Neeche button dabayein."
                else ->
                    "[Sample AI Response] $name ready: Prepared phone dialer for $number. Tap the action button below to launch the system dialer."
            }
            return AiAssistantReply(
                spokenText = spoken,
                secondaryNote = "Intent: ACTION_DIAL • Target: tel:$number (Requires Firebase for live Gemini reasoning)",
                detectedAction = PhoneActionType.DIAL_PHONE,
                actionTarget = number,
                actionButtonLabel = "Dial $number on Phone",
                isPlaceholderSample = true
            )
        }

        // 2. Detect Alarm commands
        if (lower.contains("alarm") || lower.contains("wake me") || lower.contains("अलार्म") || lower.contains("जगाना")) {
            val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""", RegexOption.IGNORE_CASE)
            val match = timeRegex.find(lower)
            var hour = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 7
            val minute = match?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0
            val amPm = match?.groupValues?.getOrNull(3)?.lowercase() ?: ""
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            hour = hour.coerceIn(0, 23)
            val formattedTime = String.format("%02d:%02d", hour, minute)

            val spoken = when (config.personaMode) {
                PersonaMode.SHUDH_HINDI ->
                    "[Sample AI Response] $name ने $formattedTime बजे का सिस्टम अलार्म तैयार कर दिया है।"
                PersonaMode.COMPANION_HINGLISH ->
                    "[Sample AI Response] Done! $name ne $formattedTime ka system alarm ready kar diya hai. Ek tap mein set karein."
                else ->
                    "[Sample AI Response] $name scheduled an Android system alarm for $formattedTime."
            }
            return AiAssistantReply(
                spokenText = spoken,
                secondaryNote = "Intent: ACTION_SET_ALARM • Time: $formattedTime (Sample AI mode)",
                detectedAction = PhoneActionType.SET_ALARM,
                actionTarget = "$hour:$minute",
                actionButtonLabel = "Set Alarm for $formattedTime",
                isPlaceholderSample = true
            )
        }

        // 3. Detect Timer commands
        if (lower.contains("timer") || lower.contains("countdown") || lower.contains("टाइमर")) {
            val minRegex = Regex("""(\d+)\s*(min|minute|minutes|मिनट|sec|second|seconds)""", RegexOption.IGNORE_CASE)
            val match = minRegex.find(lower)
            val amount = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 5
            val unit = match?.groupValues?.getOrNull(2)?.lowercase() ?: "min"
            val totalSeconds = if (unit.startsWith("sec")) amount.coerceAtLeast(5) else (amount * 60).coerceAtLeast(60)
            val labelText = if (unit.startsWith("sec")) "${amount}s" else "${amount}m"

            val spoken = when (config.personaMode) {
                PersonaMode.SHUDH_HINDI ->
                    "[Sample AI Response] $name ने $labelText का काउंटडाउन टाइमर तैयार कर दिया है।"
                PersonaMode.COMPANION_HINGLISH ->
                    "[Sample AI Response] Bilkul! $name ne $labelText ka timer ready kar diya hai."
                else ->
                    "[Sample AI Response] $name prepared a $labelText system countdown timer."
            }
            return AiAssistantReply(
                spokenText = spoken,
                secondaryNote = "Intent: ACTION_SET_TIMER • Duration: ${totalSeconds}s (Sample AI mode)",
                detectedAction = PhoneActionType.SET_TIMER,
                actionTarget = totalSeconds.toString(),
                actionButtonLabel = "Start $labelText Timer",
                isPlaceholderSample = true
            )
        }

        // 4. Detect Accessibility / UI Control setup requests
        if (lower.contains("accessibility") || lower.contains("screen control") || lower.contains("ui automation") || lower.contains("एक्सेसिबिलिटी")) {
            return AiAssistantReply(
                spokenText = "[Sample AI Response] Main $name hoon. Deep screen automation aur phone control ke liye Android Accessibility Settings kholne ka shortcut neeche hai.",
                secondaryNote = "Intent: Settings.ACTION_ACCESSIBILITY_SETTINGS (Sample AI mode)",
                detectedAction = PhoneActionType.OPEN_ACCESSIBILITY,
                actionTarget = "accessibility",
                actionButtonLabel = "Open Accessibility Settings",
                isPlaceholderSample = true
            )
        }

        // 5. Detect Wi-Fi / Network settings
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("network") || lower.contains("internet") || lower.contains("वाईफाई")) {
            return AiAssistantReply(
                spokenText = "[Sample AI Response] $name ne Wi-Fi aur Network control panel ready kar diya hai. Neeche tap karke open karein.",
                secondaryNote = "Intent: Settings.ACTION_WIFI_SETTINGS (Sample AI mode)",
                detectedAction = PhoneActionType.OPEN_WIFI_SETTINGS,
                actionTarget = "wifi",
                actionButtonLabel = "Open Wi-Fi Settings",
                isPlaceholderSample = true
            )
        }

        // 6. Detect Bluetooth settings
        if (lower.contains("bluetooth") || lower.contains("ब्लूटूथ") || lower.contains("earbuds") || lower.contains("speaker")) {
            return AiAssistantReply(
                spokenText = "[Sample AI Response] $name ne Bluetooth device pairing settings ready kar di hain.",
                secondaryNote = "Intent: Settings.ACTION_BLUETOOTH_SETTINGS (Sample AI mode)",
                detectedAction = PhoneActionType.OPEN_BLUETOOTH,
                actionTarget = "bluetooth",
                actionButtonLabel = "Open Bluetooth Settings",
                isPlaceholderSample = true
            )
        }

        // 7. Detect Default Voice Assistant settings
        if (lower.contains("default assistant") || lower.contains("wake word") || lower.contains("voice input") || lower.contains("असिस्टेंट")) {
            return AiAssistantReply(
                spokenText = "[Sample AI Response] $name ko default device assistant set karne ke liye Android Voice Input settings kholein.",
                secondaryNote = "Intent: Settings.ACTION_VOICE_INPUT_SETTINGS (Sample AI mode)",
                detectedAction = PhoneActionType.OPEN_VOICE_ASSISTANT,
                actionTarget = "voice_assistant",
                actionButtonLabel = "Configure Default Assistant",
                isPlaceholderSample = true
            )
        }

        // 8. Detect Web Search commands
        if (lower.contains("search") || lower.contains("google") || lower.contains("find ") || lower.contains("खोजो") || lower.contains("सर्च")) {
            val query = trimmed
                .replace(Regex("""^(hey\s+sonakshi|sonakshi|search\s+for|search|google|find|खोजो|सर्च\s+करो)\s*""", RegexOption.IGNORE_CASE), "")
                .ifBlank { trimmed }
            return AiAssistantReply(
                spokenText = "[Sample AI Response] $name ne \"$query\" ke liye Android Web Search tayyar kar diya hai.",
                secondaryNote = "Intent: ACTION_WEB_SEARCH • Query: $query (Sample AI mode)",
                detectedAction = PhoneActionType.WEB_SEARCH,
                actionTarget = query,
                actionButtonLabel = "Search \"$query\"",
                isPlaceholderSample = true
            )
        }

        // 9. Detect Greeting / Identity questions ("Who are you?", "Namaste", "Sonakshi")
        if (lower.contains("namaste") || lower.contains("hello") || lower.contains("hi") || lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("sonakshi") || lower.contains("सोनाक्षी")) {
            val greeting = when (config.personaMode) {
                PersonaMode.SHUDH_HINDI ->
                    "[Sample AI Response] नमस्ते! मेरा नाम $name है। मैं आपकी वॉइस और फ़ोन कंट्रोल AI असिस्टेंट हूँ। (लाइव Gemini AI के लिए Firebase सक्षम करें)।"
                PersonaMode.COMPANION_HINGLISH ->
                    "[Sample AI Response] Namaste! Main $name hoon, aapki personal AI Voice & Phone Control companion. Aap mujhse 'Call 9876543210', 'Set alarm for 7 AM', 'Set timer for 10 min', ya 'Open Wi-Fi settings' bol sakte hain!"
                else ->
                    "[Sample AI Response] Hello! I am $name (${config.personaMode.title}). Try commands like 'Call 9876543210', 'Set alarm for 7:00 AM', 'Start 5 minute timer', or 'Open Accessibility Settings'."
            }
            return AiAssistantReply(
                spokenText = greeting,
                secondaryNote = "Placeholder Sample Response — Enable Firebase AI Logic to connect live Gemini models.",
                detectedAction = PhoneActionType.NONE,
                isPlaceholderSample = true
            )
        }

        // 10. General query fallback with helpful Web Search action + sample AI explanation
        val generalReply = when (config.personaMode) {
            PersonaMode.SHUDH_HINDI ->
                "[Sample AI Response] ($name — सैंपल मोड): आपने पूछा: \"$trimmed\"। लाइव Gemini AI उत्तरों के लिए Firebase सक्षम करें, या नीचे दिए गए बटन से वेब पर खोजें।"
            PersonaMode.COMPANION_HINGLISH ->
                "[Sample AI Response] ($name — Sample Mode): Aapne kaha \"$trimmed\". Live Gemini AI reasoning ke liye Firebase AI Logic enable karein, ya neeche button daba kar isey phone par search/share karein!"
            else ->
                "[Sample AI Response] ($name — Sample Mode): Received \"$trimmed\". Because Firebase setup was declined, this is a placeholder sample response. You can still search this query on your device below."
        }
        return AiAssistantReply(
            spokenText = generalReply,
            secondaryNote = "Sample AI Output (Firebase Required for Live Gemini) • Web Search fallback ready",
            detectedAction = PhoneActionType.WEB_SEARCH,
            actionTarget = trimmed,
            actionButtonLabel = "Search Web for \"$trimmed\"",
            isPlaceholderSample = true
        )
    }
}
