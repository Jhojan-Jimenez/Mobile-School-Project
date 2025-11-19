package com.jhojan.school_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jhojan.school_project.databinding.ActivityNewsDetailBinding

class NewsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadNewsData()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadNewsData() {
        // Obtener datos del intent
        val titulo = intent.getStringExtra("NEWS_TITULO") ?: "Noticia"
        val contenido = intent.getStringExtra("NEWS_CONTENIDO") ?: "Sin contenido"
        val autor = intent.getStringExtra("NEWS_AUTOR") ?: "Anónimo"
        val fecha = intent.getStringExtra("NEWS_FECHA") ?: ""

        // Actualizar UI
        binding.tvNewsTitle.text = titulo
        binding.tvNewsContent.text = contenido
        binding.tvNewsAuthor.text = autor
        binding.tvNewsDate.text = fecha

        // Cargar imagen de stock usando Glide
        // Usando una imagen de noticias/periódico como placeholder
        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuAnP2WWoz9Cc6kaOMAkhsMy0xrMLZ4qrvd790pNEAMpwe0BK9eEVmj_5Gv4xe0PudRpyKDTfRHDUpH5_KJtl7-CrepjoNygXV5giJh3J7PYxXL64Tbu-jrmef7uFUWqw3uF6K8_Vdu3T81SuUpb0xWQ-bFIzb0LGgcHs7kGEm0jcsdi-xzq__8ciXs68RxTCoNtWkKf28fBaSMeSN0uhESIlEiCDyRd4fkrodGP3jBYRbN1OUiaTds-rqxAaCL11ORbU4uIfbMfyY4")
            .placeholder(android.R.color.darker_gray)
            .error(android.R.color.darker_gray)
            .centerCrop()
            .into(binding.imgNewsHeader)
    }
}
