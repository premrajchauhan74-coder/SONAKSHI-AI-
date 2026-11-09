package com.example.util

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.example.data.local.PhoneActionType

data class DeviceDiagnostics(
    val deviceModel: String,
    val androidVersion: String,
    val sdkInt: Int,
    val hasMicPermission: Boolean,
    val isSpeechRecognitionAvailable: Boolean,
    val isTtsEngineReady: Boolean,
    val canResolveDialer: Boolean,
    val canResolveAlarm: Boolean
)

object PhoneControlExecutor {

    fun collectDiagnostics(context: Context, isTtsReady: Boolean): DeviceDiagnostics {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val sttAvailable = runCatching {
            SpeechRecognizer.isRecognitionAvailable(context)
        }.getOrDefault(false)

        val pm = context.packageManager
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM)

        val canDial = dialIntent.resolveActivity(pm) != null
        val canAlarm = alarmIntent.resolveActivity(pm) != null

        return DeviceDiagnostics(
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            sdkInt = Build.VERSION.SDK_INT,
            hasMicPermission = hasMic,
            isSpeechRecognitionAvailable = sttAvailable,
            isTtsEngineReady = isTtsReady,
            canResolveDialer = canDial,
            canResolveAlarm = canAlarm
        )
    }

    /**
     * Executes a real Android system Intent for phone control and returns a user-readable status message.
     */
    fun executeAction(
        context: Context,
        actionType: PhoneActionType,
        targetPayload: String
    ): Result<String> {
        return runCatching {
            val intent: Intent = when (actionType) {
                PhoneActionType.DIAL_PHONE -> {
                    val sanitized = targetPayload.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
                        .ifBlank { "112" }
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
                }

                PhoneActionType.SET_ALARM -> {
                    val parts = targetPayload.split(":")
                    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 7
                    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, minute)
                        putExtra(AlarmClock.EXTRA_MESSAGE, "Sonakshi AI Alarm")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    }
                }

                PhoneActionType.SET_TIMER -> {
                    val seconds = targetPayload.toIntOrNull()?.coerceAtLeast(5) ?: 300
                    Intent(AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                        putExtra(AlarmClock.EXTRA_MESSAGE, "Sonakshi AI Timer")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    }
                }

                PhoneActionType.WEB_SEARCH -> {
                    val query = targetPayload.ifBlank { "Sonakshi AI Android" }
                    val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, query)
                    }
                    if (searchIntent.resolveActivity(context.packageManager) != null) {
                        searchIntent
                    } else {
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
                        )
                    }
                }

                PhoneActionType.OPEN_WIFI_SETTINGS -> {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }

                PhoneActionType.OPEN_ACCESSIBILITY -> {
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                }

                PhoneActionType.OPEN_VOICE_ASSISTANT -> {
                    val voiceIntent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
                    if (voiceIntent.resolveActivity(context.packageManager) != null) {
                        voiceIntent
                    } else {
                        Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    }
                }

                PhoneActionType.OPEN_BLUETOOTH -> {
                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                }

                PhoneActionType.OPEN_APP_SETTINGS -> {
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                }

                PhoneActionType.SHARE_NOTE -> {
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, targetPayload.ifBlank { "Shared via Sonakshi AI" })
                        },
                        "Share via Sonakshi AI"
                    )
                }

                PhoneActionType.NONE -> {
                    return Result.success("No external system intent required.")
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "Launched ${actionType.displayTitle} (${targetPayload.ifBlank { "system" }})"
        }.recoverCatching { primaryError ->
            // Fallback to general Android Settings if a specific emulator lacks Clock/Dialer handler
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            "Opened Android System Settings (${actionType.displayTitle} handler fallback: ${primaryError.localizedMessage ?: "redirected"})"
        }
    }
}
