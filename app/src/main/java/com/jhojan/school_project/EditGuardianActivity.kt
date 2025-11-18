package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityEditGuardianBinding

class EditGuardianActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditGuardianBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGuardianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        userId = intent.getStringExtra("USER_ID") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Error: ID de usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadGuardianData()
        setupListeners()
    }

    private fun loadGuardianData() {
        showLoading(true)

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombreCompleto = document.getString("nombre_completo") ?: ""
                    val correo = document.getString("correo") ?: ""
                    val telefono = document.getString("telefono") ?: ""
                    val direccion = document.getString("direccion") ?: ""
                    val activo = document.getBoolean("activo") ?: true

                    binding.etNombreCompleto.setText(nombreCompleto)
                    binding.etCorreo.setText(correo)
                    binding.etTelefono.setText(telefono)
                    binding.etDireccion.setText(direccion)
                    binding.switchActivo.isChecked = activo
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al cargar datos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
    }

    private fun setupListeners() {
        binding.btnSaveGuardian.setOnClickListener {
            saveGuardian()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveGuardian() {
        val nombreCompleto = binding.etNombreCompleto.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val activo = binding.switchActivo.isChecked

        if (!validateFields(nombreCompleto)) {
            return
        }

        showLoading(true)

        val guardianData = hashMapOf(
            "nombre_completo" to nombreCompleto,
            "telefono" to telefono,
            "direccion" to direccion,
            "activo" to activo
        )

        db.collection("users")
            .document(userId)
            .update(guardianData as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Acudiente actualizado exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al actualizar acudiente: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateFields(nombreCompleto: String): Boolean {
        var isValid = true

        if (nombreCompleto.isEmpty()) {
            binding.tilNombreCompleto.error = "El nombre completo es requerido"
            isValid = false
        } else {
            binding.tilNombreCompleto.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveGuardian.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
