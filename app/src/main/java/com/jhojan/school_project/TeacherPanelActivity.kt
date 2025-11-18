package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class TeacherPanelActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_panel)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        initializeViews()
        loadTeacherData()
        setupClickListeners()
    }

    private fun initializeViews() {
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
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Redirect to login if not authenticated
            finish()
            return
        }

        val teacherId = currentUser.uid
        val teacherRef = database.child("profesores").child(teacherId)

        teacherRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val teacherName = snapshot.child("nombre").getValue(String::class.java) ?: "Profesor"
                teacherNameText.text = "Hola, $teacherName"

                // Count subjects (materias)
                val materiasCount = snapshot.child("materias").childrenCount.toInt()
                subjectsCountText.text = materiasCount.toString()

                // Count pending tasks
                var pendingCount = 0
                snapshot.child("materias").children.forEach { materia ->
                    val pendientes = materia.child("pendientes").getValue(Int::class.java) ?: 0
                    pendingCount += pendientes
                }
                pendingCountText.text = pendingCount.toString()

                // Count total students
                val cantidadEstudiantes = snapshot.child("cantidad_estudiantes").getValue(Int::class.java) ?: 0
                studentsCountText.text = cantidadEstudiantes.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun setupClickListeners() {
        assignTasksCard.setOnClickListener {
            // TODO: Navigate to AssignTasksActivity
            // startActivity(Intent(this, AssignTasksActivity::class.java))
        }

        reviewTasksCard.setOnClickListener {
            // TODO: Navigate to ReviewTasksActivity
            // startActivity(Intent(this, ReviewTasksActivity::class.java))
        }

        registerGradesCard.setOnClickListener {
            // TODO: Navigate to RegisterGradesActivity
            // startActivity(Intent(this, RegisterGradesActivity::class.java))
        }

        registerAttendanceCard.setOnClickListener {
            // TODO: Navigate to RegisterAttendanceActivity
            // startActivity(Intent(this, RegisterAttendanceActivity::class.java))
        }

        notateObservationsCard.setOnClickListener {
            // TODO: Navigate to NotateObservationsActivity
            // startActivity(Intent(this, NotateObservationsActivity::class.java))
        }

        consultCalendarCard.setOnClickListener {
            // TODO: Navigate to ConsultCalendarActivity
            // startActivity(Intent(this, ConsultCalendarActivity::class.java))
        }

        sendMessagesCard.setOnClickListener {
            // TODO: Navigate to SendMessagesActivity
            // startActivity(Intent(this, SendMessagesActivity::class.java))
        }
    }
}