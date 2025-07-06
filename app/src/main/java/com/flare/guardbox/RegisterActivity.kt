package com.flare.guardbox

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flare.guardbox.model.User
import com.flare.guardbox.utils.FirebaseHelper
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginPrompt: TextView
    private val firebaseHelper = FirebaseHelper.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginPrompt = findViewById(R.id.tvLoginPrompt)

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLoginPrompt.setOnClickListener {
            finish() // Go back to login activity
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            etName.error = "Name is required"
            return
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.error = "Email is required"
            return
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.error = "Password is required"
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return
        }

        btnRegister.isEnabled = false

        firebaseHelper.registerUser(email, password) { task ->
            if (task.isSuccessful) {
                val firebaseUser = firebaseHelper.getCurrentUser()

                // Create user profile
                if (firebaseUser != null) {
                    val user = User(
                        userId = firebaseUser.uid,
                        email = email,
                        name = name
                    )
                    firebaseHelper.createUserProfile(user)
                }

                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                btnRegister.isEnabled = true
                Toast.makeText(this, "Registration failed: ${task.exception?.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}