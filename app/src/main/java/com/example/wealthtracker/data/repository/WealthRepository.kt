package com.example.wealthtracker.data.repository

import android.util.Log
import com.example.wealthtracker.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class WealthRepository {
    private val db = FirebaseFirestore.getInstance()

    // Transactions
    fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        val listener = db.collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WealthRepository", "Transactions error", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun addTransaction(transaction: Transaction) {
        db.collection("transactions").document(transaction.id).set(transaction)
    }

    fun deleteTransaction(id: String) {
        db.collection("transactions").document(id).delete()
    }

    fun addInvestmentWithReminder(investment: Investment, reminder: Reminder?) {
        val batch = db.batch()
        batch.set(db.collection("investments").document(investment.id), investment)
        if (reminder != null) {
            batch.set(db.collection("reminders").document(reminder.id), reminder)
        }
        batch.commit()
    }

    // Investments
    fun getInvestments(): Flow<List<Investment>> = callbackFlow {
        val listener = db.collection("investments")
            .orderBy("purchaseDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WealthRepository", "Investments error", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(Investment::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun addInvestment(investment: Investment) {
        db.collection("investments").document(investment.id).set(investment)
    }

    fun updateInvestment(investment: Investment) {
        db.collection("investments").document(investment.id).set(investment)
    }

    fun deleteInvestment(id: String) {
        db.collection("investments").document(id).delete()
        db.collection("reminders").document(id).delete()
    }

    // Reminders
    fun getReminders(): Flow<List<Reminder>> = callbackFlow {
        val listener = db.collection("reminders")
            .orderBy("nextDueDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WealthRepository", "Reminders error", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(Reminder::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun addReminder(reminder: Reminder) {
        db.collection("reminders").document(reminder.id).set(reminder)
    }

    fun updateReminder(reminder: Reminder) {
        db.collection("reminders").document(reminder.id).set(reminder)
    }

    fun deleteReminder(id: String) {
        db.collection("reminders").document(id).delete()
        db.collection("investments").document(id).delete()
    }

    // Shared Expenses
    fun getSharedExpenses(): Flow<List<SharedExpense>> = callbackFlow {
        val listener = db.collection("shared_expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WealthRepository", "SharedExpenses error", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(SharedExpense::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun addSharedExpense(expense: SharedExpense, splits: List<ExpenseSplit>) {
        val batch = db.batch()
        val expenseRef = db.collection("shared_expenses").document(expense.id)
        batch.set(expenseRef, expense)

        for (split in splits) {
            val splitRef = db.collection("expense_splits").document(split.id)
            batch.set(splitRef, split)
        }
        
        batch.commit()
    }

    fun getExpenseSplits(): Flow<List<ExpenseSplit>> = callbackFlow {
        val listener = db.collection("expense_splits")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WealthRepository", "ExpenseSplits error", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(ExpenseSplit::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun settleSplit(splitId: String) {
        db.collection("expense_splits").document(splitId).delete()
    }
}
