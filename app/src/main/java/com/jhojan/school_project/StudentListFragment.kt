package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        adapter = StudentAdapter(
            students = students,
            onEditClick = { student ->
                val intent = Intent(requireContext(), EditStudentActivity::class.java)
                intent.putExtra("STUDENT_ID", student.user.id)
                startActivity(intent)
            },
            onDeleteClick = { student ->
                showDeleteConfirmationDialog(student)
            }
        )

        binding.recyclerViewStudents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StudentListFragment.adapter
        }
    }

    private fun showDeleteConfirmationDialog(student: Student) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Está seguro de que desea eliminar al estudiante \"${student.user.nombreCompleto}\"?")
            .setPositiveButton("Sí") { dialog, _ ->
                deleteStudent(student)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteStudent(student: Student) {
        showLoading(true)

        db.collection("users")
            .document(student.user.id)
            .delete()
            .addOnSuccessListener {
                students.remove(student)
                adapter.notifyDataSetChanged()
                showLoading(false)
                Toast.makeText(requireContext(), "Estudiante eliminado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al eliminar estudiante: ${e.message}", Toast.LENGTH_LONG).show()
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
