package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityEditNewsBinding

class EditNewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditNewsBinding
    private lateinit var db: FirebaseFirestore
    private var newsId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        newsId = intent.getStringExtra("NEWS_ID") ?: ""

        if (newsId.isEmpty()) {
            Toast.makeText(this, "Error: ID de noticia no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadNewsData()
        setupListeners()
    }

    private fun loadNewsData() {
        showLoading(true)

        db.collection("news")
            .document(newsId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val titulo = document.getString("titulo") ?: ""
                    val contenido = document.getString("contenido") ?: ""

                    binding.etTitulo.setText(titulo)
                    binding.etContenido.setText(contenido)
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
        binding.btnSaveNews.setOnClickListener {
            saveNews()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveNews() {
        val titulo = binding.etTitulo.text.toString().trim()
        val contenido = binding.etContenido.text.toString().trim()

        if (!validateFields(titulo, contenido)) {
            return
        }

        showLoading(true)

        val newsData = hashMapOf(
            "titulo" to titulo,
            "contenido" to contenido
        )

        db.collection("news")
            .document(newsId)
            .update(newsData as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Noticia actualizada exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al actualizar noticia: ${e.message}",
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
        binding.btnSaveNews.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
