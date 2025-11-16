package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jhojan.school_project.databinding.ActivityCalendarBinding
import com.jhojan.school_project.databinding.ItemCalendarEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: Long, // timestamp
    val time: String,
    val description: String
)

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private val allEvents = mutableListOf<CalendarEvent>()
    private val eventsForSelectedDay = mutableListOf<CalendarEvent>()
    private lateinit var adapter: CalendarEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadDummyEvents()
        setupRecyclerView()
        setupCalendar()

        // Cargar eventos del día actual
        loadEventsForDate(System.currentTimeMillis())
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
            eventCalendar.timeInMillis = event.date
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

    private fun loadDummyEvents() {
        val calendar = Calendar.getInstance()

        // Evento 1: Hoy
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        calendar.set(Calendar.MINUTE, 0)
        allEvents.add(CalendarEvent(
            "1",
            "Reunión de profesores",
            calendar.timeInMillis,
            "10:00 AM",
            "Reunión general del cuerpo docente para planificación académica"
        ))

        calendar.set(Calendar.HOUR_OF_DAY, 14)
        calendar.set(Calendar.MINUTE, 30)
        allEvents.add(CalendarEvent(
            "2",
            "Clase de Matemáticas",
            calendar.timeInMillis,
            "2:30 PM",
            "Clase regular de matemáticas - Álgebra lineal"
        ))

        // Evento mañana
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        allEvents.add(CalendarEvent(
            "3",
            "Evaluación de Ciencias",
            calendar.timeInMillis,
            "9:00 AM",
            "Examen del primer período - Biología"
        ))

        // Evento en 5 días
        calendar.add(Calendar.DAY_OF_MONTH, 4)
        calendar.set(Calendar.HOUR_OF_DAY, 15)
        calendar.set(Calendar.MINUTE, 0)
        allEvents.add(CalendarEvent(
            "4",
            "Feria de Ciencias",
            calendar.timeInMillis,
            "3:00 PM",
            "Exposición de proyectos científicos de los estudiantes"
        ))

        // Evento en 10 días
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        calendar.set(Calendar.HOUR_OF_DAY, 11)
        calendar.set(Calendar.MINUTE, 0)
        allEvents.add(CalendarEvent(
            "5",
            "Día del Estudiante",
            calendar.timeInMillis,
            "11:00 AM",
            "Celebración y actividades especiales para los estudiantes"
        ))

        // Evento en 15 días
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        allEvents.add(CalendarEvent(
            "6",
            "Inicio de Evaluaciones Finales",
            calendar.timeInMillis,
            "8:00 AM",
            "Comienzan los exámenes finales del semestre"
        ))
    }

    private fun setupRecyclerView() {
        adapter = CalendarEventAdapter(eventsForSelectedDay) { event ->
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            intent.putExtra("EVENT_TITLE", event.title)
            intent.putExtra("EVENT_DATE", event.date)
            intent.putExtra("EVENT_TIME", event.time)
            intent.putExtra("EVENT_DESCRIPTION", event.description)
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
            binding.tvEventTime.text = event.time
            binding.tvEventTitle.text = event.title

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
