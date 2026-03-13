package com.example.wealthtracker.data.model

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: ReminderType,
    val amount: Double,
    val frequency: ReminderFrequency,
    val nextDueDate: Long,
    val isActive: Boolean = true,
    val notes: String? = null
)

enum class ReminderType {
    SIP, SUBSCRIPTION
}

enum class ReminderFrequency {
    MONTHLY, QUARTERLY, YEARLY
}
