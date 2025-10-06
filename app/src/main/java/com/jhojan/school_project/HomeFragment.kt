package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jhojan.school_project.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        // Top bar actions
        binding.btnBell.setOnClickListener {
            Toast.makeText(requireContext(), "Notificaciones", Toast.LENGTH_SHORT).show()
        }

        // Grid clicks
        binding.gridAcademic.getChildAt(0)?.setOnClickListener {
            Toast.makeText(requireContext(), "Gestionar Estudiantes", Toast.LENGTH_SHORT).show()
        }
        binding.gridAcademic.getChildAt(1)?.setOnClickListener {
            Toast.makeText(requireContext(), "Gestionar Docentes", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
