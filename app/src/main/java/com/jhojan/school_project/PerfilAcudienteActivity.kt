package com.jhojan.school_project

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilAcudienteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Header y Footer
    private lateinit var parentHeader: ParentHeader
    private lateinit var bottomNav: ParentBottomNavigationView

    // Views
    private lateinit var tvNombreCompleto: TextView
    private lateinit var tvRol: TextView
    private lateinit var etDireccion: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etCorreo: EditText
    private lateinit var btnGuardarCambios: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var cardPerfil: CardView
    private lateinit var progressBar: ProgressBar

    // Datos
    private var acudienteId: String = ""
    private var estudianteId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_acudiente)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        acudienteId = auth.currentUser?.uid ?: ""

        if (acudienteId.isEmpty()) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupHeaderFooter()
        setupListeners()
        cargarDatosAcudiente()
    }

    private fun initViews() {
        parentHeader = findViewById(R.id.parentHeader)
        bottomNav = findViewById(R.id.bottomNav)
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto)
        tvRol = findViewById(R.id.tvRol)
        etDireccion = findViewById(R.id.etDireccion)
        etTelefono = findViewById(R.id.etTelefono)
        etCorreo = findViewById(R.id.etCorreo)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        cardPerfil = findViewById(R.id.cardPerfil)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupHeaderFooter() {
        // Recuperar estudiante si ya existía en SharedPreferences
        estudianteId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("estudiante_id", "") ?: ""

        if (estudianteId.isNotEmpty()) {
            parentHeader.loadParentData(estudianteId)
        }

        parentHeader.setOnBackClickListener { finish() }

        // No hay item de perfil en el bottom nav, usar HOME o el que corresponda
        bottomNav.setActiveItem(ParentBottomNavigationView.NavigationItem.HOME)
    }

    private fun setupListeners() {
        btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        btnCerrarSesion.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }

    private fun cargarDatosAcudiente() {
        progressBar.visibility = View.VISIBLE
        cardPerfil.alpha = 0.5f

        db.collection("users")
            .document(acudienteId)
            .get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                cardPerfil.alpha = 1f

                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: ""
                    val apellido = document.getString("apellido") ?: ""
                    val direccion = document.getString("direccion") ?: ""
                    val telefono = document.getString("telefono") ?: ""
                    val correo = document.getString("correo") ?: ""
                    estudianteId = document.getString("estudiante_id") ?: ""

                    // Guardar estudiante_id
                    if (estudianteId.isNotEmpty()) {
                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit().putString("estudiante_id", estudianteId).apply()

                        // Cargar header con los datos del estudiante
                        parentHeader.loadParentData(estudianteId)
                    }

                    // Mostrar datos
                    tvNombreCompleto.text = "$nombre $apellido"
                    tvRol.text = "Parent/Guardian"
                    etDireccion.setText(direccion)
                    etTelefono.setText(telefono)
                    etCorreo.setText(correo)
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                cardPerfil.alpha = 1f
                Toast.makeText(
                    this,
                    "Error al cargar datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun guardarCambios() {
        val direccion = etDireccion.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val correo = etCorreo.text.toString().trim()

        // Validaciones
        if (direccion.isEmpty()) {
            etDireccion.error = "La dirección es requerida"
            etDireccion.requestFocus()
            return
        }

        if (telefono.isEmpty()) {
            etTelefono.error = "El teléfono es requerido"
            etTelefono.requestFocus()
            return
        }

        if (correo.isEmpty()) {
            etCorreo.error = "El correo es requerido"
            etCorreo.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = "Correo inválido"
            etCorreo.requestFocus()
            return
        }

        // Mostrar progreso
        progressBar.visibility = View.VISIBLE
        btnGuardarCambios.isEnabled = false

        // Actualizar en Firestore
        val updates = hashMapOf<String, Any>(
            "direccion" to direccion,
            "telefono" to telefono,
            "correo" to correo
        )

        db.collection("users")
            .document(acudienteId)
            .update(updates)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnGuardarCambios.isEnabled = true
                Toast.makeText(
                    this,
                    "Información actualizada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                // Efecto visual de éxito
                btnGuardarCambios.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.green_success)
                )
                btnGuardarCambios.postDelayed({
                    btnGuardarCambios.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.blue_primary)
                    )
                }, 1000)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnGuardarCambios.isEnabled = true
                Toast.makeText(
                    this,
                    "Error al actualizar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun mostrarDialogoCerrarSesion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cerrar Sesión")
        builder.setMessage("¿Estás seguro que deseas cerrar sesión?")

        builder.setPositiveButton("Sí") { dialog, _ ->
            cerrarSesion()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        // Personalizar colores de los botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this, R.color.red_error))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
    }

    private fun cerrarSesion() {
        // Limpiar SharedPreferences
        getSharedPreferences("user_prefs", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Cerrar sesión en Firebase
        auth.signOut()

        // Redirigir a pantalla de login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}