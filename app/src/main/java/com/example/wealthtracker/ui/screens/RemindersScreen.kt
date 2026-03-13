package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.wealthtracker.data.model.ReminderFrequency
import com.example.wealthtracker.data.model.ReminderType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RemindersScreen() {
    // Dummy Data
    val dummyReminders = remember {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        listOf(
            Reminder(title = "HDFC SIP", type = ReminderType.SIP, amount = 5000.0, frequency = ReminderFrequency.MONTHLY, nextDueDate = now + 86400000),
            Reminder(title = "Netflix", type = ReminderType.SUBSCRIPTION, amount = 499.0, frequency = ReminderFrequency.MONTHLY, nextDueDate = now - 3600000), // Overdue
            Reminder(title = "Google One", type = ReminderType.SUBSCRIPTION, amount = 1300.0, frequency = ReminderFrequency.YEARLY, nextDueDate = now + 432000000),
            Reminder(title = "ICICI SIP", type = ReminderType.SIP, amount = 2000.0, frequency = ReminderFrequency.MONTHLY, nextDueDate = now + 2 * 86400000)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Upcoming Payments",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sortedReminders = dummyReminders.sortedBy { it.nextDueDate }
            
            items(sortedReminders) { reminder ->
                ReminderItem(reminder)
            }
        }
    }
}

@Composable
fun ReminderItem(reminder: Reminder) {
    val now = System.currentTimeMillis()
    val isOverdue = reminder.nextDueDate < now
    val isDueSoon = reminder.nextDueDate <= now + 3 * 86400000L
    
    val statusColor = when {
        isOverdue -> Color(0xFFD32F2F)
        isDueSoon -> Color(0xFFFBC02D)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (reminder.type == ReminderType.SIP) Icons.Default.Schedule else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.title, fontWeight = FontWeight.Bold)
                Text(
                    "${reminder.frequency.name} • ₹${String.format(Locale.getDefault(), "%.0f", reminder.amount)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(reminder.nextDueDate))
                Text(
                    dateStr,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                Text(
                    if (isOverdue) "Overdue" else if (isDueSoon) "Due Soon" else "Upcoming",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }
    }
}
