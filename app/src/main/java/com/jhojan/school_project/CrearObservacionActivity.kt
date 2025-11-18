package com.jhojan.school_project

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CrearObservacionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // Header y Footer
    private lateinit var teacherHeader: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView

    // Views
    private lateinit var spinnerEstudiantes: Spinner
    private lateinit var etDescripcion: EditText
    private lateinit var spinnerTipoFalta: Spinner
    private lateinit var btnCrearObservacion: Button

    // Datos
    private var profesorId: String = ""
    private val listaEstudiantes = mutableListOf<User>()

    data class User(val id: String, val nombre: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_observacion)

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
    }

    private fun initViews() {
        teacherHeader = findViewById(R.id.teacherHeader)
        bottomNav = findViewById(R.id.bottomNav)
        spinnerEstudiantes = findViewById(R.id.spinnerEstudiantes)
        etDescripcion = findViewById(R.id.etDescripcion)
        spinnerTipoFalta = findViewById(R.id.spinnerTipoFalta)
        btnCrearObservacion = findViewById(R.id.btnCrearObservacion)
    }

    private fun setupHeaderFooter() {
        // Configurar Header
        teacherHeader.loadTeacherData(profesorId)
        teacherHeader.setOnBackClickListener {
            finish()
        }

        // Configurar Footer - marcar "Observador" como activo (ajusta según tu navegación)
        bottomNav.setActiveItem(TeacherBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun setupListeners() {
        btnCrearObservacion.setOnClickListener {
            validarYCrearObservacion()
        }
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

    private fun configurarSpinnerTipoFalta() {
        val tiposFalta = arrayOf("Selecciona tipo de falta", "Leve", "Media", "Grave")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposFalta)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoFalta.adapter = adapter
    }

    private fun validarYCrearObservacion() {
        val posicionEstudiante = spinnerEstudiantes.selectedItemPosition
        val descripcion = etDescripcion.text.toString().trim()
        val posicionTipoFalta = spinnerTipoFalta.selectedItemPosition

        // Validaciones
        if (posicionEstudiante == 0) {
            Toast.makeText(this, "Selecciona un estudiante", Toast.LENGTH_SHORT).show()
            return
        }

        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Ingresa una descripción", Toast.LENGTH_SHORT).show()
            return
        }

        if (posicionTipoFalta == 0) {
            Toast.makeText(this, "Selecciona un tipo de falta", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener IDs y datos seleccionados
        val estudianteId = listaEstudiantes[posicionEstudiante - 1].id
        val tipoFalta = when (posicionTipoFalta) {
            1 -> "leve"
            2 -> "media"
            3 -> "grave"
            else -> "leve"
        }

        // Crear el mapa de datos para Firestore
        val observacionData = hashMapOf(
            "estudiante" to estudianteId,
            "profesor" to profesorId,
            "descripcion" to descripcion,
            "tipo_falta" to tipoFalta,
            "fecha" to Timestamp.now()
        )

        // Deshabilitar botón mientras se guarda
        btnCrearObservacion.isEnabled = false
        btnCrearObservacion.text = "Creando..."

        // Guardar en Firestore
        db.collection("observaciones")
            .add(observacionData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Observación creada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear observación: ${e.message}", Toast.LENGTH_LONG).show()
                // Rehabilitar botón
                btnCrearObservacion.isEnabled = true
                btnCrearObservacion.text = "Crear Observación"
            }
    }

    override fun onResume() {
        super.onResume()
        // Configurar spinner de tipo de falta cada vez que se reanuda la actividad
        configurarSpinnerTipoFalta()
    }
}