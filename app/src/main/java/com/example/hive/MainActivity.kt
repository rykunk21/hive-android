package com.example.hive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hive.model.*
import com.example.hive.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiveTheme {
                HiveDashboard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveDashboard() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("TAILNET", "COMMANDS", "LOGS")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "◈ HIVE",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "TAILNET",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            letterSpacing = 4.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Computer, contentDescription = null)
                                1 -> Icon(Icons.Default.Send, contentDescription = null)
                                2 -> Icon(Icons.Default.Terminal, contentDescription = null)
                            }
                        },
                        label = { Text(title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = NeonPurple.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TailnetTab()
                1 -> CommandsTab()
                2 -> LogsTab()
            }
        }
    }
}

@Composable
fun TailnetTab() {
    val localDevice = remember { getLocalTailnetInfo() }
    val peers = remember { getSamplePeers() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "LOCAL DEVICE",
                fontSize = 12.sp,
                color = TextSecondary,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            DeviceCard(device = localDevice, isLocal = true)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "CONNECTED PEERS (${peers.count { it.status == DeviceStatus.ONLINE }}/${peers.size})",
                fontSize = 12.sp,
                color = TextSecondary,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(peers) { device ->
            DeviceCard(device = device, isLocal = false)
        }
    }
}

@Composable
fun DeviceCard(device: TailnetDevice, isLocal: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        when (device.status) {
                            DeviceStatus.ONLINE -> StatusOnline
                            DeviceStatus.OFFLINE -> StatusOffline
                            DeviceStatus.UNKNOWN -> TextSecondary
                        }
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = device.tailscaleIP,
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = device.os.uppercase(),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                if (!isLocal) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = device.lastSeen,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CommandsTab() {
    var targetIP by remember { mutableStateOf("") }
    var commandType by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf("") }
    var sentCommands by remember { mutableStateOf(listOf<TailnetCommand>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "SEND COMMAND",
            fontSize = 12.sp,
            color = TextSecondary,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = targetIP,
            onValueChange = { targetIP = it },
            label = { Text("Target IP (e.g. 100.x.x.x)", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = commandType,
            onValueChange = { commandType = it },
            label = { Text("Command Type", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = payload,
            onValueChange = { payload = it },
            label = { Text("Payload / Args", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (targetIP.isNotBlank() && commandType.isNotBlank()) {
                    val cmd = TailnetCommand(
                        id = System.currentTimeMillis().toString(),
                        targetDevice = targetIP,
                        commandType = commandType,
                        payload = payload
                    )
                    sentCommands = listOf(cmd) + sentCommands
                    // TODO: Actually send via socket/HTTP to target
                    targetIP = ""
                    commandType = ""
                    payload = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
            Icon(Icons.Default.Send, contentDescription = null, tint = DarkBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXECUTE", color = DarkBackground, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (sentCommands.isNotEmpty()) {
            Text(
                "HISTORY",
                fontSize = 12.sp,
                color = TextSecondary,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sentCommands) { cmd ->
                    CommandHistoryItem(cmd)
                }
            }
        }
    }
}

@Composable
fun CommandHistoryItem(cmd: TailnetCommand) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    cmd.commandType.uppercase(),
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    cmd.status.name,
                    color = when (cmd.status) {
                        CommandStatus.COMPLETED -> StatusOnline
                        CommandStatus.FAILED -> StatusOffline
                        else -> NeonPink
                    },
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "→ ${cmd.targetDevice}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            if (cmd.payload.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    cmd.payload,
                    color = TextPrimary.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LogsTab() {
    val logs = remember { getSampleLogs() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "EVENT LOG",
            fontSize = 12.sp,
            color = TextSecondary,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(logs) { log ->
                LogLine(log)
            }
        }
    }
}

@Composable
fun LogLine(text: String) {
    val color = when {
        text.contains("ERROR") -> StatusOffline
        text.contains("SUCCESS") -> StatusOnline
        text.contains("CONNECT") -> NeonCyan
        else -> TextSecondary
    }

    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 18.sp
    )
}

// Helpers (replace with real Tailscale API calls later)
fun getLocalTailnetInfo(): TailnetDevice {
    return TailnetDevice(
        name = "hive-android",
        tailscaleIP = "100.64.0.1",
        os = "Android",
        status = DeviceStatus.ONLINE,
        lastSeen = "now"
    )
}

fun getSamplePeers(): List<TailnetDevice> {
    return listOf(
        TailnetDevice("adderstack", "100.64.0.2", "Linux", DeviceStatus.ONLINE, "2 min ago"),
        TailnetDevice("ryans-macbook", "100.64.0.3", "macOS", DeviceStatus.ONLINE, "now"),
        TailnetDevice("home-server", "100.64.0.4", "Linux", DeviceStatus.OFFLINE, "3 hours ago"),
        TailnetDevice("pi-hole", "100.64.0.5", "Linux", DeviceStatus.ONLINE, "now")
    )
}

fun getSampleLogs(): List<String> {
    return listOf(
        "[20:45:12] CONNECT Tailnet mesh established",
        "[20:45:10] SUCCESS Peer handshake: adderstack",
        "[20:45:08] INFO Local IP assigned: 100.64.0.1",
        "[20:44:55] CONNECT Peer discovered: ryans-macbook",
        "[20:44:30] WARN home-server unreachable (timeout)",
        "[20:43:00] SUCCESS DERP relay connected: nyc"
    )
}

@Preview(showBackground = true)
@Composable
fun HiveDashboardPreview() {
    HiveTheme {
        HiveDashboard()
    }
}
