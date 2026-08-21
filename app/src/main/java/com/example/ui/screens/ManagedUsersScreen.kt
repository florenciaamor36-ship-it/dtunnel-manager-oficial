package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ManagedUser
import com.example.ui.viewmodel.TunnelViewModel

@Composable
fun ManagedUsersScreen(viewModel: TunnelViewModel) {
    val users by viewModel.managedUsers.collectAsState()
    var editorOpen by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Color(0xFF0A0F1D)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("Users & HWID", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("${users.size} managed accounts", color = Color(0xFF94A3B8)) }
            Button(onClick = { editorOpen = true }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Add") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users, key = { it.id }) { user ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E))) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(Modifier.weight(1f)) { Icon(Icons.Default.Person, null, tint = Color(0xFF00F0FF)); Spacer(Modifier.width(8.dp)); Column {
                            Text(user.username, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("HWID: ${if (user.hwid.isBlank()) "not linked" else user.hwid}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("${user.status} • expires ${user.expiresAt.ifBlank { "never" }} • max ${user.maxDevices}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("Protocols: ${user.protocols}", color = Color(0xFF64748B), fontSize = 11.sp)
                        } }
                        Row {
                            IconButton(onClick = { viewModel.resetUserHwid(user) }) { Icon(Icons.Default.LockReset, "Reset HWID", tint = Color(0xFFF59E0B)) }
                            IconButton(onClick = { viewModel.deleteManagedUser(user) }) { Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444)) }
                        }
                    }
                }
            }
        }
    }
    if (editorOpen) {
        ManagedUserEditor(onDismiss = { editorOpen = false }, onSave = { viewModel.saveManagedUser(it); editorOpen = false })
    }
}

@Composable
private fun ManagedUserEditor(onDismiss: () -> Unit, onSave: (ManagedUser) -> Unit) {
    var username by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    var hwid by remember { mutableStateOf("") }; var expires by remember { mutableStateOf("") }
    var maxDevices by remember { mutableStateOf("1") }; var protocols by remember { mutableStateOf("SSH") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add managed user") }, text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(username, { username = it }, label = { Text("Username") }, singleLine = true)
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(hwid, { hwid = it }, label = { Text("HWID (optional)") }, singleLine = true)
        OutlinedTextField(expires, { expires = it }, label = { Text("Expires YYYY-MM-DD") }, singleLine = true)
        OutlinedTextField(maxDevices, { maxDevices = it.filter(Char::isDigit) }, label = { Text("Max devices") }, singleLine = true)
        OutlinedTextField(protocols, { protocols = it }, label = { Text("Protocols") }, singleLine = true)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
    } }, confirmButton = { TextButton(onClick = {
        val max = maxDevices.toIntOrNull()
        error = when { username.isBlank() -> "Username is required"; password.isBlank() -> "Password is required"; max == null || max < 1 -> "Max devices must be at least 1"; else -> null }
        if (error == null) onSave(ManagedUser(username = username.trim(), password = password, hwid = hwid.trim(), expiresAt = expires.trim(), maxDevices = max!!, protocols = protocols.trim().ifBlank { "SSH" }))
    }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
