package com.jhojan.school_project

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TareasAcudienteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Header y Footer
    private lateinit var parentHeader: ParentHeader
    private lateinit var bottomNav: ParentBottomNavigationView

    // Views
    private lateinit var tabLayout: TabLayout
    private lateinit var layoutTareas: LinearLayout
    private lateinit var scrollTareas: ScrollView
    private lateinit var tvNoTareas: TextView

    // Datos
    private var estudianteId: String = ""
    private val todasLasTareas = mutableListOf<Tarea>()
    private var filtroActual: String = "pendientes" // pendientes, completadas, todas

    data class Tarea(
        val id: String,
        val titulo: String,
        val descripcion: String,
        val fechaEntrega: Date,
        val materia: String?,
        val profesor: String?,
        val nota: Double,
        val completada: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tareas_acudiente)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupHeaderFooter()
        setupTabs()

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
                        cargarTareas()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error obteniendo estudiante", Toast.LENGTH_LONG).show()
                }
        } else {
            parentHeader.loadParentData(acudienteId)
            cargarTareas()
        }
    }

    private fun initViews() {
        parentHeader = findViewById(R.id.parentHeader)
        bottomNav = findViewById(R.id.bottomNav)
        tabLayout = findViewById(R.id.tabLayout)
        layoutTareas = findViewById(R.id.layoutTareas)
        scrollTareas = findViewById(R.id.scrollTareas)
        tvNoTareas = findViewById(R.id.tvNoTareas)
    }

    private fun setupHeaderFooter() {
        if (estudianteId.isNotEmpty()) {
            val acudienteId = auth.currentUser?.uid ?: ""
            parentHeader.loadParentData(acudienteId)
        }

        parentHeader.setOnBackClickListener { finish() }
        bottomNav.setActiveItem(ParentBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Pendientes"))
        tabLayout.addTab(tabLayout.newTab().setText("Completadas"))
        tabLayout.addTab(tabLayout.newTab().setText("Todas"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        filtroActual = "pendientes"
                        mostrarTareasFiltradas()
                    }
                    1 -> {
                        filtroActual = "completadas"
                        mostrarTareasFiltradas()
                    }
                    2 -> {
                        filtroActual = "todas"
                        mostrarTareasFiltradas()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun cargarTareas() {
        todasLasTareas.clear()

        db.collection("tareas")
            .whereEqualTo("estudiante", estudianteId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val titulo = document.getString("titulo") ?: "Sin título"
                    val descripcion = document.getString("descripcion") ?: ""
                    val fechaEntrega = document.getTimestamp("fecha_entrega")?.toDate() ?: Date()
                    val materia = document.getString("materia")
                    val profesor = document.getString("profesor")
                    val nota = document.getDouble("nota") ?: 0.0
                    val completada = document.getBoolean("completada") ?: false

                    val tarea = Tarea(
                        id = document.id,
                        titulo = titulo,
                        descripcion = descripcion,
                        fechaEntrega = fechaEntrega,
                        materia = materia,
                        profesor = profesor,
                        nota = nota,
                        completada = completada
                    )

                    todasLasTareas.add(tarea)
                }

                // Ordenar por fecha de entrega
                todasLasTareas.sortBy { it.fechaEntrega }

                mostrarTareasFiltradas()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar tareas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarTareasFiltradas() {
        layoutTareas.removeAllViews()

        val tareasFiltradas = when (filtroActual) {
            "pendientes" -> todasLasTareas.filter { !it.completada }
            "completadas" -> todasLasTareas.filter { it.completada }
            else -> todasLasTareas
        }

        if (tareasFiltradas.isEmpty()) {
            tvNoTareas.visibility = View.VISIBLE
            scrollTareas.visibility = View.GONE

            tvNoTareas.text = when (filtroActual) {
                "pendientes" -> "No hay tareas pendientes"
                "completadas" -> "No hay tareas completadas"
                else -> "No hay tareas registradas"
            }
        } else {
            tvNoTareas.visibility = View.GONE
            scrollTareas.visibility = View.VISIBLE

            // Agrupar por fecha
            val tareasAgrupadas = agruparTareasPorFecha(tareasFiltradas)

            for ((fecha, tareas) in tareasAgrupadas) {
                agregarEncabezadoFecha(fecha)

                for (tarea in tareas) {
                    val itemView = crearVistaTarea(tarea)
                    layoutTareas.addView(itemView)
                }
            }
        }
    }

    private fun agruparTareasPorFecha(tareas: List<Tarea>): Map<String, List<Tarea>> {
        val hoy = Calendar.getInstance()
        hoy.set(Calendar.HOUR_OF_DAY, 0)
        hoy.set(Calendar.MINUTE, 0)
        hoy.set(Calendar.SECOND, 0)
        hoy.set(Calendar.MILLISECOND, 0)

        val manana = Calendar.getInstance()
        manana.add(Calendar.DAY_OF_YEAR, 1)
        manana.set(Calendar.HOUR_OF_DAY, 0)
        manana.set(Calendar.MINUTE, 0)
        manana.set(Calendar.SECOND, 0)
        manana.set(Calendar.MILLISECOND, 0)

        return tareas.groupBy { tarea ->
            val fechaTarea = Calendar.getInstance()
            fechaTarea.time = tarea.fechaEntrega
            fechaTarea.set(Calendar.HOUR_OF_DAY, 0)
            fechaTarea.set(Calendar.MINUTE, 0)
            fechaTarea.set(Calendar.SECOND, 0)
            fechaTarea.set(Calendar.MILLISECOND, 0)

            when {
                fechaTarea.timeInMillis == hoy.timeInMillis -> "Hoy"
                fechaTarea.timeInMillis == manana.timeInMillis -> "Mañana"
                else -> {
                    val sdf = SimpleDateFormat("MMMM d", Locale("es", "ES"))
                    sdf.format(tarea.fechaEntrega)
                }
            }
        }
    }

    private fun agregarEncabezadoFecha(fecha: String) {
        val tvFecha = TextView(this)
        tvFecha.text = fecha
        tvFecha.textSize = 18f
        tvFecha.setTextColor(Color.parseColor("#212121"))
        tvFecha.setTypeface(null, android.graphics.Typeface.BOLD)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dpToPx(16), 0, dpToPx(12))
        tvFecha.layoutParams = params

        layoutTareas.addView(tvFecha)
    }

    private fun crearVistaTarea(tarea: Tarea): View {
        val itemView = LayoutInflater.from(this).inflate(
            R.layout.item_tarea_acudiente,
            layoutTareas,
            false
        )

        val cardTarea = itemView.findViewById<CardView>(R.id.cardTarea)
        val tvTitulo = itemView.findViewById<TextView>(R.id.tvTituloTarea)
        val tvMateria = itemView.findViewById<TextView>(R.id.tvMateria)
        val tvHoraEntrega = itemView.findViewById<TextView>(R.id.tvHoraEntrega)
        val tvNota = itemView.findViewById<TextView>(R.id.tvNota)
        val iconEstado = itemView.findViewById<ImageView>(R.id.iconEstado)

        tvTitulo.text = tarea.titulo
        tvMateria.text = tarea.materia ?: "Sin materia"

        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        tvHoraEntrega.text = "Entrega: ${sdf.format(tarea.fechaEntrega)}"

        if (tarea.completada) {
            iconEstado.setImageResource(R.drawable.ic_check_circle)
            iconEstado.setColorFilter(ContextCompat.getColor(this, R.color.green_success))

            if (tarea.nota > 0) {
                tvNota.visibility = View.VISIBLE
                tvNota.text = "Nota: ${tarea.nota}"

                val colorNota = when {
                    tarea.nota >= 4.0 -> R.color.green_success
                    tarea.nota >= 3.0 -> R.color.orange_accent
                    else -> R.color.red_error
                }
                tvNota.setTextColor(ContextCompat.getColor(this, colorNota))
            } else {
                tvNota.visibility = View.GONE
            }
        } else {
            iconEstado.setImageResource(R.drawable.ic_clock)
            iconEstado.setColorFilter(ContextCompat.getColor(this, R.color.orange_accent))
            tvNota.visibility = View.GONE
        }

        // Click para ver detalle
        cardTarea.setOnClickListener {
            mostrarDetalleTarea(tarea)
        }

        return itemView
    }

    private fun mostrarDetalleTarea(tarea: Tarea) {
        val intent = Intent(this, DetalleTareaAcudienteActivity::class.java)
        intent.putExtra("tarea_id", tarea.id)
        intent.putExtra("titulo", tarea.titulo)
        intent.putExtra("descripcion", tarea.descripcion)
        intent.putExtra("materia", tarea.materia)
        intent.putExtra("profesor", tarea.profesor)
        intent.putExtra("fecha_entrega", tarea.fechaEntrega.time)
        intent.putExtra("nota", tarea.nota)
        intent.putExtra("completada", tarea.completada)
        startActivity(intent)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}