package com.example.wealthtracker.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.ExpenseSplit
import com.example.wealthtracker.data.model.SharedExpense
import com.example.wealthtracker.ui.viewmodel.WealthViewModel
import java.util.Locale

@Composable
fun SharedExpensesScreen(viewModel: WealthViewModel) {
    var selectedPerson by remember { mutableStateOf<String?>(null) }

    val sharedExpenses = viewModel.sharedExpenses
    val splits = viewModel.expenseSplits

    // Only pending (unsettled) splits
    val pendingSplits = splits.filter { !it.isSettled }

    // People who actually have PENDING dues
    val peopleWithPendingDues = pendingSplits.map { it.personName }.distinct()

    // If the currently selected person no longer has pending dues, reset filter
    if (selectedPerson != null && selectedPerson !in peopleWithPendingDues) {
        selectedPerson = null
    }

    val totalOwedToYou = pendingSplits.sumOf { it.amount }
    // Amount settled so far
    val totalSettled = splits.filter { it.isSettled }.sumOf { it.amount }

    val displayedSplits = pendingSplits.filter {
        selectedPerson == null || it.personName == selectedPerson
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SharedSummarySection(totalOwedToYou, totalSettled)

        // ── Filter by Person (only shown when there are pending dues) ────────
        if (peopleWithPendingDues.isNotEmpty()) {
            Text(
                "Filter by Person",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedPerson == null,
                        onClick = { selectedPerson = null },
                        label = { Text("All") }
                    )
                }
                items(peopleWithPendingDues) { person ->
                    val countForPerson = pendingSplits.count { it.personName == person }
                    FilterChip(
                        selected = selectedPerson == person,
                        onClick = { selectedPerson = person },
                        label = { Text("$person ($countForPerson)") }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pending Splits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (displayedSplits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFF388E3C))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("All settled up!", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(displayedSplits, key = { it.id }) { split ->
                    val expense = sharedExpenses.find { it.id == split.sharedExpenseId }
                    ExpenseSplitItem(split, expense, onSettle = { viewModel.settleSplit(split.id) })
                }
            }
        }
    }
}

@Composable
fun SharedSummarySection(owedToYou: Double, totalSettled: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pending Balance", style = MaterialTheme.typography.labelLarge)
            Text(
                "₹${String.format(Locale.getDefault(), "%.2f", owedToYou)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Owed to you", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "₹${String.format(Locale.getDefault(), "%.2f", owedToYou)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )
                }
                VerticalDivider(modifier = Modifier.height(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total settled", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "₹${String.format(Locale.getDefault(), "%.2f", totalSettled)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseSplitItem(split: ExpenseSplit, expense: SharedExpense?, onSettle: () -> Unit) {
    ListItem(
        headlineContent = { Text(expense?.description ?: "Shared Expense", fontWeight = FontWeight.Medium) },
        supportingContent = { Text("${split.personName} owes you", style = MaterialTheme.typography.bodySmall) },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${String.format(Locale.getDefault(), "%.2f", split.amount)}", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                Button(
                    onClick = onSettle,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Settle", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    )
}
