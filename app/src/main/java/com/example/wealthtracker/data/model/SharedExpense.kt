package com.example.wealthtracker.data.model

import java.util.UUID

data class SharedExpense(
    var id: String = UUID.randomUUID().toString(),
    var totalAmount: Double = 0.0,
    var description: String = "",
    var paidBy: String = "", // "You" or person's name
    var timestamp: Long = System.currentTimeMillis()
)

data class ExpenseSplit(
    var id: String = UUID.randomUUID().toString(),
    var sharedExpenseId: String = "",
    var personName: String = "",
    var amount: Double = 0.0,
    @get:com.google.firebase.firestore.PropertyName("isSettled")
    @set:com.google.firebase.firestore.PropertyName("isSettled")
    var isSettled: Boolean = false,
    var settlementTimestamp: Long? = null
)

data class Person(
    var id: String = UUID.randomUUID().toString(),
    var name: String = ""
)
