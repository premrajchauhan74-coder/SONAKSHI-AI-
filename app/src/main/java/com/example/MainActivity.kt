package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoAwesomeMotion
import androidx.compose.material.icons.outlined.BuildCircle
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SonakshiScreen
import com.example.ui.SonakshiViewModel
import com.example.ui.screens.AssistantCommandScreen
import com.example.ui.screens.FullSetupWizardScreen
import com.example.ui.screens.MacrosStudioScreen
import com.example.ui.screens.PhoneControlCenterScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val viewModel: SonakshiViewModel = viewModel(
                    factory = SonakshiViewModel.Factory(application)
                )
                SonakshiAiApp(viewModel = viewModel)
            }
        }
    }
}

private data class NavDestination(
    val screen: SonakshiScreen,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val NAV_DESTINATIONS = listOf(
    NavDestination(
        screen = SonakshiScreen.ASSISTANT_HUB,
        labelRes = R.string.nav_assistant,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_tab_assistant"
    ),
    NavDestination(
        screen = SonakshiScreen.PHONE_CONTROL,
        labelRes = R.string.nav_phone_control,
        selectedIcon = Icons.Filled.PhoneAndroid,
        unselectedIcon = Icons.Outlined.PhoneAndroid,
        testTag = "nav_tab_phone_control"
    ),
    NavDestination(
        screen = SonakshiScreen.MACROS_STUDIO,
        labelRes = R.string.nav_macros,
        selectedIcon = Icons.Filled.AutoAwesomeMotion,
        unselectedIcon = Icons.Outlined.AutoAwesomeMotion,
        testTag = "nav_tab_macros"
    ),
    NavDestination(
        screen = SonakshiScreen.FULL_SETUP,
        labelRes = R.string.nav_setup,
        selectedIcon = Icons.Filled.BuildCircle,
        unselectedIcon = Icons.Outlined.BuildCircle,
        testTag = "nav_tab_setup"
    )
)

@Composable
fun SonakshiAiApp(viewModel: SonakshiViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val macros by viewModel.macros.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val isProcessingAi by viewModel.isProcessingAi.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveTranscript.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val pendingGuardAction by viewModel.pendingGuardAction.collectAsStateWithLifecycle()
    val diagnostics by viewModel.diagnostics.collectAsStateWithLifecycle()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshDiagnostics()
        if (granted) {
            viewModel.postStatus("Microphone permission granted for ${config.assistantName}!")
            viewModel.startVoiceListening(context)
        } else {
            viewModel.postStatus("Microphone permission declined — use text or quick voice chips.")
        }
    }

    val handleMicTrigger: () -> Unit = {
        if (isListening) {
            viewModel.stopVoiceListening()
        } else if (diagnostics.hasMicPermission) {
            viewModel.startVoiceListening(context)
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 640.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        containerColor = ObsidianSurface,
                        tonalElevation = 8.dp
                    ) {
                        NAV_DESTINATIONS.forEach { dest ->
                            val selected = currentScreen == dest.screen
                            val label = if (dest.screen == SonakshiScreen.ASSISTANT_HUB) {
                                config.assistantName
                            } else {
                                stringResource(dest.labelRes)
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectScreen(dest.screen) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF001F26),
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = ObsidianSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        NAV_DESTINATIONS.forEach { dest ->
                            val selected = currentScreen == dest.screen
                            val label = if (dest.screen == SonakshiScreen.ASSISTANT_HUB) {
                                config.assistantName
                            } else {
                                stringResource(dest.labelRes)
                            }
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectScreen(dest.screen) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Floating Status Toast / Banner
                    AnimatedVisibility(visible = statusBanner != null) {
                        statusBanner?.let { bannerText ->
                            Surface(
                                color = ObsidianSurfaceVariant,
                                border = BorderStroke(1.dp, EmeraldReady.copy(alpha = 0.6f)),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .testTag("status_banner")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = bannerText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldReady,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.dismissStatusBanner() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss notification",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (currentScreen) {
                            SonakshiScreen.ASSISTANT_HUB -> {
                                AssistantCommandScreen(
                                    messages = messages,
                                    config = config,
                                    isListening = isListening,
                                    isSpeaking = isSpeaking,
                                    isProcessingAi = isProcessingAi,
                                    liveTranscript = liveTranscript,
                                    hasMicPermission = diagnostics.hasMicPermission,
                                    onMicClick = handleMicTrigger,
                                    onSendCommand = { viewModel.sendUserCommand(it) },
                                    onExecuteAction = { msgId, actionType, target ->
                                        viewModel.requestOrExecutePhoneAction(msgId, actionType, target)
                                    },
                                    onSpeakMessage = { viewModel.speakText(it) },
                                    onStopSpeaking = { viewModel.stopSpeaking() },
                                    onToggleAutoSpeak = {
                                        viewModel.updateAssistantConfig {
                                            it.copy(autoSpeakResponses = !it.autoSpeakResponses)
                                        }
                                    },
                                    onClearChat = { viewModel.clearChatHistory() },
                                    onOpenSetupTab = {
                                        viewModel.selectScreen(SonakshiScreen.FULL_SETUP)
                                    }
                                )
                            }

                            SonakshiScreen.PHONE_CONTROL -> {
                                PhoneControlCenterScreen(
                                    config = config,
                                    onExecutePhoneAction = { action, payload ->
                                        viewModel.requestOrExecutePhoneAction(null, action, payload)
                                    },
                                    onSpeakAndExecute = { speech, action, payload ->
                                        viewModel.speakText(speech)
                                        viewModel.requestOrExecutePhoneAction(null, action, payload)
                                    },
                                    onToggleTouchGuard = {
                                        viewModel.updateAssistantConfig {
                                            it.copy(touchGuardEnabled = !it.touchGuardEnabled)
                                        }
                                    },
                                    onBackToHome = {
                                        viewModel.selectScreen(SonakshiScreen.ASSISTANT_HUB)
                                    }
                                )
                            }

                            SonakshiScreen.MACROS_STUDIO -> {
                                MacrosStudioScreen(
                                    assistantName = config.assistantName,
                                    macros = macros,
                                    onRunMacro = { viewModel.runMacro(it) },
                                    onToggleMacro = { viewModel.toggleMacroEnabled(it) },
                                    onDeleteMacro = { viewModel.deleteMacro(it) },
                                    onCreateMacro = { title, trigger, spoken, action, payload, cat ->
                                        viewModel.addCustomMacro(title, trigger, spoken, action, payload, cat)
                                    },
                                    onBackToHome = {
                                        viewModel.selectScreen(SonakshiScreen.ASSISTANT_HUB)
                                    }
                                )
                            }

                            SonakshiScreen.FULL_SETUP -> {
                                FullSetupWizardScreen(
                                    config = config,
                                    diagnostics = diagnostics,
                                    onRequestMicPermission = {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    onExecutePhoneAction = { action, payload ->
                                        viewModel.requestOrExecutePhoneAction(null, action, payload)
                                    },
                                    onUpdateConfig = { transform ->
                                        viewModel.updateAssistantConfig(transform)
                                    },
                                    onTestVoice = { greeting ->
                                        viewModel.speakText(greeting)
                                    },
                                    onRefreshDiagnostics = {
                                        viewModel.refreshDiagnostics()
                                        viewModel.postStatus("System diagnostics refreshed.")
                                    },
                                    onBackToHome = {
                                        viewModel.selectScreen(SonakshiScreen.ASSISTANT_HUB)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Touch-Guard Confirmation Dialog for Sensitive Phone Actions
    pendingGuardAction?.let { guard ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelPendingGuardAction() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonMagenta
                )
            },
            title = {
                Text(
                    text = "${config.assistantName} Touch-Guard Verification",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = guard.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingGuardAction() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF001F26)
                    ),
                    modifier = Modifier.testTag("confirm_touch_guard_button")
                ) {
                    Text("Confirm & Launch", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelPendingGuardAction() },
                    modifier = Modifier.testTag("cancel_touch_guard_button")
                ) {
                    Text("Cancel")
                }
            },
            containerColor = ObsidianSurface,
            titleContentColor = Color.White,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
