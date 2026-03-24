package com.example.wealthtracker.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthtracker.data.model.*
import com.example.wealthtracker.data.repository.WealthRepository
import kotlinx.coroutines.launch

class WealthViewModel : ViewModel() {
    private val repository = WealthRepository()

    // UI States (SnapshotStateList ensures no changes needed in UI files)
    val transactions = mutableStateListOf<Transaction>()
    val investments = mutableStateListOf<Investment>()
    val sharedExpenses = mutableStateListOf<SharedExpense>()
    val expenseSplits = mutableStateListOf<ExpenseSplit>()
    val reminders = mutableStateListOf<Reminder>()

    init {
        // Collect real-time streams and update UI state
        viewModelScope.launch {
            repository.getTransactions().collect { list ->
                transactions.clear()
                transactions.addAll(list)
            }
        }
        viewModelScope.launch {
            repository.getInvestments().collect { list ->
                investments.clear()
                investments.addAll(list)
            }
        }
        viewModelScope.launch {
            repository.getSharedExpenses().collect { list ->
                sharedExpenses.clear()
                sharedExpenses.addAll(list)
            }
        }
        viewModelScope.launch {
            repository.getExpenseSplits().collect { list ->
                expenseSplits.clear()
                expenseSplits.addAll(list)
            }
        }
        viewModelScope.launch {
            repository.getReminders().collect { list ->
                reminders.clear()
                reminders.addAll(list)
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        repository.addTransaction(transaction)
    }

    fun addInvestment(investment: Investment) {
        repository.addInvestment(investment)
    }

    fun addSharedExpense(expense: SharedExpense, splits: List<ExpenseSplit>) {
        repository.addSharedExpense(expense, splits)
    }

    fun addReminder(reminder: Reminder) {
        repository.addReminder(reminder)
    }

    fun deleteInvestment(id: String) {
        repository.deleteInvestment(id)
    }

    fun updateInvestment(updated: Investment) {
        repository.updateInvestment(updated)
    }

    fun deleteTransaction(id: String) {
        repository.deleteTransaction(id)
    }

    fun deleteReminder(id: String) {
        repository.deleteReminder(id)
    }

    fun markReminderDone(id: String) {
        val reminder = reminders.find { it.id == id } ?: return
        
        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = reminder.nextDueDate }
        when (reminder.frequency) {
            ReminderFrequency.MONTHLY -> calendar.add(java.util.Calendar.MONTH, 1)
            ReminderFrequency.QUARTERLY -> calendar.add(java.util.Calendar.MONTH, 3)
            ReminderFrequency.YEARLY -> calendar.add(java.util.Calendar.YEAR, 1)
        }
        repository.updateReminder(reminder.copy(nextDueDate = calendar.timeInMillis))
    }

    fun settleSplit(splitId: String) {
        repository.settleSplit(splitId)
    }
}
