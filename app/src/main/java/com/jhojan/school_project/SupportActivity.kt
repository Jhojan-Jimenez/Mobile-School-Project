package com.jhojan.school_project

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivitySupportBinding

class SupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySupportBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupListeners()
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Pre-llenar el correo del usuario si está disponible
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val correo = document.getString("correo") ?: currentUser.email ?: ""
                        val cedula = document.getString("cedula") ?: ""

                        binding.etEmail.setText(correo)
                        binding.etIdentification.setText(cedula)
                    }
                }
        }
    }

    private fun setupListeners() {
        binding.btnConfirm.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val identification = binding.etIdentification.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (validateInputs(email, identification, description)) {
                createSupportTicket(email, identification, description)
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createSupportTicket(email: String, identification: String, description: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val reportData = hashMapOf(
            "correo" to email,
            "identificacion" to identification,
            "descripcion" to description,
            "usuario_id" to currentUser.uid,
            "fecha_creacion" to Timestamp.now(),
            "estado" to "Pendiente",
            "motivo" to "Inicio de sesión"
        )

        db.collection("reportes")
            .add(reportData)
            .addOnSuccessListener { documentReference ->
                Log.d("SupportActivity", "Reporte creado con ID: ${documentReference.id}")
                Toast.makeText(
                    this,
                    "Ticket de soporte registrado exitosamente. Nuestro equipo se pondrá en contacto pronto.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("SupportActivity", "Error al crear reporte", e)
                Toast.makeText(
                    this,
                    "Error al registrar el ticket: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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