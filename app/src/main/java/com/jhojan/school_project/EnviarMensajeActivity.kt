package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EnviarMensajeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Header y Footer
    private lateinit var parentHeader: ParentHeader
    private lateinit var bottomNav: ParentBottomNavigationView

    // Views
    private lateinit var spinnerProfesor: Spinner
    private lateinit var etTitulo: EditText
    private lateinit var etAsunto: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var tvFecha: TextView
    private lateinit var btnEnviar: Button
    private lateinit var progressBar: ProgressBar

    // Datos
    private var acudienteId: String = ""
    private val profesoresList = mutableListOf<ProfesorData>()
    private var profesorSeleccionadoId: String = ""

    data class ProfesorData(
        val id: String,
        val nombre: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enviar_mensaje)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupHeaderFooter()
        setupFechaActual()
        cargarProfesores()
        setupListeners()
    }

    private fun initViews() {
        parentHeader = findViewById(R.id.parentHeader)
        bottomNav = findViewById(R.id.bottomNav)
        spinnerProfesor = findViewById(R.id.spinnerProfesor)
        etTitulo = findViewById(R.id.etTitulo)
        etAsunto = findViewById(R.id.etAsunto)
        etDescripcion = findViewById(R.id.etDescripcion)
        tvFecha = findViewById(R.id.tvFecha)
        btnEnviar = findViewById(R.id.btnEnviar)
        progressBar = findViewById(R.id.progressBar)

        acudienteId = auth.currentUser?.uid ?: ""
    }

    private fun setupHeaderFooter() {
        parentHeader.loadParentData(acudienteId)
        parentHeader.setOnBackClickListener { finish() }
        bottomNav.setActiveItem(ParentBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun setupFechaActual() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        val fechaActual = dateFormat.format(Date())
        tvFecha.text = fechaActual
    }

    private fun cargarProfesores() {
        progressBar.visibility = View.VISIBLE
        spinnerProfesor.isEnabled = false

        db.collection("users")
            .whereEqualTo("rol", "Profesor")
            .get()
            .addOnSuccessListener { documents ->
                profesoresList.clear()
                profesoresList.add(ProfesorData("", "Seleccione un profesor"))

                for (document in documents) {
                    val id = document.id
                    val nombre = document.getString("nombre_completo")
                        ?: document.getString("nombre")
                        ?: "Profesor"

                    profesoresList.add(ProfesorData(id, nombre))
                }

                setupSpinner()
                progressBar.visibility = View.GONE
                spinnerProfesor.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar profesores: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            profesoresList.map { it.nombre }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProfesor.adapter = adapter

        spinnerProfesor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                profesorSeleccionadoId = profesoresList[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                profesorSeleccionadoId = ""
            }
        }
    }

    private fun setupListeners() {
        btnEnviar.setOnClickListener {
            enviarMensaje()
        }
    }

    private fun enviarMensaje() {
        val titulo = etTitulo.text.toString().trim()
        val asunto = etAsunto.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()

        // Validaciones
        if (profesorSeleccionadoId.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione un profesor", Toast.LENGTH_SHORT).show()
            return
        }

        if (titulo.isEmpty()) {
            etTitulo.error = "El título es requerido"
            etTitulo.requestFocus()
            return
        }

        if (asunto.isEmpty()) {
            etAsunto.error = "El asunto es requerido"
            etAsunto.requestFocus()
            return
        }

        if (descripcion.isEmpty()) {
            etDescripcion.error = "La descripción es requerida"
            etDescripcion.requestFocus()
            return
        }

        // Deshabilitar botón mientras se envía
        btnEnviar.isEnabled = false
        progressBar.visibility = View.VISIBLE

        // Crear objeto mensaje
        val mensaje = hashMapOf(
            "titulo" to titulo,
            "asunto" to asunto,
            "descripcion" to descripcion,
            "fecha" to Date(),
            "acudiente_id" to acudienteId,
            "profesor_id" to profesorSeleccionadoId,
            "leido" to false,
            "timestamp" to System.currentTimeMillis()
        )

        // Guardar en Firestore
        db.collection("mensajes")
            .add(mensaje)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Mensaje enviado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al enviar mensaje: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                btnEnviar.isEnabled = true
                progressBar.visibility = View.GONE
            }
    }
}