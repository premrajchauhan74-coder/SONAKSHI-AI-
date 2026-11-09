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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AssistantConfig
import com.example.data.local.PhoneActionType
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun PhoneControlCenterScreen(
    config: AssistantConfig,
    onExecutePhoneAction: (PhoneActionType, String) -> Unit,
    onSpeakAndExecute: (String, PhoneActionType, String) -> Unit,
    onToggleTouchGuard: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler { onBackToHome() }

    var dialNumber by rememberSaveable { mutableStateOf("+919876543210") }
    var alarmHour by rememberSaveable { mutableStateOf("07") }
    var alarmMinute by rememberSaveable { mutableStateOf("00") }
    var searchQuery by rememberSaveable { mutableStateOf("Sonakshi AI Android automation") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${config.assistantName} Phone Control Deck",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "Direct 1-tap Android System Intents with real voice feedback and Touch-Guard protection.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Touch-Guard Security Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (config.touchGuardEnabled) EmeraldReady else NeonMagenta,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Touch-Guard Call & Accessibility Shield",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Asks for confirmation before launching outbound phone calls or accessibility panels.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = config.touchGuardEnabled,
                        onCheckedChange = { onToggleTouchGuard() },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldReady),
                        modifier = Modifier.testTag("touch_guard_switch")
                    )
                }
            }
        }

        // 1. Direct Smart Dialer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = ElectricCyan)
                        Text(
                            text = "Direct Voice & System Dialer (फ़ोन कॉल)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dialNumber,
                            onValueChange = { dialNumber = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("phone_dial_input")
                        )

                        Button(
                            onClick = {
                                onSpeakAndExecute(
                                    "${config.assistantName} dialing $dialNumber",
                                    PhoneActionType.DIAL_PHONE,
                                    dialNumber
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = Color(0xFF001F26)
                            ),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("launch_dialer_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Dial", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Quick System Alarm & Countdown Timer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = NeonMagenta)
                        Text(
                            text = "Alarm & Timer Automation (अलार्म और टाइमर)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = alarmHour,
                            onValueChange = { if (it.length <= 2) alarmHour = it },
                            label = { Text("Hour (0-23)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = alarmMinute,
                            onValueChange = { if (it.length <= 2) alarmMinute = it },
                            label = { Text("Min (0-59)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val h = alarmHour.toIntOrNull()?.coerceIn(0, 23) ?: 7
                                val m = alarmMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                                onSpeakAndExecute(
                                    "${config.assistantName} setting system alarm for $h:$m",
                                    PhoneActionType.SET_ALARM,
                                    "$h:$m"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("set_alarm_button")
                        ) {
                            Text("Set Alarm", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Instant Countdown Timers:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "1 Min" to 60,
                            "5 Min" to 300,
                            "10 Min" to 600,
                            "25 Min" to 1500
                        ).forEach { (label, seconds) ->
                            FilledTonalButton(
                                onClick = {
                                    onSpeakAndExecute(
                                        "${config.assistantName} starting $label timer",
                                        PhoneActionType.SET_TIMER,
                                        seconds.toString()
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("timer_preset_$seconds")
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        // 3. System Control Shortcuts Grid
        item {
            Text(
                text = "ANDROID SYSTEM CONTROL HOOKS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SystemControlTile(
                        title = "Accessibility UI",
                        subtitle = "Screen Control",
                        icon = Icons.Default.AccessibilityNew,
                        accent = ElectricCyan,
                        tag = "tile_accessibility",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onExecutePhoneAction(PhoneActionType.OPEN_ACCESSIBILITY, "accessibility")
                        }
                    )
                    SystemControlTile(
                        title = "Voice Assistant",
                        subtitle = "Default Role",
                        icon = Icons.Default.RecordVoiceOver,
                        accent = NeonMagenta,
                        tag = "tile_voice_assistant",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onExecutePhoneAction(PhoneActionType.OPEN_VOICE_ASSISTANT, "voice")
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SystemControlTile(
                        title = "Wi-Fi & Internet",
                        subtitle = "Network Panel",
                        icon = Icons.Default.Wifi,
                        accent = EmeraldReady,
                        tag = "tile_wifi",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onExecutePhoneAction(PhoneActionType.OPEN_WIFI_SETTINGS, "wifi")
                        }
                    )
                    SystemControlTile(
                        title = "Bluetooth Audio",
                        subtitle = "Pair Devices",
                        icon = Icons.Default.Bluetooth,
                        accent = NeonViolet,
                        tag = "tile_bluetooth",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onExecutePhoneAction(PhoneActionType.OPEN_BLUETOOTH, "bluetooth")
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SystemControlTile(
                        title = "App Permissions",
                        subtitle = "System Settings",
                        icon = Icons.Default.SettingsApplications,
                        accent = ElectricCyan,
                        tag = "tile_app_settings",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onExecutePhoneAction(PhoneActionType.OPEN_APP_SETTINGS, "app_settings")
                        }
                    )
                    SystemControlTile(
                        title = "Broadcast Note",
                        subtitle = "Share Intent",
                        icon = Icons.Default.Share,
                        accent = NeonMagenta,
                        tag = "tile_share_note",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onExecutePhoneAction(
                                PhoneActionType.SHARE_NOTE,
                                "Controlled hands-free via ${config.assistantName} AI on Android!"
                            )
                        }
                    )
                }
            }
        }

        // 4. Web Search Launcher
        item {
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
                        Icon(Icons.Default.Public, contentDescription = null, tint = ElectricCyan)
                        Text(
                            text = "Direct Web Search Action",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Query") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = {
                                onExecutePhoneAction(PhoneActionType.WEB_SEARCH, searchQuery)
                            },
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("launch_web_search_button")
                        ) {
                            Text("Search")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemControlTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.testTag(tag),
        shape = MaterialTheme.shapes.medium,
        color = ObsidianSurfaceVariant,
        border = BorderStroke(1.dp, ObsidianCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(26.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
