package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityCalendarBinding
import com.jhojan.school_project.databinding.ItemCalendarEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarEvent(
    val id: String,
    val titulo: String,
    val fecha: Long, // timestamp
    val alcance: String,
    val cursoId: String = "",
    val cursoNombre: String = "",
    val asignaturaId: String = "",
    val asignaturaNombre: String = "",
    val estudianteId: String = "",
    val estudianteNombre: String = ""
)

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private val allEvents = mutableListOf<CalendarEvent>()
    private val eventsForSelectedDay = mutableListOf<CalendarEvent>()
    private lateinit var adapter: CalendarEventAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        setupCalendar()
        loadEventsFromFirebase()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            loadEventsForDate(calendar.timeInMillis)
        }
    }

    private fun loadEventsForDate(dateInMillis: Long) {
        eventsForSelectedDay.clear()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateInMillis
        val selectedDay = calendar.get(Calendar.DAY_OF_YEAR)
        val selectedYear = calendar.get(Calendar.YEAR)

        // Filtrar eventos para el día seleccionado
        for (event in allEvents) {
            val eventCalendar = Calendar.getInstance()
            eventCalendar.timeInMillis = event.fecha
            val eventDay = eventCalendar.get(Calendar.DAY_OF_YEAR)
            val eventYear = eventCalendar.get(Calendar.YEAR)

            if (selectedDay == eventDay && selectedYear == eventYear) {
                eventsForSelectedDay.add(event)
            }
        }

        // Actualizar UI
        if (eventsForSelectedDay.isEmpty()) {
            binding.recyclerViewEvents.visibility = View.GONE
            binding.tvNoEvents.visibility = View.VISIBLE
        } else {
            binding.recyclerViewEvents.visibility = View.VISIBLE
            binding.tvNoEvents.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }

        // Actualizar título
        val dateFormat = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "ES"))
        binding.tvEventsTitle.text = "Eventos del ${dateFormat.format(dateInMillis)}"
    }

    private fun loadEventsFromFirebase() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Primero obtener información del usuario para filtrar eventos según alcance
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val userRole = userDoc.getString("rol") ?: ""
                val userGrado = userDoc.getString("grado") ?: ""
                val userGrupo = userDoc.getString("grupo") ?: ""

                // Cargar todos los eventos
                db.collection("events")
                    .get()
                    .addOnSuccessListener { documents ->
                        allEvents.clear()

                        for (document in documents) {
                            val titulo = document.getString("titulo") ?: ""
                            val fecha = document.getTimestamp("fecha")?.toDate()?.time ?: 0L
                            val alcance = document.getString("alcance") ?: ""
                            val cursoId = document.getString("curso_id") ?: ""
                            val cursoNombre = document.getString("curso_nombre") ?: ""
                            val asignaturaId = document.getString("asignatura_id") ?: ""
                            val asignaturaNombre = document.getString("asignatura_nombre") ?: ""
                            val estudianteId = document.getString("estudiante_id") ?: ""
                            val estudianteNombre = document.getString("estudiante_nombre") ?: ""

                            // Filtrar eventos según el alcance
                            val shouldShowEvent = when (alcance) {
                                "Colegio" -> true
                                "Curso" -> {
                                    // Verificar si el usuario pertenece al curso
                                    if (userRole == "Estudiante") {
                                        // Comparar con el curso del estudiante
                                        cursoNombre == "$userGrado - $userGrupo"
                                    } else {
                                        true // Profesores y admin ven todos los cursos
                                    }
                                }
                                "Asignatura" -> true // Por ahora mostrar todas las asignaturas
                                "Estudiante" -> {
                                    // Solo mostrar si es para este estudiante específico
                                    estudianteId == currentUser.uid
                                }
                                else -> false
                            }

                            if (shouldShowEvent) {
                                val event = CalendarEvent(
                                    id = document.id,
                                    titulo = titulo,
                                    fecha = fecha,
                                    alcance = alcance,
                                    cursoId = cursoId,
                                    cursoNombre = cursoNombre,
                                    asignaturaId = asignaturaId,
                                    asignaturaNombre = asignaturaNombre,
                                    estudianteId = estudianteId,
                                    estudianteNombre = estudianteNombre
                                )
                                allEvents.add(event)
                            }
                        }

                        // Cargar eventos del día actual
                        loadEventsForDate(System.currentTimeMillis())
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al cargar eventos: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar datos del usuario: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = CalendarEventAdapter(eventsForSelectedDay) { event ->
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            intent.putExtra("EVENT_TITULO", event.titulo)
            intent.putExtra("EVENT_FECHA", event.fecha)
            intent.putExtra("EVENT_ALCANCE", event.alcance)
            intent.putExtra("EVENT_CURSO_NOMBRE", event.cursoNombre)
            intent.putExtra("EVENT_ASIGNATURA_NOMBRE", event.asignaturaNombre)
            intent.putExtra("EVENT_ESTUDIANTE_NOMBRE", event.estudianteNombre)
            startActivity(intent)
        }
        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            this.adapter = this@CalendarActivity.adapter
        }
    }
}

class CalendarEventAdapter(
    private val events: List<CalendarEvent>,
    private val onEventClick: (CalendarEvent) -> Unit
) : RecyclerView.Adapter<CalendarEventAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCalendarEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: CalendarEvent) {
            // Formatear la hora desde el timestamp
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = event.fecha
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvEventTime.text = timeFormat.format(calendar.time)

            binding.tvEventTitle.text = event.titulo

            binding.root.setOnClickListener {
                onEventClick(event)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalendarEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}
