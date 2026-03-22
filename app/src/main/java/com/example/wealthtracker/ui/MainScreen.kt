package com.example.wealthtracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wealthtracker.ui.navigation.Screen
import com.example.wealthtracker.ui.navigation.bottomNavItems
import com.example.wealthtracker.ui.screens.*
import com.example.wealthtracker.ui.viewmodel.WealthViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Instantiate ViewModel here
    val viewModel: WealthViewModel = viewModel()

    Scaffold(
        bottomBar = {
            val hideBottomBar = currentDestination?.route in listOf(
                Screen.AddTransaction.route,
                Screen.AddInvestment.route,
                Screen.AddReminder.route
            )
            
            if (!hideBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { screen.icon?.let { Icon(it, contentDescription = null) } },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (currentDestination?.route) {
                Screen.Expenses.route -> {
                    FloatingActionButton(onClick = { navController.navigate(Screen.AddTransaction.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                }
                Screen.Investments.route -> {
                    FloatingActionButton(onClick = { navController.navigate(Screen.AddInvestment.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Investment")
                    }
                }
                Screen.Reminders.route -> {
                    FloatingActionButton(onClick = { navController.navigate(Screen.AddReminder.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel) }
            composable(Screen.Expenses.route) { ExpensesScreen(viewModel) }
            composable(Screen.Investments.route) { InvestmentsScreen(viewModel) }
            composable(Screen.Shared.route) { SharedExpensesScreen(viewModel) }
            composable(Screen.Reminders.route) { RemindersScreen(viewModel) }
            
            composable(Screen.AddTransaction.route) { 
                AddTransactionScreen(viewModel, onNavigateBack = { navController.popBackStack() }) 
            }
            composable(Screen.AddInvestment.route) {
                AddInvestmentScreen(viewModel, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.AddReminder.route) {
                AddReminderScreen(viewModel, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
