package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityCreateUserBinding

class CreateUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Campos específicos por rol
    private val roleSpecificFields = mutableMapOf<String, TextInputLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRoleSpinner()
        setupListeners()
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Seleccionar rol", "Estudiante", "Acudiente", "Profesor", "Administrativo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRol.adapter = adapter

        binding.spinnerRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRole = roles[position]
                updateRoleSpecificFields(selectedRole)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateRoleSpecificFields(role: String) {
        // Limpiar campos anteriores
        binding.containerRoleSpecific.removeAllViews()
        roleSpecificFields.clear()

        when (role) {
            "Estudiante" -> {
                addTextField("grado", "Grado")
                addTextField("grupo", "Grupo")
            }
            "Acudiente" -> {
                addTextField("parentezco", "Parentezco")
            }
            "Profesor" -> {
                addTextField("departamento", "Departamento")
                addTextField("asignatura", "Asignatura")
            }
            "Administrativo" -> {
                addTextField("cargo", "Cargo")
            }
        }
    }

    private fun addTextField(fieldId: String, hint: String) {
        val textInputLayout = TextInputLayout(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 32
            }
            this.hint = hint
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }

        val editText = TextInputEditText(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        textInputLayout.addView(editText)
        binding.containerRoleSpecific.addView(textInputLayout)
        roleSpecificFields[fieldId] = textInputLayout
    }

    private fun setupListeners() {
        binding.btnCreateUser.setOnClickListener {
            createUser()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createUser() {
        // Validar campos básicos
        val nombre = binding.etNombre.text.toString().trim()
        val apellido = binding.etApellido.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val rol = binding.spinnerRol.selectedItem.toString()
        val telefono = binding.etTelefono.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()

        if (!validateBasicFields(nombre, apellido, email, password, rol, telefono, direccion)) {
            return
        }

        // Obtener campos específicos del rol
        val roleData = mutableMapOf<String, String>()
        roleSpecificFields.forEach { (key, layout) ->
            val editText = layout.editText
            val value = editText?.text.toString().trim()
            if (value.isEmpty()) {
                layout.error = "Este campo es requerido"
                return
            }
            roleData[key] = value
        }

        showLoading(true)

        // Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    // Crear documento en Firestore
                    val userData = hashMapOf(
                        "id" to uid,
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "rol" to rol,
                        "telefono" to telefono,
                        "Direccion" to direccion
                    )

                    // Agregar campos específicos del rol
                    roleData.forEach { (key, value) ->
                        userData[key] = value
                    }

                    db.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            showLoading(false)
                            Toast.makeText(
                                this,
                                "Usuario creado exitosamente",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            Toast.makeText(
                                this,
                                "Error al guardar datos: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            // Eliminar usuario de Auth si falla Firestore
                            result.user?.delete()
                        }
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Error al obtener UID", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al crear usuario: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateBasicFields(
        nombre: String,
        apellido: String,
        email: String,
        password: String,
        rol: String,
        telefono: String,
        direccion: String
    ): Boolean {
        var isValid = true

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es requerido"
            isValid = false
        } else {
            binding.tilNombre.error = null
        }

        if (apellido.isEmpty()) {
            binding.tilApellido.error = "El apellido es requerido"
            isValid = false
        } else {
            binding.tilApellido.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "El correo es requerido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (rol == "Seleccionar rol") {
            Toast.makeText(this, "Debe seleccionar un rol", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (telefono.isEmpty()) {
            binding.tilTelefono.error = "El teléfono es requerido"
            isValid = false
        } else {
            binding.tilTelefono.error = null
        }

        if (direccion.isEmpty()) {
            binding.tilDireccion.error = "La dirección es requerida"
            isValid = false
        } else {
            binding.tilDireccion.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCreateUser.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}