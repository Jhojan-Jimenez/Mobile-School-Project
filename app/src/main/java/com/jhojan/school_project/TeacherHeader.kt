package com.jhojan.school_project

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.R

class TeacherHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var ivBackArrow: ImageView
    private var imgTeacher: ImageView
    private var tvTeacherName: TextView
    private var tvTeacherRole: TextView

    private var onBackClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.activity_teacher_header, this, true)

        ivBackArrow = findViewById(R.id.ivBackArrow)
        imgTeacher = findViewById(R.id.imgTeacher)
        tvTeacherName = findViewById(R.id.tvTeacherName)
        tvTeacherRole = findViewById(R.id.tvTeacherRole)

        // Configurar click en la flecha de regreso
        ivBackArrow.setOnClickListener {
            onBackClickListener?.invoke()
        }
    }

    /**
     * Configura el header con los datos del profesor desde Firestore
     * @param profesorId ID del profesor en Firestore (ej: "prof_001")
     */
    fun loadTeacherData(profesorId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("profesores")
            .whereEqualTo("id", profesorId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val profesor = documents.documents[0]
                    val nombre = profesor.getString("nombre") ?: "Profesor"
                    val fotoUrl = profesor.getString("foto_perfil")

                    tvTeacherName.text = nombre

                    // Cargar imagen si existe URL, sino usar imagen por defecto
                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(fotoUrl)
                            .placeholder(R.drawable.default_teacher_avatar)
                            .error(R.drawable.default_teacher_avatar)
                            .circleCrop()
                            .into(imgTeacher)
                    } else {
                        imgTeacher.setImageResource(R.drawable.default_teacher_avatar)
                    }
                } else {
                    // No se encontró el profesor
                    tvTeacherName.text = "Profesor"
                    imgTeacher.setImageResource(R.drawable.default_teacher_avatar)
                }
            }
            .addOnFailureListener { error ->
                // Error al cargar datos
                tvTeacherName.text = "Profesor"
                imgTeacher.setImageResource(R.drawable.default_teacher_avatar)
            }
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
                .circleCrop()
                .into(imgTeacher)
        } else {
            imgTeacher.setImageResource(R.drawable.default_teacher_avatar)
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