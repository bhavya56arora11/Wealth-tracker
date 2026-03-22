package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.Transaction
import com.example.wealthtracker.data.model.TransactionType
import com.example.wealthtracker.ui.viewmodel.WealthViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class DateRangeFilter { TODAY, THIS_WEEK, THIS_MONTH, ALL_TIME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: WealthViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf<TransactionType?>(null) }
    var selectedDateRange by remember { mutableStateOf(DateRangeFilter.ALL_TIME) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showChart by remember { mutableStateOf(false) }

    val allTransactions = viewModel.transactions

    val filteredTransactions = allTransactions.filter {
        val matchesSearch = it.merchant?.contains(searchQuery, ignoreCase = true) == true ||
                it.category.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedFilterType == null || it.type == selectedFilterType

        val calendar = Calendar.getInstance()
        val transactionTime = it.timestamp
        val matchesDate = when (selectedDateRange) {
            DateRangeFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
                transactionTime >= calendar.timeInMillis
            }
            DateRangeFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
                transactionTime >= calendar.timeInMillis
            }
            DateRangeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
                transactionTime >= calendar.timeInMillis
            }
            DateRangeFilter.ALL_TIME -> true
        }

        matchesSearch && matchesType && matchesDate
    }

    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense

    // ── Transaction Detail Dialog ────────────────────────────────────────────
    selectedTransaction?.let { transaction ->
        AlertDialog(
            onDismissRequest = { selectedTransaction = null },
            title = { Text(transaction.merchant ?: transaction.category) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Amount", "₹${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                        if (transaction.type == TransactionType.EXPENSE) Color(0xFFD32F2F) else Color(0xFF388E3C))
                    DetailRow("Type", if (transaction.type == TransactionType.EXPENSE) "Expense" else "Income")
                    DetailRow("Category", transaction.category)
                    DetailRow("Date", SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp)))
                    if (transaction.note != null) DetailRow("Note", transaction.note)
                }
            },
            confirmButton = { TextButton(onClick = { selectedTransaction = null }) { Text("Close") } },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction.id)
                        selectedTransaction = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Transactions") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SummaryCards(totalIncome, totalExpense, netSavings)

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search transactions") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showChart = !showChart }) {
                            Icon(
                                if (showChart) Icons.AutoMirrored.Filled.List else Icons.Default.PieChart,
                                contentDescription = "Toggle View"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = selectedFilterType == null, onClick = { selectedFilterType = null }, label = { Text("All") }) }
                    item { FilterChip(selected = selectedFilterType == TransactionType.EXPENSE, onClick = { selectedFilterType = TransactionType.EXPENSE }, label = { Text("Expenses") }) }
                    item { FilterChip(selected = selectedFilterType == TransactionType.INCOME, onClick = { selectedFilterType = TransactionType.INCOME }, label = { Text("Income") }) }
                }
            }

            if (showChart && filteredTransactions.isNotEmpty()) {
                CategoryPieChart(filteredTransactions.filter { it.type == TransactionType.EXPENSE })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTransactions.sortedByDescending { it.timestamp }, key = { it.id }) { transaction ->
                        TransactionListItem(transaction, onClick = { selectedTransaction = transaction })
                    }
                    if (filteredTransactions.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No transactions found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCards(income: Double, expense: Double, savings: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryCard("Income", income, Color(0xFF4CAF50), Modifier.weight(1f))
        SummaryCard("Expense", expense, Color(0xFFF44336), Modifier.weight(1f))
        SummaryCard("Savings", savings, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
    }
}

@Composable
fun SummaryCard(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
            Text(text = "₹${String.format(Locale.getDefault(), "%.0f", amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
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
                text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"} ₹${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.EXPENSE) Color(0xFFD32F2F) else Color(0xFF388E3C)
            )
        },
        leadingContent = {
            val icon = when (transaction.category) {
                "Food & Dining" -> Icons.Default.Restaurant
                "Transportation" -> Icons.Default.DirectionsCar
                "Shopping" -> Icons.Default.ShoppingBag
                else -> Icons.Default.Category
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun CategoryPieChart(transactions: List<Transaction>) {
    val categoryData = transactions.groupBy { it.category }.mapValues { it.value.sumOf { t -> t.amount } }
    val total = categoryData.values.sum()
    val colors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFF6D4C41))

    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(180.dp)) {
            var startAngle = -90f
            categoryData.values.forEachIndexed { index, value ->
                val sweepAngle = (value / total * 360).toFloat()
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
    }
}
