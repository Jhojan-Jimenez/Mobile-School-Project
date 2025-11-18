package com.jhojan.school_project

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tuapp.activities.TareasActivity
import com.tuapp.activities.AsignaturasActivity
import com.tuapp.activities.PerfilActivity

class TeacherBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class NavigationItem {
        INICIO, TAREAS, ASIGNATURAS, PERFIL
    }

    private var navInicio: LinearLayout
    private var navTareas: LinearLayout
    private var navAsignaturas: LinearLayout
    private var navPerfil: LinearLayout

    private var iconInicio: ImageView
    private var iconTareas: ImageView
    private var iconAsignaturas: ImageView
    private var iconPerfil: ImageView

    private var textInicio: TextView
    private var textTareas: TextView
    private var textAsignaturas: TextView
    private var textPerfil: TextView

    private var currentItem: NavigationItem = NavigationItem.INICIO

    private val colorAzul: Int
    private val colorGris: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.activity_teacher_bottom_navigation, this, true)

        // Inicializar colores
        colorAzul = ContextCompat.getColor(context, R.color.azul)
        colorGris = ContextCompat.getColor(context, R.color.gray)

        // Inicializar vistas
        navInicio = findViewById(R.id.navInicio)
        navTareas = findViewById(R.id.navTareas)
        navAsignaturas = findViewById(R.id.navAsignaturas)
        navPerfil = findViewById(R.id.navPerfil)

        iconInicio = findViewById(R.id.iconInicio)
        iconTareas = findViewById(R.id.iconTareas)
        iconAsignaturas = findViewById(R.id.iconAsignaturas)
        iconPerfil = findViewById(R.id.iconPerfil)

        textInicio = findViewById(R.id.textInicio)
        textTareas = findViewById(R.id.textTareas)
        textAsignaturas = findViewById(R.id.textAsignaturas)
        textPerfil = findViewById(R.id.textPerfil)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        navInicio.setOnClickListener {
            if (currentItem != NavigationItem.INICIO) {
                val intent = Intent(context, TeacherPanelActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        }

        navTareas.setOnClickListener {
            if (currentItem != NavigationItem.TAREAS) {
                val intent = Intent(context, TareasActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        }

        navAsignaturas.setOnClickListener {
            if (currentItem != NavigationItem.ASIGNATURAS) {
                val intent = Intent(context, AsignaturasActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        }

        navPerfil.setOnClickListener {
            if (currentItem != NavigationItem.PERFIL) {
                val intent = Intent(context, PerfilActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        }
    }

    /**
     * Establece el ítem activo en el footer
     */
    fun setActiveItem(item: NavigationItem) {
        currentItem = item

        // Resetear todos los ítems a gris
        resetAllItems()

        // Activar el ítem seleccionado
        when (item) {
            NavigationItem.INICIO -> {
                iconInicio.setColorFilter(colorAzul)
                textInicio.setTextColor(colorAzul)
            }
            NavigationItem.TAREAS -> {
                iconTareas.setColorFilter(colorAzul)
                textTareas.setTextColor(colorAzul)
            }
            NavigationItem.ASIGNATURAS -> {
                iconAsignaturas.setColorFilter(colorAzul)
                textAsignaturas.setTextColor(colorAzul)
            }
            NavigationItem.PERFIL -> {
                iconPerfil.setColorFilter(colorAzul)
                textPerfil.setTextColor(colorAzul)
            }
        }
    }

    private fun resetAllItems() {
        iconInicio.setColorFilter(colorGris)
        iconTareas.setColorFilter(colorGris)
        iconAsignaturas.setColorFilter(colorGris)
        iconPerfil.setColorFilter(colorGris)

        textInicio.setTextColor(colorGris)
        textTareas.setTextColor(colorGris)
        textAsignaturas.setTextColor(colorGris)
        textPerfil.setTextColor(colorGris)
    }

    /**
     * Listener opcional para capturar eventos de navegación personalizados
     */
    fun setOnNavigationItemSelectedListener(listener: (NavigationItem) -> Unit) {
        navInicio.setOnClickListener {
            listener(NavigationItem.INICIO)
            setActiveItem(NavigationItem.INICIO)
        }

        navTareas.setOnClickListener {
            listener(NavigationItem.TAREAS)
            setActiveItem(NavigationItem.TAREAS)
        }

        navAsignaturas.setOnClickListener {
            listener(NavigationItem.ASIGNATURAS)
            setActiveItem(NavigationItem.ASIGNATURAS)
        }

        navPerfil.setOnClickListener {
            listener(NavigationItem.PERFIL)
            setActiveItem(NavigationItem.PERFIL)
        }
    }
}