package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentGuardianListBinding

class GuardianListFragment : Fragment() {

    private var _binding: FragmentGuardianListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: GuardianAdapter
    private val guardians = mutableListOf<Guardian>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuardianListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupListeners()
        loadGuardians()
    }

    private fun setupRecyclerView() {
        adapter = GuardianAdapter(guardians) { guardian ->
            // Handle edit click
            Toast.makeText(requireContext(), "Editar: ${guardian.user.nombre}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewGuardians.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GuardianListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddGuardian.setOnClickListener {
            val intent = Intent(requireContext(), CreateUserActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadGuardians() {
        showLoading(true)

        db.collection("users")
            .whereEqualTo("rol", "Acudiente")
            .get()
            .addOnSuccessListener { documents ->
                guardians.clear()
                for (document in documents) {
                    val user = User(
                        id = document.getString("id") ?: "",
                        nombre = document.getString("nombre") ?: "",
                        apellido = document.getString("apellido") ?: "",
                        rol = document.getString("rol") ?: "",
                        telefono = document.getString("telefono") ?: "",
                        Direccion = document.getString("Direccion") ?: "",
                        correo = document.getString("correo") ?: ""
                    )

                    val guardian = Guardian(
                        parentesco = document.getString("parentesco") ?: "",
                        user = user
                    )
                    guardians.add(guardian)
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (guardians.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay acudientes registrados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al cargar acudientes: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewGuardians.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadGuardians()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
