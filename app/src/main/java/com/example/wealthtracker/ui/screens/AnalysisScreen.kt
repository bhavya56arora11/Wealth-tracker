package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.Transaction
import com.example.wealthtracker.data.model.TransactionType
import java.util.Locale

@Composable
fun AnalysisScreen() {
    var selectedPeriod by remember { mutableStateOf("Last 30 Days") }
    val periods = listOf("Last 7 Days", "Last 30 Days", "Last 90 Days", "Custom")

    // Dummy data for analysis
    val dummyTransactions = remember {
        listOf(
            Transaction(amount = 500.0, type = TransactionType.EXPENSE, category = "Food & Dining", merchant = "Starbucks"),
            Transaction(amount = 450.0, type = TransactionType.EXPENSE, category = "Food & Dining", merchant = "Starbucks"),
            Transaction(amount = 120.0, type = TransactionType.EXPENSE, category = "Transportation", merchant = "Uber"),
            Transaction(amount = 1500.0, type = TransactionType.EXPENSE, category = "Shopping", merchant = "Amazon"),
            Transaction(amount = 800.0, type = TransactionType.EXPENSE, category = "Shopping", merchant = "Amazon"),
            Transaction(amount = 300.0, type = TransactionType.EXPENSE, category = "Food & Dining", merchant = "Starbucks"),
            Transaction(amount = 2000.0, type = TransactionType.EXPENSE, category = "Bills", merchant = "Electric Co")
        )
    }

    val merchantStats = dummyTransactions
        .filter { it.type == TransactionType.EXPENSE && it.merchant != null }
        .groupBy { it.merchant!! }
        .map { (name, trans) ->
            MerchantStat(
                name = name,
                totalSpent = trans.sumOf { it.amount },
                count = trans.size,
                average = trans.sumOf { it.amount } / trans.size
            )
        }
        .sortedByDescending { it.totalSpent }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Spending Analysis",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(periods) { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period },
                    label = { Text(period) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (merchantStats.isNotEmpty()) {
            Text(
                "Top Merchants",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.SemiBold
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(merchantStats) { stat ->
                    MerchantAnalysisCard(stat, merchantStats.first().totalSpent.toFloat())
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No merchant data available for analysis")
            }
        }
    }
}

data class MerchantStat(
    val name: String,
    val totalSpent: Double,
    val count: Int,
    val average: Double
)

@Composable
fun MerchantAnalysisCard(stat: MerchantStat, maxSpent: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stat.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    "₹${String.format(Locale.getDefault(), "%.2f", stat.totalSpent)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Spending bar
            LinearProgressIndicator(
                progress = (stat.totalSpent / maxSpent).toFloat(),
                modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 2.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${stat.count} transactions", style = MaterialTheme.typography.bodySmall)
                Text("Avg: ₹${String.format(Locale.getDefault(), "%.2f", stat.average)}", style = MaterialTheme.typography.bodySmall)
                
                // Trend Indicator (Dummy)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (stat.totalSpent > 1000) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (stat.totalSpent > 1000) Color.Red else Color.Green
                    )
                    Text(
                        if (stat.totalSpent > 1000) "Increasing" else "Decreasing",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (stat.totalSpent > 1000) Color.Red else Color.Green
                    )
                }
            }
        }
    }
}
