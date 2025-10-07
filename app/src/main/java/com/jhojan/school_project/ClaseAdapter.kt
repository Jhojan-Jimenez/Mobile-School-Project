package com.jhojan.school_project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jhojan.school_project.databinding.ItemClaseBinding

class ClaseAdapter(
    private val clases: List<Clase>,
    private val onClaseClick: (Clase) -> Unit
) : RecyclerView.Adapter<ClaseAdapter.ClaseViewHolder>() {

    inner class ClaseViewHolder(private val binding: ItemClaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clase: Clase) {
            binding.tvClaseName.text = clase.nombre
            binding.tvClaseDetails.text = "Grado ${clase.grado} - Grupo ${clase.grupo}"
            binding.tvAsignatura.text = "Asignatura: ${clase.asignatura}"

            binding.root.setOnClickListener {
                onClaseClick(clase)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClaseViewHolder {
        val binding = ItemClaseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClaseViewHolder, position: Int) {
        holder.bind(clases[position])
    }

    override fun getItemCount(): Int = clases.size
}
