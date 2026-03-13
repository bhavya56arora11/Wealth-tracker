package com.example.wealthtracker.data.model

import java.util.UUID

data class SharedExpense(
    val id: String = UUID.randomUUID().toString(),
    val totalAmount: Double,
    val description: String,
    val paidBy: String, // "You" or person's name
    val timestamp: Long = System.currentTimeMillis()
)

data class ExpenseSplit(
    val id: String = UUID.randomUUID().toString(),
    val sharedExpenseId: String,
    val personName: String,
    val amount: Double,
    val isSettled: Boolean = false,
    val settlementTimestamp: Long? = null
)

data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
