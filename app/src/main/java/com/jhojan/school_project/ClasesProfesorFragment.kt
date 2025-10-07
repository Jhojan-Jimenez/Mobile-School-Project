package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentClasesProfesorBinding
import java.text.SimpleDateFormat
import java.util.*

class ClasesProfesorFragment : Fragment() {

    private var _binding: FragmentClasesProfesorBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val clases = mutableListOf<Clase>()
    private lateinit var adapter: ClaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClasesProfesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDate()
        loadClases()
    }

    private fun setupRecyclerView() {
        adapter = ClaseAdapter(clases) { clase ->
            // Navegar al fragmento de asistencia
            val bundle = bundleOf(
                "claseId" to clase.id,
                "claseName" to clase.nombre
            )
            findNavController().navigate(R.id.action_clasesProfesor_to_asistencia, bundle)
        }
        binding.rvClases.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClases.adapter = adapter
    }

    private fun setupDate() {
        val sdf = SimpleDateFormat("EEEE, dd 'de' MMMM yyyy", Locale("es", "ES"))
        binding.tvDate.text = sdf.format(Date())
    }

    private fun loadClases() {
        val currentUser = auth.currentUser ?: return
        showLoading(true)

        // Obtener el día de hoy en español
        val sdf = SimpleDateFormat("EEEE", Locale("es", "ES"))
        val today = sdf.format(Date()).replaceFirstChar { it.uppercase() }

        db.collection("clases")
            .whereEqualTo("teacherId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                clases.clear()
                for (document in documents) {
                    val horario = document.get("horario") as? List<String> ?: emptyList()

                    if (horario.contains(today)) {
                        val clase = Clase(
                            id = document.id,
                            nombre = document.getString("nombre") ?: "",
                            asignatura = document.getString("asignatura") ?: "",
                            grado = document.getString("grado") ?: "",
                            grupo = document.getString("grupo") ?: "",
                            teacherId = document.getString("teacherId") ?: "",
                            teacherName = document.getString("teacherName") ?: "",
                            horario = horario,
                            año = document.getString("año") ?: ""
                        )
                        clases.add(clase)
                    }
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (clases.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                showLoading(false)
                binding.tvEmptyState.visibility = View.VISIBLE
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
