package com.jhojan.school_project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentStudentNewsBinding
import com.jhojan.school_project.databinding.ItemStudentNewsBinding

data class NewsItem(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val autor: String = "",
    val fecha: String = ""
)

class StudentNewsFragment : Fragment() {

    private var _binding: FragmentStudentNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private val newsList = mutableListOf<NewsItem>()
    private lateinit var adapter: StudentNewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        loadNews()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = StudentNewsAdapter(newsList) { news ->
            // Abrir detalle de la noticia
            val intent = android.content.Intent(requireContext(), NewsDetailActivity::class.java)
            intent.putExtra("NEWS_TITULO", news.titulo)
            intent.putExtra("NEWS_CONTENIDO", news.contenido)
            intent.putExtra("NEWS_AUTOR", news.autor)
            intent.putExtra("NEWS_FECHA", news.fecha)
            startActivity(intent)
        }
        binding.recyclerViewNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@StudentNewsFragment.adapter
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
                    requireContext(),
                    "Error al cargar noticias: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("StudentNewsFragment", "Error loading news", e)
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.recyclerViewNews.visibility = View.GONE
            binding.tvNoNews.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class StudentNewsAdapter(
    private val newsList: List<NewsItem>,
    private val onNewsClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<StudentNewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(private val binding: ItemStudentNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(news: NewsItem) {
            binding.tvNewsTitle.text = news.titulo
            binding.tvNewsContent.text = news.contenido
            binding.tvNewsAuthor.text = news.autor
            binding.tvNewsDate.text = news.fecha

            // Click listener para abrir el detalle
            binding.root.setOnClickListener {
                onNewsClick(news)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemStudentNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount(): Int = newsList.size
}
