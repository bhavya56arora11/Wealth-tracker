package com.example.wealthtracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wealthtracker.ui.navigation.Screen
import com.example.wealthtracker.ui.navigation.bottomNavItems
import com.example.wealthtracker.ui.screens.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            // Hide bottom bar on "Add" screens
            val hideBottomBar = currentDestination?.route in listOf(
                Screen.AddTransaction.route,
                Screen.AddInvestment.route
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
                Screen.Expenses.route, Screen.Dashboard.route -> {
                    FloatingActionButton(onClick = { navController.navigate(Screen.AddTransaction.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                }
                Screen.Investments.route -> {
                    FloatingActionButton(onClick = { navController.navigate(Screen.AddInvestment.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Investment")
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
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Expenses.route) { ExpensesScreen() }
            composable(Screen.Investments.route) { InvestmentsScreen() }
            composable(Screen.Shared.route) { SharedExpensesScreen() }
            composable(Screen.Reminders.route) { RemindersScreen() }
            composable(Screen.Analysis.route) { AnalysisScreen() }
            
            composable(Screen.AddTransaction.route) { 
                AddTransactionScreen(onNavigateBack = { navController.popBackStack() }) 
            }
            composable(Screen.AddInvestment.route) {
                AddInvestmentScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
