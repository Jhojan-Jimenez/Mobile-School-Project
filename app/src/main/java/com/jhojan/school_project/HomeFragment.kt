package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupClickListeners()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        loadStudentsCount()
        loadTeachersCount()
        loadGuardiansCount()
        loadCoursesCount()
        loadSubjectsCount()
    }

    private fun loadStudentsCount() {
        db.collection("users")
            .whereEqualTo("rol", "Estudiante")
            .get()
            .addOnSuccessListener { documents ->
                binding.tvCountStudents.text = documents.size().toString()
            }
            .addOnFailureListener {
                binding.tvCountStudents.text = "0"
            }
    }

    private fun loadTeachersCount() {
        db.collection("users")
            .whereEqualTo("rol", "Profesor")
            .get()
            .addOnSuccessListener { documents ->
                binding.tvCountTeachers.text = documents.size().toString()
            }
            .addOnFailureListener {
                binding.tvCountTeachers.text = "0"
            }
    }

    private fun loadGuardiansCount() {
        db.collection("users")
            .whereEqualTo("rol", "Acudiente")
            .get()
            .addOnSuccessListener { documents ->
                binding.tvCountGuardians.text = documents.size().toString()
            }
            .addOnFailureListener {
                binding.tvCountGuardians.text = "0"
            }
    }

    private fun loadCoursesCount() {
        db.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                binding.tvCountCourses.text = documents.size().toString()
            }
            .addOnFailureListener {
                binding.tvCountCourses.text = "0"
            }
    }

    private fun loadSubjectsCount() {
        db.collection("subjects")
            .get()
            .addOnSuccessListener { documents ->
                binding.tvCountSubjects.text = documents.size().toString()
            }
            .addOnFailureListener {
                binding.tvCountSubjects.text = "0"
            }
    }

    private fun setupClickListeners() {
        // Tarjetas del Resumen - click listeners
        binding.cardStudents.setOnClickListener {
            findNavController().navigate(R.id.nav_estudiantes)
        }

        binding.cardTeachers.setOnClickListener {
            findNavController().navigate(R.id.nav_docentes)
        }

        binding.cardGuardians.setOnClickListener {
            findNavController().navigate(R.id.nav_acudientes)
        }

        binding.cardCourses.setOnClickListener {
            findNavController().navigate(R.id.nav_cursos)
        }

        binding.cardSubjects.setOnClickListener {
            findNavController().navigate(R.id.nav_asignaturas)
        }

        binding.cardNews.setOnClickListener {
            findNavController().navigate(R.id.nav_noticias)
        }

        // Grid "Gestión Académica" - click listeners
        binding.gridAcademic.getChildAt(0)?.setOnClickListener {
            // Gestionar Estudiantes
            findNavController().navigate(R.id.nav_estudiantes)
        }

        binding.gridAcademic.getChildAt(1)?.setOnClickListener {
            // Gestionar Docentes
            findNavController().navigate(R.id.nav_docentes)
        }

        binding.gridAcademic.getChildAt(2)?.setOnClickListener {
            // Gestionar Acudientes
            findNavController().navigate(R.id.nav_acudientes)
        }

        binding.gridAcademic.getChildAt(3)?.setOnClickListener {
            // Gestionar Cursos
            findNavController().navigate(R.id.nav_cursos)
        }

        binding.gridAcademic.getChildAt(4)?.setOnClickListener {
            // Gestionar Asignaturas
            findNavController().navigate(R.id.nav_asignaturas)
        }


        binding.cardCalendario.setOnClickListener {
            findNavController().navigate(R.id.nav_eventos)
        }

        // Comunicación y Reportes - click listeners
        binding.cardReportes.setOnClickListener {
            Toast.makeText(requireContext(), "Generar Reportes - Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
