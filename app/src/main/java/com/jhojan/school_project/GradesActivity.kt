package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jhojan.school_project.databinding.ActivityGradesBinding
import com.jhojan.school_project.databinding.ItemGradeBinding

data class Grade(
    val subject: String,
    val value: String
)

class GradesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGradesBinding
    private val grades = mutableListOf<Grade>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadDummyGrades()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadDummyGrades() {
        grades.addAll(
            listOf(
                Grade("Matemáticas", "8.5"),
                Grade("Ciencias", "9.0"),
                Grade("Español", "8.0"),
                Grade("Historia", "8.7"),
                Grade("Inglés", "9.2"),
                Grade("Química", "8.3"),
                Grade("Física", "8.8"),
                Grade("Educación Física", "9.5"),
                Grade("Artes", "9.0"),
                Grade("Biología", "8.6"),
                Grade("Geografía", "8.9"),
                Grade("Música", "9.3")
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = GradeAdapter(grades)
        binding.recyclerViewGrades.apply {
            layoutManager = LinearLayoutManager(this@GradesActivity)
            this.adapter = adapter
        }
    }
}

class GradeAdapter(
    private val grades: List<Grade>
) : RecyclerView.Adapter<GradeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemGradeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(grade: Grade) {
            binding.tvGradeSubject.text = grade.subject
            binding.tvGradeValue.text = grade.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGradeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(grades[position])
    }

    override fun getItemCount(): Int = grades.size
}
