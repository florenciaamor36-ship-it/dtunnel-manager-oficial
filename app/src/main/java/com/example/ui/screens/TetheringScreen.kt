package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TunnelViewModel

@Composable
fun TetheringScreen(viewModel: TunnelViewModel) {
    val context = LocalContext.current
    var isProxyServerRunning by remember { mutableStateOf(false) }
    var proxyPort by remember { mutableStateOf("1080") }
    var selectedTabMode by remember { mutableIntStateOf(0) } // 0: USB Compartir internet, 1: Wi-Fi Directo / Hotspot, 2: Smart TV Proxy

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Router, contentDescription = "Compartir internet", tint = Color(0xFF00F0FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Compartir internet & Proxy Share",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Proxy Server Master Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "HTTP/SOCKS Proxy Server", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = if (isProxyServerRunning) "Active on 0.0.0.0:$proxyPort" else "Server Stopped", fontSize = 12.sp, color = if (isProxyServerRunning) Color(0xFF10B981) else Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = isProxyServerRunning,
                        onCheckedChange = {
                            isProxyServerRunning = it
                            Toast.makeText(context, if (it) "Proxy Compartir internet Server Started on port $proxyPort" else "Proxy Server Stopped", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF), checkedTrackColor = Color(0xFF131C2E))
                    )
                }

                OutlinedTextField(
                    value = proxyPort,
                    onValueChange = { proxyPort = it },
                    label = { Text("Puerto proxy local") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
            }
        }

        // Mode Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTabMode == 0,
                onClick = { selectedTabMode = 0 },
                label = { Text("Compartir por USB") },
                leadingIcon = { Icon(Icons.Default.Usb, contentDescription = "USB", modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = selectedTabMode == 1,
                onClick = { selectedTabMode = 1 },
                label = { Text("Wi-Fi Directo") },
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = "Wifi", modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = selectedTabMode == 2,
                onClick = { selectedTabMode = 2 },
                label = { Text("Smart TV") },
                leadingIcon = { Icon(Icons.Default.Tv, contentDescription = "TV", modifier = Modifier.size(16.dp)) }
            )
        }

        when (selectedTabMode) {
            0 -> UsbTetherCard()
            1 -> WifiDirectProxyCard()
            2 -> SmartTvProxyCard(proxyPort)
        }
    }
}

@Composable
fun UsbTetherCard() {
    val context = LocalContext.current
    val adbCommand = "adb forward tcp:1080 tcp:1080"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Usb, contentDescription = "USB", tint = Color(0xFF00F0FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "USB Debugging Compartir internet (PdaNet Style)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(
                text = "Share VPN internet over USB cable directly to your PC or secondary device using ADB port forwarding without needing root access.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Text(text = "Run this command on your computer (requires ADB):", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050B14), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = adbCommand,
                    color = Color(0xFF00F0FF),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("ADB Command", adbCommand)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "ADB command copied!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF0A0F1D))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Copy ADB Command", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WifiDirectProxyCard() {
    val context = LocalContext.current
    val proxyString = "192.168.43.1:1080"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Wifi, contentDescription = "Wifi", tint = Color(0xFF00F0FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Wi-Fi Directo & Hotspot Proxy", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(
                text = "Enable your Android Wi-Fi Hotspot or Wi-Fi Directo. Connected devices can route their traffic through this proxy IP and port.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050B14), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Proxy Gateway / IP", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(text = proxyString, fontSize = 16.sp, color = Color(0xFF00F0FF), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Proxy", proxyString))
                    Toast.makeText(context, "Proxy IP copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF00F0FF))
                }
            }
        }
    }
}

@Composable
fun SmartTvProxyCard(port: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Tv, contentDescription = "TV", tint = Color(0xFF00F0FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Smart TV Internet Sharing (Android TV / FireTV)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(
                text = "Configure your Smart TV Wi-Fi settings to use manual proxy:\n\n" +
                       "1. Connect your Smart TV to the same Wi-Fi Hotspot or Wi-Fi Directo as this phone.\n" +
                       "2. In TV Wi-Fi settings, set Proxy to Manual.\n" +
                       "3. Enter Proxy Host: `192.168.43.1` and Proxy Puerto: `$port`.\n" +
                       "4. Enjoy secure VPN streaming on your Smart TV!",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
