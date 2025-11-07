package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentNewsListBinding

class NewsListFragment : Fragment() {

    private var _binding: FragmentNewsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: NewsAdapter
    private val newsList = mutableListOf<News>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupListeners()
        loadNews()
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(newsList) { news ->
            Toast.makeText(requireContext(), "Editar: ${news.titulo}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NewsListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddNews.setOnClickListener {
            Toast.makeText(requireContext(), "Agregar noticia - Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNews() {
        showLoading(true)

        db.collection("news")
            .get()
            .addOnSuccessListener { documents ->
                newsList.clear()
                for (document in documents) {
                    val news = News(
                        id = document.id,
                        titulo = document.getString("titulo") ?: "",
                        contenido = document.getString("contenido") ?: "",
                        fecha = document.getString("fecha") ?: "",
                        autor = document.getString("autor") ?: ""
                    )
                    newsList.add(news)
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (newsList.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay noticias registradas", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al cargar noticias: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewNews.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadNews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
