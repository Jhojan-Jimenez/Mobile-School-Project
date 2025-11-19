package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityStudentNewsBinding
import com.jhojan.school_project.databinding.ItemStudentNewsBinding

class StudentNewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentNewsBinding
    private lateinit var db: FirebaseFirestore
    private val newsList = mutableListOf<NewsItem>()
    private lateinit var adapter: StudentNewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        loadNews()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = StudentNewsAdapter(newsList) { news ->
            // Abrir detalle de la noticia
            val intent = Intent(this, NewsDetailActivity::class.java)
            intent.putExtra("NEWS_TITULO", news.titulo)
            intent.putExtra("NEWS_CONTENIDO", news.contenido)
            intent.putExtra("NEWS_AUTOR", news.autor)
            intent.putExtra("NEWS_FECHA", news.fecha)
            startActivity(intent)
        }
        binding.recyclerViewNews.apply {
            layoutManager = LinearLayoutManager(this@StudentNewsActivity)
            this.adapter = this@StudentNewsActivity.adapter
        }
    }

    private fun loadNews() {
        showLoading(true)

        db.collection("news")
            .get()
            .addOnSuccessListener { documents ->
                newsList.clear()

                for (document in documents) {
                    val news = NewsItem(
                        id = document.id,
                        titulo = document.getString("titulo") ?: "",
                        contenido = document.getString("contenido") ?: "",
                        autor = document.getString("autor") ?: "Anónimo",
                        fecha = document.getString("fecha") ?: ""
                    )
                    newsList.add(news)
                }

                adapter.notifyDataSetChanged()
                showLoading(false)

                if (newsList.isEmpty()) {
                    binding.tvNoNews.visibility = View.VISIBLE
                    binding.recyclerViewNews.visibility = View.GONE
                } else {
                    binding.tvNoNews.visibility = View.GONE
                    binding.recyclerViewNews.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al cargar noticias: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("StudentNewsActivity", "Error loading news", e)
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.recyclerViewNews.visibility = View.GONE
            binding.tvNoNews.visibility = View.GONE
        }
    }
}
