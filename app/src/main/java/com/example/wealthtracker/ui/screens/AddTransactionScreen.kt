package com.example.wealthtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import com.example.wealthtracker.data.model.*
import com.example.wealthtracker.ui.viewmodel.WealthViewModel
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(viewModel: WealthViewModel, onNavigateBack: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }


    // Validation
    var amountError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }

    val categories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories
    val scrollState = rememberScrollState()

    // Merchant Autocomplete Logic
    val existingMerchants = viewModel.transactions.mapNotNull { it.merchant }.distinct()
    val merchantSuggestions = existingMerchants.filter { it.contains(merchant, ignoreCase = true) && it != merchant }.take(3)

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
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) { amount = it; amountError = false } },
                label = { Text("Amount") },
                isError = amountError,
                supportingText = if (amountError) {{ Text("Enter a valid amount") }} else null,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("₹") }
            )

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

            Text("Category", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category; categoryError = false },
                        label = { Text(category) }
                    )
                }
            }
            if (categoryError) {
                Text("Please select a category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }

            Column {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Vendor (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (merchantSuggestions.isNotEmpty()) {
                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        merchantSuggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { merchant = suggestion },
                                label = { Text(suggestion) }
                            )
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
                onClick = {
                    amountError = amount.toDoubleOrNull() == null || amount.isBlank()
                    categoryError = selectedCategory.isEmpty()
                    if (amountError || categoryError) return@Button

                    val amtValue = amount.toDouble()
                    viewModel.addTransaction(Transaction(
                        amount = amtValue,
                        type = selectedType,
                        category = selectedCategory,
                        merchant = merchant.ifEmpty { null },
                        note = note.ifEmpty { null }
                    ))
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Transaction", modifier = Modifier.padding(8.dp))
            }
        }
    }
}
