package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.Reminder
import com.example.wealthtracker.data.model.ReminderType
import com.example.wealthtracker.ui.viewmodel.WealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: WealthViewModel) {
    val allReminders = viewModel.reminders

    // Split into active and done
    val activeReminders = allReminders.filter { it.isActive }.sortedBy { it.nextDueDate }
    val doneReminders = allReminders.filter { !it.isActive }

    // Confirmation dialog state
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Transaction Blocked", color = MaterialTheme.colorScheme.error) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("OK") }
            }
        )
    }

    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text("Delete Reminder") },
            text = { Text("Remove \"${reminder.title}\" permanently?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteReminder(reminder.id); reminderToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Upcoming Payments") }) }
    ) { innerPadding ->
        if (allReminders.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("No reminders set") }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Active reminders ─────────────────────────────────────────
                if (activeReminders.isNotEmpty()) {
                    items(activeReminders, key = { it.id }) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onMarkDone = { 
                                val error = viewModel.markReminderDone(reminder.id)
                                if (error != null) errorMessage = error
                            },
                            onDelete = { reminderToDelete = reminder }
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("All reminders marked done!", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // ── Done section ─────────────────────────────────────────────
                if (doneReminders.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            "Completed (${doneReminders.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(doneReminders, key = { "done_${it.id}" }) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            isDone = true,
                            onMarkDone = {},  // already done, no-op
                            onDelete = { reminderToDelete = reminder }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    isDone: Boolean = false,
    onMarkDone: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isOverdue = !isDone && reminder.nextDueDate < now
    val isDueSoon = !isDone && reminder.nextDueDate <= now + 3 * 86400000L

    val statusColor = when {
        isDone -> MaterialTheme.colorScheme.outline
        isOverdue -> Color(0xFFD32F2F)
        isDueSoon -> Color(0xFFFBC02D)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (reminder.type == ReminderType.SIP) Icons.Default.Schedule else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and frequency
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        reminder.title,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${reminder.frequency.name.lowercase().replaceFirstChar { it.titlecase() }} • ₹${String.format(Locale.getDefault(), "%.0f", reminder.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Date + status label
                Column(horizontalAlignment = Alignment.End) {
                    val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(reminder.nextDueDate))
                    Text(
                        dateStr,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        when {
                            isDone -> "Done"
                            isOverdue -> "Overdue"
                            isDueSoon -> "Due Soon"
                            else -> "Upcoming"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }

            // ── Action buttons ───────────────────────────────────────────
            if (!isDone) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onMarkDone,
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Mark Done", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                // Done items only show a slim delete option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
