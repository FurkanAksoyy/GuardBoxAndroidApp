package com.flare.guardbox.utils

import com.flare.guardbox.model.ActivityLog
import com.flare.guardbox.model.GuardBoxStatus
import com.flare.guardbox.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class FirebaseHelper private constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userRef: DatabaseReference = database.getReference("users")
    private val boxStatusRef: DatabaseReference = database.getReference("boxStatus")
    private val activityLogRef: DatabaseReference = database.getReference("activityLogs")

    companion object {
        private var instance: FirebaseHelper? = null

        fun getInstance(): FirebaseHelper {
            if (instance == null) {
                instance = FirebaseHelper()
            }
            return instance!!
        }
    }

    // Auth methods
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun registerUser(email: String, password: String, listener: OnCompleteListener<com.google.firebase.auth.AuthResult>) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(listener)
    }

    fun loginUser(email: String, password: String, listener: OnCompleteListener<com.google.firebase.auth.AuthResult>) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(listener)
    }

    fun logoutUser() {
        auth.signOut()
    }

    // User methods
    fun createUserProfile(user: User) {
        userRef.child(user.userId).setValue(user)
    }

    fun getUserProfile(userId: String, listener: ValueEventListener) {
        userRef.child(userId).addListenerForSingleValueEvent(listener)
    }

    // GuardBox status methods
    fun getBoxStatus(deviceId: String, listener: ValueEventListener) {
        boxStatusRef.child(deviceId).addValueEventListener(listener)
    }

    fun setBoxLockStatus(deviceId: String, isLocked: Boolean) {
        boxStatusRef.child(deviceId).child("locked").setValue(isLocked)

        // Aktivite günlüğüne kaydet
        val message = if (isLocked) "Box was locked" else "Box was unlocked"
        val log = ActivityLog(
            activityType = if (isLocked) "BOX_LOCKED" else "BOX_UNLOCKED",
            timestamp = System.currentTimeMillis(),
            message = message,
            deviceId = deviceId
        )
        addActivityLog(log)
    }

    // Activity log methods
    fun getActivityLogs(deviceId: String, listener: ValueEventListener) {
        activityLogRef.child(deviceId)
            .orderByChild("timestamp")
            .limitToLast(20) // Son 20 aktivite
            .addValueEventListener(listener)
    }

    fun addActivityLog(log: ActivityLog) {
        val logId = activityLogRef.child(log.deviceId).push().key
        if (logId != null) {
            activityLogRef.child(log.deviceId).child(logId).setValue(log)
        }
    }
}