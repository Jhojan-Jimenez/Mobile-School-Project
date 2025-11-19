package com.jhojan.school_project

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Calendar

// IMPORTS DE ACTIVITIES AÚN NO CREADAS — COMENTADOS
// import com.jhojan.school_project.StudentHomeActivity
// import com.jhojan.school_project.StudentTareasActivity
import com.jhojan.school_project.CalendarioAcudienteActivity
// import com.jhojan.school_project.StudentPerfilActivity

class ParentBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class NavigationItem {
        HOME, TAREAS, CALENDARIO, PERFIL
    }

    private var navHome: LinearLayout
    private var navInbox: LinearLayout
    private var navCalendario: LinearLayout
    private var navPerfil: LinearLayout

    private var iconHome: ImageView

    private var iconInbox: ImageView
    private var iconCalendario: ImageView
    private var iconPerfil: ImageView

    private var textHome: TextView
    private var textInbox: TextView
    private var textCalendario: TextView
    private var textPerfil: TextView

    private var currentItem: NavigationItem = NavigationItem.HOME

    private val colorAzul: Int
    private val colorGris: Int

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.activity_parent_bottom_navigation_view, this, true)

        // Inicializar colores
        colorAzul = ContextCompat.getColor(context, R.color.azul)
        colorGris = ContextCompat.getColor(context, R.color.gray)

        // Inicializar vistas
        navHome = findViewById(R.id.navHome)
        navInbox = findViewById(R.id.navInbox)
        navCalendario = findViewById(R.id.navCalendario)
        navPerfil = findViewById(R.id.navPerfil)

        iconHome = findViewById(R.id.iconHome)
        iconInbox = findViewById(R.id.iconInbox)
        iconCalendario = findViewById(R.id.iconCalendario)
        iconPerfil = findViewById(R.id.iconPerfil)

        textHome = findViewById(R.id.textHome)
        textInbox = findViewById(R.id.textInbox)
        textCalendario = findViewById(R.id.textCalendario)
        textPerfil = findViewById(R.id.textPerfil)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 🔵 NOTA:
        // Como aún NO has creado las Activities, las líneas con Intent están comentadas.
        // Cuando las crees, descomenta cada bloque completo.

        navHome.setOnClickListener {
            if (currentItem != NavigationItem.HOME) {
                val intent = Intent(context, ParentHomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        }

        navInbox.setOnClickListener {
            if (currentItem != NavigationItem.TAREAS) {
                val intent = Intent(context, EnviarMensajeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        }

        navCalendario.setOnClickListener {
            if (currentItem != NavigationItem.CALENDARIO) {
                val intent = Intent(context, CalendarioAcudienteActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        }

        navPerfil.setOnClickListener {
            if (currentItem != NavigationItem.PERFIL) {
                // val intent = Intent(context, StudentPerfilActivity::class.java)
                // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                // context.startActivity(intent)
            }
        }
    }

    /**
     * Establece el ítem activo en el footer
     */
    fun setActiveItem(item: NavigationItem) {
        currentItem = item

        // Resetear todos los ítems
        resetAllItems()

        // Activar el ítem seleccionado
        when (item) {
            NavigationItem.HOME -> {
                iconHome.setColorFilter(colorAzul)
                textHome.setTextColor(colorAzul)
            }
            NavigationItem.TAREAS -> {
                iconInbox.setColorFilter(colorAzul)
                textInbox.setTextColor(colorAzul)
            }
            NavigationItem.CALENDARIO -> {
                iconCalendario.setColorFilter(colorAzul)
                textCalendario.setTextColor(colorAzul)
            }
            NavigationItem.PERFIL -> {
                iconPerfil.setColorFilter(colorAzul)
                textPerfil.setTextColor(colorAzul)
            }
        }
    }

    private fun resetAllItems() {
        iconHome.setColorFilter(colorGris)
        iconInbox.setColorFilter(colorGris)
        iconCalendario.setColorFilter(colorGris)
        iconPerfil.setColorFilter(colorGris)

        textHome.setTextColor(colorGris)
        textInbox.setTextColor(colorGris)
        textCalendario.setTextColor(colorGris)
        textPerfil.setTextColor(colorGris)
    }

    /**
     * Listener para navegación opcional
     */
    fun setOnNavigationItemSelectedListener(listener: (NavigationItem) -> Unit) {
        navHome.setOnClickListener {
            listener(NavigationItem.HOME)
            setActiveItem(NavigationItem.HOME)
        }

        navInbox.setOnClickListener {
            listener(NavigationItem.TAREAS)
            setActiveItem(NavigationItem.TAREAS)
        }

        navCalendario.setOnClickListener {
            listener(NavigationItem.CALENDARIO)
            setActiveItem(NavigationItem.CALENDARIO)
        }

        navPerfil.setOnClickListener {
            listener(NavigationItem.PERFIL)
            setActiveItem(NavigationItem.PERFIL)
        }
    }
}