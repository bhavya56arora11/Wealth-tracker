package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DashboardScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Dashboard Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun InvestmentsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Investments Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun SharedExpensesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Shared Expenses Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun RemindersScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Reminders Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun AnalysisScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Merchant Analysis Screen", style = MaterialTheme.typography.headlineMedium)
    }
}
