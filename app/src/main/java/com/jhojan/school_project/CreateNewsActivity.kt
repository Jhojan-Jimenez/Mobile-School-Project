package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityCreateNewsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateNewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateNewsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCreateNews.setOnClickListener {
            createNews()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createNews() {
        val titulo = binding.etTitulo.text.toString().trim()
        val contenido = binding.etContenido.text.toString().trim()

        if (!validateFields(titulo, contenido)) {
            return
        }

        showLoading(true)

        // Obtener fecha actual
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = dateFormat.format(Date())

        // Obtener autor (usuario actual)
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: ""

        // Primero obtener el nombre del usuario desde Firestore
        if (userId.isNotEmpty()) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val autor = document.getString("nombre_completo") ?: "Anónimo"
                    saveNews(titulo, contenido, fecha, autor)
                }
                .addOnFailureListener {
                    // Si falla obtener el usuario, guardar con autor anónimo
                    saveNews(titulo, contenido, fecha, "Anónimo")
                }
        } else {
            // Si no hay usuario autenticado, guardar con autor anónimo
            saveNews(titulo, contenido, fecha, "Anónimo")
        }
    }

    private fun saveNews(titulo: String, contenido: String, fecha: String, autor: String) {
        val newsData = hashMapOf(
            "titulo" to titulo,
            "contenido" to contenido,
            "fecha" to fecha,
            "autor" to autor
        )

        db.collection("news")
            .add(newsData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Noticia creada exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al crear noticia: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateFields(titulo: String, contenido: String): Boolean {
        var isValid = true

        if (titulo.isEmpty()) {
            binding.tilTitulo.error = "El título es requerido"
            isValid = false
        } else {
            binding.tilTitulo.error = null
        }

        if (contenido.isEmpty()) {
            binding.tilContenido.error = "El contenido es requerido"
            isValid = false
        } else {
            binding.tilContenido.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCreateNews.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
