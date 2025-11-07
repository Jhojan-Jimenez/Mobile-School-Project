package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.jhojan.school_project.databinding.ActivityAdminPanelBinding

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // Configurar botón de volver
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.nav_home)
        }

        // Escuchar cambios de destino para actualizar el título y visibilidad del botón
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Actualizar título según el fragment actual
            binding.headerTitle.text = when (destination.id) {
                R.id.nav_home -> {
                    binding.btnBack.visibility = View.GONE
                    "Panel de Administrador"
                }
                R.id.nav_estudiantes -> {
                    binding.btnBack.visibility = View.VISIBLE
                    "Estudiantes"
                }
                R.id.nav_docentes -> {
                    binding.btnBack.visibility = View.VISIBLE
                    "Docentes"
                }
                R.id.nav_cursos -> {
                    binding.btnBack.visibility = View.VISIBLE
                    "Cursos"
                }
                R.id.nav_configuraciones -> {
                    binding.btnBack.visibility = View.VISIBLE
                    "Configuración"
                }
                else -> {
                    binding.btnBack.visibility = View.VISIBLE
                    destination.label ?: "Panel de Administrador"
                }
            }
        }
    }
}
