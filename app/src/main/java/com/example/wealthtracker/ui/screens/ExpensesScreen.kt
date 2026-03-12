package com.example.wealthtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthtracker.data.model.Transaction
import com.example.wealthtracker.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

enum class DateRangeFilter {
    TODAY, THIS_WEEK, THIS_MONTH, ALL_TIME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf<TransactionType?>(null) }
    var selectedDateRange by remember { mutableStateOf(DateRangeFilter.ALL_TIME) }
    var showCategoryBreakdown by remember { mutableStateOf(true) }

    // Dummy Data
    val dummyTransactions = remember {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val todayStart = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val weekStart = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = calendar.timeInMillis

        listOf(
            Transaction(amount = 500.0, type = TransactionType.EXPENSE, category = "Food & Dining", merchant = "Starbucks", timestamp = now - 3600000),
            Transaction(amount = 25000.0, type = TransactionType.INCOME, category = "Salary", merchant = "Tech Corp", timestamp = now - 86400000),
            Transaction(amount = 120.0, type = TransactionType.EXPENSE, category = "Transportation", merchant = "Uber", timestamp = now - 172800000),
            Transaction(amount = 1500.0, type = TransactionType.EXPENSE, category = "Shopping", merchant = "Amazon", timestamp = now - 259200000),
            Transaction(amount = 450.0, type = TransactionType.EXPENSE, category = "Food & Dining", merchant = "Pizza Hut", timestamp = now),
            Transaction(amount = 3000.0, type = TransactionType.EXPENSE, category = "Bills & Utilities", merchant = "Electricity Bill", timestamp = now - 10 * 86400000),
            Transaction(amount = 200.0, type = TransactionType.INCOME, category = "Gifts/Bonuses", merchant = "Friend", timestamp = now - 432000000)
        )
    }

    val filteredTransactions = dummyTransactions.filter {
        val matchesSearch = it.merchant?.contains(searchQuery, ignoreCase = true) == true || 
                          it.category.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedFilterType == null || it.type == selectedFilterType
        
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val matchesDate = when (selectedDateRange) {
            DateRangeFilter.TODAY -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                it.timestamp >= calendar.timeInMillis
            }
            DateRangeFilter.THIS_WEEK -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                it.timestamp >= calendar.timeInMillis
            }
            DateRangeFilter.THIS_MONTH -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                it.timestamp >= calendar.timeInMillis
            }
            DateRangeFilter.ALL_TIME -> true
        }
        
        matchesSearch && matchesType && matchesDate
    }

    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Cards
        SummaryCards(totalIncome, totalExpense, netSavings)

        // Filters Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search merchant or category") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Type and Date Filters
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilterType == null,
                        onClick = { selectedFilterType = null },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilterType == TransactionType.EXPENSE,
                        onClick = { selectedFilterType = TransactionType.EXPENSE },
                        label = { Text("Expenses") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilterType == TransactionType.INCOME,
                        onClick = { selectedFilterType = TransactionType.INCOME },
                        label = { Text("Income") }
                    )
                }
                item { VerticalDivider(modifier = Modifier.height(32.dp).padding(vertical = 4.dp)) }
                DateRangeFilter.values().forEach { range ->
                    item {
                        FilterChip(
                            selected = selectedDateRange == range,
                            onClick = { selectedDateRange = range },
                            label = { Text(range.name.replace("_", " ").lowercase().capitalize()) }
                        )
                    }
                }
            }
        }

        // Category Breakdown (Visual representation of distribution)
        if (selectedFilterType != TransactionType.INCOME) {
            val categorySpending = filteredTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { t -> t.amount } }
                .toList()
                .sortedByDescending { it.second }

            if (categorySpending.isNotEmpty()) {
                Text(
                    "Category Distribution",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categorySpending) { (category, amount) ->
                        CategorySpendingCard(category, amount, (amount / totalExpense).toFloat())
                    }
                }
            }
        }

        // Transaction List
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${filteredTransactions.size} found", style = MaterialTheme.typography.bodySmall)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(filteredTransactions.sortedByDescending { it.timestamp }) { transaction ->
                TransactionListItem(transaction)
            }
        }
    }
}

@Composable
fun CategorySpendingCard(category: String, amount: Double, percentage: Float) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(category, style = MaterialTheme.typography.labelSmall)
            Text("₹${String.format("%.0f", amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = percentage,
                modifier = Modifier.width(60.dp).height(4.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TransactionListItem(transaction: Transaction) {
    val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(transaction.timestamp))
    
    ListItem(
        headlineContent = { 
            Text(
                transaction.merchant ?: transaction.category,
                fontWeight = FontWeight.Medium
            ) 
        },
        supportingContent = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(transaction.category, style = MaterialTheme.typography.bodySmall)
                Text(" • ", style = MaterialTheme.typography.bodySmall)
                Text(dateStr, style = MaterialTheme.typography.bodySmall)
            }
        },
        trailingContent = {
            Text(
                text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"} ₹${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.EXPENSE) Color(0xFFD32F2F) else Color(0xFF388E3C)
            )
        },
        leadingContent = {
            val icon = when(transaction.category) {
                "Food & Dining" -> Icons.Default.Restaurant
                "Transportation" -> Icons.Default.DirectionsCar
                "Shopping" -> Icons.Default.ShoppingBag
                "Salary" -> Icons.Default.Payments
                else -> Icons.Default.Category
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, 
                        contentDescription = null, 
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        modifier = Modifier.clickable { /* Detail View */ }
    )
}
