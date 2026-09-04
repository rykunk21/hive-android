package com.example.hive.model

data class TailnetDevice(
    val name: String,
    val tailscaleIP: String,
    val os: String,
    val status: DeviceStatus,
    val lastSeen: String
)

enum class DeviceStatus {
    ONLINE, OFFLINE, UNKNOWN
}

data class TailnetCommand(
    val id: String,
    val targetDevice: String,
    val commandType: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: CommandStatus = CommandStatus.PENDING
)

enum class CommandStatus {
    PENDING, SENT, ACKNOWLEDGED, COMPLETED, FAILED
}
