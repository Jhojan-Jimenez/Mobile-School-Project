package com.jhojan.school_project

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CrearMensajeEventoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // Header y Footer
    private lateinit var teacherHeader: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView

    // Views
    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerEstudiantes: Spinner
    private lateinit var etDescripcion: EditText
    private lateinit var layoutFechaEvento: LinearLayout
    private lateinit var tvFechaEvento: TextView
    private lateinit var btnSeleccionarFecha: Button
    private lateinit var btnCrear: Button

    // Datos
    private var profesorId: String = ""
    private val listaEstudiantes = mutableListOf<User>()
    private var fechaEventoSeleccionada: Date? = null

    data class User(val id: String, val nombre: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_mensaje_evento)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener el ID del profesor desde SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        profesorId = prefs.getString("profesor_id", "") ?: ""

        // Si no hay profesor guardado, usar uno por defecto
        if (profesorId.isEmpty()) {
            profesorId = "ZW6kOK1PaGVcteR4N9mzSQlcxjd2"
        }

        // Inicializar vistas
        initViews()

        // Configurar Header y Footer
        setupHeaderFooter()

        // Cargar datos
        cargarEstudiantes()

        // Configurar listeners
        setupListeners()

        // Configurar spinners
        configurarSpinners()
    }

    private fun initViews() {
        teacherHeader = findViewById(R.id.teacherHeader)
        bottomNav = findViewById(R.id.bottomNav)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        spinnerEstudiantes = findViewById(R.id.spinnerEstudiantes)
        etDescripcion = findViewById(R.id.etDescripcion)
        layoutFechaEvento = findViewById(R.id.layoutFechaEvento)
        tvFechaEvento = findViewById(R.id.tvFechaEvento)
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha)
        btnCrear = findViewById(R.id.btnCrear)
    }

    private fun setupHeaderFooter() {
        // Configurar Header
        teacherHeader.loadTeacherData(profesorId)
        teacherHeader.setOnBackClickListener {
            finish()
        }

        // Configurar Footer
        bottomNav.setActiveItem(TeacherBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun setupListeners() {
        // Listener para cambio de tipo (Mensaje/Evento)
        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 2) { // Evento
                    layoutFechaEvento.visibility = View.VISIBLE
                } else {
                    layoutFechaEvento.visibility = View.GONE
                    fechaEventoSeleccionada = null
                    tvFechaEvento.text = "No seleccionada"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Listener para seleccionar fecha
        btnSeleccionarFecha.setOnClickListener {
            mostrarDatePicker()
        }

        // Listener para crear mensaje/evento
        btnCrear.setOnClickListener {
            validarYCrear()
        }
    }

    private fun configurarSpinners() {
        // Configurar spinner de tipo
        val tipos = arrayOf("Selecciona tipo", "Mensaje", "Evento")
        val adapterTipo = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adapterTipo
    }

    private fun cargarEstudiantes() {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                listaEstudiantes.clear()
                for (document in documents) {
                    val id = document.id
                    val nombre = document.getString("nombre")
                        ?: document.getString("name")
                        ?: document.getString("displayName")
                        ?: "Usuario sin nombre"

                    val rol = document.getString("rol") ?: document.getString("role")
                    if (rol == null || rol == "estudiante" || rol == "student") {
                        listaEstudiantes.add(User(id, nombre))
                    }
                }
                actualizarSpinnerEstudiantes()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar estudiantes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarSpinnerEstudiantes() {
        val nombres = mutableListOf("Selecciona un estudiante")
        nombres.addAll(listaEstudiantes.map { it.nombre })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstudiantes.adapter = adapter
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()

        // Si ya hay una fecha seleccionada, usar esa
        if (fechaEventoSeleccionada != null) {
            calendar.time = fechaEventoSeleccionada!!
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada = Calendar.getInstance()
                fechaSeleccionada.set(year, month, dayOfMonth)
                fechaEventoSeleccionada = fechaSeleccionada.time

                // Formatear y mostrar la fecha
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvFechaEvento.text = dateFormat.format(fechaEventoSeleccionada!!)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun validarYCrear() {
        val posicionTipo = spinnerTipo.selectedItemPosition
        val posicionEstudiante = spinnerEstudiantes.selectedItemPosition
        val descripcion = etDescripcion.text.toString().trim()

        // Validaciones
        if (posicionTipo == 0) {
            Toast.makeText(this, "Selecciona el tipo (Mensaje o Evento)", Toast.LENGTH_SHORT).show()
            return
        }

        if (posicionEstudiante == 0) {
            Toast.makeText(this, "Selecciona un estudiante", Toast.LENGTH_SHORT).show()
            return
        }

        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Ingresa una descripción", Toast.LENGTH_SHORT).show()
            return
        }

        val esEvento = posicionTipo == 2

        // Si es evento, validar que se haya seleccionado una fecha
        if (esEvento && fechaEventoSeleccionada == null) {
            Toast.makeText(this, "Selecciona la fecha del evento", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener ID del estudiante
        val estudianteId = listaEstudiantes[posicionEstudiante - 1].id

        // Crear el mapa de datos para Firestore
        val data = hashMapOf<String, Any>(
            "estudiante" to estudianteId,
            "profesor" to profesorId,
            "descripcion" to descripcion,
            "tipo" to if (esEvento) "evento" else "mensaje"
        )

        // Agregar fecha según el tipo
        if (esEvento) {
            data["eventDate"] = Timestamp(fechaEventoSeleccionada!!)
        } else {
            data["fecha"] = Timestamp.now()
        }

        // Deshabilitar botón mientras se guarda
        btnCrear.isEnabled = false
        btnCrear.text = "Creando..."

        // Guardar en Firestore
        db.collection("mensajes_eventos")
            .add(data)
            .addOnSuccessListener {
                val tipoTexto = if (esEvento) "Evento" else "Mensaje"
                Toast.makeText(this, "$tipoTexto creado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear: ${e.message}", Toast.LENGTH_LONG).show()
                // Rehabilitar botón
                btnCrear.isEnabled = true
                btnCrear.text = "Crear"
            }
    }
}