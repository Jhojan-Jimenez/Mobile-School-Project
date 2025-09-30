package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Inicializar Firebase explícitamente
            FirebaseApp.initializeApp(this)
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            setupListeners()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error en onCreate", e)
            Toast.makeText(this, "Error al iniciar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        binding.tvSupport.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "El correo es requerido"
            return false
        }
        binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "La contraseña es requerida"
            return false
        }
        binding.tilPassword.error = null

        return true
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso, obtener datos del usuario
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        loadUserDataAndRedirect(currentUser.uid)
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showLoading(false)
                    // Error en login
                    Toast.makeText(
                        this,
                        "Correo o contraseña inválidos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun loadUserDataAndRedirect(uid: String) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)

                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        // Redirigir según rol
                        val intent = when {
                            user.rol.equals("Administrativo", ignoreCase = true) ->
                                Intent(this, AdminPanelActivity::class.java)
                            user.rol.equals("Estudiante", ignoreCase = true) ->
                                Intent(this, StudentPanelActivity::class.java)
                            user.rol.equals("Acudiente", ignoreCase = true) ->
                                Intent(this, GuardianPanelActivity::class.java)
                            user.rol.equals("Profesor", ignoreCase = true) ->
                                Intent(this, TeacherPanelActivity::class.java)
                            else -> null
                        }

                        if (intent != null) {
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "No se pudieron cargar los datos del usuario", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Usuario no encontrado en la base de datos", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("LoginActivity", "Error al cargar usuario", e)
                Toast.makeText(this, "Error al cargar los datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
    }
}