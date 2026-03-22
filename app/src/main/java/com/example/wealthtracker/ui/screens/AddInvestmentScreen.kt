package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.Investment
import com.example.wealthtracker.data.model.InvestmentType
import com.example.wealthtracker.ui.viewmodel.WealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentScreen(viewModel: WealthViewModel, onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InvestmentType.STOCK) }
    var units by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var fundCode by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var unitsError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val investmentTypes = InvestmentType.entries

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
                onValueChange = { name = it; nameError = false },
                label = { Text("Investment Name") },
                isError = nameError,
                supportingText = if (nameError) {{ Text("Name cannot be empty") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Type", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                investmentTypes.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.name.replace("_", " ")) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = units,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { units = it; unitsError = false } },
                    label = { Text("Units") },
                    isError = unitsError,
                    supportingText = if (unitsError) {{ Text("Required") }} else null,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = buyPrice,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { buyPrice = it; priceError = false } },
                    label = { Text("Price/Unit") },
                    isError = priceError,
                    supportingText = if (priceError) {{ Text("Required") }} else null,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("₹") }
                )
            }

            if (selectedType != InvestmentType.STOCK) {
                OutlinedTextField(
                    value = fundCode,
                    onValueChange = { fundCode = it },
                    label = { Text("Fund Code (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("For automatic NAV updates") }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    nameError = name.isBlank()
                    unitsError = units.toDoubleOrNull() == null
                    priceError = buyPrice.toDoubleOrNull() == null
                    if (!nameError && !unitsError && !priceError) {
                        viewModel.addInvestment(Investment(
                            name = name.trim(),
                            type = selectedType,
                            units = units.toDouble(),
                            purchasePrice = buyPrice.toDouble(),
                            fundCode = fundCode.ifEmpty { null }
                        ))
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to Portfolio", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
