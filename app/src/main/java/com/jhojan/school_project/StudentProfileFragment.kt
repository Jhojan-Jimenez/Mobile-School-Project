package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentStudentProfileBinding

class StudentProfileFragment : Fragment() {

    private var _binding: FragmentStudentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        showLoading(true)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Información del estudiante
                        val nombreCompleto = document.getString("nombre_completo") ?: "No especificado"
                        val grado = document.getString("grado") ?: "No especificado"
                        val grupo = document.getString("grupo") ?: "No especificado"

                        // Información de contacto
                        val correo = document.getString("correo") ?: "No especificado"
                        val telefono = document.getString("telefono") ?: "No especificado"
                        val direccion = document.getString("Direccion") ?: "No especificada"

                        // Actualizar UI
                        binding.tvProfileName.text = nombreCompleto
                        binding.tvProfileGrade.text = grado
                        binding.tvProfileGroup.text = grupo
                        binding.tvProfileEmail.text = correo
                        binding.tvProfilePhone.text = telefono
                        binding.tvProfileAddress.text = direccion

                        showLoading(false)
                    } else {
                        showLoading(false)
                        Toast.makeText(requireContext(), "No se encontró información del usuario", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            showLoading(false)
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.fabEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos al volver de la edición
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
