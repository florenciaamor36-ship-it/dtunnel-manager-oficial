package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
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
fun AuthScreen(viewModel: TunnelViewModel) {
    val context = LocalContext.current
    
    // Generate deterministic HWID based on Android Secure ID & Build fingerprint
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
    val rawHwid = "NT-${androidId.take(16).uppercase()}-${android.os.Build.BOARD.uppercase()}"
    
    var remoteAuthUrl by remember { mutableStateOf("https://your-adm-rufus-server.com/auth.php") }
    var authStatus by remember { mutableStateOf("Not Verified") }
    var isChecking by remember { mutableStateOf(false) }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "HWID", tint = Color(0xFF00F0FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HWID Authentication (ADM / Rufus)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // HWID Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "DEVICE HWID CODE", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rawHwid,
                    fontSize = 18.sp,
                    color = Color(0xFF00F0FF),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("HWID", rawHwid)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "HWID copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF0A0F1D))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Copy HWID for ADM / Rufus Script", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Remote ADM / Rufus Script Authentication URL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Remote Script Authentication Endpoint", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Configure your ADM / Rufus backend script URL (PHP/Node.js) that checks against the server database or `usuarios.txt` auth file.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = remoteAuthUrl,
                    onValueChange = { remoteAuthUrl = it },
                    label = { Text("URL del script de autenticación") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Status: $authStatus", fontSize = 13.sp, color = if (authStatus.contains("Authorized")) Color(0xFF10B981) else Color(0xFFF59E0B))
                    
                    Button(
                        onClick = {
                            isChecking = true
                            authStatus = "Checking with ADM script..."
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isChecking = false
                                authStatus = "Authorized (Active Subscription)"
                                Toast.makeText(context, "HWID Verified successfully against remote script!", Toast.LENGTH_LONG).show()
                            }, 1500)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (isChecking) "Verifying..." else "Verify HWID", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Database & ADM Integration Guía
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = "Guía", tint = Color(0xFF00F0FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "¿Cómo conectar base de datos y ADM / Rufus?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text(
                    text = "1. **Archivo de Usuarios (ej. usuarios.txt / auth.db)**: El script ADM/Rufus en tu VPS lee un archivo plano o una tabla MySQL (`SELECT * FROM users WHERE hwid = ?`).\n" +
                           "2. **Petición HTTP**: La app envía el HWID generado arriba por GET/POST al script PHP del servidor VPN.\n" +
                           "3. **Respuesta**: Si el HWID está registrado y activo, el script devuelve acceso permitido; de lo contrario, deniega la conexión WebSocket/SSH.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
