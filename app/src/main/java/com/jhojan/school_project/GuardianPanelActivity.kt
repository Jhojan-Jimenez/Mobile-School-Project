package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityGuardianPanelBinding

class GuardianPanelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardianPanelBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // --- Demo data models ---
    data class TaskItem(val title: String, val subtitle: String, val imageUrl: String)
    data class GradeItem(val subject: String, val average: String, val imageUrl: String)
    data class LinkItem(val title: String, val subtitle: String, val imageUrl: String, val onClick: () -> Unit = {})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Toolbar
        binding.topAppBar.setNavigationOnClickListener {
            // abrir drawer/menú si lo tienes
        }
        binding.topAppBar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_settings) {
                Toast.makeText(this, "Abrir ajustes", Toast.LENGTH_SHORT).show()
                true
            } else false
        }

        // Hero image (del HTML original)
        val heroUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCqFTQWsfb53VRGgy5aVAyohjXzBzZU0r2yZ9HntqKPlIv8XjumI5tWud_0Tc5yV6O8uhdxYS988ZKqWsONP898zMo5muyZMXhd82GHrdusZeQ8GAx8-ark208xO_kOZsAwxBolKJOYEcqR_5Kh6XeSB9g07FG4XK6E80T5RU_7fupAbc4LbQ_uqXsCGLqDa63Tv59i4fkuU7PnSNCD6b_B4Mzfobn4PgSOLMpEBJuKei2U2o15ilK8sB30pgVM10z8KeW90JSkOgA"
        Glide.with(this).load(heroUrl).into(binding.imgHero)

        // Spinner (selector de hijo)
        val children = listOf("", "two", "three") // reemplaza con tus datos reales
        binding.spChildSelector.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            children
        )

        setupRecyclerViews()
        loadUserData()
        setupListeners()
        setupBottomNav()
    }

    private fun setupRecyclerViews() {
        // --- TAREAS PENDIENTES ---
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
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = TaskAdapter(tasks)

        // --- PROMEDIOS ---
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
        binding.rvGrades.layoutManager = LinearLayoutManager(this)
        binding.rvGrades.adapter = GradeAdapter(grades)

        // --- SEGUIMIENTO ACADÉMICO ---
        val academic = listOf(
            LinkItem(
                "Calendario Académico",
                "Ver fechas importantes",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuClbyyk5SpOCZrMDGt2UOnEvn3nXH9j1CfCUY2WAWLiT6tMQFqMgBZC8SJWbjnQKr0ddOT7F31LEa9XG-Fp_kNZ8h58DGMM1GHPKrlBS_vW4cuEF1GUEfatwawjIOKQ8d7Cr8H6VJZOXkloolFuI8xODcvXnbXL2A_pDAeisvCMOY8eCUyPC0hR-OzkZYRC7xGmfMK4Ec6-HmDDfYD5crJDeBKj2fjGdTmbpuKq6JUNYZ_x4bkp8Gw1BQtLEH12M3vT1IMv54CZKcc"
            ) { Toast.makeText(this, "Abrir calendario", Toast.LENGTH_SHORT).show() },
            LinkItem(
                "Consultar TAreas",
                "Revisar tareas asignadas",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAKgEDYzfZzXtV1Os1FTlmYnmv3WvaahBJ07-XeU8D5kLpKfN8-4eIIyMb-Ho_17i2Et0dQYC16ge5sRbMLKBOZkPC6CKfT_fQyweZGp0w6ylK8r_c-1MjakShjZ0P2RfwVBJmb7BIhD8-npvVKn1g91LSJaknbllOKIiETm2ZGM_yy6v9x4FPRhq89CfPgRocI9jNTcGfnH5Z5swUvW6JySiyiiB49M3ZmaZ8C2y4rfD9CHu0pkPtVeK3ZCCQywgPG4HiMg84l6Dg"
            ) { Toast.makeText(this, "Abrir tareas", Toast.LENGTH_SHORT).show() },
            LinkItem(
                "Ver Notas Generales",
                "Consultar calificaciones",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAsnLQ8rJgri9bINKlEtoPc6LqVf5HIxx6-6I7-xfXHcPXioighpy1hvwuhF_Hsav3zgOZr4Ue6mucPOPkXFxCmW1quMaNi0SCPXbs50ZiPNYgfQo483mLeAz_8o8wnoM9DA-0Bq5Ye9-unoKoln-r5p9K6L4WRaRT9frhwO_dDHDaFLJ5ACRqZU6NBmx69Tpx3Inc6IVkpLa7TQQTValGFBFz5ZHgqFzg10spfKKuwetSe0M3yPZfEb-SiScVQ8FtIjPKvDGgGnYA"
            ) { Toast.makeText(this, "Abrir notas", Toast.LENGTH_SHORT).show() },
            LinkItem(
                "Contactar Profesores",
                "Comunicarse con docentes",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuC3qd5PPF1xPbv-wWG8qU8SUZ8PyCY6WXXJQtt6uVLE9VVBvQ2gQhZHuk4m9FwyiOJ6qHLPRqRQJRszD0cCAx4Ob5bDCa_D6MYYLStM7n4eCkxED250_t32dh8jqD1iWFYbDqEoYpPmi0Emescsr74BQEH9J0lt2kqEVqO63e4OnA-i9fGk49FoeHTJ7BQWxeKJEX9_Jw3Ok5-tzRcEWJmDegSfGxD32FiUpDGUpJvSoqbwjT4d5lJ5QzvET9veqfoYqXNvAE7cFB8"
            ) { Toast.makeText(this, "Abrir contactos", Toast.LENGTH_SHORT).show() }
        )
        binding.rvAcademic.layoutManager = LinearLayoutManager(this)
        binding.rvAcademic.adapter = LinkAdapter(academic)

        // --- CONFIGURACIÓN ACADÉMICA ---
        val settings = listOf(
            LinkItem(
                "Historial de Reportes",
                "Ver reportes anteriores",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDaMqZrnCh2CKQ9kpNwnZ6gAsV_rK3zz-uB-u0_wWuqlGCQ31cC6vo8Ib_Or4LUCeL1iMsyxVupQzLwHPpxLH7PgqqL8VRFF-ymf4BBSqRlrsCpjDO7fTB8j9aKLeDN3IZixlV0-yN00uizk6x1E-nZso8TGX9NX8UeoPrLmdsj2GIkfKf2x1GP8kaJJewwni9H5iZ39lu_kFOUMCXAaUKYgUMnTD3g3kDzDtEV1G9iaemu3vQLs7bJa5n3HYSYo0NFra90U0H4z64"
            ) { Toast.makeText(this, "Abrir reportes", Toast.LENGTH_SHORT).show() },
            LinkItem(
                "Crear Reclamo",
                "Presentar una queja",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuCESmhWiq2azF74Ari4A34oi3o5hU-AYdtT-25seSo7lPGvxEYwhhzk83jO6sjNDpokG4sP14d-4dYC4pLhaAgirfaUkuTsRtzbcgPt3s-veTbfy7JWBSduEY0RvBG9r3xfunV3_WCfCCGnJW7VBaZ8fm7-zQ8uyqX1uwa-PMg4JGpAqSlWvDPd6kWpiEMoyW6H83fiCbSi6EJXMvopuNy5RnbG89tT8wF0V5Ak7KMPgPdzmnQFB88wWgOdPwPEwzbXHWUk4ISMIQ4"
            ) { Toast.makeText(this, "Abrir reclamo", Toast.LENGTH_SHORT).show() },
            LinkItem(
                "Cambiar Información de Contacto",
                "Actualizar datos personales",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAyDJyHhwEtz1Mco2PqYucLEOWEsHukCLeRFp0ZrY6WT5iY8TicNiAPX1I6cha1cQ_sI0821WWrNkvXc3P1SlzZQD9b8deWsBqZosm1j9b58xbGY7JULQEVbFx-UHN9cwGE0t5JjWjdQi41lH7kTTvnBsKIbehAuOpTtAyfjvGhWnsHcmVIEkc49D90kkFPznKae1VZEKexrEF3M-Us0Oq71PGeY05QU61GTw-UQjVdwCtaCsQx872xq99IgVQThoDmwYiBT5ZfTdk"
            ) { Toast.makeText(this, "Abrir contacto", Toast.LENGTH_SHORT).show() }
        )
        binding.rvSettings.layoutManager = LinearLayoutManager(this)
        binding.rvSettings.adapter = LinkAdapter(settings)
    }

    private fun setupBottomNav() {
        // marca "Perfil" como seleccionado por defecto
        binding.bottomNav.selectedItemId = R.id.nav_profile
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_profile -> true
                R.id.nav_support -> { Toast.makeText(this, "Soporte", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_bell -> { Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_configuraciones -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    // --- Tu código original de Firebase (sin cambios) ---
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
                            binding.tvUserName.text = "${user.nombre} ${user.apellido}"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GuardianPanel", "Error al cargar usuario", e)
                }
        }
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // --- Adapters ---
    inner class TaskAdapter(private val items: List<TaskItem>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<TaskAdapter.VH>() {

        inner class VH(val v: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
            val title = v.findViewById<android.widget.TextView>(R.id.tvTaskTitle)
            val subtitle = v.findViewById<android.widget.TextView>(R.id.tvTaskSubtitle)
            val img = v.findViewById<android.widget.ImageView>(R.id.imgTask)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
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

        inner class VH(val v: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
            val subject = v.findViewById<android.widget.TextView>(R.id.tvGradeSubject)
            val value = v.findViewById<android.widget.TextView>(R.id.tvGradeValue)
            val img = v.findViewById<android.widget.ImageView>(R.id.imgGrade)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
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

        inner class VH(val v: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
            val title = v.findViewById<android.widget.TextView>(R.id.tvLinkTitle)
            val subtitle = v.findViewById<android.widget.TextView>(R.id.tvLinkSubtitle)
            val img = v.findViewById<android.widget.ImageView>(R.id.imgLink)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
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
}
