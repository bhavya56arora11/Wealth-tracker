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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentScreen(onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Stock") }
    var units by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var fundCode by remember { mutableStateOf("") }
    
    val investmentTypes = listOf("Stock", "Mutual Fund", "SIP")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Investment") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Investment Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Type", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                investmentTypes.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = units,
                    onValueChange = { units = it },
                    label = { Text("Units") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = buyPrice,
                    onValueChange = { buyPrice = it },
                    label = { Text("Buy Price/Unit") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            if (selectedType != "Stock") {
                OutlinedTextField(
                    value = fundCode,
                    onValueChange = { fundCode = it },
                    label = { Text("Fund Code (for NAV updates)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { /* TODO: Save to DB */ onNavigateBack() },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotEmpty() && units.isNotEmpty() && buyPrice.isNotEmpty()
            ) {
                Text("Save Investment")
            }
        }
    }
}
