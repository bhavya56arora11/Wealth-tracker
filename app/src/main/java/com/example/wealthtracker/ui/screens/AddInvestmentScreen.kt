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
import com.example.wealthtracker.data.model.InvestmentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentScreen(onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InvestmentType.STOCK) }
    var units by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var fundCode by remember { mutableStateOf("") }
    
    val investmentTypes = InvestmentType.values()

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
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g. Apple Inc or HDFC Fund") }
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
                    onValueChange = { units = it },
                    label = { Text("Units") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("0.0000") }
                )
                OutlinedTextField(
                    value = buyPrice,
                    onValueChange = { buyPrice = it },
                    label = { Text("Price/Unit") },
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
                    placeholder = { Text("For automatic NAV updates") },
                    supportingText = { Text("Match with AMFI fund code for live updates") }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { /* TODO: Save to DB */ onNavigateBack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotEmpty() && units.isNotEmpty() && buyPrice.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to Portfolio", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
