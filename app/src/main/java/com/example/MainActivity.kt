package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TunnelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: TunnelViewModel = viewModel()
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF131C2E),
                            contentColor = Color(0xFF00F0FF)
                        ) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Dashboard") },
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = Color(0xFF00F0FF),
                                    indicatorColor = Color(0xFF00F0FF),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Config") },
                                label = { Text("Config") },
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = Color(0xFF00F0FF),
                                    indicatorColor = Color(0xFF00F0FF),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Code, contentDescription = "Payload") },
                                label = { Text("Payload") },
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = Color(0xFF00F0FF),
                                    indicatorColor = Color(0xFF00F0FF),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Terminal, contentDescription = "Logs") },
                                label = { Text("Logs") },
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = Color(0xFF00F0FF),
                                    indicatorColor = Color(0xFF00F0FF),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Dns, contentDescription = "Servers") },
                                label = { Text("Servers") },
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = Color(0xFF00F0FF),
                                    indicatorColor = Color(0xFF00F0FF),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Auth") },
                                label = { Text("Auth") },
                                selected = selectedTab == 5,
                                onClick = { selectedTab = 5 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = Color(0xFF00F0FF),
                                    indicatorColor = Color(0xFF00F0FF),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Router, contentDescription = "Tethering") },
                                label = { Text("Share") },
                                selected = selectedTab == 6,
                                onClick = { selectedTab = 6 },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = Color(0xFF00F0FF),
                                    indicatorColor = Color(0xFF00F0FF),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> DashboardScreen(viewModel)
                            1 -> ConfigScreen(viewModel)
                            2 -> PayloadScreen(viewModel)
                            3 -> LogsScreen(viewModel)
                            4 -> ProfilesScreen(viewModel)
                            5 -> AuthScreen(viewModel)
                            6 -> TetheringScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
