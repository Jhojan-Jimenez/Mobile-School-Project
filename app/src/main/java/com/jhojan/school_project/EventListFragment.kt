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
import com.jhojan.school_project.databinding.FragmentEventsListBinding

class EventListFragment : Fragment() {

    private var _binding: FragmentEventsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: EventAdapter
    private val events = mutableListOf<Event>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupListeners()
        loadEvents()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter(events) { event ->
            Toast.makeText(requireContext(), "Editar: ${event.titulo}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@EventListFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddEvent.setOnClickListener {
            val intent = Intent(requireContext(), CreateEventActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadEvents() {
        showLoading(true)

        db.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                events.clear()
                for (document in documents) {
                    val event = Event(
                        id = document.id,
                        titulo = document.getString("titulo") ?: "",
                        alcance = document.getString("alcance") ?: "",
                        curso_id = document.getString("curso_id") ?: "",
                        curso_nombre = document.getString("curso_nombre") ?: "",
                        asignatura_id = document.getString("asignatura_id") ?: "",
                        asignatura_nombre = document.getString("asignatura_nombre") ?: "",
                        estudiante_id = document.getString("estudiante_id") ?: "",
                        estudiante_nombre = document.getString("estudiante_nombre") ?: ""
                    )
                    events.add(event)
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (events.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay eventos registrados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Error al cargar eventos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewEvents.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
