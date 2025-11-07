package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.jhojan.school_project.databinding.FragmentStudentHomeBinding

class StudentHomeFragment : Fragment() {

    private var _binding: FragmentStudentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClicks()
    }

    private fun setupUI() {
        binding.tvStudentName.text = "Alex Ramirez"
        binding.tvRole.text = "Estudiante"

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

    private fun setupClicks() {
        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir ajustes", Toast.LENGTH_SHORT).show()
        }
        binding.btnBell.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir notificaciones", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
