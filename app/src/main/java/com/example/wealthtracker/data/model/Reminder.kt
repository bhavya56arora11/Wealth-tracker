package com.example.wealthtracker.data.model

import java.util.UUID

data class Reminder(
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var type: ReminderType = ReminderType.SIP,
    var amount: Double = 0.0,
    var frequency: ReminderFrequency = ReminderFrequency.MONTHLY,
    var nextDueDate: Long = 0L,
    @get:com.google.firebase.firestore.PropertyName("isActive")
    @set:com.google.firebase.firestore.PropertyName("isActive")
    var isActive: Boolean = true,
    var notes: String? = null
)

enum class ReminderType {
    SIP, SUBSCRIPTION
}

enum class ReminderFrequency {
    MONTHLY, QUARTERLY, YEARLY
}
