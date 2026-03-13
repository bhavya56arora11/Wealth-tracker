package com.example.wealthtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.ReceiptLong)
    object Investments : Screen("investments", "Investments", Icons.Default.TrendingUp)
    object Shared : Screen("shared", "Shared", Icons.Default.Group)
    object Reminders : Screen("reminders", "Reminders", Icons.Default.Notifications)
    object Analysis : Screen("analysis", "Analysis", Icons.Default.Assessment)
    
    // Sub-screens
    object AddTransaction : Screen("add_transaction", "Add Transaction")
    object AddInvestment : Screen("add_investment", "Add Investment")
    object AddReminder : Screen("add_reminder", "Add Reminder")
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Expenses,
    Screen.Investments,
    Screen.Shared,
    Screen.Reminders
)
