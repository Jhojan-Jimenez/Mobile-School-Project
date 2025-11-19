package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ParentHomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var parentHeader: ParentHeader
    private lateinit var bottomNav: ParentBottomNavigationView

    private lateinit var tvWelcomeMessage: TextView
    private lateinit var tvGradeLevel: TextView
    private lateinit var tvGradeAverage: TextView
    private lateinit var pendingTasksContainer: LinearLayout

    private lateinit var viewAcademicCalendarCard: LinearLayout
    private lateinit var checkTasksCard: LinearLayout
    private lateinit var viewGeneralNotesCard: LinearLayout
    private lateinit var contactTeachersCard: LinearLayout
    private lateinit var reportHistoryCard: LinearLayout
    private lateinit var createClaimsCard: LinearLayout
    private lateinit var changeContactInfoCard: LinearLayout

    private var estudianteId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtener el ID del estudiante
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)


        estudianteId = prefs.getString("estudiante_id", "") ?: ""

        // Si no hay estudiante guardado, usar el usuario actual de Firebase
        if (estudianteId.isEmpty()) {
            estudianteId = auth.currentUser?.uid ?: ""
        }

        initializeViews()
        setupHeaderAndFooter()
        loadStudentData()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Header y Footer
        parentHeader = findViewById(R.id.parentHeader)
        bottomNav = findViewById(R.id.bottomNav)

        // Tarjeta de Bienvenida
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage)
        tvGradeLevel = findViewById(R.id.tvGradeLevel)

        // Promedio
        tvGradeAverage = findViewById(R.id.tvGradeAverage)

        // Contenedor de tareas pendientes
        pendingTasksContainer = findViewById(R.id.pendingTasksContainer)

        // Tarjetas de seguimiento académico
        viewAcademicCalendarCard = findViewById(R.id.viewAcademicCalendarCard)
        checkTasksCard = findViewById(R.id.checkTasksCard)
        viewGeneralNotesCard = findViewById(R.id.viewGeneralNotesCard)
        contactTeachersCard = findViewById(R.id.contactTeachersCard)
        reportHistoryCard = findViewById(R.id.reportHistoryCard)
        createClaimsCard = findViewById(R.id.createClaimsCard)
        changeContactInfoCard = findViewById(R.id.changeContactInfoCard)
    }

    private fun setupHeaderAndFooter() {
        // Configurar Header
        // Opción 1: Cargar desde Firestore
        val acudienteId = auth.currentUser?.uid ?: ""
        parentHeader.loadParentData(acudienteId)

        // Opción 2: Hardcodear (descomenta si prefieres)
        // studentHeader.setStudentDataWithDrawable("María González", R.drawable.student_photo)

        // Configurar botón de regreso
        parentHeader.setOnBackClickListener {
            finish()
        }

        // Configurar Footer - marcar Home como activo
        bottomNav.setActiveItem(ParentBottomNavigationView.NavigationItem.HOME)
    }

    private fun loadRealStudentData(estudianteId: String) {

        db.collection("users")
            .document(estudianteId)
            .get()
            .addOnSuccessListener { studentDoc ->

                if (studentDoc.exists()) {
                    val nombre = studentDoc.getString("nombre") ?: "Estudiante"
                    val grado = studentDoc.getString("grado") ?: "Grado 1"
                    val promedio = studentDoc.getDouble("promedio") ?: 8.5

                    tvWelcomeMessage.text = "Bienvenido"
                    tvGradeLevel.text = grado
                    tvGradeAverage.text = String.format("%.1f", promedio)

                    loadPendingTasks()

                } else {
                    tvWelcomeMessage.text = "Estudiante no encontrado"
                }
            }
            .addOnFailureListener {
                tvWelcomeMessage.text = "Error cargando estudiante"
            }
    }


    private fun loadStudentData() {

        val acudienteId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(acudienteId)
            .get()
            .addOnSuccessListener { acudienteDoc ->

                if (acudienteDoc.exists()) {
                    // Obtener el estudiante_id del acudiente
                    estudianteId = acudienteDoc.getString("estudiante_id") ?: ""

                    if (estudianteId.isEmpty()) {
                        tvWelcomeMessage.text = "No se encontró estudiante asignado"
                        return@addOnSuccessListener
                    }

                    // Guardar estudiante_id en SharedPreferences
                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    prefs.edit().putString("estudiante_id", estudianteId).apply()

                    // Ahora cargar datos del estudiante real
                    loadRealStudentData(estudianteId)

                } else {
                    tvWelcomeMessage.text = "Error cargando acudiente"
                }
            }
            .addOnFailureListener {
                tvWelcomeMessage.text = "Error consultando Firestore"
            }
    }


    private fun loadPendingTasks() {
        db.collection("tareas")
            .whereEqualTo("estudiante_id", estudianteId)
            .whereEqualTo("estado", "pendiente")
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                pendingTasksContainer.removeAllViews()

                if (documents.isEmpty) {
                    addNoTasksView()
                } else {
                    for (document in documents) {
                        val titulo = document.getString("titulo") ?: "Tarea sin título"
                        val fechaEntrega = document.getString("fecha_entrega") ?: "Sin definir"
                        addTaskItem(titulo, fechaEntrega)
                    }
                }
            }
            .addOnFailureListener {
                // Tareas de ejemplo en caso de error
                addTaskItem("Tarea de Matemáticas", "Mañana")
                addTaskItem("Proyecto de Ciencias", "Próxima semana")
            }
    }

    private fun addTaskItem(title: String, dueDate: String) {
        val taskView = layoutInflater.inflate(R.layout.item_pending_task, pendingTasksContainer, false)

        val tvTaskTitle = taskView.findViewById<TextView>(R.id.tvTaskTitle)
        val tvTaskDue = taskView.findViewById<TextView>(R.id.tvTaskDue)

        tvTaskTitle.text = title
        tvTaskDue.text = "Entrega: $dueDate"

        pendingTasksContainer.addView(taskView)
    }

    private fun addNoTasksView() {
        val taskView = layoutInflater.inflate(R.layout.item_pending_task, pendingTasksContainer, false)

        val tvTaskTitle = taskView.findViewById<TextView>(R.id.tvTaskTitle)
        val tvTaskDue = taskView.findViewById<TextView>(R.id.tvTaskDue)

        tvTaskTitle.text = "No hay tareas pendientes"
        tvTaskDue.text = "¡Estás al día!"

        pendingTasksContainer.addView(taskView)
    }

    private fun setupClickListeners() {
        viewAcademicCalendarCard.setOnClickListener {
            startActivity(Intent(this, CalendarioAcudienteActivity::class.java))
        }

        checkTasksCard.setOnClickListener {
            startActivity(Intent(this, TareasAcudienteActivity::class.java))
        }

        viewGeneralNotesCard.setOnClickListener {
            startActivity(Intent(this, MateriasAcudienteActivity::class.java))
        }

        contactTeachersCard.setOnClickListener {
            startActivity(Intent(this, EnviarMensajeActivity::class.java))
        }

        reportHistoryCard.setOnClickListener {
            startActivity(Intent(this, ObservacionesAcudienteActivity::class.java))
        }

        createClaimsCard.setOnClickListener {
            // TODO: Navegar a CrearPQRSActivity
        }

        changeContactInfoCard.setOnClickListener {
            // TODO: Navegar a CambiarContactoActivity
        }
    }
}
