package com.jhojan.school_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttendanceAdapter(
    private val attendanceList: List<AttendanceActivity.AttendanceItem>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvStudentEmail: TextView = itemView.findViewById(R.id.tvStudentEmail)
        val checkboxPresent: CheckBox = itemView.findViewById(R.id.checkboxPresent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val attendanceItem = attendanceList[position]

        holder.tvStudentName.text = attendanceItem.student.nombre
        holder.tvStudentEmail.text = attendanceItem.student.email
        holder.checkboxPresent.isChecked = attendanceItem.isPresent

        // Importante: remover listener antes de agregar uno nuevo para evitar callbacks duplicados
        holder.checkboxPresent.setOnCheckedChangeListener(null)
        holder.checkboxPresent.isChecked = attendanceItem.isPresent

        holder.checkboxPresent.setOnCheckedChangeListener { _, isChecked ->
            attendanceItem.isPresent = isChecked
        }

        holder.itemView.setOnClickListener {
            holder.checkboxPresent.isChecked = !holder.checkboxPresent.isChecked
        }
    }

    override fun getItemCount(): Int = attendanceList.size
}