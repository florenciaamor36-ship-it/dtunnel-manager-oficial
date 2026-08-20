package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LogEntry
import com.example.data.LogType
import com.example.ui.viewmodel.TunnelViewModel

@Composable
fun LogsScreen(viewModel: TunnelViewModel) {
    val logs by viewModel.logs.collectAsState()

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
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Terminal, contentDescription = "Logs", tint = Color(0xFF00F0FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "dtunnel Console Logs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            IconButton(onClick = { viewModel.clearLogs() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Logs", tint = Color(0xFF94A3B8))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF050B14)),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            text = "No logs yet. Start the tunnel to view real-time WebSocket & SSH connection output.",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                items(logs) { log ->
                    Text(
                        text = "[${android.text.format.DateFormat.format("HH:mm:ss", log.timestamp)}] ${log.message}",
                        color = when (log.type) {
                            LogType.SUCCESS -> Color(0xFF10B981)
                            LogType.PAYLOAD -> Color(0xFF00F0FF)
                            LogType.PROXY -> Color(0xFF3B82F6)
                            LogType.SSH -> Color(0xFFF59E0B)
                            LogType.ERROR -> Color(0xFFEF4444)
                            LogType.INFO -> Color(0xFF94A3B8)
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
