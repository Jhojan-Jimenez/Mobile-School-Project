package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilProfesorActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Header y Footer
    private lateinit var teacherHeader: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView

    // Views
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutPerfil: LinearLayout
    private lateinit var tvNombre: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvDepartamento: TextView
    private lateinit var tvAsignatura: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var tvApellido: TextView
    private lateinit var tvRol: TextView
    private lateinit var btnLogout: Button

    // Datos
    private var profesorId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_profesor)

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Obtener el ID del profesor
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        profesorId = prefs.getString("profesor_id", "") ?: ""

        // Si no hay profesor guardado, usar el del usuario autenticado
        if (profesorId.isEmpty()) {
            profesorId = auth.currentUser?.uid ?: ""
        }

        // Si aún no hay ID, usar el por defecto
        if (profesorId.isEmpty()) {
            profesorId = "ZW6kOK1PaGVcteR4N9mzSQlcxjd2"
        }

        // Inicializar vistas
        initViews()

        // Configurar Header y Footer
        setupHeaderFooter()

        // Configurar listeners
        setupListeners()

        // Cargar datos del profesor
        cargarDatosProfesor()
    }

    private fun initViews() {
        teacherHeader = findViewById(R.id.teacherHeader)
        bottomNav = findViewById(R.id.bottomNav)
        progressBar = findViewById(R.id.progressBar)
        layoutPerfil = findViewById(R.id.layoutPerfil)
        tvNombre = findViewById(R.id.tvNombre)
        tvEmail = findViewById(R.id.tvEmail)
        tvTelefono = findViewById(R.id.tvTelefono)
        tvDepartamento = findViewById(R.id.tvDepartamento)
        tvAsignatura = findViewById(R.id.tvAsignatura)
        tvDireccion = findViewById(R.id.tvDireccion)
        tvApellido = findViewById(R.id.tvApellido)
        tvRol = findViewById(R.id.tvRol)
        btnLogout = findViewById(R.id.btnLogout)
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
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun cargarDatosProfesor() {
        // Mostrar progressBar
        progressBar.visibility = View.VISIBLE
        layoutPerfil.visibility = View.GONE

        db.collection("users")
            .document(profesorId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtener datos del documento
                    val nombre = document.getString("nombre") ?: "No disponible"
                    val apellido = document.getString("apellido") ?: "No disponible"
                    val email = document.getString("email") ?: "No disponible"
                    val telefono = document.getString("telefono") ?: "No disponible"
                    val departamento = document.getString("departamento") ?: "No disponible"
                    val asignatura = document.getString("asignatura") ?: "No disponible"
                    val direccion = document.getString("Direccion") ?: "No disponible"
                    val rol = document.getString("rol") ?: "Profesor"

                    // Mostrar datos en las vistas
                    tvNombre.text = nombre
                    tvApellido.text = apellido
                    tvEmail.text = email
                    tvTelefono.text = telefono
                    tvDepartamento.text = departamento
                    tvAsignatura.text = asignatura
                    tvDireccion.text = direccion
                    tvRol.text = rol.capitalize()

                    // Ocultar progressBar y mostrar perfil
                    progressBar.visibility = View.GONE
                    layoutPerfil.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "No se encontró el perfil del profesor", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun logout() {
        // Cerrar sesión en Firebase
        auth.signOut()

        // Limpiar SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Redirigir al login y limpiar el stack de actividades
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}