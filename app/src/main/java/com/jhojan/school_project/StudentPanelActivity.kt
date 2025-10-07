package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jhojan.school_project.databinding.ActivityStudentPanelBinding

class StudentPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupBottomNav()
        setupClicks()
    }

    private fun setupUI() {
        // Nombre y rol (reemplaza por datos de Firestore si quieres)
        binding.tvStudentName.text = "Alex Ramirez"
        binding.tvRole.text = "Estudiante"

        // Imágenes (placeholder gris si falla la carga)
        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuAnP2WWoz9Cc6kaOMAkhsMy0xrMLZ4qrvd790pNEAMpwe0BK9eEVmj_5Gv4xe0PudRpyKDTfRHDUpH5_KJtl7-CrepjoNygXV5giJh3J7PYxXL64Tbu-jrmef7uFUWqw3uF6K8_Vdu3T81SuUpb0xWQ-bFIzb0LGgcHs7kGEm0jcsdi-xzq__8ciXs68RxTCoNtWkKf28fBaSMeSN0uhESIlEiCDyRd4fkrodGP3jBYRbN1OUiaTds-rqxAaCL11ORbU4uIfbMfyY4")
            .placeholder(android.R.color.darker_gray)
            .into(binding.imgAvatar)

        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuAKn05uR2YOZRa3vdVty-Y9PIASuyVr-XEcMrGz5mocSER54XVboJHkizlqrsJcmwDyKEDQ6lzZ8-R_93bpI335s3ao5qFj9SI5hkI-toF2J2f2PicAeSLiNkUf8gLRs297VsTvh2lDMQFMUrk4wIG1axll2O-rq36xu2_PVG15QjhcpJhBlCVpIVo4EaHwRHmzPjp5NLKm8YYJGHPDTQBjjr9-lhcEE7XWBZMBaTML2T28ju0RT9DGaSWHHXGbhhNB3USUf78ijT8")
            .placeholder(android.R.color.darker_gray)
            .into(binding.imgCard1)

        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuBJNku3r68W-MwS-UFV21PSgaSHwq9X6kv45mMR0XD8vw5sahpDBvuHFvZIkN6qev2JHc1t3qpLkidmSTh_bJQiC_UbFj9C_MPD01vyDB3GgBDCgtz0a5q1AzotgQCyObvrFnqMnbIFNVsN4ISNlZnlgSeDRXM0M_be0MZ78K999Y8JG3b9UuEGas4l1r8cHfAZnbbwvTCYO1HApvT1w8MtRobDKJ7QLCGaL-j2VPsnktq63ofXIjQf1aaT3wmmEqkOrjL7ZTIQYC0")
            .placeholder(android.R.color.darker_gray)
            .into(binding.imgCard2)

        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuDYc7_Uiz98j5i7uIXe0zBhxLRzKFRzVeHZnBBukLFjX0WLv9zRD6U3Tpv2Xu8xNQ22u40VOHGYDETfeQlSf3oklz97alJVY_trUpMeQEQsUfXtSaQxaUalofv8cOBBYqP1JExhH_NRiL90ZFbPQhNRXwZkWVJjig0Oto4_sruPHMAmPp9kjEIX6zmSrp8EnGaZ6RblY38NGWRjc4AlKljbq8k_bBrPLVhfP9EMz74KIEQdDJsNEMC566KPcWkYYwOoOO2lu-BI74k")
            .placeholder(android.R.color.darker_gray)
            .into(binding.imgCard3)
    }

    private fun setupBottomNav() {
        // Tipo explícito para evitar "Cannot infer type..."
        binding.bottomNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_help -> {
                    Toast.makeText(this, "Soporte", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_bell -> {
                    Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_configuraciones -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Seleccionado por defecto (Perfil como en tu captura)
        binding.bottomNav.selectedItemId = R.id.nav_profile
    }

    private fun setupClicks() {
        binding.btnSettings.setOnClickListener {
            Toast.makeText(this, "Abrir ajustes", Toast.LENGTH_SHORT).show()
        }
        binding.btnBell.setOnClickListener {
            Toast.makeText(this, "Abrir notificaciones", Toast.LENGTH_SHORT).show()
        }
    }
}
