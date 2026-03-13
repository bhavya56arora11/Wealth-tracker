package com.example.wealthtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wealthtracker.data.model.SharedExpense
import java.util.Locale

@Composable
fun SharedExpensesScreen() {
    var selectedPerson by remember { mutableStateOf<String?>(null) }
    
    // Dummy Data
    val people = listOf("Rahul", "Sneha", "Amit", "Priya")
    val dummySharedExpenses = remember {
        listOf(
            SharedExpense(totalAmount = 1200.0, description = "Dinner at Olive", paidBy = "You"),
            SharedExpense(totalAmount = 3000.0, description = "Electricity Bill", paidBy = "Rahul"),
            SharedExpense(totalAmount = 450.0, description = "Movie Tickets", paidBy = "You"),
            SharedExpense(totalAmount = 800.0, description = "Groceries", paidBy = "Sneha"),
            SharedExpense(totalAmount = 150.0, description = "Snacks", paidBy = "Amit")
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SharedSummarySection()

        // People Filter
        Text(
            "Quick Settlement", 
            style = MaterialTheme.typography.titleSmall, 
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedPerson == null,
                    onClick = { selectedPerson = null },
                    label = { Text("All") }
                )
            }
            items(people) { person ->
                FilterChip(
                    selected = selectedPerson == person,
                    onClick = { selectedPerson = person },
                    label = { Text(person) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { /* Sort/Filter */ }) {
                Icon(Icons.Default.FilterList, contentDescription = null)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val displayList = if (selectedPerson == null) dummySharedExpenses 
                             else dummySharedExpenses.filter { it.paidBy == selectedPerson || it.paidBy == "You" }
            
            items(displayList) { expense ->
                SharedExpenseListItem(expense)
            }
        }
    }
}

@Composable
fun SharedSummarySection() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Net Balance", style = MaterialTheme.typography.labelLarge)
            Text("₹450.00", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Owed to you", style = MaterialTheme.typography.labelSmall)
                    Text("₹1,250", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                }
                VerticalDivider(modifier = Modifier.height(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You owe", style = MaterialTheme.typography.labelSmall)
                    Text("₹800", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@Composable
fun SharedExpenseListItem(expense: SharedExpense) {
    ListItem(
        headlineContent = { Text(expense.description, fontWeight = FontWeight.Medium) },
        supportingContent = { 
            Text(
                if (expense.paidBy == "You") "You paid ₹${String.format(Locale.getDefault(), "%.2f", expense.totalAmount)}" 
                else "${expense.paidBy} paid ₹${String.format(Locale.getDefault(), "%.2f", expense.totalAmount)}",
                style = MaterialTheme.typography.bodySmall
            ) 
        },
        trailingContent = {
            val splitAmount = expense.totalAmount / 3 // Placeholder logic
            Column(horizontalAlignment = Alignment.End) {
                if (expense.paidBy == "You") {
                    Text("You get", style = MaterialTheme.typography.labelSmall, color = Color(0xFF388E3C))
                    Text("₹${String.format(Locale.getDefault(), "%.2f", splitAmount * 2)}", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                } else {
                    Text("You owe", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD32F2F))
                    Text("₹${String.format(Locale.getDefault(), "%.2f", splitAmount)}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                }
            }
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                }
            }
        },
        modifier = Modifier.clickable { /* Detail View & Settle Button */ }
    )
}
