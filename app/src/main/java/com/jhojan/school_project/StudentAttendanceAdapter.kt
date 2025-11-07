package com.jhojan.school_project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jhojan.school_project.databinding.ItemStudentAttendanceBinding

class StudentAttendanceAdapter(
    private val students: MutableList<EstudianteAsistencia>
) : RecyclerView.Adapter<StudentAttendanceAdapter.StudentAttendanceViewHolder>() {

    inner class StudentAttendanceViewHolder(private val binding: ItemStudentAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: EstudianteAsistencia, position: Int) {
            binding.tvStudentName.text = student.studentName

            // Actualizar botones según el estado actual
            updateButtonStates(student.status)

            // Listeners para los botones
            binding.btnPresente.setOnClickListener {
                updateStatus(position, "presente")
            }
            binding.btnAusente.setOnClickListener {
                updateStatus(position, "ausente")
            }
            binding.btnTarde.setOnClickListener {
                updateStatus(position, "tarde")
            }
            binding.btnExcusado.setOnClickListener {
                updateStatus(position, "excusado")
            }
        }

        private fun updateStatus(position: Int, status: String) {
            students[position] = students[position].copy(status = status)
            updateButtonStates(status)
        }

        private fun updateButtonStates(status: String) {
            val context = binding.root.context
            val activeColor = ContextCompat.getColor(context, R.color.estudiante)
            val inactiveColor = ContextCompat.getColor(context, R.color.gray_400)
            val ausenteColor = ContextCompat.getColor(context, android.R.color.holo_red_dark)
            val tardeColor = ContextCompat.getColor(context, R.color.acudiente)
            val excusadoColor = ContextCompat.getColor(context, R.color.blue_primary)

            // Resetear todos los botones a inactivo
            binding.btnPresente.backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_400)
            binding.btnAusente.backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_400)
            binding.btnTarde.backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_400)
            binding.btnExcusado.backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_400)

            // Activar el botón seleccionado
            when (status) {
                "presente" -> binding.btnPresente.backgroundTintList = ContextCompat.getColorStateList(context, R.color.estudiante)
                "ausente" -> binding.btnAusente.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_red_dark)
                "tarde" -> binding.btnTarde.backgroundTintList = ContextCompat.getColorStateList(context, R.color.acudiente)
                "excusado" -> binding.btnExcusado.backgroundTintList = ContextCompat.getColorStateList(context, R.color.blue_primary)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentAttendanceViewHolder {
        val binding = ItemStudentAttendanceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudentAttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentAttendanceViewHolder, position: Int) {
        holder.bind(students[position], position)
    }

    override fun getItemCount(): Int = students.size

    fun getAttendanceData(): List<EstudianteAsistencia> = students
}
