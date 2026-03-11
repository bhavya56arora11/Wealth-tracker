package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    
    val categories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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

            // Category Selection (Simple implementation for MVP)
            Text("Category", style = MaterialTheme.typography.titleMedium)
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 0.dp
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category) }
                    )
                }
            }

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Vendor (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

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
                enabled = amount.isNotEmpty() && selectedCategory.isNotEmpty()
            ) {
                Text("Save Transaction")
            }
        }
    }
}
