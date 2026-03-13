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
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Dummy Data
    val dummyTransactions = remember {
        val now = System.currentTimeMillis()
        listOf(
            Transaction(amount = 500.0, type = TransactionType.EXPENSE, category = "Food & Dining", merchant = "Starbucks", timestamp = now - 3600000, note = "Coffee with friends"),
            Transaction(amount = 25000.0, type = TransactionType.INCOME, category = "Salary", merchant = "Tech Corp", timestamp = now - 86400000, note = "Monthly paycheck"),
            Transaction(amount = 120.0, type = TransactionType.EXPENSE, category = "Transportation", merchant = "Uber", timestamp = now - 172800000),
            Transaction(amount = 1500.0, type = TransactionType.EXPENSE, category = "Shopping", merchant = "Amazon", timestamp = now - 259200000, note = "New headphones"),
            Transaction(amount = 3000.0, type = TransactionType.EXPENSE, category = "Bills & Utilities", merchant = "Electricity Bill", timestamp = now - 10 * 86400000),
            Transaction(amount = 200.0, type = TransactionType.INCOME, category = "Gifts/Bonuses", merchant = "Friend", timestamp = now - 432000000)
        )
    }

    val filteredTransactions = dummyTransactions.filter {
        val matchesSearch = it.merchant?.contains(searchQuery, ignoreCase = true) == true || 
                          it.category.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedFilterType == null || it.type == selectedFilterType
        
        val calendar = Calendar.getInstance()
        val transactionTime = it.timestamp
        val matchesDate = when (selectedDateRange) {
            DateRangeFilter.TODAY -> {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                transactionTime >= calendar.timeInMillis
            }
            DateRangeFilter.THIS_WEEK -> {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                transactionTime >= calendar.timeInMillis
            }
            DateRangeFilter.THIS_MONTH -> {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                transactionTime >= calendar.timeInMillis
            }
            DateRangeFilter.ALL_TIME -> true
        }
        
        matchesSearch && matchesType && matchesDate
    }

    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense

    Column(modifier = Modifier.fillMaxSize()) {
        SummaryCards(totalIncome, totalExpense, netSavings)

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search transactions") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                item { 
                    VerticalDivider(modifier = Modifier.height(32.dp).padding(vertical = 4.dp)) 
                }
                DateRangeFilter.values().forEach { range ->
                    item {
                        FilterChip(
                            selected = selectedDateRange == range,
                            onClick = { selectedDateRange = range },
                            label = { Text(range.name.replace("_", " ").lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) }
                        )
                    }
                }
            }
        }

        Text(
            "Transactions", 
            style = MaterialTheme.typography.titleMedium, 
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredTransactions.sortedByDescending { it.timestamp }) { transaction ->
                TransactionListItem(transaction, onClick = { selectedTransaction = transaction })
            }
        }
    }

    // Transaction Detail Dialog
    selectedTransaction?.let { transaction ->
        AlertDialog(
            onDismissRequest = { selectedTransaction = null },
            title = { Text(transaction.merchant ?: transaction.category) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Amount", "₹${String.format("%.2f", transaction.amount)}", if (transaction.type == TransactionType.EXPENSE) Color.Red else Color.Green)
                    DetailRow("Category", transaction.category)
                    DetailRow("Date", SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp)))
                    if (transaction.note != null) {
                        DetailRow("Note", transaction.note)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTransaction = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SummaryCards(income: Double, expense: Double, savings: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard("Income", income, Color(0xFF4CAF50), Modifier.weight(1f))
        SummaryCard("Expense", expense, Color(0xFFF44336), Modifier.weight(1f))
        SummaryCard("Savings", savings, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
    }
}

@Composable
fun SummaryCard(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
            Text(
                text = "₹${String.format("%.0f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun TransactionListItem(transaction: Transaction, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(transaction.timestamp))
    
    ListItem(
        headlineContent = { Text(transaction.merchant ?: transaction.category, fontWeight = FontWeight.Medium) },
        supportingContent = { Text("${transaction.category} • $dateStr", style = MaterialTheme.typography.bodySmall) },
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
                    Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
