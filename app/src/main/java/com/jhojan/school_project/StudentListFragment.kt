package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentStudentListBinding

class StudentListFragment : Fragment() {

    private var _binding: FragmentStudentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: StudentAdapter
    private val students = mutableListOf<Student>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupListeners()
        loadStudents()
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(students) { student ->
            // Handle edit click
            Toast.makeText(requireContext(), "Editar: ${student.user.nombre}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewStudents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StudentListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddStudent.setOnClickListener {
            val intent = Intent(requireContext(), CreateUserActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadStudents() {
        showLoading(true)

        db.collection("users")
            .whereEqualTo("rol", "Estudiante")
            .get()
            .addOnSuccessListener { documents ->
                students.clear()
                for (document in documents) {
                    val user = User(
                        id = document.getString("id") ?: "",
                        nombre = document.getString("nombre") ?: "",
                        apellido = document.getString("apellido") ?: "",
                        rol = document.getString("rol") ?: "",
                        telefono = document.getString("telefono") ?: "",
                        Direccion = document.getString("Direccion") ?: "",
                        correo = document.getString("correo") ?: ""
                    )

                    val student = Student(
                        grado = document.getString("grado") ?: "",
                        grupo = document.getString("grupo") ?: "",
                        user = user
                    )
                    students.add(student)
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (students.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay estudiantes registrados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al cargar estudiantes: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewStudents.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadStudents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
