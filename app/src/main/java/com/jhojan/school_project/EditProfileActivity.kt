package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadCurrentData()
        setupListeners()
    }

    private fun loadCurrentData() {
        showLoading(true)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Cargar datos actuales
                        binding.etNombreCompleto.setText(document.getString("nombre_completo") ?: "")
                        binding.etGrado.setText(document.getString("grado") ?: "")
                        binding.etGrupo.setText(document.getString("grupo") ?: "")
                        binding.etTelefono.setText(document.getString("telefono") ?: "")
                        binding.etDireccion.setText(document.getString("Direccion") ?: "")

                        showLoading(false)
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "No se encontró información del usuario", Toast.LENGTH_SHORT).show()
                        finish()
                    }
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
        } else {
            showLoading(false)
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveProfile() {
        val nombreCompleto = binding.etNombreCompleto.text.toString().trim()
        val grado = binding.etGrado.text.toString().trim()
        val grupo = binding.etGrupo.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()

        if (!validateFields(nombreCompleto, grado, grupo)) {
            return
        }

        showLoading(true)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Preparar datos para actualizar
            val updates = hashMapOf<String, Any>(
                "nombre_completo" to nombreCompleto,
                "grado" to grado,
                "grupo" to grupo,
                "telefono" to telefono,
                "Direccion" to direccion
            )

            // Actualizar en Firestore
            db.collection("users")
                .document(currentUser.uid)
                .update(updates)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Perfil actualizado exitosamente",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Error al actualizar perfil: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun validateFields(nombreCompleto: String, grado: String, grupo: String): Boolean {
        var isValid = true

        if (nombreCompleto.isEmpty()) {
            binding.tilNombreCompleto.error = "El nombre completo es requerido"
            isValid = false
        } else {
            binding.tilNombreCompleto.error = null
        }

        if (grado.isEmpty()) {
            binding.tilGrado.error = "El grado es requerido"
            isValid = false
        } else {
            binding.tilGrado.error = null
        }

        if (grupo.isEmpty()) {
            binding.tilGrupo.error = "El grupo es requerido"
            isValid = false
        } else {
            binding.tilGrupo.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
