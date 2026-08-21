package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TunnelViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val vm: TunnelViewModel = viewModel()
                var selectedTab by remember { mutableIntStateOf(0) }
                var drawerOpen by remember { mutableStateOf(false) }
                val items = listOf(
                    Triple("Panel", Icons.Default.Home, 0), Triple("Configuración", Icons.Default.Settings, 1),
                    Triple("Carga útil", Icons.Default.Code, 2), Triple("Registros", Icons.Default.Terminal, 3),
                    Triple("Servidores", Icons.Default.Dns, 4), Triple("Autenticación", Icons.Default.Fingerprint, 5),
                    Triple("Compartir", Icons.Default.Router, 6), Triple("Usuarios", Icons.Default.Person, 7),
                    Triple("Importar / Exportar", Icons.Default.ImportExport, 8)
                )
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                LaunchedEffect(drawerOpen) { if (drawerOpen) drawerState.open() else drawerState.close() }
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = {
                        ModalDrawerSheet(drawerContainerColor = Color(0xFF131C2E)) {
                            Text("Dtunnel VPS Manager", color = Color(0xFF00F0FF), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(24.dp))
                            items.forEach { (label, icon, tab) ->
                                NavigationDrawerItem(
                                    icon = { Icon(icon, label) }, label = { Text(label) }, selected = selectedTab == tab,
                                    onClick = { selectedTab = tab; drawerOpen = false },
                                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = Color(0xFF00F0FF), selectedTextColor = Color(0xFF0A0F1D), unselectedTextColor = Color.White, unselectedIconColor = Color(0xFF94A3B8))
                                )
                            }
                        }
                    }
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(items.first { it.third == selectedTab }.first) },
                            navigationIcon = { IconButton(onClick = { drawerOpen = true }) { Icon(Icons.Default.Menu, "Abrir menú") } }
                        )
                    }) { padding ->
                        Box(Modifier.fillMaxSize().padding(padding)) {
                            when (selectedTab) {
                                0 -> DashboardScreen(vm, onOpenLogs = { selectedTab = 3 }, onOpenHwid = { selectedTab = 5 }); 1 -> ConfigScreen(vm); 2 -> PayloadScreen(vm); 3 -> LogsScreen(vm)
                                4 -> ProfilesScreen(vm); 5 -> AuthScreen(vm); 6 -> TetheringScreen(vm); 7 -> ManagedUsersScreen(vm)
                                8 -> BackupScreen(vm)
                            }
                        }
                    }
                }
            }
        }
    }
}
