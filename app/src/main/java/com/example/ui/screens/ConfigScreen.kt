package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: TunnelViewModel) {
    val selectedProfile by viewModel.selectedProfile.collectAsState()

    var name by remember(selectedProfile) { mutableStateOf(selectedProfile?.name ?: "Custom Server") }
    var sshHost by remember(selectedProfile) { mutableStateOf(selectedProfile?.sshHost ?: "") }
    var sshPort by remember(selectedProfile) { mutableStateOf(selectedProfile?.sshPort?.toString() ?: "22") }
    var sshUser by remember(selectedProfile) { mutableStateOf(selectedProfile?.sshUser ?: "") }
    var sshPass by remember(selectedProfile) { mutableStateOf(selectedProfile?.sshPass ?: "") }
    var tunnelType by remember(selectedProfile) { mutableStateOf(selectedProfile?.tunnelType ?: "SSH + WebSocket") }
    var wsHost by remember(selectedProfile) { mutableStateOf(selectedProfile?.wsHost ?: "") }
    var wsPath by remember(selectedProfile) { mutableStateOf(selectedProfile?.wsPath ?: "/ws") }
    var sni by remember(selectedProfile) { mutableStateOf(selectedProfile?.sni ?: "") }

    var expandedType by remember { mutableStateOf(false) }
    val tunnelTypes = listOf("SSH Direct", "SSH + WebSocket", "SSL + SSH", "SSL + WebSocket")

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tunnel & SSH Configuration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Profile Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        // Tunnel Type Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedType,
            onExpandedChange = { expandedType = !expandedType },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = tunnelType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tunnel Protocol") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expandedType,
                onDismissRequest = { expandedType = false },
                modifier = Modifier.background(Color(0xFF1E293B))
            ) {
                tunnelTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type, color = Color.White) },
                        onClick = {
                            tunnelType = type
                            expandedType = false
                        }
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = sshHost,
                onValueChange = { sshHost = it },
                label = { Text("SSH Host / IP") },
                modifier = Modifier.weight(2f),
                colors = textFieldColors()
            )
            OutlinedTextField(
                value = sshPort,
                onValueChange = { sshPort = it },
                label = { Text("Port") },
                modifier = Modifier.weight(1f),
                colors = textFieldColors()
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = sshUser,
                onValueChange = { sshUser = it },
                label = { Text("Username") },
                modifier = Modifier.weight(1f),
                colors = textFieldColors()
            )
            OutlinedTextField(
                value = sshPass,
                onValueChange = { sshPass = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.weight(1f),
                colors = textFieldColors()
            )
        }

        if (tunnelType.contains("WebSocket")) {
            HorizontalDivider(color = Color(0xFF334155))
            Text(text = "WebSocket Options", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F0FF))
            OutlinedTextField(
                value = wsHost,
                onValueChange = { wsHost = it },
                label = { Text("Remote Proxy / WS Host (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            OutlinedTextField(
                value = wsPath,
                onValueChange = { wsPath = it },
                label = { Text("WebSocket Path") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        if (tunnelType.contains("SSL")) {
            HorizontalDivider(color = Color(0xFF334155))
            Text(text = "SSL / TLS Options", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F0FF))
            OutlinedTextField(
                value = sni,
                onValueChange = { sni = it },
                label = { Text("SNI (Server Name Indication)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val profile = ServerProfile(
                    id = selectedProfile?.id ?: 0L,
                    name = name,
                    sshHost = sshHost,
                    sshPort = sshPort.toIntOrNull() ?: 22,
                    sshUser = sshUser,
                    sshPass = sshPass,
                    tunnelType = tunnelType,
                    wsHost = wsHost,
                    wsPath = wsPath,
                    sni = sni,
                    customPayload = selectedProfile?.customPayload ?: "",
                    wsHeaders = selectedProfile?.wsHeaders ?: "",
                    isSelected = true
                )
                viewModel.saveProfile(profile)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = Color(0xFF0A0F1D))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Save & Apply Configuration", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00F0FF),
    unfocusedBorderColor = Color(0xFF334155),
    focusedLabelColor = Color(0xFF00F0FF),
    unfocusedLabelColor = Color(0xFF94A3B8),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF00F0FF),
    focusedContainerColor = Color(0xFF1E293B),
    unfocusedContainerColor = Color(0xFF1E293B)
)
