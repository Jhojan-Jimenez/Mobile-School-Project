package com.jhojan.school_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jhojan.school_project.databinding.ActivitySupportBinding

class SupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnConfirm.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val identification = binding.etIdentification.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (validateInputs(email, identification, description)) {
                // Solo visual - mostrar mensaje de éxito
                Toast.makeText(
                    this,
                    "Ticket de soporte registrado exitosamente. Nuestro equipo se pondrá en contacto pronto.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(email: String, identification: String, description: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "El correo es requerido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (identification.isEmpty()) {
            binding.tilIdentification.error = "La identificación es requerida"
            isValid = false
        } else {
            binding.tilIdentification.error = null
        }

        if (description.isEmpty()) {
            binding.tilDescription.error = "La descripción es requerida"
            isValid = false
        } else {
            binding.tilDescription.error = null
        }

        if (!isValid) {
            Toast.makeText(this, "Datos incompletos", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }
}