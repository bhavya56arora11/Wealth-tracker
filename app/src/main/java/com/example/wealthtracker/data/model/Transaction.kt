package com.example.wealthtracker.data.model

import java.util.UUID

data class Transaction(
    var id: String = UUID.randomUUID().toString(),
    var amount: Double = 0.0,
    var type: TransactionType = TransactionType.EXPENSE,
    var category: String = "",
    var merchant: String? = null,
    var note: String? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var isShared: Boolean = false,
    var sharedExpenseId: String? = null
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
