package com.jhojan.school_project

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ParentHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var ivBackArrow: ImageView
    private var imgParent: ImageView
    private var tvParentName: TextView
    private var tvParentRole: TextView

    private var onBackClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.activity_parent_header, this, true)

        ivBackArrow = findViewById(R.id.ivBackArrow)
        imgParent = findViewById(R.id.imgParent)
        tvParentName = findViewById(R.id.tvParentName)
        tvParentRole = findViewById(R.id.tvParentRole)

        // Siempre es ACUDIENTE
        tvParentRole.text = "Acudiente"

        ivBackArrow.setOnClickListener {
            onBackClickListener?.invoke()
        }
    }

    /**
     * Carga la información del ACUDIENTE desde Firestore
     */
    fun loadParentData(parentId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(parentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {

                    val nombre = document.getString("nombre")
                        ?: document.getString("name")
                        ?: "Acudiente"

                    val fotoUrl = document.getString("foto_perfil")
                        ?: document.getString("photoURL")

                    tvParentName.text = nombre

                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(fotoUrl)
                            .placeholder(R.drawable.default_parent_avatar)
                            .error(R.drawable.default_parent_avatar)
                            .circleCrop()
                            .into(imgParent)
                    } else {
                        imgParent.setImageResource(R.drawable.default_parent_avatar)
                    }

                } else {
                    tvParentName.text = "Acudiente"
                    imgParent.setImageResource(R.drawable.default_parent_avatar)
                }
            }
            .addOnFailureListener {
                tvParentName.text = "Acudiente"
                imgParent.setImageResource(R.drawable.default_parent_avatar)
            }
    }

    /**
     * Configura manualmente los datos del acudiente
     */
    fun setParentData(nombre: String, fotoUrl: String? = null) {
        tvParentName.text = nombre

        if (!fotoUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(fotoUrl)
                .placeholder(R.drawable.default_parent_avatar)
                .error(R.drawable.default_parent_avatar)
                .circleCrop()
                .into(imgParent)
        } else {
            imgParent.setImageResource(R.drawable.default_parent_avatar)
        }
    }

    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClickListener = listener
    }
}
