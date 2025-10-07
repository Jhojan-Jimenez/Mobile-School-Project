package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Al entrar a este fragment, abrir la Activity de Settings
        val intent = Intent(requireContext(), SettingsActivity::class.java)
        startActivity(intent)

        // Volver al fragment anterior
        requireActivity().onBackPressedDispatcher.onBackPressed()

        return null
    }
}
