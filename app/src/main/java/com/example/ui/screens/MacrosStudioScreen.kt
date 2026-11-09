package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.AutomationMacroEntity
import com.example.data.local.PhoneActionType
import com.example.ui.theme.AmberPulse
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun MacrosStudioScreen(
    assistantName: String,
    macros: List<AutomationMacroEntity>,
    onRunMacro: (AutomationMacroEntity) -> Unit,
    onToggleMacro: (AutomationMacroEntity) -> Unit,
    onDeleteMacro: (Int) -> Unit,
    onCreateMacro: (String, String, String, PhoneActionType, String, String) -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler { onBackToHome() }

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "$assistantName Agentic Macros",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Custom voice routines saved in on-device Room storage. Say the trigger phrase in chat or tap Run Macro to speak + execute.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (macros.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                        border = BorderStroke(1.dp, ObsidianCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesomeMotion,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No Custom Macros Yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Tap 'New Voice Macro' below to create a custom Hindi or English routine for $assistantName.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(macros, key = { it.id }) { macro ->
                    MacroCard(
                        macro = macro,
                        onRun = { onRunMacro(macro) },
                        onToggle = { onToggleMacro(macro) },
                        onDelete = { onDeleteMacro(macro.id) }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showCreateDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("New Voice Macro", fontWeight = FontWeight.Bold) },
            containerColor = ElectricCyan,
            contentColor = Color(0xFF001F26),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_macro_fab")
        )
    }

    if (showCreateDialog) {
        CreateMacroDialog(
            assistantName = assistantName,
            onDismiss = { showCreateDialog = false },
            onSave = { title, trigger, spoken, actionType, payload, category ->
                onCreateMacro(title, trigger, spoken, actionType, payload, category)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun MacroCard(
    macro: AutomationMacroEntity,
    onRun: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val actionEnum = runCatching { PhoneActionType.valueOf(macro.actionType) }
        .getOrDefault(PhoneActionType.NONE)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("macro_card_${macro.id}"),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(
            1.dp,
            if (macro.isEnabled) ElectricCyan.copy(alpha = 0.4f) else ObsidianCardBorder
        )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (macro.isEnabled) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        Text(
                            text = macro.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Say: \"${macro.triggerPhrase}\"",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonMagenta
                        )
                    }
                }

                Switch(
                    checked = macro.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("toggle_macro_${macro.id}")
                )
            }

            Surface(
                color = ObsidianSurfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Spoken TTS: \"${macro.spokenResponse}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = ElectricCyan.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = actionEnum.displayTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = AmberPulse.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Runs: ${macro.runCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberPulse,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("delete_macro_${macro.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete macro",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onRun,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldReady,
                            contentColor = Color(0xFF031B12)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("run_macro_${macro.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Run Macro", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateMacroDialog(
    assistantName: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, PhoneActionType, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var triggerPhrase by rememberSaveable { mutableStateOf("Hey $assistantName, ") }
    var spokenResponse by rememberSaveable {
        mutableStateOf("Zaroor! Main $assistantName hoon, aapka routine shuru ho gaya hai.")
    }
    var selectedAction by rememberSaveable { mutableStateOf(PhoneActionType.SET_TIMER) }
    var actionPayload by rememberSaveable { mutableStateOf("600") }

    val selectableActions = listOf(
        PhoneActionType.SET_TIMER,
        PhoneActionType.SET_ALARM,
        PhoneActionType.DIAL_PHONE,
        PhoneActionType.WEB_SEARCH,
        PhoneActionType.OPEN_WIFI_SETTINGS,
        PhoneActionType.OPEN_ACCESSIBILITY
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create $assistantName Voice Macro",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Macro Name (e.g., Evening Workout)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("macro_title_input")
                )

                OutlinedTextField(
                    value = triggerPhrase,
                    onValueChange = { triggerPhrase = it },
                    label = { Text("Voice Trigger Phrase") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("macro_trigger_input")
                )

                OutlinedTextField(
                    value = spokenResponse,
                    onValueChange = { spokenResponse = it },
                    label = { Text("Spoken Hindi/English TTS Greeting") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("macro_spoken_input")
                )

                Text(
                    text = "Linked Phone Control Action:",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectableActions.forEach { action ->
                        FilterChip(
                            selected = selectedAction == action,
                            onClick = {
                                selectedAction = action
                                actionPayload = when (action) {
                                    PhoneActionType.SET_TIMER -> "600"
                                    PhoneActionType.SET_ALARM -> "6:30"
                                    PhoneActionType.DIAL_PHONE -> "+919876543210"
                                    PhoneActionType.WEB_SEARCH -> "Android News"
                                    else -> "system"
                                }
                            },
                            label = { Text(action.displayTitle) }
                        )
                    }
                }

                OutlinedTextField(
                    value = actionPayload,
                    onValueChange = { actionPayload = it },
                    label = { Text("Action Parameter (Number / HH:MM / Seconds / Query)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title,
                            triggerPhrase,
                            spokenResponse,
                            selectedAction,
                            actionPayload,
                            "Custom Routine"
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_macro_button")
            ) {
                Text("Save Macro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
