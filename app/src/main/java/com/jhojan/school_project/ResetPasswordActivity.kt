package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityResetPasswordBinding
import kotlin.random.Random

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSendCode.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (validateEmail(email)) {
                checkUserExistsAndSendCode(email)
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "El correo es requerido"
            return false
        }
        binding.tilEmail.error = null

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Correo inválido"
            return false
        }
        binding.tilEmail.error = null

        return true
    }

    private fun checkUserExistsAndSendCode(email: String) {
        showLoading(true)

        // Buscar en Firestore si el usuario existe
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    showLoading(false)
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_LONG).show()
                } else {
                    // Usuario encontrado, generar código
                    val verificationCode = generateVerificationCode()
                    val userId = documents.documents[0].id

                    // Guardar código en Firestore
                    saveVerificationCode(userId, email, verificationCode)
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("ResetPassword", "Error al buscar usuario", e)
                Toast.makeText(this, "Error al verificar usuario", Toast.LENGTH_LONG).show()
            }
    }

    private fun generateVerificationCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    private fun saveVerificationCode(userId: String, email: String, code: String) {
        val codeData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "code" to code,
            "timestamp" to System.currentTimeMillis(),
            "expiresAt" to System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutos
        )

        db.collection("verification_codes")
            .document(email)
            .set(codeData)
            .addOnSuccessListener {
                showLoading(false)
                // En producción, aquí se enviaría el código por correo
                // Por ahora, mostramos el código en un Toast (solo para desarrollo)
                Toast.makeText(
                    this,
                    "Código enviado: $code",
                    Toast.LENGTH_LONG
                ).show()

                // Navegar a la pantalla de verificación
                val intent = Intent(this, VerifyCodeActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("ResetPassword", "Error al guardar código", e)
                Toast.makeText(this, "Error al enviar código", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSendCode.isEnabled = !show
        binding.etEmail.isEnabled = !show
    }
}