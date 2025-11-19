package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MateriasAcudienteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Header y Footer
    private lateinit var parentHeader: ParentHeader
    private lateinit var bottomNav: ParentBottomNavigationView

    // Views
    private lateinit var layoutMaterias: LinearLayout
    private lateinit var scrollMaterias: ScrollView
    private lateinit var tvNoMaterias: TextView
    private lateinit var tvPromedioGeneral: TextView
    private lateinit var progressPromedioGeneral: ProgressBar

    // Datos
    private var estudianteId: String = ""
    private val materiasList = mutableListOf<MateriaData>()

    data class MateriaData(
        val id: String,
        val nombre: String,
        val profesor: String?,
        val color: String?,
        val totalTareas: Int,
        val tareasCompletadas: Int,
        val promedio: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materias_acudiente)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupHeaderFooter()

        // Recuperar estudiante
        estudianteId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("estudiante_id", "") ?: ""

        val acudienteId = auth.currentUser?.uid ?: ""

        if (estudianteId.isEmpty()) {
            db.collection("users")
                .document(acudienteId)
                .get()
                .addOnSuccessListener { acudienteDoc ->
                    if (acudienteDoc.exists()) {
                        estudianteId = acudienteDoc.getString("estudiante_id") ?: ""

                        if (estudianteId.isEmpty()) {
                            Toast.makeText(this, "No se encontró estudiante asignado", Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }

                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit().putString("estudiante_id", estudianteId).apply()

                        parentHeader.loadParentData(acudienteId)
                        cargarMaterias()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error obteniendo estudiante", Toast.LENGTH_LONG).show()
                }
        } else {
            parentHeader.loadParentData(acudienteId)
            cargarMaterias()
        }
    }

    private fun initViews() {
        parentHeader = findViewById(R.id.parentHeader)
        bottomNav = findViewById(R.id.bottomNav)
        layoutMaterias = findViewById(R.id.layoutMaterias)
        scrollMaterias = findViewById(R.id.scrollMaterias)
        tvNoMaterias = findViewById(R.id.tvNoMaterias)
        tvPromedioGeneral = findViewById(R.id.tvPromedioGeneral)
        progressPromedioGeneral = findViewById(R.id.progressPromedioGeneral)
    }

    private fun setupHeaderFooter() {
        if (estudianteId.isNotEmpty()) {
            val acudienteId = auth.currentUser?.uid ?: ""
            parentHeader.loadParentData(acudienteId)
        }

        parentHeader.setOnBackClickListener { finish() }
        bottomNav.setActiveItem(ParentBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun cargarMaterias() {
        materiasList.clear()

        // Primero obtenemos todas las tareas del estudiante
        db.collection("tareas")
            .whereEqualTo("estudiante", estudianteId)
            .get()
            .addOnSuccessListener { tareasSnapshot ->

                // Agrupar tareas por materia
                val tareasPorMateria = mutableMapOf<String, MutableList<TareaInfo>>()

                for (document in tareasSnapshot) {
                    val materiaId = document.getString("materia") ?: continue
                    val completada = document.getBoolean("completada") ?: false
                    val nota = document.getDouble("nota") ?: 0.0

                    if (!tareasPorMateria.containsKey(materiaId)) {
                        tareasPorMateria[materiaId] = mutableListOf()
                    }

                    tareasPorMateria[materiaId]?.add(
                        TareaInfo(
                            completada = completada,
                            nota = nota
                        )
                    )
                }

                // Ahora cargar la información de cada materia
                if (tareasPorMateria.isEmpty()) {
                    mostrarSinMaterias()
                    return@addOnSuccessListener
                }

                val materiasIds = tareasPorMateria.keys.toList()
                cargarInfoMaterias(materiasIds, tareasPorMateria)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar tareas: ${e.message}", Toast.LENGTH_SHORT).show()
                mostrarSinMaterias()
            }
    }

    private fun cargarInfoMaterias(
        materiasIds: List<String>,
        tareasPorMateria: Map<String, List<TareaInfo>>
    ) {
        db.collection("subjects")
            .get()
            .addOnSuccessListener { subjectsSnapshot ->

                for (document in subjectsSnapshot) {
                    val materiaId = document.id

                    if (!materiasIds.contains(materiaId)) continue

                    val nombre = document.getString("nombre") ?: "Materia"
                    val profesor = document.getString("profesor")
                    val color = document.getString("color")

                    val tareas = tareasPorMateria[materiaId] ?: emptyList()
                    val totalTareas = tareas.size
                    val tareasCompletadas = tareas.count { it.completada }

                    // Calcular promedio solo con tareas completadas que tengan nota
                    val tareasConNota = tareas.filter { it.completada && it.nota > 0 }
                    val promedio = if (tareasConNota.isNotEmpty()) {
                        tareasConNota.map { it.nota }.average()
                    } else {
                        0.0
                    }

                    materiasList.add(
                        MateriaData(
                            id = materiaId,
                            nombre = nombre,
                            profesor = profesor,
                            color = color,
                            totalTareas = totalTareas,
                            tareasCompletadas = tareasCompletadas,
                            promedio = promedio
                        )
                    )
                }

                // Ordenar por nombre
                materiasList.sortBy { it.nombre }

                mostrarMaterias()
                calcularPromedioGeneral()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar materias: ${e.message}", Toast.LENGTH_SHORT).show()
                mostrarSinMaterias()
            }
    }

    private fun mostrarMaterias() {
        if (materiasList.isEmpty()) {
            mostrarSinMaterias()
            return
        }

        tvNoMaterias.visibility = View.GONE
        scrollMaterias.visibility = View.VISIBLE

        layoutMaterias.removeAllViews()

        for (materia in materiasList) {
            val itemView = crearVistaMateria(materia)
            layoutMaterias.addView(itemView)
        }
    }

    private fun mostrarSinMaterias() {
        tvNoMaterias.visibility = View.VISIBLE
        scrollMaterias.visibility = View.GONE
        tvPromedioGeneral.text = "0.0"
        progressPromedioGeneral.progress = 0
    }

    private fun crearVistaMateria(materia: MateriaData): View {
        val itemView = LayoutInflater.from(this).inflate(
            R.layout.item_materia_acudiente,
            layoutMaterias,
            false
        )

        val cardMateria = itemView.findViewById<CardView>(R.id.cardMateria)
        val viewColorMateria = itemView.findViewById<View>(R.id.viewColorMateria)
        val tvNombreMateria = itemView.findViewById<TextView>(R.id.tvNombreMateria)
        val tvProfesorMateria = itemView.findViewById<TextView>(R.id.tvProfesorMateria)
        val tvEstadisticas = itemView.findViewById<TextView>(R.id.tvEstadisticas)
        val tvPromedio = itemView.findViewById<TextView>(R.id.tvPromedio)
        val progressPromedio = itemView.findViewById<ProgressBar>(R.id.progressPromedio)

        // Color de la materia
        if (!materia.color.isNullOrEmpty()) {
            try {
                viewColorMateria.setBackgroundColor(android.graphics.Color.parseColor(materia.color))
            } catch (e: Exception) {
                viewColorMateria.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_primary))
            }
        }

        // Nombre
        tvNombreMateria.text = materia.nombre

        // Profesor
        if (!materia.profesor.isNullOrEmpty()) {
            cargarNombreProfesor(materia.profesor, tvProfesorMateria)
        } else {
            tvProfesorMateria.text = "Sin profesor asignado"
        }

        // Estadísticas
        tvEstadisticas.text = "${materia.tareasCompletadas}/${materia.totalTareas} tareas completadas"

        // Promedio
        if (materia.promedio > 0) {
            tvPromedio.text = String.format("%.1f", materia.promedio)
            progressPromedio.progress = ((materia.promedio / 5.0) * 100).toInt()

            val colorPromedio = when {
                materia.promedio >= 4.0 -> R.color.green_success
                materia.promedio >= 3.0 -> R.color.orange_accent
                else -> R.color.red_error
            }
            tvPromedio.setTextColor(ContextCompat.getColor(this, colorPromedio))
        } else {
            tvPromedio.text = "S/N"
            tvPromedio.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            progressPromedio.progress = 0
        }

        // Click para ver detalle
        cardMateria.setOnClickListener {
            mostrarDetalleMateria(materia)
        }

        return itemView
    }

    private fun calcularPromedioGeneral() {
        val materiasConPromedio = materiasList.filter { it.promedio > 0 }

        if (materiasConPromedio.isEmpty()) {
            tvPromedioGeneral.text = "0.0"
            progressPromedioGeneral.progress = 0
            return
        }

        val promedioGeneral = materiasConPromedio.map { it.promedio }.average()
        tvPromedioGeneral.text = String.format("%.1f", promedioGeneral)
        progressPromedioGeneral.progress = ((promedioGeneral / 5.0) * 100).toInt()

        val colorPromedio = when {
            promedioGeneral >= 4.0 -> R.color.green_success
            promedioGeneral >= 3.0 -> R.color.orange_accent
            else -> R.color.red_error
        }
        tvPromedioGeneral.setTextColor(ContextCompat.getColor(this, colorPromedio))
    }

    private fun cargarNombreProfesor(profesorId: String, textView: TextView) {
        db.collection("users")
            .document(profesorId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombreCompleto = document.getString("nombre_completo")
                        ?: document.getString("nombre")
                        ?: "Profesor"

                    textView.text = "Prof. $nombreCompleto"
                } else {
                    textView.text = "Profesor no encontrado"
                }
            }
            .addOnFailureListener {
                textView.text = "Error al cargar profesor"
            }
    }

    private fun mostrarDetalleMateria(materia: MateriaData) {
        val intent = Intent(this, DetalleMateriaAcudienteActivity::class.java)
        intent.putExtra("materia_id", materia.id)
        intent.putExtra("materia_nombre", materia.nombre)
        intent.putExtra("materia_color", materia.color)
        intent.putExtra("materia_profesor", materia.profesor)
        intent.putExtra("estudiante_id", estudianteId)
        startActivity(intent)
    }

    data class TareaInfo(
        val completada: Boolean,
        val nota: Double
    )
}