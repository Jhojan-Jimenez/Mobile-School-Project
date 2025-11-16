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
import com.jhojan.school_project.databinding.FragmentTeacherListBinding

class TeacherListFragment : Fragment() {

    private var _binding: FragmentTeacherListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: TeacherAdapter
    private val teachers = mutableListOf<Teacher>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupListeners()
        loadTeachers()
    }

    private fun setupRecyclerView() {
        adapter = TeacherAdapter(teachers) { teacher ->
            // Handle edit click
            Toast.makeText(requireContext(), "Editar: ${teacher.user.nombre}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewTeachers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TeacherListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddTeacher.setOnClickListener {
            val intent = Intent(requireContext(), CreateUserActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadTeachers() {
        showLoading(true)

        db.collection("users")
            .whereEqualTo("rol", "Profesor")
            .get()
            .addOnSuccessListener { documents ->
                teachers.clear()
                for (document in documents) {
                    val user = User(
                        id = document.getString("id") ?: "",
                        nombre_completo = document.getString("nombre_completo") ?: "",
                        rol = document.getString("rol") ?: "",
                        telefono = document.getString("telefono") ?: "",
                        correo = document.getString("correo") ?: "",
                        activo = document.getBoolean("activo") ?: true,
                        // Legacy fields
                        nombre = document.getString("nombre") ?: "",
                        apellido = document.getString("apellido") ?: "",
                        Direccion = document.getString("Direccion") ?: ""
                    )

                    val teacher = Teacher(
                        especialidad = document.getString("especialidad") ?: "",
                        user = user,
                        // Legacy fields
                        departamento = document.getString("departamento") ?: "",
                        asignatura = document.getString("asignatura") ?: ""
                    )
                    teachers.add(teacher)
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (teachers.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay profesores registrados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al cargar profesores: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewTeachers.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadTeachers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
