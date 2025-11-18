package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.TeacherHeader
import com.jhojan.school_project.TeacherBottomNavigationView

class TeacherPanelActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var teacherNameText: TextView
    private lateinit var subjectsCountText: TextView
    private lateinit var pendingCountText: TextView
    private lateinit var studentsCountText: TextView

    private lateinit var assignTasksCard: CardView
    private lateinit var reviewTasksCard: CardView
    private lateinit var registerGradesCard: CardView
    private lateinit var registerAttendanceCard: CardView
    private lateinit var notateObservationsCard: CardView
    private lateinit var consultCalendarCard: CardView
    private lateinit var sendMessagesCard: CardView

    private lateinit var header: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_panel)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        loadTeacherData()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Header
        header = findViewById(R.id.teacherHeader)

        // Footer
        bottomNav = findViewById(R.id.bottomNavT)
        bottomNav.setActiveItem(TeacherBottomNavigationView.NavigationItem.INICIO)

        // Header eventos
        header.setOnBackClickListener { finish() }

        teacherNameText = findViewById(R.id.teacherNameText)
        subjectsCountText = findViewById(R.id.subjectsCountText)
        pendingCountText = findViewById(R.id.pendingCountText)
        studentsCountText = findViewById(R.id.studentsCountText)

        assignTasksCard = findViewById(R.id.assignTasksCard)
        reviewTasksCard = findViewById(R.id.reviewTasksCard)
        registerGradesCard = findViewById(R.id.registerGradesCard)
        registerAttendanceCard = findViewById(R.id.registerAttendanceCard)
        notateObservationsCard = findViewById(R.id.notateObservationsCard)
        consultCalendarCard = findViewById(R.id.consultCalendarCard)
        sendMessagesCard = findViewById(R.id.sendMessagesCard)
    }

    private fun loadTeacherData() {
        val currentUser = auth.currentUser ?: return finish()

        val teacherId = currentUser.uid

        // Cargar header usando tu componente propio
        header.loadTeacherData(teacherId)

        // Firestore: profesores/{teacherId}
        db.collection("profesores")
            .document(teacherId)
            .get()
            .addOnSuccessListener { document ->

                if (document != null && document.exists()) {

                    // Nombre
                    val teacherName = document.getString("nombre") ?: "Profesor"
                    teacherNameText.text = "Hola, $teacherName"

                    // materias: lista dentro del documento
                    val materias = document.get("materias") as? List<Map<String, Any>> ?: emptyList()
                    subjectsCountText.text = materias.size.toString()

                    // Suma total de pendientes
                    val totalPendientes = materias.sumOf {
                        (it["pendientes"] as? Long ?: 0L)
                    }
                    pendingCountText.text = totalPendientes.toString()

                    // Total estudiantes
                    val estudiantes = document.getLong("cantidad_estudiantes") ?: 0
                    studentsCountText.text = estudiantes.toString()
                }
            }
            .addOnFailureListener {
                // manejo de errores opcional
            }
    }

    private fun setupClickListeners() {

        assignTasksCard.setOnClickListener {
            startActivity(Intent(this, CrearTareaActivity::class.java))
        }

        reviewTasksCard.setOnClickListener {
            // startActivity(Intent(this, ReviewTasksActivity::class.java))
        }

        registerGradesCard.setOnClickListener {
            // startActivity(Intent(this, RegisterGradesActivity::class.java))
        }

        registerAttendanceCard.setOnClickListener {
            // startActivity(Intent(this, RegisterAttendanceActivity::class.java))
        }

        notateObservationsCard.setOnClickListener {
            // startActivity(Intent(this, NotateObservationsActivity::class.java))
        }

        consultCalendarCard.setOnClickListener {
            // startActivity(Intent(this, ConsultCalendarActivity::class.java))
        }

        sendMessagesCard.setOnClickListener {
            // startActivity(Intent(this, SendMessagesActivity::class.java))
        }
    }
}
