package com.jhojan.school_project

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.TeacherHeader
import com.jhojan.school_project.TeacherBottomNavigationView
import java.util.*

class CrearTareaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // Header y Footer
    private lateinit var teacherHeader: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView

    // Views
    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var tvFechaEntrega: TextView
    private lateinit var btnSeleccionarFecha: Button
    private lateinit var spinnerCursos: Spinner
    private lateinit var spinnerMaterias: Spinner
    private lateinit var spinnerEstudiantes: Spinner
    private lateinit var btnCrearTarea: Button

    // Datos
    private var fechaEntregaTimestamp: Timestamp? = null
    private var profesorId: String = ""
    private val listaCursos = mutableListOf<Course>()
    private val listaMaterias = mutableListOf<Subject>()
    private val listaEstudiantes = mutableListOf<User>()

    data class Course(val id: String, val nombre: String, val codigo: String)
    data class Subject(val id: String, val nombre: String, val cursoId: String)
    data class User(val id: String, val nombre: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_tarea)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener el ID del profesor desde SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        profesorId = prefs.getString("profesor_id", "") ?: ""

        // Si no hay profesor guardado, usar uno por defecto (ajusta esto según tu lógica)
        if (profesorId.isEmpty()) {
            profesorId = "ZW6kOK1PaGVcteR4N9mzSQlcxjd2" // ID por defecto
        }

        // Inicializar vistas
        initViews()

        // Configurar Header y Footer
        setupHeaderFooter()

        // Cargar datos
        cargarCursos()
        cargarEstudiantes()

        // Configurar listeners
        setupListeners()
    }

    private fun initViews() {
        teacherHeader = findViewById(R.id.teacherHeader)
        bottomNav = findViewById(R.id.bottomNav)
        etTitulo = findViewById(R.id.etTitulo)
        etDescripcion = findViewById(R.id.etDescripcion)
        tvFechaEntrega = findViewById(R.id.tvFechaEntrega)
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha)
        spinnerCursos = findViewById(R.id.spinnerCursos)
        spinnerMaterias = findViewById(R.id.spinnerMaterias)
        spinnerEstudiantes = findViewById(R.id.spinnerEstudiantes)
        btnCrearTarea = findViewById(R.id.btnCrearTarea)
    }

    private fun setupHeaderFooter() {
        // Configurar Header
        teacherHeader.loadTeacherData(profesorId)
        teacherHeader.setOnBackClickListener {
            finish()
        }

        // Configurar Footer - marcar "Tareas" como activo
        bottomNav.setActiveItem(TeacherBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun setupListeners() {
        btnSeleccionarFecha.setOnClickListener {
            mostrarDateTimePicker()
        }

        spinnerCursos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) {
                    val cursoSeleccionado = listaCursos[position - 1]
                    cargarMateriasPorCurso(cursoSeleccionado.id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnCrearTarea.setOnClickListener {
            validarYCrearTarea()
        }
    }

    private fun cargarCursos() {
        db.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                listaCursos.clear()
                for (document in documents) {
                    val id = document.id
                    val nombre = document.getString("nombre") ?: ""
                    val codigo = document.getString("codigo") ?: ""
                    listaCursos.add(Course(id, nombre, codigo))
                }
                actualizarSpinnerCursos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar cursos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarMateriasPorCurso(cursoId: String) {
        db.collection("subjects")
            .whereEqualTo("curso_id", cursoId)
            .get()
            .addOnSuccessListener { documents ->
                listaMaterias.clear()
                for (document in documents) {
                    val id = document.id
                    val nombre = document.getString("nombre") ?: ""
                    val cursoIdDoc = document.getString("curso_id") ?: ""
                    listaMaterias.add(Subject(id, nombre, cursoIdDoc))
                }
                actualizarSpinnerMaterias()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar materias: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarEstudiantes() {
        // Cargar todos los usuarios y filtrar estudiantes
        // Ajusta el filtro según tu estructura (ej: whereEqualTo("rol", "estudiante"))
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                listaEstudiantes.clear()
                for (document in documents) {
                    val id = document.id
                    // Intenta obtener diferentes posibles campos de nombre
                    val nombre = document.getString("nombre")
                        ?: document.getString("name")
                        ?: document.getString("displayName")
                        ?: "Usuario sin nombre"

                    // Opcional: filtrar por rol si tienes ese campo
                    val rol = document.getString("rol") ?: document.getString("role")
                    if (rol == null || rol == "estudiante" || rol == "student") {
                        listaEstudiantes.add(User(id, nombre))
                    }
                }
                actualizarSpinnerEstudiantes()
                seleccionarEstudiantePorDefecto()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar estudiantes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun seleccionarEstudiantePorDefecto() {
        val estudianteIdPorDefecto = "rFXz5WbIdsQMJM3GzO5ww4dkfBm2"
        val posicion = listaEstudiantes.indexOfFirst { it.id == estudianteIdPorDefecto }
        if (posicion != -1) {
            // +1 porque la primera posición es el placeholder "Selecciona un estudiante"
            spinnerEstudiantes.setSelection(posicion + 1)
        }
    }

    private fun actualizarSpinnerCursos() {
        val nombres = mutableListOf("Selecciona un curso")
        nombres.addAll(listaCursos.map { "${it.nombre} (${it.codigo})" })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCursos.adapter = adapter
    }

    private fun actualizarSpinnerMaterias() {
        val nombres = mutableListOf("Selecciona una materia")
        nombres.addAll(listaMaterias.map { it.nombre })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMaterias.adapter = adapter
    }

    private fun actualizarSpinnerEstudiantes() {
        val nombres = mutableListOf("Selecciona un estudiante")
        nombres.addAll(listaEstudiantes.map { it.nombre })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstudiantes.adapter = adapter
    }

    private fun mostrarDateTimePicker() {
        val calendar = Calendar.getInstance()

        // DatePicker
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // TimePicker
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)

                        // Crear Timestamp
                        fechaEntregaTimestamp = Timestamp(calendar.time)

                        // Mostrar en TextView
                        val fechaFormateada = String.format(
                            "%02d/%02d/%d %02d:%02d",
                            dayOfMonth, month + 1, year, hourOfDay, minute
                        )
                        tvFechaEntrega.text = fechaFormateada
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun validarYCrearTarea() {
        val titulo = etTitulo.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val posicionCurso = spinnerCursos.selectedItemPosition
        val posicionMateria = spinnerMaterias.selectedItemPosition
        val posicionEstudiante = spinnerEstudiantes.selectedItemPosition

        // Validaciones
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Ingresa un título", Toast.LENGTH_SHORT).show()
            return
        }

        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Ingresa una descripción", Toast.LENGTH_SHORT).show()
            return
        }

        if (fechaEntregaTimestamp == null) {
            Toast.makeText(this, "Selecciona una fecha de entrega", Toast.LENGTH_SHORT).show()
            return
        }

        if (posicionCurso == 0) {
            Toast.makeText(this, "Selecciona un curso", Toast.LENGTH_SHORT).show()
            return
        }

        if (posicionMateria == 0) {
            Toast.makeText(this, "Selecciona una materia", Toast.LENGTH_SHORT).show()
            return
        }

        if (posicionEstudiante == 0) {
            Toast.makeText(this, "Selecciona un estudiante", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener IDs seleccionados
        val materiaId = listaMaterias[posicionMateria - 1].id
        val estudianteId = listaEstudiantes[posicionEstudiante - 1].id

        // Crear el mapa de datos según la estructura de Firestore
        val tareaData = hashMapOf(
            "titulo" to titulo,
            "descripcion" to descripcion,
            "fecha_entrega" to fechaEntregaTimestamp!!,
            "materia" to materiaId,
            "profesor" to profesorId,
            "estudiante" to estudianteId,
            "completada" to false,  // Nueva: Estado de completado
            "nota" to 0  // Nueva: Nota inicial en 0
        )

        // Deshabilitar botón mientras se guarda
        btnCrearTarea.isEnabled = false
        btnCrearTarea.text = "Creando..."

        // Guardar en Firestore
        db.collection("tareas")
            .add(tareaData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Tarea creada exitosamente", Toast.LENGTH_SHORT).show()
                finish() // Cerrar la actividad
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear tarea: ${e.message}", Toast.LENGTH_LONG).show()
                // Rehabilitar botón
                btnCrearTarea.isEnabled = true
                btnCrearTarea.text = "Crear Tarea"
            }
    }
}