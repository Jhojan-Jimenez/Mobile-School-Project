package com.jhojan.school_project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jhojan.school_project.databinding.ItemGuardianBinding

class GuardianAdapter(
    private val guardians: MutableList<Guardian>,
    private val onEditClick: (Guardian) -> Unit
) : RecyclerView.Adapter<GuardianAdapter.GuardianViewHolder>() {

    inner class GuardianViewHolder(private val binding: ItemGuardianBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(guardian: Guardian) {
            binding.tvGuardianName.text = "${guardian.user.nombre} ${guardian.user.apellido}"
            binding.tvGuardianInfo.text = guardian.parentesco

            binding.btnEdit.setOnClickListener {
                onEditClick(guardian)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianViewHolder {
        val binding = ItemGuardianBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GuardianViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuardianViewHolder, position: Int) {
        holder.bind(guardians[position])
    }

    override fun getItemCount(): Int = guardians.size
}
