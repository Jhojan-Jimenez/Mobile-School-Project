package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityTeacherPanelBinding

class TeacherPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherPanelBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUI()
        loadUserData()
        setupListeners()
    }

    private fun setupUI() {
        // Título por defecto (lo sobreescribimos cuando llegue el usuario)
        binding.topAppBar.title = "Carlos G. - Docente"

        // Imagen de portada (del HTML que compartiste)
        val heroUrl =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDyF_n38BaBvMj_4SoNnB1PI4NZzbuEPejuceXpw1wswTcCKZjVvFaTVV17MD5Yl-XONv_PDldkBlPcKVk6QKApQcmPDIWef6HNj_7xjN4pQk5sVpUAj2Q4chOi0Xo7BbI3Q4WWDbndb9qXrtUoDCE-FY0iOMMtoaKsA2CKmtmRDzspXYJVeKFUbWYIrQnNI825bL2q20sRyiPCW07l7DX1zkRjnG2WdMLRdzD0ZWobAiH1pH24vHxOBoAU_ozQRp2obiSjZbXckvY"
        Glide.with(this).load(heroUrl).into(binding.imgHero)

        // Subtítulo por defecto (puedes actualizarlo con datos reales luego)
        binding.tvSubtitle.text =
            "Actualmente enseñas 5 asignaturas y tienes 2 tareas pendientes por calificar"

        // Contadores demo (cámbialos por valores reales si los tienes en Firestore)
        binding.tvSubjects.text = "5"
        binding.tvPendingTasks.text = "2"
        binding.tvStudents.text = "120"
    }

    /** Carga nombre del usuario desde Firestore */
    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            // Muestra "Nombre Apellido"
                            val nombreCompleto = "${user.nombre} ${user.apellido}".trim()
                            binding.tvUserName.text = "Hola, $nombreCompleto"
                            // Actualiza el título del AppBar con el rol
                            binding.topAppBar.title = "$nombreCompleto - Docente"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TeacherPanel", "Error al cargar usuario", e)
                }
        }
    }

    /** Listeners básicos: menú, campana y logout */
    private fun setupListeners() {
        // Icono de navegación del AppBar (engranaje del sistema en este layout)
        binding.topAppBar.setNavigationOnClickListener {
            Toast.makeText(this, "Ajustes (pendiente)", Toast.LENGTH_SHORT).show()
        }

        // Botón de notificaciones del header
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notificaciones (pendiente)", Toast.LENGTH_SHORT).show()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
