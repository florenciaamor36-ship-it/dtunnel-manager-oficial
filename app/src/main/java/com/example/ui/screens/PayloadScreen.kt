package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TunnelViewModel

@Composable
fun PayloadScreen(viewModel: TunnelViewModel) {
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    var payload by remember(selectedProfile) {
        mutableStateOf(selectedProfile?.customPayload ?: "GET / HTTP/1.1[crlf]Host: [host_port][crlf]Upgrade: websocket[crlf][crlf]")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Custom Payload Editor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Supports [host_port], [crlf], [method], [protocol]",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Icon(imageVector = Icons.Default.Code, contentDescription = "Payload", tint = Color(0xFF00F0FF))
        }

        // Preset buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetButton(text = "WebSocket") {
                payload = "GET /ws HTTP/1.1[crlf]Host: [host_port][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]"
            }
            PresetButton(text = "HTTP Proxy") {
                payload = "CONNECT [host_port] HTTP/1.1[crlf]Host: [host_port][crlf]Proxy-Connection: Keep-Alive[crlf][crlf]"
            }
            PresetButton(text = "Direct SSL") {
                payload = "GET / HTTP/1.1[crlf]Host: [host_port][crlf]Connection: Keep-Alive[crlf][crlf]"
            }
        }

        OutlinedTextField(
            value = payload,
            onValueChange = { payload = it },
            label = { Text("HTTP Payload Request") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = textFieldColors()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    payload += "[crlf]"
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Insert [crlf]", color = Color(0xFF00F0FF))
            }
            Button(
                onClick = {
                    payload += "[host_port]"
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Insert [host_port]", color = Color(0xFF00F0FF))
            }
        }

        Button(
            onClick = {
                selectedProfile?.let { profile ->
                    viewModel.saveProfile(profile.copy(customPayload = payload))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = Color(0xFF0A0F1D))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Save Payload", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PresetButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F0FF)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(text = text, fontSize = 11.sp)
    }
}
