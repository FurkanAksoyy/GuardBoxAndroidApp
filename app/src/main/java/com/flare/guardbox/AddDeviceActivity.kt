package com.flare.guardbox

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flare.guardbox.model.User
import com.flare.guardbox.utils.FirebaseHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var etDeviceId: EditText
    private lateinit var btnAddDevice: Button
    private lateinit var btnCancel: Button
    private val firebaseHelper = FirebaseHelper.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        // View'ları başlat
        etDeviceId = findViewById(R.id.etDeviceId)
        btnAddDevice = findViewById(R.id.btnAddDevice)
        btnCancel = findViewById(R.id.btnCancel)

        // Buton tıklama olaylarını ayarla
        btnAddDevice.setOnClickListener {
            addDevice()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun addDevice() {
        val deviceId = etDeviceId.text.toString().trim()

        if (deviceId.isEmpty()) {
            etDeviceId.error = "Cihaz ID'si gerekli"
            return
        }

        // Kullanıcı hesabına cihazı ekle
        val currentUser = firebaseHelper.getCurrentUser()
        if (currentUser != null) {
            firebaseHelper.getUserProfile(currentUser.uid, object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        // Cihazı ekle
                        user.addDeviceId(deviceId)
                        firebaseHelper.createUserProfile(user)

                        Toast.makeText(this@AddDeviceActivity,
                            "Cihaz başarıyla eklendi", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@AddDeviceActivity,
                        "Cihaz eklenemedi: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}