package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityVerifyCodeBinding

class VerifyCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyCodeBinding
    private lateinit var db: FirebaseFirestore
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.btnValidateCode.setOnClickListener {
            val code = binding.etCode.text.toString().trim()

            if (validateCode(code)) {
                verifyCode(code)
            }
        }
    }

    private fun validateCode(code: String): Boolean {
        if (code.isEmpty()) {
            binding.tilCode.error = "El código es requerido"
            return false
        }
        binding.tilCode.error = null

        if (code.length != 6) {
            binding.tilCode.error = "El código debe tener 6 dígitos"
            return false
        }
        binding.tilCode.error = null

        return true
    }

    private fun verifyCode(code: String) {
        showLoading(true)

        db.collection("verification_codes")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)

                if (document.exists()) {
                    val savedCode = document.getString("code")
                    val expiresAt = document.getLong("expiresAt") ?: 0
                    val currentTime = System.currentTimeMillis()

                    if (savedCode == code) {
                        if (currentTime <= expiresAt) {
                            // Código válido
                            Toast.makeText(this, "Código válido", Toast.LENGTH_SHORT).show()

                            // Navegar a nueva contraseña
                            val intent = Intent(this, NewPasswordActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                            finish()
                        } else {
                            // Código expirado
                            Toast.makeText(this, "Código inválido o expirado", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Código incorrecto
                        Toast.makeText(this, "Código inválido o expirado", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Código inválido o expirado", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("VerifyCode", "Error al verificar código", e)
                Toast.makeText(this, "Error al verificar código", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnValidateCode.isEnabled = !show
        binding.etCode.isEnabled = !show
    }
}