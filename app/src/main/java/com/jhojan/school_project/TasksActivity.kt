package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityTasksBinding
import com.jhojan.school_project.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class TaskStatus {
    UPCOMING, OVERDUE, COMPLETED
}

data class Task(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val estudiante: String,
    val fecha_entrega: Long,
    val link: String,
    val materia: String,
    val nota: Double,
    val profesor: String,
    val completada: Boolean,
    val status: TaskStatus = TaskStatus.UPCOMING,
    val subjectName: String = ""
)

class TasksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTasksBinding
    private val allTasks = mutableListOf<Task>()
    private val filteredTasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter
    private var currentFilter = TaskStatus.UPCOMING
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        setupTabs()
        loadTasks()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterTasks(TaskStatus.UPCOMING)
                    1 -> filterTasks(TaskStatus.OVERDUE)
                    2 -> filterTasks(TaskStatus.COMPLETED)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterTasks(status: TaskStatus) {
        currentFilter = status
        filteredTasks.clear()

        for (task in allTasks) {
            if (task.status == status) {
                filteredTasks.add(task)
            }
        }

        // Update UI
        if (filteredTasks.isEmpty()) {
            binding.recyclerViewTasks.visibility = View.GONE
            binding.tvNoTasks.visibility = View.VISIBLE
        } else {
            binding.recyclerViewTasks.visibility = View.VISIBLE
            binding.tvNoTasks.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadTasks() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showLoading(true)

        db.collection("tareas")
            .whereEqualTo("estudiante", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                allTasks.clear()
                val currentTime = System.currentTimeMillis()

                for (document in documents) {
                    val titulo = document.getString("titulo") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val estudiante = document.getString("estudiante") ?: ""
                    val fechaEntrega = document.getTimestamp("fecha_entrega")?.toDate()?.time ?: 0L
                    val link = document.getString("link") ?: ""
                    val materia = document.getString("materia") ?: ""
                    val nota = document.getDouble("nota") ?: 0.0
                    val profesor = document.getString("profesor") ?: ""
                    val completada = document.getBoolean("completada") ?: false

                    // Determinar el estado de la tarea
                    val status = when {
                        completada -> TaskStatus.COMPLETED
                        fechaEntrega < currentTime -> TaskStatus.OVERDUE
                        else -> TaskStatus.UPCOMING
                    }

                    val task = Task(
                        id = document.id,
                        titulo = titulo,
                        descripcion = descripcion,
                        estudiante = estudiante,
                        fecha_entrega = fechaEntrega,
                        link = link,
                        materia = materia,
                        nota = nota,
                        profesor = profesor,
                        completada = completada,
                        status = status
                    )

                    allTasks.add(task)
                }

                // Cargar nombres de materias
                loadSubjectNames()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error al cargar tareas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadSubjectNames() {
        var loadedCount = 0
        val totalTasks = allTasks.size

        if (totalTasks == 0) {
            showLoading(false)
            filterTasks(TaskStatus.UPCOMING)
            return
        }

        for (i in allTasks.indices) {
            val task = allTasks[i]
            db.collection("subjects")
                .document(task.materia)
                .get()
                .addOnSuccessListener { document ->
                    val subjectName = document.getString("nombre") ?: "Materia"
                    allTasks[i] = task.copy(subjectName = subjectName)

                    loadedCount++
                    if (loadedCount == totalTasks) {
                        showLoading(false)
                        filterTasks(TaskStatus.UPCOMING)
                    }
                }
                .addOnFailureListener {
                    allTasks[i] = task.copy(subjectName = "Materia")

                    loadedCount++
                    if (loadedCount == totalTasks) {
                        showLoading(false)
                        filterTasks(TaskStatus.UPCOMING)
                    }
                }
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.recyclerViewTasks.visibility = View.GONE
            binding.tvNoTasks.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(filteredTasks) { task ->
            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra("TASK_ID", task.id)
            intent.putExtra("TASK_TITULO", task.titulo)
            intent.putExtra("TASK_DESCRIPCION", task.descripcion)
            intent.putExtra("TASK_FECHA_ENTREGA", task.fecha_entrega)
            intent.putExtra("TASK_LINK", task.link)
            intent.putExtra("TASK_MATERIA", task.materia)
            intent.putExtra("TASK_SUBJECT_NAME", task.subjectName)
            intent.putExtra("TASK_NOTA", task.nota)
            intent.putExtra("TASK_COMPLETADA", task.completada)
            intent.putExtra("TASK_STATUS", task.status.name)
            startActivity(intent)
        }
        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(this@TasksActivity)
            this.adapter = this@TasksActivity.adapter
        }
    }
}

class TaskAdapter(
    private val tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.titulo

            // Format due date
            val dateFormat = SimpleDateFormat("EEE, d MMM", Locale("es", "ES"))
            val formattedDate = dateFormat.format(task.fecha_entrega)
            binding.tvTaskSubtitle.text = "${task.subjectName} • ${formattedDate}"

            binding.root.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size
}
