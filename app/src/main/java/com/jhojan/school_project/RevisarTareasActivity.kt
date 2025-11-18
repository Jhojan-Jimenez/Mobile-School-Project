package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.TeacherHeader
import com.jhojan.school_project.TeacherBottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class RevisarTareasActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var teacherHeader: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView
    private lateinit var recyclerTareas: RecyclerView
    private lateinit var tareasAdapter: TareasAdapter

    private var profesorId: String = ""
    private val listaTareas = mutableListOf<Tarea>()

    data class Tarea(
        val id: String,
        val titulo: String,
        val descripcion: String,
        val fechaEntrega: Timestamp,
        val materia: String,
        val estudiante: String,
        val nota: Long,
        val completada: Boolean,
        val link: String?,
        val profesor: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_revisar_tareas)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener ID del profesor
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        profesorId = prefs.getString("profesor_id", "") ?: ""

        if (profesorId.isEmpty()) {
            profesorId = "ZW6kOK1PaGVcteR4N9mzSQlcxjd2" // ID por defecto
        }

        // Inicializar vistas
        initViews()

        // Configurar Header y Footer
        setupHeaderFooter()

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar tareas
        cargarTareas()
    }

    private fun initViews() {
        teacherHeader = findViewById(R.id.teacherHeader)
        bottomNav = findViewById(R.id.bottomNav)
        recyclerTareas = findViewById(R.id.recyclerTareas)
    }

    private fun setupHeaderFooter() {
        teacherHeader.loadTeacherData(profesorId)
        teacherHeader.setOnBackClickListener {
            finish()
        }

        bottomNav.setActiveItem(TeacherBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun setupRecyclerView() {
        tareasAdapter = TareasAdapter(listaTareas) { tarea ->
            // Click en una tarea - abrir detalle para calificar
            val intent = Intent(this, CalificarTareaActivity::class.java)
            intent.putExtra("tarea_id", tarea.id)
            intent.putExtra("titulo", tarea.titulo)
            intent.putExtra("descripcion", tarea.descripcion)
            intent.putExtra("fecha_entrega", tarea.fechaEntrega.toDate().time)
            intent.putExtra("materia", tarea.materia)
            intent.putExtra("estudiante", tarea.estudiante)
            intent.putExtra("nota", tarea.nota)
            intent.putExtra("completada", tarea.completada)
            intent.putExtra("link", tarea.link)
            startActivity(intent)
        }

        recyclerTareas.apply {
            layoutManager = LinearLayoutManager(this@RevisarTareasActivity)
            adapter = tareasAdapter
        }
    }

    private fun cargarTareas() {
        // Cargar todas las tareas del profesor
        db.collection("tareas")
            .whereEqualTo("profesor", profesorId)
            .get()
            .addOnSuccessListener { documents ->
                listaTareas.clear()
                for (document in documents) {
                    val tarea = Tarea(
                        id = document.id,
                        titulo = document.getString("titulo") ?: "",
                        descripcion = document.getString("descripcion") ?: "",
                        fechaEntrega = document.getTimestamp("fecha_entrega") ?: Timestamp.now(),
                        materia = document.getString("materia") ?: "",
                        estudiante = document.getString("estudiante") ?: "",
                        nota = document.getLong("nota") ?: 0,
                        completada = document.getBoolean("completada") ?: false,
                        link = document.getString("link"),
                        profesor = document.getString("profesor") ?: ""
                    )
                    listaTareas.add(tarea)
                }

                // Ordenar por fecha de entrega (más recientes primero)
                listaTareas.sortByDescending { it.fechaEntrega.toDate() }

                tareasAdapter.notifyDataSetChanged()

                if (listaTareas.isEmpty()) {
                    Toast.makeText(this, "No hay tareas para revisar", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar tareas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        // Recargar tareas al volver de calificar
        cargarTareas()
    }
}

// Adapter para el RecyclerView
class TareasAdapter(
    private val tareas: List<RevisarTareasActivity.Tarea>,
    private val onTareaClick: (RevisarTareasActivity.Tarea) -> Unit
) : RecyclerView.Adapter<TareasAdapter.TareaViewHolder>() {

    class TareaViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: android.widget.TextView = view.findViewById(R.id.tvTitulo)
        val tvDescripcion: android.widget.TextView = view.findViewById(R.id.tvDescripcion)
        val tvFechaEntrega: android.widget.TextView = view.findViewById(R.id.tvFechaEntrega)
        val tvEstado: android.widget.TextView = view.findViewById(R.id.tvEstado)
        val tvNota: android.widget.TextView = view.findViewById(R.id.tvNota)
        val cardTarea: androidx.cardview.widget.CardView = view.findViewById(R.id.cardTarea)

        val tvEstudiante: android.widget.TextView = view.findViewById(R.id.tvEstudiante)

    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TareaViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea_revisar, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]

        holder.tvTitulo.text = tarea.titulo
        holder.tvEstudiante.text = "Estudiante: ${tarea.estudiante}"
        holder.tvDescripcion.text = tarea.descripcion

        // Formatear fecha
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvFechaEntrega.text = "Entrega: ${sdf.format(tarea.fechaEntrega.toDate())}"

        // Estado
        if (tarea.completada) {
            holder.tvEstado.text = "✓ Completada"
            holder.tvEstado.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            holder.tvEstado.text = "○ Pendiente"
            holder.tvEstado.setTextColor(android.graphics.Color.parseColor("#FF9800"))
        }

        // Nota
        if (tarea.nota > 0) {
            holder.tvNota.text = "Nota: ${tarea.nota}/100"
            holder.tvNota.visibility = android.view.View.VISIBLE
        } else {
            holder.tvNota.text = "Sin calificar"
            holder.tvNota.visibility = android.view.View.VISIBLE
        }

        // Click listener
        holder.cardTarea.setOnClickListener {
            onTareaClick(tarea)
        }
    }

    override fun getItemCount() = tareas.size
}