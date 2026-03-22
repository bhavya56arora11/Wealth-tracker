package com.example.wealthtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.Investment
import com.example.wealthtracker.data.model.InvestmentType
import com.example.wealthtracker.ui.viewmodel.WealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(viewModel: WealthViewModel) {
    var lastUpdate by remember { mutableStateOf(System.currentTimeMillis()) }
    var investmentToEdit by remember { mutableStateOf<Investment?>(null) }
    var investmentToDelete by remember { mutableStateOf<Investment?>(null) }

    val currentList = viewModel.investments

    val totalInvested = currentList.sumOf { it.units * it.purchasePrice }
    val currentValue = currentList.sumOf { it.units * (it.currentNav ?: it.purchasePrice) }
    val totalGain = currentValue - totalInvested
    val gainPercentage = if (totalInvested > 0) (totalGain / totalInvested) * 100 else 0.0

    // ── Edit Dialog ──────────────────────────────────────────────────────────
    investmentToEdit?.let { inv ->
        EditInvestmentDialog(
            investment = inv,
            onDismiss = { investmentToEdit = null },
            onConfirm = { updated ->
                viewModel.updateInvestment(updated)
                investmentToEdit = null
            }
        )
    }

    // ── Delete Confirm Dialog ────────────────────────────────────────────────
    investmentToDelete?.let { inv ->
        AlertDialog(
            onDismissRequest = { investmentToDelete = null },
            title = { Text("Delete Investment") },
            text = { Text("Remove \"${inv.name}\" from your portfolio?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteInvestment(inv.id)
                        investmentToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { investmentToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Investment Portfolio") },
                actions = {
                    IconButton(onClick = { lastUpdate = System.currentTimeMillis() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh NAV")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            InvestmentSummaryCard(currentValue, totalInvested, totalGain, gainPercentage, lastUpdate)

            if (currentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No investments yet", style = MaterialTheme.typography.titleMedium)
                        Text("Add your first investment to get started", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.id }) { investment ->
                        InvestmentItem(
                            investment = investment,
                            onEdit = { investmentToEdit = investment },
                            onDelete = { investmentToDelete = investment }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvestmentDialog(
    investment: Investment,
    onDismiss: () -> Unit,
    onConfirm: (Investment) -> Unit
) {
    var name by remember { mutableStateOf(investment.name) }
    var units by remember { mutableStateOf(investment.units.toString()) }
    var currentNav by remember { mutableStateOf(investment.currentNav?.toString() ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var unitsError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Investment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Name") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Name cannot be empty") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = units,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { units = it; unitsError = false } },
                    label = { Text("Units") },
                    isError = unitsError,
                    supportingText = if (unitsError) {{ Text("Enter a valid number") }} else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currentNav,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) currentNav = it },
                    label = { Text("Current NAV / Price (Optional)") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                nameError = name.isBlank()
                unitsError = units.toDoubleOrNull() == null
                if (!nameError && !unitsError) {
                    onConfirm(investment.copy(
                        name = name.trim(),
                        units = units.toDouble(),
                        currentNav = currentNav.toDoubleOrNull()
                    ))
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun InvestmentSummaryCard(current: Double, invested: Double, gain: Double, percentage: Double, lastUpdate: Long) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(lastUpdate))

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Portfolio Value", style = MaterialTheme.typography.labelLarge)
                Text("Last Updated: $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
            }
            Text(
                "₹${String.format(Locale.getDefault(), "%,.2f", current)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Invested Amount", style = MaterialTheme.typography.labelMedium)
                    Text("₹${String.format(Locale.getDefault(), "%,.2f", invested)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Returns", style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (gain >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (gain >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${if (gain >= 0) "+" else ""}₹${String.format(Locale.getDefault(), "%.2f", gain)} (${String.format(Locale.getDefault(), "%.1f", percentage)}%)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (gain >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvestmentItem(investment: Investment, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDetails by remember { mutableStateOf(false) }
    val investedValue = investment.units * investment.purchasePrice
    val currentValue = investment.units * (investment.currentNav ?: investment.purchasePrice)
    val gain = currentValue - investedValue
    val gainPercentage = if (investedValue > 0) (gain / investedValue) * 100 else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetails = !showDetails },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(investment.name, fontWeight = FontWeight.Bold, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                investment.type.name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${investment.units} units", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${String.format(Locale.getDefault(), "%.2f", currentValue)}", fontWeight = FontWeight.Bold)
                    Text(
                        "${if (gain >= 0) "+" else ""}₹${String.format(Locale.getDefault(), "%.2f", gain)} (${String.format(Locale.getDefault(), "%.1f", gainPercentage)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (gain >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F)
                    )
                }
            }

            AnimatedVisibility(visible = showDetails) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                    InvestmentDetailRow("Purchase Price", "₹${String.format(Locale.getDefault(), "%.2f", investment.purchasePrice)}")
                    InvestmentDetailRow("Current NAV", "₹${String.format(Locale.getDefault(), "%.2f", investment.currentNav ?: 0.0)}")
                    InvestmentDetailRow("Invested Value", "₹${String.format(Locale.getDefault(), "%.2f", investedValue)}")
                    if (investment.fundCode != null) {
                        InvestmentDetailRow("Fund Code", investment.fundCode)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onEdit) { Text("Edit") }
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Delete") }
                    }
                }
            }
        }
    }
}

@Composable
fun InvestmentDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
