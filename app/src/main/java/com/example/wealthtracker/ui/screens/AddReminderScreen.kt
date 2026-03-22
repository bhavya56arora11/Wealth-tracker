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

    // DatePicker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis() + 86400000L)

    // Validation error states
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val selectedDateMillis = datePickerState.selectedDateMillis
    val displayDate = selectedDateMillis?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Select a date"

    // ── DatePicker Dialog ────────────────────────────────────────────────────
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
                    amountError = amount.toDoubleOrNull() == null || amount.isBlank()
                    dateError = selectedDateMillis == null

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
