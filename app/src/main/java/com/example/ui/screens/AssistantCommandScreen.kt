package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AssistantConfig
import com.example.data.local.ChatMessageEntity
import com.example.data.local.PhoneActionType
import com.example.ui.theme.AmberPulse
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import kotlin.math.cos
import kotlin.math.sin

private data class QuickVoiceChip(
    val label: String,
    val command: String
)

private val QUICK_VOICE_CHIPS = listOf(
    QuickVoiceChip("📞 Call 9876543210", "Call 9876543210"),
    QuickVoiceChip("⏰ Alarm 06:30 AM", "Set alarm for 6:30 am"),
    QuickVoiceChip("⏳ Timer 10 Min", "Start a 10 minute timer"),
    QuickVoiceChip("🙏 Namaste Sonakshi", "Namaste Sonakshi, tum kya kar sakti ho?"),
    QuickVoiceChip("📶 Wi-Fi Settings", "Open Wi-Fi settings"),
    QuickVoiceChip("♿ Accessibility Control", "Open Accessibility settings"),
    QuickVoiceChip("🔍 Search Android 16", "Search for Android 16 voice automation")
)

@Composable
fun AssistantCommandScreen(
    messages: List<ChatMessageEntity>,
    config: AssistantConfig,
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessingAi: Boolean,
    liveTranscript: String,
    hasMicPermission: Boolean,
    onMicClick: () -> Unit,
    onSendCommand: (String) -> Unit,
    onExecuteAction: (Int, PhoneActionType, String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onClearChat: () -> Unit,
    onOpenSetupTab: () -> Unit
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Hero Neural Soundwave Core Header
            item(key = "hero_header") {
                SonakshiHeroHeader(
                    config = config,
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    liveTranscript = liveTranscript,
                    hasMicPermission = hasMicPermission,
                    onMicClick = onMicClick,
                    onStopSpeaking = onStopSpeaking,
                    onToggleAutoSpeak = onToggleAutoSpeak,
                    onClearChat = onClearChat
                )
            }

            // 2. Firebase AI Logic Notice Banner (Required when Firebase setup was declined)
            item(key = "firebase_notice") {
                FirebaseSampleModeBanner(onOpenSetupTab = onOpenSetupTab)
            }

            // 3. Quick Bilingual Voice Chips
            item(key = "quick_chips") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "QUICK VOICE & PHONE COMMANDS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QUICK_VOICE_CHIPS.forEachIndexed { index, chip ->
                            AssistChip(
                                onClick = { onSendCommand(chip.command) },
                                label = {
                                    Text(
                                        text = chip.label,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = ObsidianSurfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, ObsidianCardBorder),
                                modifier = Modifier.testTag("quick_chip_$index")
                            )
                        }
                    }
                }
            }

            // 4. Chat Message List
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onExecuteAction = onExecuteAction,
                    onSpeakMessage = onSpeakMessage
                )
            }

            if (isProcessingAi) {
                item(key = "processing_indicator") {
                    Text(
                        text = "${config.assistantName} is analyzing phone intent...",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 5. Bottom Voice & Command Composer Bar
        CommandComposerBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            isListening = isListening,
            assistantName = config.assistantName,
            onMicClick = onMicClick,
            onSend = {
                if (inputText.isNotBlank()) {
                    onSendCommand(inputText)
                    inputText = ""
                }
            }
        )
    }
}

