package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eliminar
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Editar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ServerProfile
import com.example.ui.viewmodel.TunnelViewModel

@Composable
fun ProfilesScreen(viewModel: TunnelViewModel) {
    val profiles by viewModel.profiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    var editorOpen by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<ServerProfile?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0F1D)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Perfiles VPS", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Button(
                onClick = { editingProfile = null; editorOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF0A0F1D))
                Spacer(Modifier.width(4.dp))
                Text("Agregar VPS", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(profiles, key = { it.id }) { profile ->
                val isSeleccionado = selectedProfile?.id == profile.id
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectProfile(profile) },
                    colors = CardDefaults.cardColors(containerColor = if (isSeleccionado) Color(0xFF1E293B) else Color(0xFF131C2E)),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSeleccionado) BorderStroke(1.dp, Color(0xFF00F0FF)) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, contentDescription = "VPS", tint = if (isSeleccionado) Color(0xFF00F0FF) else Color(0xFF64748B), modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(profile.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("${profile.sshHost}:${profile.sshPort} • ${profile.sshUser}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                Text(profile.tunnelType, fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            if (isSeleccionado) Icon(Icons.Default.CheckCircle, contentDescription = "Seleccionado", tint = Color(0xFF10B981))
                            IconButton(onClick = { editingProfile = profile; editorOpen = true }) {
                                Icon(Icons.Default.Editar, contentDescription = "Editar", tint = Color(0xFF00F0FF))
                            }
                            if (profiles.size > 1) {
                                IconButton(onClick = { viewModel.deleteProfile(profile) }) {
                                    Icon(Icons.Default.Eliminar, contentDescription = "Eliminar", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editorOpen) {
        VpsProfileEditaror(
            initial = editingProfile,
            onDismiss = { editorOpen = false },
            onGuardar = { profile ->
                if (editingProfile == null) viewModel.saveProfile(profile) else viewModel.updateProfile(profile)
                editorOpen = false
            }
        )
    }
}

@Composable
private fun VpsProfileEditaror(
    initial: ServerProfile?,
    onDismiss: () -> Unit,
    onGuardar: (ServerProfile) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var host by remember { mutableStateOf(initial?.sshHost ?: "") }
    var port by remember { mutableStateOf((initial?.sshPort ?: 22).toString()) }
    var user by remember { mutableStateOf(initial?.sshUser ?: "") }
    var password by remember { mutableStateOf(initial?.sshPass ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Agregar VPS" else "Editar VPS") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(host, { host = it }, label = { Text("IP o dominio") }, singleLine = true)
                OutlinedTextField(port, { port = it.filter(Char::isDigit) }, label = { Text("Puerto SSH") }, singleLine = true)
                OutlinedTextField(user, { user = it }, label = { Text("Usuario SSH") }, singleLine = true)
                OutlinedTextField(password, { password = it }, label = { Text("Contraseña SSH") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedPuerto = port.toIntOrNull()
                error = when {
                    name.isBlank() -> "Enter a name"
                    host.isBlank() -> "Enter an IP o dominio"
                    user.isBlank() -> "Enter an Usuario SSH"
                    parsedPuerto == null || parsedPuerto !in 1..65535 -> "Enter a valid port"
                    else -> null
                }
                if (error == null) {
                    onGuardar(ServerProfile(initial?.id ?: 0L, name.trim(), host.trim(), parsedPuerto!!, user.trim(), password, initial?.tunnelType ?: "SSH Direct", initial?.wsHost ?: "", initial?.wsPath ?: "/ws", initial?.wsHeaders ?: "", initial?.sni ?: "", initial?.customCarga útil ?: "", initial?.isSeleccionado ?: false))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
