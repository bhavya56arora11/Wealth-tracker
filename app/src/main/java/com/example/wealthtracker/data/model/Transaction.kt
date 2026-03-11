package com.example.wealthtracker.data.model

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val merchant: String? = null,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isShared: Boolean = false,
    val sharedExpenseId: String? = null
)

enum class TransactionType {
    INCOME, EXPENSE
}

val expenseCategories = listOf(
    "Food & Dining",
    "Transportation",
    "Shopping",
    "Entertainment",
    "Bills & Utilities",
    "Health & Fitness",
    "Education",
    "Travel",
    "Groceries",
    "Personal Care",
    "Miscellaneous"
)

val incomeCategories = listOf(
    "Salary",
    "Freelance/Contract Work",
    "Investment Returns",
    "Gifts/Bonuses",
    "Business Income",
    "Other Sources"
)
