package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import com.example.wealthtracker.data.model.Investment
import com.example.wealthtracker.data.model.InvestmentType
import com.example.wealthtracker.data.model.Reminder
import com.example.wealthtracker.data.model.ReminderFrequency
import com.example.wealthtracker.data.model.ReminderType
import com.example.wealthtracker.ui.viewmodel.WealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(viewModel: WealthViewModel, onNavigateBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ReminderType.SIP) }
    var selectedFrequency by remember { mutableStateOf(ReminderFrequency.MONTHLY) }
    var notes by remember { mutableStateOf("") }

    // Investment specific fields
    var invType by remember { mutableStateOf(InvestmentType.STOCK) }
    var invUnits by remember { mutableStateOf("") }
    var invPrice by remember { mutableStateOf("") }
    var invFundCode by remember { mutableStateOf("") }
    var invUnitsError by remember { mutableStateOf(false) }
    var invPriceError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis() + 86400000L)

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val selectedDateMillis = datePickerState.selectedDateMillis
    val displayDate = selectedDateMillis?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Select a date"

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false; dateError = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Reminder") },
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
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Reminder Title") },
                isError = titleError,
                supportingText = if (titleError) {{ Text("Title cannot be empty") }} else null,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Type", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility(visible = selectedType != ReminderType.SIP) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { amount = it; amountError = false } },
                    label = { Text("Amount") },
                    isError = amountError,
                    supportingText = if (amountError) {{ Text("Enter a valid amount") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("₹") }
                )
            }

            AnimatedVisibility(visible = selectedType == ReminderType.SIP) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Investment Details", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        InvestmentType.entries.forEach { type ->
                            FilterChip(
                                selected = invType == type,
                                onClick = { invType = type },
                                label = { Text(type.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = invUnits,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { invUnits = it; invUnitsError = false } },
                            label = { Text("Units") },
                            isError = invUnitsError,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = invPrice,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { invPrice = it; invPriceError = false } },
                            label = { Text("Price/Unit") },
                            isError = invPriceError,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("₹") }
                        )
                    }
                    if (invType != InvestmentType.STOCK) {
                        OutlinedTextField(
                            value = invFundCode,
                            onValueChange = { invFundCode = it },
                            label = { Text("Fund Code (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Text("Frequency", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderFrequency.entries.forEach { freq ->
                    FilterChip(
                        selected = selectedFrequency == freq,
                        onClick = { selectedFrequency = freq },
                        label = { Text(freq.name.lowercase().replaceFirstChar { it.titlecase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Date Picker Field ────────────────────────────────────────────
            OutlinedTextField(
                value = displayDate,
                onValueChange = {},
                label = { Text("Next Due Date") },
                readOnly = true,
                isError = dateError,
                supportingText = if (dateError) {{ Text("Please select a due date") }} else null,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    titleError = title.isBlank()
                    dateError = selectedDateMillis == null

                    if (selectedType == ReminderType.SIP) {
                        invUnitsError = invUnits.toDoubleOrNull() == null
                        invPriceError = invPrice.toDoubleOrNull() == null
                        amountError = false 

                        if (!titleError && !invUnitsError && !invPriceError && !dateError) {
                            val genId = java.util.UUID.randomUUID().toString()
                            val u = invUnits.toDouble()
                            val p = invPrice.toDouble()
                            val totalAmount = u * p

                            val reminder = Reminder(
                                id = genId,
                                title = title.trim(),
                                type = ReminderType.SIP,
                                amount = totalAmount,
                                frequency = selectedFrequency,
                                nextDueDate = selectedDateMillis!!,
                                notes = notes.ifEmpty { null }
                            )
                            val investment = Investment(
                                id = genId,
                                name = title.trim(),
                                type = invType,
                                units = u,
                                purchasePrice = p,
                                fundCode = invFundCode.ifEmpty { null }
                            )
                            viewModel.addInvestment(investment, reminder)
                            onNavigateBack()
                        }
                    } else {
                        amountError = amount.toDoubleOrNull() == null || amount.isBlank()
                        
                        if (!titleError && !amountError && !dateError) {
                            viewModel.addReminder(Reminder(
                                title = title.trim(),
                                type = selectedType,
                                amount = amount.toDouble(),
                                frequency = selectedFrequency,
                                nextDueDate = selectedDateMillis!!,
                                notes = notes.ifEmpty { null }
                            ))
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Set Reminder", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
