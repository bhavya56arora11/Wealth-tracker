package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.TransactionType
import com.example.wealthtracker.ui.viewmodel.WealthViewModel
import java.util.*

enum class AnalysisPeriod(val label: String, val days: Int) {
    WEEK("Last 7 Days", 7),
    MONTH("Last 30 Days", 30),
    QUARTER("Last 90 Days", 90),
    ALL("All Time", -1)
}

data class MerchantStat(
    val name: String,
    val totalSpent: Double,
    val count: Int,
    val average: Double
)

@Composable
fun DashboardScreen(viewModel: WealthViewModel) {
    var selectedPeriod by remember { mutableStateOf(AnalysisPeriod.MONTH) }

    val transactions = viewModel.transactions
    val investments = viewModel.investments
    val splits = viewModel.expenseSplits
    val reminders = viewModel.reminders

    val totalInvested = investments.sumOf { it.units * it.purchasePrice }
    val currentValue = investments.sumOf { it.units * (it.currentNav ?: it.purchasePrice) }
    val totalGain = currentValue - totalInvested
    val gainPercentage = if (totalInvested > 0) (totalGain / totalInvested) * 100 else 0.0

    // Month start at midnight of the 1st
    val monthStartCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val monthStart = monthStartCal.timeInMillis

    val monthlyInc = transactions.filter { it.type == TransactionType.INCOME && it.timestamp >= monthStart }.sumOf { it.amount }
    val monthlyExp = transactions.filter { it.type == TransactionType.EXPENSE && it.timestamp >= monthStart }.sumOf { it.amount }

    val amountOwedToYou = splits.filter { !it.isSettled }.sumOf { it.amount }
    val nextReminder = reminders.filter { it.nextDueDate >= System.currentTimeMillis() }.minByOrNull { it.nextDueDate }

    // Filter transactions for analysis section based on selected period
    val periodStart = if (selectedPeriod == AnalysisPeriod.ALL) 0L
    else System.currentTimeMillis() - selectedPeriod.days * 86400000L

    val periodTransactions = transactions.filter {
        it.timestamp >= periodStart && it.type == TransactionType.EXPENSE
    }

    val merchantStats = periodTransactions
        .filter { it.merchant != null }
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

    val categoryData = periodTransactions
        .groupBy { it.category }
        .mapValues { it.value.sumOf { t -> t.amount } }
        .filter { it.value > 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        item {
            Text(
                "Wealth Overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
        }

        // ── Net Worth Card ───────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Estimated Net Worth", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    Text(
                        "₹${String.format(Locale.getDefault(), "%,.0f", currentValue + monthlyInc - monthlyExp)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DashboardMiniStat("Monthly Inc", "₹${String.format(Locale.getDefault(), "%.0f", monthlyInc)}", Icons.Default.ArrowUpward)
                        DashboardMiniStat("Monthly Exp", "₹${String.format(Locale.getDefault(), "%.0f", monthlyExp)}", Icons.Default.ArrowDownward)
                    }
                }
            }
        }

        // ── Quick Insight Cards ──────────────────────────────────────────────
        item {
            Text(
                "Quick Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardFeatureCard(
                    title = "Investments",
                    value = "${if (totalGain >= 0) "+" else ""}${String.format(Locale.getDefault(), "%.1f", gainPercentage)}%",
                    subtitle = if (totalGain >= 0) "Portfolio growing" else "Market dip",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = if (totalGain >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                )
                DashboardFeatureCard(
                    title = "Shared",
                    value = "₹${String.format(Locale.getDefault(), "%.0f", amountOwedToYou)}",
                    subtitle = "Owed to you",
                    icon = Icons.Default.Group,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Next Reminder ────────────────────────────────────────────────────
        if (nextReminder != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Next Reminder", style = MaterialTheme.typography.labelMedium)
                            Text(nextReminder.title, fontWeight = FontWeight.SemiBold)
                        }
                        Text("₹${String.format(Locale.getDefault(), "%.0f", nextReminder.amount)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ── Spending Analysis Header ─────────────────────────────────────────
        item {
            Text(
                "Spending Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
        }

        // ── Period Filter Chips ──────────────────────────────────────────────
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AnalysisPeriod.entries) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period.label) }
                    )
                }
            }
        }

        // ── Category Pie Chart ───────────────────────────────────────────────
        if (categoryData.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("By Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DashboardPieChart(categoryData, modifier = Modifier.size(140.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            val pieColors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFF6D4C41))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                categoryData.entries.take(5).forEachIndexed { index, (cat, amt) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier.size(10.dp),
                                            shape = RoundedCornerShape(2.dp),
                                            color = pieColors[index % pieColors.size]
                                        ) {}
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(cat, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), maxLines = 1)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("₹${String.format(Locale.getDefault(), "%.0f", amt)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expense data for this period", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Top Merchants ────────────────────────────────────────────────────
        if (merchantStats.isNotEmpty()) {
            item {
                Text(
                    "Top Merchants",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            val maxSpent = merchantStats.first().totalSpent.toFloat()
            items(merchantStats) { stat ->
                MerchantAnalysisCard(stat, maxSpent, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun DashboardPieChart(categoryData: Map<String, Double>, modifier: Modifier = Modifier) {
    val total = categoryData.values.sum()
    val colors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFF6D4C41))
    Canvas(modifier = modifier) {
        var startAngle = -90f
        categoryData.values.forEachIndexed { index, value ->
            val sweepAngle = (value / total * 360).toFloat()
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 36f, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun MerchantAnalysisCard(stat: MerchantStat, maxSpent: Float, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            LinearProgressIndicator(
                progress = { (stat.totalSpent / maxSpent).toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp),
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
            }
        }
    }
}

@Composable
fun DashboardMiniStat(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun DashboardFeatureCard(title: String, value: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
