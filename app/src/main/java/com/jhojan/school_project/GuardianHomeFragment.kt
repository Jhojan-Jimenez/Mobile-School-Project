package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentGuardianHomeBinding

class GuardianHomeFragment : Fragment() {

    private var _binding: FragmentGuardianHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    data class TaskItem(val title: String, val subtitle: String, val imageUrl: String)
    data class GradeItem(val subject: String, val average: String, val imageUrl: String)
    data class LinkItem(val title: String, val subtitle: String, val imageUrl: String, val onClick: () -> Unit = {})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuardianHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUI()
        setupRecyclerViews()
        loadUserData()
        setupListeners()
    }

    private fun setupUI() {
        val heroUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCqFTQWsfb53VRGgy5aVAyohjXzBzZU0r2yZ9HntqKPlIv8XjumI5tWud_0Tc5yV6O8uhdxYS988ZKqWsONP898zMo5muyZMXhd82GHrdusZeQ8GAx8-ark208xO_kOZsAwxBolKJOYEcqR_5Kh6XeSB9g07FG4XK6E80T5RU_7fupAbc4LbQ_uqXsCGLqDa63Tv59i4fkuU7PnSNCD6b_B4Mzfobn4PgSOLMpEBJuKei2U2o15ilK8sB30pgVM10z8KeW90JSkOgA"
        Glide.with(this).load(heroUrl).into(binding.imgHero)

        val children = listOf("Hijo 1", "Hijo 2", "Hijo 3")
        binding.spChildSelector.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            children
        )
    }

    private fun setupRecyclerViews() {
        // Tareas Pendientes
        val tasks = listOf(
            TaskItem(
                "Matemáticas",
                "Resolver ecuaciones cuadráticas",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuCR8C2pGi0Ipd8XrcjDwHhqeAd82_ryp_poxj0KWAF5vTDSkzQjZvPYaq4e0ERYTtcpPGyzRe4ex124rT__MS8jo5KUq8DW9TcTfy25B4yBe_70EjiZ5BB_AZk7gamFnp0AFEU_E7JHmZD32u5ZaVGxORrFEGbJwJHpQA6JrPrnc2Mv-g220sez2PBLOMl7jt0lZLq70Kgu_FIomMpL37IzcMYXyPySlua79rjGpDxa32gpzTowBNi4YlMwEdI618f_mxcv2tMQBoM"
            ),
            TaskItem(
                "Historia",
                "Leer capítulo sobre la Revolución Francesa",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuCLWG43Y55MMCk1VATJp3U6ZJsLsXdXHpcHl1vyomV9ZgxWkgMRqI5SxWjQnNoUhIPP0yPfQquqjvbqpzVXXF-__lXu21ZYO5gbQBuNsNWAmfFybbomLkIuC1ZXLSdJW_U7kxY4xMmb__0n9Hew05UXsLE3guwxr96zf0gx0Gr67_7kclw6Pzc91yCGAdrTC2hgekHX2QREmKI6r4bCmiCvZnfMij1yhftMePily-gT6dHucITm2M8bR-ksV9OfylzeSKCS9BUx0-o"
            )
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = TaskAdapter(tasks)

        // Promedio de Calificaciones
        val grades = listOf(
            GradeItem(
                "Matemáticas",
                "8.5",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDh8d3JDYLt04pWCc4-5cx9aXN-Y6xo7MCzrwdMTCoAtbeTvZ1gVk3R02y1Bhj23wTEZVlfEinzhIQ7gl72S49t0-7XxPHtz6_7TpbO8oR8yQcd7eEgCndM2A_G8-RRbsdbK1wu7LFUQRQOOF-zPZ94BWghjAf_Md3Ve7pLcUoBKV9cU-pT7-t1QIap2REMPCIJy9w0UZ7RuVCQeogK7j7vosB6Gm2sZkFUyNiY17NcBkdBQoZaf0b8nQrrh-RmSPwDWK8DJgPkBwc"
            ),
            GradeItem(
                "Historia",
                "9.2",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuBf9Aaj_5AxLdsb9ernBuz_t6cZTaNp6xKWcdaHg3vOFlvla4_5Zr833-yzdXFhkva4Qb09GNll7gr_svgMJm_AlSEVS9gReH3OONp4rFcdggpBqgwI9Q2D6QWfDclO0OfS1WxEBs8_2TUwekUAQ0ng-4_JjRg5OuswS-OxQCZAvpq0qU8oDMi9FuQq7n01baowxZ9s1pirRcynUxIcRLGtXcHyFNzqIzIu8mRMxDg94tfX450wWNflLl2twKXiwlsW5EVBDonntiU"
            )
        )
        binding.rvGrades.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGrades.adapter = GradeAdapter(grades)

        // Seguimiento Académico
        val academic = listOf(
            LinkItem(
                "Calendario Académico",
                "Ver fechas importantes",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuClbyyk5SpOCZrMDGt2UOnEvn3nXH9j1CfCUY2WAWLiT6tMQFqMgBZC8SJWbjnQKr0ddOT7F31LEa9XG-Fp_kNZ8h58DGMM1GHPKrlBS_vW4cuEF1GUEfatwawjIOKQ8d7Cr8H6VJZOXkloolFuI8xODcvXnbXL2A_pDAeisvCMOY8eCUyPC0hR-OzkZYRC7xGmfMK4Ec6-HmDDfYD5crJDeBKj2fjGdTmbpuKq6JUNYZ_x4bkp8Gw1BQtLEH12M3vT1IMv54CZKcc"
            ) { Toast.makeText(requireContext(), "Abrir calendario", Toast.LENGTH_SHORT).show() }
        )
        binding.rvAcademic.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAcademic.adapter = LinkAdapter(academic)

        // Configuración Académica
        val settings = listOf(
            LinkItem(
                "Historial de Reportes",
                "Ver reportes anteriores",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDaMqZrnCh2CKQ9kpNwnZ6gAsV_rK3zz-uB-u0_wWuqlGCQ31cC6vo8Ib_Or4LUCeL1iMsyxVupQzLwHPpxLH7PgqqL8VRFF-ymf4BBSqRlrsCpjDO7fTB8j9aKLeDN3IZixlV0-yN00uizk6x1E-nZso8TGX9NX8UeoPrLmdsj2GIkfKf2x1GP8kaJJewwni9H5iZ39lu_kFOUMCXAaUKYgUMnTD3g3kDzDtEV1G9iaemu3vQLs7bJa5n3HYSYo0NFra90U0H4z64"
            ) { Toast.makeText(requireContext(), "Abrir reportes", Toast.LENGTH_SHORT).show() }
        )
        binding.rvSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSettings.adapter = LinkAdapter(settings)
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
                            binding.tvUserName.text = "Hola, ${user.nombre} ${user.apellido}!"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GuardianHomeFragment", "Error al cargar usuario", e)
                }
        }
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    inner class TaskAdapter(private val items: List<TaskItem>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<TaskAdapter.VH>() {

        inner class VH(val v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
            val title = v.findViewById<android.widget.TextView>(R.id.tvTaskTitle)
            val subtitle = v.findViewById<android.widget.TextView>(R.id.tvTaskSubtitle)
            val img = v.findViewById<android.widget.ImageView>(R.id.imgTask)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = layoutInflater.inflate(R.layout.item_task, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.subtitle.text = item.subtitle
            Glide.with(holder.img).load(item.imageUrl).into(holder.img)
        }

        override fun getItemCount() = items.size
    }

    inner class GradeAdapter(private val items: List<GradeItem>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<GradeAdapter.VH>() {

        inner class VH(val v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
            val subject = v.findViewById<android.widget.TextView>(R.id.tvGradeSubject)
            val value = v.findViewById<android.widget.TextView>(R.id.tvGradeValue)
            val img = v.findViewById<android.widget.ImageView>(R.id.imgGrade)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = layoutInflater.inflate(R.layout.item_grade, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.subject.text = item.subject
            holder.value.text = item.average
            Glide.with(holder.img).load(item.imageUrl).into(holder.img)
        }

        override fun getItemCount() = items.size
    }

    inner class LinkAdapter(private val items: List<LinkItem>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<LinkAdapter.VH>() {

        inner class VH(val v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
            val title = v.findViewById<android.widget.TextView>(R.id.tvLinkTitle)
            val subtitle = v.findViewById<android.widget.TextView>(R.id.tvLinkSubtitle)
            val img = v.findViewById<android.widget.ImageView>(R.id.imgLink)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = layoutInflater.inflate(R.layout.item_link, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.subtitle.text = item.subtitle
            Glide.with(holder.img).load(item.imageUrl).into(holder.img)
            holder.v.setOnClickListener { item.onClick.invoke() }
        }

        override fun getItemCount() = items.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
