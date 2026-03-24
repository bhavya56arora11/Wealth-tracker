package com.example.wealthtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.ExpenseSplit
import com.example.wealthtracker.data.model.SharedExpense
import com.example.wealthtracker.ui.viewmodel.WealthViewModel
import java.text.SimpleDateFormat
import java.util.*

private val OweGreen   = Color(0xFF2E7D32)
private val OweRed     = Color(0xFFC62828)
private val OweGreenBg = Color(0xFFE8F5E9)
private val OweRedBg   = Color(0xFFFFEBEE)

private data class PersonBalance(
    val displayName: String,
    val key: String,
    val theyOweYou: Double,
    val youOweThem: Double,
    val netBalance: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedExpensesScreen(viewModel: WealthViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    val sharedExpenses = viewModel.sharedExpenses
    val splits         = viewModel.expenseSplits

    // derivedStateOf correctly tracks SnapshotStateList content changes
    val personBalances: List<PersonBalance> by remember {
        derivedStateOf {
            val groups = mutableMapOf<String, Triple<Double, Double, String>>()
            splits.filter { !it.isSettled }.forEach { split ->
                val expense = sharedExpenses.find { it.id == split.sharedExpenseId }
                    ?: return@forEach
                val key = split.personName.trim().lowercase(Locale.getDefault())
                val display = key.split(" ").joinToString(" ") { w ->
                    w.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) }
                }
                val cur = groups.getOrDefault(key, Triple(0.0, 0.0, display))
                val youPaid = expense.paidBy.trim().lowercase(Locale.getDefault()) == "you"
                groups[key] = if (youPaid)
                    Triple(cur.first + split.amount, cur.second, display)
                else
                    Triple(cur.first, cur.second + split.amount, display)
            }
            groups.map { (key, t) ->
                PersonBalance(t.third, key, t.first, t.second, t.first - t.second)
            }.sortedByDescending { it.netBalance }
        }
    }

    val totalOwed   = personBalances.filter { it.netBalance > 0 }.sumOf { it.netBalance }
    val totalYouOwe = personBalances.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
    val netOverall  = totalOwed - totalYouOwe

    if (showAddDialog) {
        AddSharedExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { personName, amount, description, youPaid ->
                val normalised = personName.trim().lowercase(Locale.getDefault())
                val id = UUID.randomUUID().toString()
                viewModel.addSharedExpense(
                    SharedExpense(id = id, totalAmount = amount, description = description,
                        paidBy = if (youPaid) "you" else normalised),
                    listOf(ExpenseSplit(sharedExpenseId = id, personName = normalised, amount = amount))
                )
                showAddDialog = false
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp)) {
        item { OverallBalanceSummary(totalOwed, totalYouOwe, netOverall, onAddClick = { showAddDialog = true }) }

        if (personBalances.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(48.dp), tint = OweGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("All settled up!", style = MaterialTheme.typography.titleMedium)
                        Text("No pending balances", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("People", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Tap to see details", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(personBalances, key = { it.key }) { balance ->
                val personSplits = splits.filter {
                    it.personName.trim().lowercase(Locale.getDefault()) == balance.key
                }
                PersonBalanceCard(
                    balance = balance, splits = personSplits, sharedExpenses = sharedExpenses,
                    onSettle = { viewModel.settleSplit(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun OverallBalanceSummary(totalOwed: Double, totalYouOwe: Double, netOverall: Double, onAddClick: () -> Unit) {
    val netColor = if (netOverall >= 0) OweGreen else OweRed
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Net Balance", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${if (netOverall >= 0) "+" else "-"}₹${String.format(Locale.getDefault(), "%.2f", kotlin.math.abs(netOverall))}",
                        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = netColor
                    )
                    Text(if (netOverall >= 0) "you're owed overall" else "you owe overall",
                        style = MaterialTheme.typography.bodySmall, color = netColor.copy(alpha = 0.7f))
                }
                FilledTonalButton(onClick = onAddClick, modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = OweGreenBg) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("You're owed", style = MaterialTheme.typography.labelSmall, color = OweGreen)
                        Text("₹${String.format(Locale.getDefault(), "%.2f", totalOwed)}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OweGreen)
                    }
                }
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = OweRedBg) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("You owe", style = MaterialTheme.typography.labelSmall, color = OweRed)
                        Text("₹${String.format(Locale.getDefault(), "%.2f", totalYouOwe)}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OweRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonBalanceCard(
    balance: PersonBalance, splits: List<ExpenseSplit>, sharedExpenses: List<SharedExpense>,
    onSettle: (String) -> Unit, modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val isPositive = balance.netBalance >= 0
    val netColor   = if (isPositive) OweGreen else OweRed
    val netBgColor = if (isPositive) OweGreenBg else OweRedBg

    Card(modifier = modifier.fillMaxWidth().clickable { expanded = !expanded }, shape = RoundedCornerShape(14.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = netBgColor, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(balance.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = netColor)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(balance.displayName, fontWeight = FontWeight.SemiBold)
                    Text(if (isPositive) "owes you" else "you owe",
                        style = MaterialTheme.typography.bodySmall, color = netColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${String.format(Locale.getDefault(), "%.2f", kotlin.math.abs(balance.netBalance))}",
                        fontWeight = FontWeight.Bold, color = netColor, style = MaterialTheme.typography.titleMedium)
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    splits.sortedByDescending {
                        sharedExpenses.find { e -> e.id == it.sharedExpenseId }?.timestamp ?: 0L
                    }.forEach { split ->
                        val expense  = sharedExpenses.find { it.id == split.sharedExpenseId }
                        val youPaid  = expense?.paidBy?.trim()?.lowercase(Locale.getDefault()) == "you"
                        val entryClr = if (youPaid) OweGreen else OweRed
                        val entryBg  = if (youPaid) OweGreenBg else OweRedBg
                        val dateStr  = expense?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.timestamp)) } ?: "—"

                        Surface(shape = RoundedCornerShape(10.dp),
                            color = if (split.isSettled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else entryBg,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (youPaid) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null,
                                    tint = if (split.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else entryClr,
                                    modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(expense?.description ?: "Shared Expense",
                                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                                        color = if (split.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                                    Text("$dateStr · ${if (youPaid) "you paid" else "they paid"}${if (split.isSettled) " · settled" else ""}",
                                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${if (youPaid) "+" else "-"}₹${String.format(Locale.getDefault(), "%.2f", split.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (split.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else entryClr,
                                        style = MaterialTheme.typography.bodyMedium)
                                    if (!split.isSettled) {
                                        TextButton(onClick = { onSettle(split.id) },
                                            contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                                            Text("Settle", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Breakdown row when both directions exist
                    if (balance.theyOweYou > 0 && balance.youOweThem > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("They owe you", style = MaterialTheme.typography.labelSmall, color = OweGreen)
                            Text("+₹${String.format(Locale.getDefault(), "%.2f", balance.theyOweYou)}", style = MaterialTheme.typography.labelSmall, color = OweGreen, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("You owe them", style = MaterialTheme.typography.labelSmall, color = OweRed)
                            Text("-₹${String.format(Locale.getDefault(), "%.2f", balance.youOweThem)}", style = MaterialTheme.typography.labelSmall, color = OweRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSharedExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (personName: String, amount: Double, description: String, youPaid: Boolean) -> Unit
) {
    var personName  by remember { mutableStateOf("") }
    var amount      by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var youPaid     by remember { mutableStateOf(true) }
    var nameError   by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var descError   by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = personName, onValueChange = { personName = it; nameError = false },
                    label = { Text("Person's Name") }, isError = nameError,
                    supportingText = if (nameError) {{ Text("Name cannot be empty") }} else null,
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { amount = it; amountError = false } },
                    label = { Text("Amount") }, isError = amountError,
                    supportingText = if (amountError) {{ Text("Enter a valid amount") }} else null,
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it; descError = false },
                    label = { Text("Purpose / Description") }, isError = descError,
                    supportingText = if (descError) {{ Text("Please enter a description") }} else null,
                    singleLine = true, modifier = Modifier.fillMaxWidth())

                Text("Who paid?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = youPaid, onClick = { youPaid = true }, label = { Text("I paid") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OweGreenBg, selectedLabelColor = OweGreen))
                    FilterChip(selected = !youPaid, onClick = { youPaid = false }, label = { Text("They paid") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OweRedBg, selectedLabelColor = OweRed))
                }
                Surface(shape = RoundedCornerShape(8.dp),
                    color = if (youPaid) OweGreenBg else OweRedBg, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (youPaid) "✓ They will owe you ₹${amount.ifEmpty { "0" }}"
                        else "✓ You will owe them ₹${amount.ifEmpty { "0" }}",
                        modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall,
                        color = if (youPaid) OweGreen else OweRed, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                nameError   = personName.isBlank()
                amountError = amount.toDoubleOrNull() == null || amount.isBlank()
                descError   = description.isBlank()
                if (!nameError && !amountError && !descError)
                    onConfirm(personName.trim(), amount.toDouble(), description.trim(), youPaid)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
