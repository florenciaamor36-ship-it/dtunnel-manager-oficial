package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.TunnelViewModel

@Composable
fun BackupScreen(viewModel: TunnelViewModel) {
    var message by remember { mutableStateOf("") }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            try { viewModel.getApplication<android.app.Application>().contentResolver.openOutputStream(uri)?.use { it.write(viewModel.exportSnapshot().toByteArray()) }; message = "Respaldo exportado correctamente" }
            catch (_: Exception) { message = "No se pudo exportar el respaldo" }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try { val text = viewModel.getApplication<android.app.Application>().contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""; viewModel.importSnapshot(text); message = "Respaldo importado correctamente" }
            catch (_: Exception) { message = "El archivo no es válido" }
        }
    }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Respaldos", style = MaterialTheme.typography.headlineSmall)
        Text("Exportá e importá perfiles, usuarios, HWID y configuraciones sin mostrar contraseñas en el archivo.")
        Button(onClick = { exportLauncher.launch("dtunnel-backup.json") }, modifier = Modifier.fillMaxWidth()) { Text("Exportar respaldo") }
        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) { Text("Importar respaldo") }
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
    }
}
