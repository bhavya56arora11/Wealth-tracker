package com.example.wealthtracker.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.wealthtracker.data.model.*
import java.util.*

class WealthViewModel : ViewModel() {
    // Transactions
    val transactions = mutableStateListOf<Transaction>()

    // Investments
    val investments = mutableStateListOf<Investment>()

    // Shared Expenses
    val sharedExpenses = mutableStateListOf<SharedExpense>()
    val expenseSplits = mutableStateListOf<ExpenseSplit>()

    // Reminders
    val reminders = mutableStateListOf<Reminder>()

    init {
        addMockData()
    }

    fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction)
    }

    fun addInvestment(investment: Investment) {
        investments.add(investment)
    }

    fun addSharedExpense(expense: SharedExpense, splits: List<ExpenseSplit>) {
        sharedExpenses.add(0, expense)
        expenseSplits.addAll(splits)
        
        // Also add to transactions for history
        addTransaction(Transaction(
            amount = expense.totalAmount,
            type = TransactionType.EXPENSE,
            category = "Shared",
            merchant = expense.description,
            timestamp = expense.timestamp,
            isShared = true,
            sharedExpenseId = expense.id
        ))
    }

    fun addReminder(reminder: Reminder) {
        reminders.add(reminder)
    }

    fun deleteInvestment(id: String) {
        investments.removeAll { it.id == id }
    }

    fun updateInvestment(updated: Investment) {
        val index = investments.indexOfFirst { it.id == updated.id }
        if (index != -1) investments[index] = updated
    }

    fun deleteTransaction(id: String) {
        transactions.removeAll { it.id == id }
    }

    fun deleteReminder(id: String) {
        reminders.removeAll { it.id == id }
    }

    fun markReminderDone(id: String) {
        val index = reminders.indexOfFirst { it.id == id }
        if (index != -1) reminders[index] = reminders[index].copy(isActive = false)
    }

    fun settleSplit(splitId: String) {
        val index = expenseSplits.indexOfFirst { it.id == splitId }
        if (index != -1) {
            val oldSplit = expenseSplits[index]
            expenseSplits[index] = oldSplit.copy(isSettled = true, settlementTimestamp = System.currentTimeMillis())
        }
    }

    private fun addMockData() {
        val now = System.currentTimeMillis()
        transactions.addAll(listOf(
            Transaction(amount = 25000.0, type = TransactionType.INCOME, category = "Salary", merchant = "Tech Corp", timestamp = now - 86400000),
            Transaction(amount = 500.0, type = TransactionType.EXPENSE, category = "Food & Dining", merchant = "Starbucks", timestamp = now - 3600000)
        ))
        
        investments.add(Investment(name = "HDFC Flexi Cap", type = InvestmentType.MUTUAL_FUND, units = 100.0, purchasePrice = 1200.0, currentNav = 1350.0))
        
        // Bidirectional Splitwise demo (all names stored lowercase)
        val id1 = UUID.randomUUID().toString()
        val id2 = UUID.randomUUID().toString()
        val id3 = UUID.randomUUID().toString()
        sharedExpenses.add(SharedExpense(id = id1, totalAmount = 400.0, description = "Dinner at Barbeque Nation", paidBy = "you"))
        expenseSplits.add(ExpenseSplit(sharedExpenseId = id1, personName = "rahul", amount = 400.0))
        sharedExpenses.add(SharedExpense(id = id2, totalAmount = 300.0, description = "Grocery run", paidBy = "amit"))
        expenseSplits.add(ExpenseSplit(sharedExpenseId = id2, personName = "amit", amount = 300.0))
        sharedExpenses.add(SharedExpense(id = id3, totalAmount = 250.0, description = "Movie tickets", paidBy = "you"))
        expenseSplits.add(ExpenseSplit(sharedExpenseId = id3, personName = "priya", amount = 250.0))

        
        reminders.add(Reminder(title = "Netflix", type = ReminderType.SUBSCRIPTION, amount = 499.0, frequency = ReminderFrequency.MONTHLY, nextDueDate = now + 86400000))
    }
}
