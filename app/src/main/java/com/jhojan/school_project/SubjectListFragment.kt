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
import com.jhojan.school_project.databinding.FragmentSubjectsListBinding

class SubjectListFragment : Fragment() {

    private var _binding: FragmentSubjectsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: SubjectAdapter
    private val subjects = mutableListOf<Subject>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupListeners()
        loadSubjects()
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter(
            subjects = subjects,
            onEditClick = { subject ->
                val intent = Intent(requireContext(), EditSubjectActivity::class.java)
                intent.putExtra("SUBJECT_ID", subject.id)
                startActivity(intent)
            },
            onDeleteClick = { subject ->
                showDeleteConfirmationDialog(subject)
            }
        )

        binding.recyclerViewSubjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SubjectListFragment.adapter
        }
    }

    private fun showDeleteConfirmationDialog(subject: Subject) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Está seguro de que desea eliminar la materia \"${subject.nombre}\"?")
            .setPositiveButton("Sí") { dialog, _ ->
                deleteSubject(subject)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteSubject(subject: Subject) {
        showLoading(true)

        db.collection("subjects")
            .document(subject.id)
            .delete()
            .addOnSuccessListener {
                subjects.remove(subject)
                adapter.notifyDataSetChanged()
                showLoading(false)
                Toast.makeText(requireContext(), "Materia eliminada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al eliminar materia: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupListeners() {
        binding.fabAddSubject.setOnClickListener {
            val intent = Intent(requireContext(), CreateSubjectActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadSubjects() {
        showLoading(true)

        db.collection("subjects")
            .get()
            .addOnSuccessListener { documents ->
                subjects.clear()
                for (document in documents) {
                    val subject = Subject(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        codigo = document.getString("codigo") ?: "",
                        curso_id = document.getString("curso_id") ?: "",
                        curso_nombre = document.getString("curso_nombre") ?: ""
                    )
                    subjects.add(subject)
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (subjects.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay asignaturas registradas", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al cargar asignaturas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewSubjects.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadSubjects()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
