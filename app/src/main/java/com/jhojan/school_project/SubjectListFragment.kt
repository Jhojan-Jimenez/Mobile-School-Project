package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        adapter = SubjectAdapter(subjects) { subject ->
            Toast.makeText(requireContext(), "Editar: ${subject.nombre}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewSubjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SubjectListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddSubject.setOnClickListener {
            Toast.makeText(requireContext(), "Agregar asignatura - Próximamente", Toast.LENGTH_SHORT).show()
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
                        area = document.getString("area") ?: ""
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
