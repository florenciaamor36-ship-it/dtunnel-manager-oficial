package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.TunnelState
import com.example.ui.viewmodel.TunnelViewModel

@Composable
fun DashboardScreen(viewModel: TunnelViewModel) {
    val tunnelState by viewModel.tunnelState.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()

    val isConnected = tunnelState == TunnelState.CONNECTED
    val isConnecting = tunnelState == TunnelState.CONNECTING

    // Pulse animation for connecting/connected state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnecting || isConnected) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = SystemUiController.screenModifier(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ACTIVE SERVER",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedProfile?.name ?: "No Server Seleccionado",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Badge(
                        containerColor = when (tunnelState) {
                            TunnelState.CONNECTED -> Color(0xFF10B981)
                            TunnelState.CONNECTING -> Color(0xFFF59E0B)
                            TunnelState.ERROR -> Color(0xFFEF4444)
                            TunnelState.DISCONNECTED -> Color(0xFF64748B)
                        }
                    ) {
                        Text(
                            text = tunnelState.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip(label = "Protocol", value = selectedProfile?.tunnelType ?: "SSH+WS")
                    InfoChip(label = "Host", value = selectedProfile?.sshHost ?: "N/A")
                    InfoChip(label = "Puerto", value = selectedProfile?.sshPort?.toString() ?: "22")
                }
            }
        }

        // Center Connect Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
            ) {
                // Glow effect background
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    when (tunnelState) {
                                        TunnelState.CONNECTED -> Color(0xFF10B981).copy(alpha = 0.4f)
                                        TunnelState.CONNECTING -> Color(0xFFF59E0B).copy(alpha = 0.4f)
                                        else -> Color(0xFF00F0FF).copy(alpha = 0.2f)
                                    },
                                    Color.Transparent
                                )
                            )
                        )
                )

                Button(
                    onClick = {
                        if (isConnected || isConnecting) {
                            viewModel.stopTunnel()
                        } else {
                            viewModel.startTunnel()
                        }
                    },
                    modifier = Modifier
                        .size(150.dp)
                        .testTag("connect_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (tunnelState) {
                            TunnelState.CONNECTED -> Color(0xFF10B981)
                            TunnelState.CONNECTING -> Color(0xFFF59E0B)
                            else -> Color(0xFF00F0FF)
                        }
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (tunnelState) {
                                TunnelState.CONNECTED -> Icons.Default.Lock
                                TunnelState.CONNECTING -> Icons.Default.Refresh
                                else -> Icons.Default.PowerSettingsNew
                            },
                            contentDescription = "Estado de conexión",
                            tint = Color(0xFF0A0F1D),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (tunnelState) {
                                TunnelState.CONNECTED -> "DISCONNECT"
                                TunnelState.CONNECTING -> "CONNECTING"
                                else -> "CONNECT"
                            },
                            color = Color(0xFF0A0F1D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (tunnelState) {
                    TunnelState.CONNECTED -> "dtunnel secure protocol active (WebSocket / SSL)"
                    TunnelState.CONNECTING -> "Establishing secure tunnel..."
                    else -> "Tap button to start secure SSH/WebSocket VPN"
                },
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // Bottom stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Download,
                label = "Download",
                value = if (isConnected) "2.4 MB/s" else "0.0 KB/s",
                color = Color(0xFF00F0FF)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Upload,
                label = "Upload",
                value = if (isConnected) "450 KB/s" else "0.0 KB/s",
                color = Color(0xFF3B82F6)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.NetworkPing,
                label = "Ping",
                value = if (isConnected) "38 ms" else "-- ms",
                color = Color(0xFF10B981)
            )
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(text = value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

object SystemUiController {
    @Composable
    fun screenModifier(): Modifier {
        return Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp)
    }
}
