package com.example.wealthtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.TransactionType
import com.example.wealthtracker.data.model.expenseCategories
import com.example.wealthtracker.data.model.incomeCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(onNavigateBack: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    // Shared Expense State
    var isShared by remember { mutableStateOf(false) }
    var numPeople by remember { mutableStateOf("2") }
    var paidByYou by remember { mutableStateOf(true) }

    val categories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("₹") }
            )

            // Type Selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { 
                        selectedType = TransactionType.EXPENSE
                        selectedCategory = ""
                    },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { 
                        selectedType = TransactionType.INCOME
                        selectedCategory = ""
                    },
                    label = { Text("Income") }
                )
            }

            // Category Selection - Scrollable Chips
            Text("Category", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Vendor (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Shared Expense Toggle
            if (selectedType == TransactionType.EXPENSE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Shared Expense", style = MaterialTheme.typography.titleMedium)
                            Switch(checked = isShared, onCheckedChange = { isShared = it })
                        }
                        
                        AnimatedVisibility(visible = isShared) {
                            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = numPeople,
                                    onValueChange = { numPeople = it },
                                    label = { Text("Number of People") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = paidByYou, onCheckedChange = { paidByYou = it })
                                    Text("Paid by you")
                                }
                                
                                val perPerson = try { amount.toDouble() / numPeople.toInt() } catch(e: Exception) { 0.0 }
                                Text(
                                    "Each person owes: ₹${String.format("%.2f", perPerson)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = { /* TODO: Save to DB */ onNavigateBack() },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotEmpty() && selectedCategory.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Transaction", modifier = Modifier.padding(8.dp))
            }
        }
    }
}
