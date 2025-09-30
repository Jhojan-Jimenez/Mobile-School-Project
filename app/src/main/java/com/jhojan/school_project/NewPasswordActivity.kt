package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityNewPasswordBinding

class NewPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        email = intent.getStringExtra("email") ?: ""

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: correo no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSavePassword.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validatePasswords(newPassword, confirmPassword)) {
                updatePassword(newPassword)
            }
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

    private fun updatePassword(newPassword: String) {
        showLoading(true)

        // Obtener el UID del usuario desde Firestore
        db.collection("users")
            .whereEqualTo("correo", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userId = documents.documents[0].id

                    // Usar sendPasswordResetEmail como método seguro
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            // Eliminar código de verificación usado
                            db.collection("verification_codes")
                                .document(email)
                                .delete()

                            showLoading(false)
                            Toast.makeText(
                                this,
                                "Se ha enviado un correo para restablecer la contraseña",
                                Toast.LENGTH_LONG
                            ).show()

                            // Volver al login
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            Log.e("NewPassword", "Error al enviar correo", e)
                            Toast.makeText(
                                this,
                                "Error al restablecer contraseña",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("NewPassword", "Error al buscar usuario", e)
                Toast.makeText(this, "Error al actualizar contraseña", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSavePassword.isEnabled = !show
        binding.etNewPassword.isEnabled = !show
        binding.etConfirmPassword.isEnabled = !show
    }
}