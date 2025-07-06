package com.flare.guardbox.model

data class User(
    var userId: String = "",
    var email: String = "",
    var name: String = "",
    var deviceIds: MutableList<String> = mutableListOf()
) {
    fun addDeviceId(deviceId: String) {
        deviceIds.add(deviceId)
    }
}