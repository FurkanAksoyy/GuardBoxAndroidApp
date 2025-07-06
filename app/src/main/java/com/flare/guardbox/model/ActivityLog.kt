package com.flare.guardbox.model

data class ActivityLog(
    var activityType: String = "", // PACKAGE_DELIVERED, BOX_OPENED, BOX_LOCKED, TAMPERING_DETECTED
    var timestamp: Long = 0,
    var message: String = "",
    var deviceId: String = ""
)