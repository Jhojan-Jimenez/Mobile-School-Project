package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityClasesProfesorBinding
import java.text.SimpleDateFormat
import java.util.*

class ClasesProfesorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClasesProfesorBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val clases = mutableListOf<Clase>()
    private lateinit var adapter: ClaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClasesProfesorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupDate()
        loadClases()
    }

    private fun setupRecyclerView() {
        adapter = ClaseAdapter(clases) { clase ->
            // Abrir actividad de asistencia
            val intent = Intent(this, AsistenciaActivity::class.java)
            intent.putExtra("claseId", clase.id)
            intent.putExtra("claseName", clase.nombre)
            startActivity(intent)
        }
        binding.rvClases.layoutManager = LinearLayoutManager(this)
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
        val today = sdf.format(Date()).replaceFirstChar { it.uppercase() } // "Lunes", "Martes", etc.

        db.collection("clases")
            .whereEqualTo("teacherId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                clases.clear()
                for (document in documents) {
                    val horario = document.get("horario") as? List<String> ?: emptyList()

                    // Filtrar solo las clases de hoy
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

                // Mostrar mensaje si no hay clases
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
}