@Composable
private fun SonakshiHeroHeader(
    config: AssistantConfig,
    isListening: Boolean,
    isSpeaking: Boolean,
    liveTranscript: String,
    hasMicPermission: Boolean,
    onMicClick: () -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onClearChat: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sonakshi_orb")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_phase"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sonakshi_hero_card"),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, ObsidianCardBorder),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_sonakshi_hero_1794206066257),
                contentDescription = stringResource(R.string.hero_banner_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x99090C18),
                                Color(0xCC090C18),
                                Color(0xF5090C18)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = ElectricCyan.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "WAKE: \"${config.wakeWord.uppercase()}\"",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = NeonMagenta.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = config.personaMode.badge,
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonMagenta,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onToggleAutoSpeak,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("toggle_tts_button")
                        ) {
                            Icon(
                                imageVector = if (config.autoSpeakResponses) {
                                    Icons.AutoMirrored.Filled.VolumeUp
                                } else {
                                    Icons.Default.VolumeOff
                                },
                                contentDescription = stringResource(R.string.tts_toggle_desc),
                                tint = if (config.autoSpeakResponses) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.clear_chat_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${config.assistantName} AI",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                        Text(
                            text = config.personaMode.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                isListening -> liveTranscript.ifBlank { "Listening for voice command..." }
                                isSpeaking -> "${config.assistantName} is speaking via Android TTS..."
                                hasMicPermission -> "Mic Ready • Tap orb to speak Hindi or English"
                                else -> "Tap orb to grant Mic permission or use quick chips"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Animated Neural Orb Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isSpeaking) onStopSpeaking() else onMicClick()
                            }
                            .testTag("hero_voice_orb")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val baseRadius = size.minDimension / 2.6f
                            val activeBoost = if (isListening || isSpeaking) 1.18f else 1.0f
                            val r1 = baseRadius * activeBoost * (1f + 0.08f * sin(phase))
                            val r2 = baseRadius * 0.78f * (1f + 0.1f * cos(phase * 1.5f))

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        if (isListening) NeonMagenta.copy(alpha = 0.45f)
                                        else ElectricCyan.copy(alpha = 0.35f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension / 2f
                                ),
                                radius = size.minDimension / 2f,
                                center = center
                            )

                            drawCircle(
                                color = if (isListening) NeonMagenta else ElectricCyan,
                                radius = r1,
                                center = center,
                                style = Stroke(width = 2.5.dp.toPx())
                            )

                            drawCircle(
                                color = NeonViolet,
                                radius = r2,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        Icon(
                            imageVector = when {
                                isSpeaking -> Icons.Default.StopCircle
                                isListening -> Icons.Default.Mic
                                hasMicPermission -> Icons.Default.Mic
                                else -> Icons.Default.MicOff
                            },
                            contentDescription = stringResource(R.string.mic_button_desc),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FirebaseSampleModeBanner(onOpenSetupTab: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSetupTab() }
            .testTag("firebase_notice_banner"),
        shape = MaterialTheme.shapes.medium,
        color = AmberPulse.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, AmberPulse.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Firebase AI status notice",
                tint = AmberPulse,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sample AI Mode Active (Firebase AI Logic Not Enabled)",
                    style = MaterialTheme.typography.titleSmall,
                    color = AmberPulse
                )
                Text(
                    text = "AI replies use labeled placeholder responses because Firebase setup was declined. On-device TTS Voice & Android Phone Control Intents work genuinely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onExecuteAction: (Int, PhoneActionType, String) -> Unit,
    onSpeakMessage: (String) -> Unit
) {
    val actionEnum = runCatching { PhoneActionType.valueOf(message.actionType) }
        .getOrDefault(PhoneActionType.NONE)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 520.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isUser) 20.dp else 6.dp,
                bottomEnd = if (message.isUser) 6.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    ObsidianSurface
                }
            ),
            border = BorderStroke(
                1.dp,
                if (message.isUser) ElectricCyan.copy(alpha = 0.4f) else ObsidianCardBorder
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!message.isUser) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (message.isUser) ElectricCyan else NeonMagenta
                        )
                        if (message.isSampleAi) {
                            Surface(
                                color = AmberPulse.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "SAMPLE AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberPulse,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (!message.isUser) {
                        IconButton(
                            onClick = { onSpeakMessage(message.messageText) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("speak_msg_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Read response aloud",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Text(
                    text = message.messageText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (message.secondaryHint.isNotBlank()) {
                    Text(
                        text = message.secondaryHint,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Interactive Phone Action Execution Card when an Android Intent is attached
                AnimatedVisibility(visible = actionEnum != PhoneActionType.NONE) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = ObsidianSurfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (message.wasActionExecuted) EmeraldReady.copy(alpha = 0.6f)
                            else ElectricCyan.copy(alpha = 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when (actionEnum) {
                                        PhoneActionType.DIAL_PHONE -> Icons.Default.Call
                                        PhoneActionType.SET_ALARM -> Icons.Default.Alarm
                                        PhoneActionType.SET_TIMER -> Icons.Default.Timer
                                        PhoneActionType.WEB_SEARCH -> Icons.Default.Public
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = null,
                                    tint = if (message.wasActionExecuted) EmeraldReady else ElectricCyan
                                )
                                Column {
                                    Text(
                                        text = actionEnum.displayTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White
                                    )
                                    Text(
                                        text = actionEnum.hindiTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onExecuteAction(message.id, actionEnum, message.actionTarget)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (message.wasActionExecuted) EmeraldReady else ElectricCyan,
                                    contentColor = Color(0xFF051318)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("execute_action_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (message.wasActionExecuted) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.PlayArrow
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (message.wasActionExecuted) "Run Again" else "Execute",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandComposerBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    isListening: Boolean,
    assistantName: String,
    onMicClick: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledIconButton(
                onClick = onMicClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isListening) NeonMagenta else ElectricCyan.copy(alpha = 0.18f),
                    contentColor = if (isListening) Color.White else ElectricCyan
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("composer_mic_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.StopCircle else Icons.Default.Mic,
                    contentDescription = stringResource(R.string.mic_button_desc)
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        text = "Ask $assistantName or say 'Call 9876543210'...",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = ObsidianSurfaceVariant,
                    unfocusedContainerColor = ObsidianSurfaceVariant
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field")
            )

            FilledIconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF001F26),
                    disabledContainerColor = ObsidianSurfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send_button_desc)
                )
            }
        }
    }
}
