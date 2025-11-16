package com.jhojan.school_project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jhojan.school_project.databinding.ItemEventBinding

class EventAdapter(
    private val events: MutableList<Event>,
    private val onEditClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.tvEventTitle.text = event.titulo

            // Mostrar información según el alcance
            val info = when (event.alcance) {
                "Colegio" -> "Alcance: Todo el colegio"
                "Curso" -> "Alcance: Curso - ${event.curso_nombre}"
                "Asignatura" -> "Alcance: Asignatura - ${event.asignatura_nombre}"
                "Estudiante" -> "Alcance: Estudiante - ${event.estudiante_nombre}"
                else -> "Alcance: ${event.alcance}"
            }
            binding.tvEventInfo.text = info

            binding.btnEdit.setOnClickListener {
                onEditClick(event)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}
