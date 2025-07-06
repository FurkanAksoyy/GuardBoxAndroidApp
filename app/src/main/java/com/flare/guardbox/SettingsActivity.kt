package com.flare.guardbox

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.flare.guardbox.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvUserEmail: TextView
    private val firebaseHelper = FirebaseHelper.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        // Get user email
        tvUserEmail = findViewById(R.id.tvUserEmail)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            tvUserEmail.text = currentUser.email
        } else {
            tvUserEmail.text = "Not logged in"
        }

        // Set up settings items
        setupSettingsItems()
    }

    private fun setupSettingsItems() {
        // Account settings
        findViewById<View>(R.id.layoutAccount).setOnClickListener {
            // Open account settings or profile screen
            Toast.makeText(this, "Account settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // Notification settings
        findViewById<View>(R.id.layoutNotifications).setOnClickListener {
            // Open notification settings
            Toast.makeText(this, "Notification settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // About
        findViewById<View>(R.id.layoutAbout).setOnClickListener {
            showAboutDialog()
        }

        // Contact
        findViewById<View>(R.id.layoutContact).setOnClickListener {
            showContactDialog()
        }

        // Logout
        findViewById<View>(R.id.layoutLogout).setOnClickListener {
            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun showAboutDialog() {
        val aboutMessage = """
            GuardBox v1.0.0
            
            A smart package security solution for safe and verified deliveries.
            
            © 2025 GuardBox Team
            All rights reserved.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("About GuardBox")
            .setMessage(aboutMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showContactDialog() {
        val contactMessage = """
            For support and inquiries:
            
            Email: support@guardbox.com
            Phone: +1 (555) 123-4567
            
            Working hours: 
            Monday - Friday: 9:00 AM - 5:00 PM
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Contact Us")
            .setMessage(contactMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}