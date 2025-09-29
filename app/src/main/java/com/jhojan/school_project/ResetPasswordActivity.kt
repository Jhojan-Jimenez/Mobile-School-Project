package com.jhojan.school_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jhojan.school_project.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSavePassword.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validatePasswords(newPassword, confirmPassword)) {
                // Solo visual - mostrar mensaje de éxito
                Toast.makeText(
                    this,
                    "Contraseña restablecida exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "La contraseña es requerida"
            return false
        }
        binding.tilNewPassword.error = null

        if (newPassword.length < 6) {
            binding.tilNewPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        binding.tilNewPassword.error = null

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Debe confirmar la contraseña"
            return false
        }
        binding.tilConfirmPassword.error = null

        if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = "Contraseña inválida o no coincide"
            return false
        }
        binding.tilConfirmPassword.error = null

        return true
    }
}