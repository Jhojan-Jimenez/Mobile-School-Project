package com.jhojan.school_project.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jhojan.school_project.R
import de.hdodenhof.circleimageview.CircleImageView

class TeacherHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var ivBackArrow: ImageView
    private var ivTeacherPhoto: CircleImageView
    private var tvTeacherName: TextView
    private var tvTeacherRole: TextView

    private var onBackClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.teacher_header, this, true)

        ivBackArrow = findViewById(R.id.ivBackArrow)
        ivTeacherPhoto = findViewById(R.id.ivTeacherPhoto)
        tvTeacherName = findViewById(R.id.tvTeacherName)
        tvTeacherRole = findViewById(R.id.tvTeacherRole)

        // Configurar click en la flecha de regreso
        ivBackArrow.setOnClickListener {
            onBackClickListener?.invoke()
        }
    }

    /**
     * Configura el header con los datos del profesor desde Firebase
     * @param profesorId ID del profesor en Firebase (ej: "prof_001")
     */
    fun loadTeacherData(profesorId: String) {
        val database = FirebaseDatabase.getInstance()
        val profesorRef = database.getReference("profesores/$profesorId")

        profesorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Profesor"
                val fotoUrl = snapshot.child("foto_perfil").getValue(String::class.java)

                tvTeacherName.text = nombre

                // Cargar imagen si existe URL, sino usar imagen por defecto
                if (!fotoUrl.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(fotoUrl)
                        .placeholder(R.drawable.default_teacher_avatar)
                        .error(R.drawable.default_teacher_avatar)
                        .into(ivTeacherPhoto)
                } else {
                    ivTeacherPhoto.setImageResource(R.drawable.default_teacher_avatar)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Mantener valores por defecto en caso de error
                tvTeacherName.text = "Profesor"
                ivTeacherPhoto.setImageResource(R.drawable.default_teacher_avatar)
            }
        })
    }

    /**
     * Configura los datos del profesor manualmente (sin Firebase)
     * @param nombre Nombre del profesor
     * @param fotoUrl URL de la foto o null para usar imagen por defecto
     */
    fun setTeacherData(nombre: String, fotoUrl: String? = null) {
        tvTeacherName.text = nombre

        if (!fotoUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(fotoUrl)
                .placeholder(R.drawable.default_teacher_avatar)
                .error(R.drawable.default_teacher_avatar)
                .into(ivTeacherPhoto)
        } else {
            ivTeacherPhoto.setImageResource(R.drawable.default_teacher_avatar)
        }
    }

    /**
     * Configura el listener para el botón de regreso
     * @param listener Función a ejecutar cuando se presione la flecha
     */
    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClickListener = listener
    }

    /**
     * Cambia el texto del rol (por defecto es "Docente")
     * @param rol Texto del rol a mostrar
     */
    fun setRole(rol: String) {
        tvTeacherRole.text = rol
    }
}