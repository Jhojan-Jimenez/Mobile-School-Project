package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentCoursesListBinding

class CourseListFragment : Fragment() {

    private var _binding: FragmentCoursesListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CourseAdapter
    private val courses = mutableListOf<Course>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupListeners()
        loadCourses()
    }

    private fun setupRecyclerView() {
        adapter = CourseAdapter(courses) { course ->
            Toast.makeText(requireContext(), "Editar: ${course.nombre}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CourseListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddCourse.setOnClickListener {
            Toast.makeText(requireContext(), "Agregar curso - Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCourses() {
        showLoading(true)

        db.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                courses.clear()
                for (document in documents) {
                    val course = Course(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        grado = document.getString("grado") ?: "",
                        grupo = document.getString("grupo") ?: "",
                        descripcion = document.getString("descripcion") ?: ""
                    )
                    courses.add(course)
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (courses.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay cursos registrados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al cargar cursos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewCourses.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadCourses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
