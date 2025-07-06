package com.flare.guardbox.model

data class GuardBoxStatus(
    val connectionStatus: String? = "offline",
    val batteryLevel: Int? = null,
    var isLocked: Boolean = false,
    var hasPackage: Boolean = false,
    var lastUpdated: Long = 0,
    var isVibrationDetected: Boolean = false,
    var deviceId: String = ""

)