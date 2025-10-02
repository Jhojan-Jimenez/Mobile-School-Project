package com.jhojan.school_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jhojan.school_project.databinding.ActivityAdminPanelBinding

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top bar actions
        binding.btnBell.setOnClickListener {
            Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
        }

        // Ejemplos de clicks sobre algunas tarjetas
        binding.gridAcademic.getChildAt(0)?.setOnClickListener {
            Toast.makeText(this, "Gestionar Estudiantes", Toast.LENGTH_SHORT).show()
        }
        binding.gridAcademic.getChildAt(1)?.setOnClickListener {
            Toast.makeText(this, "Gestionar Docentes", Toast.LENGTH_SHORT).show()
        }

        // Bottom nav demo
        binding.bottomNav.setOnItemSelectedListener {
            Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
            true
        }
    }
}
