package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentTeacherHomeBinding

class TeacherHomeFragment : Fragment() {

    private var _binding: FragmentTeacherHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUI()
        loadUserData()
        setupListeners()
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = "Carlos G. - Docente"

        val heroUrl =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDyF_n38BaBvMj_4SoNnB1PI4NZzbuEPejuceXpw1wswTcCKZjVvFaTVV17MD5Yl-XONv_PDldkBlPcKVk6QKApQcmPDIWef6HNj_7xjN4pQk5sVpUAj2Q4chOi0Xo7BbI3Q4WWDbndb9qXrtUoDCE-FY0iOMMtoaKsA2CKmtmRDzspXYJVeKFUbWYIrQnNI825bL2q20sRyiPCW07l7DX1zkRjnG2WdMLRdzD0ZWobAiH1pH24vHxOBoAU_ozQRp2obiSjZbXckvY"
        Glide.with(this).load(heroUrl).into(binding.imgHero)

        binding.tvSubtitle.text =
            "Actualmente enseñas 5 asignaturas y tienes 2 tareas pendientes por calificar"

        binding.tvSubjects.text = "5"
        binding.tvPendingTasks.text = "2"
        binding.tvStudents.text = "120"
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            val nombreCompleto = "${user.nombre} ${user.apellido}".trim()
                            binding.tvUserName.text = "Hola, $nombreCompleto"
                            binding.tvHeaderTitle.text = "$nombreCompleto - Docente"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TeacherHomeFragment", "Error al cargar usuario", e)
                }
        }
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Ajustes (pendiente)", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Notificaciones (pendiente)", Toast.LENGTH_SHORT).show()
        }

        binding.rowRegisterAttendance.setOnClickListener {
            findNavController().navigate(R.id.clasesProfesorFragment)
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
