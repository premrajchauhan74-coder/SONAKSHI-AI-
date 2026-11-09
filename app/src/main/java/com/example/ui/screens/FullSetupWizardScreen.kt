package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AssistantConfig
import com.example.data.local.PersonaMode
import com.example.data.local.PhoneActionType
import com.example.ui.theme.AmberPulse
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.util.DeviceDiagnostics
import java.util.Locale

@Composable
fun FullSetupWizardScreen(
    config: AssistantConfig,
    diagnostics: DeviceDiagnostics,
    onRequestMicPermission: () -> Unit,
    onExecutePhoneAction: (PhoneActionType, String) -> Unit,
    onUpdateConfig: ((AssistantConfig) -> AssistantConfig) -> Unit,
    onTestVoice: (String) -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler { onBackToHome() }

    var editableName by rememberSaveable(config.assistantName) { mutableStateOf(config.assistantName) }
    var editableWakeWord by rememberSaveable(config.wakeWord) { mutableStateOf(config.wakeWord) }

    val readinessChecks = listOf(
        diagnostics.isTtsEngineReady,
        diagnostics.hasMicPermission,
        diagnostics.canResolveDialer,
        diagnostics.canResolveAlarm
    )
    val passedCount = readinessChecks.count { it }
    val readinessProgress = passedCount / readinessChecks.size.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + Overall Setup Readiness Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${config.assistantName} AI Android Full Setup",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "4-Phase System Diagnostics, Voice Persona & Phone Control Setup",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = onRefreshDiagnostics,
                            modifier = Modifier.testTag("refresh_diagnostics_button")
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh telemetry",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Scan")
                        }
                    }

                    LinearProgressIndicator(
                        progress = { readinessProgress },
                        color = EmeraldReady,
                        trackColor = ObsidianSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )

                    Text(
                        text = "On-Device Readiness: $passedCount / ${readinessChecks.size} core hooks active (${diagnostics.deviceModel} • ${diagnostics.androidVersion})",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan
                    )
                }
            }
        }

        // PHASE 1: Core Device & Audio Engine Telemetry
        item {
            SetupPhaseCard(
                phaseBadge = "PHASE 1",
                title = "Core APK & Speech Engine Telemetry",
                subtitle = "Verifies Android TTS synthesis, SpeechRecognizer service, and Intent resolution."
            ) {
                TelemetryRow(
                    label = "Device & OS Build",
                    value = "${diagnostics.deviceModel} (API ${diagnostics.sdkInt})",
                    isReady = true
                )
                TelemetryRow(
                    label = "Android TextToSpeech (TTS) Engine",
                    value = if (diagnostics.isTtsEngineReady) "READY (Hindi + English)" else "Initializing...",
                    isReady = diagnostics.isTtsEngineReady
                )
                TelemetryRow(
                    label = "On-Device SpeechRecognizer Service",
                    value = if (diagnostics.isSpeechRecognitionAvailable) "AVAILABLE" else "Fallback Mode",
                    isReady = diagnostics.isSpeechRecognitionAvailable
                )
                TelemetryRow(
                    label = "Phone Dialer & Clock Alarm Handlers",
                    value = if (diagnostics.canResolveDialer && diagnostics.canResolveAlarm) "VERIFIED" else "System Fallback Ready",
                    isReady = diagnostics.canResolveDialer || diagnostics.canResolveAlarm
                )
            }
        }

        // PHASE 2: AI Engine & Firebase AI Logic Status
        item {
            SetupPhaseCard(
                phaseBadge = "PHASE 2",
                title = "AI Reasoning Engine Status",
                subtitle = "In AI Studio Android apps, live Gemini models are powered securely by Firebase AI Logic."
            ) {
                Surface(
                    color = AmberPulse.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, AmberPulse.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = AmberPulse
                        )
                        Column {
                            Text(
                                text = "PlaceholderAiRepository Active (Firebase Declined)",
                                style = MaterialTheme.typography.titleSmall,
                                color = AmberPulse
                            )
                            Text(
                                text = "Generative AI replies currently return clearly labeled sample responses. Enable Firebase AI Logic in AI Studio to connect live Gemini models without embedding client-side API keys.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // PHASE 3: Phone Control Permissions & System Hooks
        item {
            SetupPhaseCard(
                phaseBadge = "PHASE 3",
                title = "Phone Control Permissions & System Hooks",
                subtitle = "Grant Microphone access for live voice commands and configure Android system shortcuts."
            ) {
                // Microphone Permission Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (diagnostics.hasMicPermission) EmeraldReady else NeonMagenta
                        )
                        Column {
                            Text(
                                text = "Microphone Access (RECORD_AUDIO)",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = if (diagnostics.hasMicPermission) {
                                    "Granted — Live voice capture active"
                                } else {
                                    "Required for speaking to ${config.assistantName}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onRequestMicPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (diagnostics.hasMicPermission) EmeraldReady else ElectricCyan,
                            contentColor = Color(0xFF001F26)
                        ),
                        modifier = Modifier.testTag("grant_mic_permission_button")
                    ) {
                        Text(
                            text = if (diagnostics.hasMicPermission) "Granted" else "Grant Mic",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onExecutePhoneAction(PhoneActionType.OPEN_ACCESSIBILITY, "accessibility")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("setup_accessibility_button")
                    ) {
                        Icon(
                            Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Accessibility")
                    }

                    OutlinedButton(
                        onClick = {
                            onExecutePhoneAction(PhoneActionType.OPEN_VOICE_ASSISTANT, "voice_assistant")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("setup_default_assistant_button")
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Default Assist")
                    }
                }
            }
        }

        // PHASE 4: Sonakshi Identity, Persona & Voice Synthesis Tuning
        item {
            SetupPhaseCard(
                phaseBadge = "PHASE 4",
                title = "Identity, Persona & Voice Synthesis",
                subtitle = "Customize assistant name (Sonakshi), wake phrase, bilingual persona, and TTS voice tuning."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = editableName,
                        onValueChange = { editableName = it },
                        label = { Text("Assistant Name") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assistant_name_input")
                    )

                    OutlinedTextField(
                        value = editableWakeWord,
                        onValueChange = { editableWakeWord = it },
                        label = { Text("Wake Phrase") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wake_word_input")
                    )

                    Button(
                        onClick = {
                            val cleanName = editableName.trim().ifBlank { "Sonakshi" }
                            val cleanWake = editableWakeWord.trim().ifBlank { "Hey $cleanName" }
                            onUpdateConfig {
                                it.copy(assistantName = cleanName, wakeWord = cleanWake)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF001F26)
                        ),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("save_identity_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Select ${config.assistantName} Persona Mode:",
                    style = MaterialTheme.typography.titleSmall,
                    color = ElectricCyan
                )

                PersonaMode.entries.forEach { mode ->
                    Surface(
                        onClick = {
                            onUpdateConfig { it.copy(personaMode = mode) }
                        },
                        shape = MaterialTheme.shapes.small,
                        color = if (config.personaMode == mode) {
                            ElectricCyan.copy(alpha = 0.14f)
                        } else {
                            ObsidianSurfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            if (config.personaMode == mode) ElectricCyan else ObsidianCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("persona_option_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.personaMode == mode,
                                onClick = { onUpdateConfig { it.copy(personaMode = mode) } },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricCyan)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = mode.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Voice Speech Rate & Pitch Sliders
                Text(
                    text = String.format(Locale.US, "Voice Speed: %.2fx", config.speechRate),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                Slider(
                    value = config.speechRate,
                    onValueChange = { rate ->
                        onUpdateConfig { it.copy(speechRate = rate) }
                    },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan
                    )
                )

                Text(
                    text = String.format(Locale.US, "Voice Pitch: %.2fx", config.voicePitch),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                Slider(
                    value = config.voicePitch,
                    onValueChange = { pitch ->
                        onUpdateConfig { it.copy(voicePitch = pitch) }
                    },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonMagenta,
                        activeTrackColor = NeonMagenta
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Execute Safe Intents",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Text(
                            text = "Automatically open alarm/timer/search intents right after ${config.assistantName} responds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = config.autoExecuteSafeIntents,
                        onCheckedChange = { enabled ->
                            onUpdateConfig { it.copy(autoExecuteSafeIntents = enabled) }
                        },
                        modifier = Modifier.testTag("auto_execute_switch")
                    )
                }

                Button(
                    onClick = {
                        onTestVoice(config.personaMode.defaultGreeting)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonMagenta,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_sonakshi_voice_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Test ${config.assistantName} Voice Synthesis Now",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupPhaseCard(
    phaseBadge: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = ElectricCyan.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = phaseBadge,
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String,
    isReady: Boolean
) {
    Surface(
        color = ObsidianSurfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Memory,
                    contentDescription = null,
                    tint = if (isReady) EmeraldReady else AmberPulse,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = if (isReady) EmeraldReady else AmberPulse
            )
        }
    }
}
