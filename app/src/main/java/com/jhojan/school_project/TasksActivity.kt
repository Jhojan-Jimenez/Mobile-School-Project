package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
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
    val title: String,
    val subject: String,
    val dueDate: Long, // timestamp
    val status: TaskStatus,
    val description: String
)

class TasksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTasksBinding
    private val allTasks = mutableListOf<Task>()
    private val filteredTasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter
    private var currentFilter = TaskStatus.UPCOMING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadDummyTasks()
        setupRecyclerView()
        setupTabs()

        // Load initial filter
        filterTasks(TaskStatus.UPCOMING)
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

    private fun loadDummyTasks() {
        val calendar = Calendar.getInstance()

        // Tareas próximamente (futuras)
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        allTasks.add(Task(
            "1",
            "Resolver ecuaciones cuadráticas",
            "Matemáticas",
            calendar.timeInMillis,
            TaskStatus.UPCOMING,
            "Completar ejercicios de las páginas 45-48 del libro de texto. Incluir procedimiento detallado."
        ))

        calendar.add(Calendar.DAY_OF_MONTH, 3)
        allTasks.add(Task(
            "2",
            "Informe sobre el sistema solar",
            "Ciencias",
            calendar.timeInMillis,
            TaskStatus.UPCOMING,
            "Investigar y crear un informe completo sobre los planetas del sistema solar. Mínimo 5 páginas."
        ))

        calendar.add(Calendar.DAY_OF_MONTH, 5)
        allTasks.add(Task(
            "3",
            "Completar ejercicios de gramática",
            "Inglés",
            calendar.timeInMillis,
            TaskStatus.UPCOMING,
            "Unit 7: Present Perfect and Past Simple. Ejercicios 1-10."
        ))

        // Tareas vencidas (pasadas)
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_MONTH, -2)
        allTasks.add(Task(
            "4",
            "Lectura del capítulo 5",
            "Español",
            calendar.timeInMillis,
            TaskStatus.OVERDUE,
            "Leer el capítulo 5 de Don Quijote y preparar un resumen de una página."
        ))

        calendar.add(Calendar.DAY_OF_MONTH, -3)
        allTasks.add(Task(
            "5",
            "Línea de tiempo",
            "Historia",
            calendar.timeInMillis,
            TaskStatus.OVERDUE,
            "Crear una línea de tiempo ilustrada de la Revolución Industrial (1760-1840)."
        ))

        // Tareas completadas
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_MONTH, -5)
        allTasks.add(Task(
            "6",
            "Laboratorio de reacciones químicas",
            "Química",
            calendar.timeInMillis,
            TaskStatus.COMPLETED,
            "Completar el informe del laboratorio sobre reacciones ácido-base."
        ))

        calendar.add(Calendar.DAY_OF_MONTH, -7)
        allTasks.add(Task(
            "7",
            "Investigación sobre deportes olímpicos",
            "Educación Física",
            calendar.timeInMillis,
            TaskStatus.COMPLETED,
            "Investigar la historia de tres deportes olímpicos y presentar un informe."
        ))

        calendar.add(Calendar.DAY_OF_MONTH, -10)
        allTasks.add(Task(
            "8",
            "Proyecto de pintura",
            "Artes",
            calendar.timeInMillis,
            TaskStatus.COMPLETED,
            "Crear una pintura con tema libre utilizando técnicas de acuarela."
        ))
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(filteredTasks) { task ->
            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra("TASK_ID", task.id)
            intent.putExtra("TASK_TITLE", task.title)
            intent.putExtra("TASK_SUBJECT", task.subject)
            intent.putExtra("TASK_DUE_DATE", task.dueDate)
            intent.putExtra("TASK_STATUS", task.status.name)
            intent.putExtra("TASK_DESCRIPTION", task.description)
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
            binding.tvTaskTitle.text = task.title
            binding.tvTaskSubtitle.text = task.subject

            // Format due date
            val dateFormat = SimpleDateFormat("EEE, d MMM", Locale("es", "ES"))
            val formattedDate = dateFormat.format(task.dueDate)
            binding.tvTaskSubtitle.text = "${task.subject} • ${formattedDate}"

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
