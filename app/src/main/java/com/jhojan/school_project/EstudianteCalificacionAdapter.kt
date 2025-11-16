package com.jhojan.school_project

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EstudianteCalificacionAdapter(
    private val estudiantes: MutableList<Estudiante>
) : RecyclerView.Adapter<EstudianteCalificacionAdapter.EstudianteViewHolder>() {

    inner class EstudianteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreEstudiante)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFechaEntrega)
        val etCalificacion: EditText = itemView.findViewById(R.id.etCalificacion)

        fun bind(estudiante: Estudiante, position: Int) {
            tvNombre.text = estudiante.nombre
            tvEstado.text = estudiante.estado.texto
            tvEstado.setTextColor(estudiante.estado.color)
            tvFecha.text = "Fecha de entrega: ${estudiante.fechaEntrega}"

            // Mostrar calificación existente o hint
            if (estudiante.calificacion > 0.0) {
                etCalificacion.setText(estudiante.calificacion.toString())
            } else {
                etCalificacion.setText("")
                etCalificacion.hint = "0.0 - 5.0"
            }

            // Agregar TextWatcher para actualizar la calificación
            etCalificacion.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val texto = s.toString()
                    if (texto.isNotEmpty()) {
                        try {
                            val calificacion = texto.toDouble()
                            estudiantes[position].calificacion = calificacion
                        } catch (e: NumberFormatException) {
                            // Manejo de error si el texto no es un número válido
                        }
                    } else {
                        estudiantes[position].calificacion = 0.0
                    }
                }
            })

            // Establecer hint según el estado
            when (estudiante.estado) {
                EstadoEntrega.NO_ENTREGADO -> {
                    etCalificacion.isEnabled = false
                    etCalificacion.hint = "0.0"
                    etCalificacion.setText("0.0")
                }
                else -> {
                    etCalificacion.isEnabled = true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante_calificacion, parent, false)
        return EstudianteViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteViewHolder, position: Int) {
        holder.bind(estudiantes[position], position)
    }

    override fun getItemCount(): Int = estudiantes.size

    // Método para obtener todas las calificaciones
    fun obtenerCalificaciones(): List<Pair<String, Double>> {
        return estudiantes.map { Pair(it.nombre, it.calificacion) }
    }

    // Método para actualizar un estudiante específico
    fun actualizarEstudiante(position: Int, estudiante: Estudiante) {
        estudiantes[position] = estudiante
        notifyItemChanged(position)
    }
}