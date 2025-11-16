package com.jhojan.school_project

data class User(
    val id: String = "",
    val nombre_completo: String = "",
    val rol: String = "",
    val telefono: String = "",
    val correo: String = "",
    val activo: Boolean = true,
    // Campos legacy para compatibilidad temporal
    val nombre: String = "",
    val apellido: String = "",
    val Direccion: String = ""
) {
    // Computed property para obtener nombre completo si existe, o construir desde nombre+apellido
    val nombreCompleto: String
        get() = nombre_completo.ifEmpty { "$nombre $apellido".trim() }
}

data class Student(
    val grado: String = "",
    val grupo: String = "",
    val user: User = User()
)
data class Guardian(
    val direccion: String = "",
    val user: User = User(),
    // Campo legacy para compatibilidad temporal
    val parentesco: String = ""
)
data class Teacher(
    val especialidad: String = "",
    val user: User = User(),
    // Campos legacy para compatibilidad temporal
    val departamento: String = "",
    val asignatura: String = ""
)

data class Clase(
    val id: String = "",
    val nombre: String = "",
    val asignatura: String = "",
    val grado: String = "",
    val grupo: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val horario: List<String> = listOf(), // ["Lunes", "Miércoles", "Viernes"]
    val año: String = ""
)

data class EstudianteAsistencia(
    val studentId: String = "",
    val studentName: String = "",
    val status: String = "" // "presente", "ausente", "tarde", "excusado"
)

data class SesionAsistencia(
    val id: String = "",
    val claseId: String = "",
    val date: String = "",
    val timestamp: Long = 0,
    val teacherId: String = "",
    val students: List<EstudianteAsistencia> = listOf()
)

data class Course(
    val id: String = "",
    val nombre: String = "",
    val codigo: String = ""
)

data class Event(
    val id: String = "",
    val titulo: String = "",
    val alcance: String = "", // "Colegio", "Curso", "Asignatura", "Estudiante"
    val curso_id: String = "",
    val curso_nombre: String = "",
    val asignatura_id: String = "",
    val asignatura_nombre: String = "",
    val estudiante_id: String = "",
    val estudiante_nombre: String = ""
)

data class Subject(
    val id: String = "",
    val nombre: String = "",
    val codigo: String = "",
    val curso_id: String = "",
    val curso_nombre: String = "" // Para mostrar el nombre del curso en la UI
)

data class News(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val fecha: String = "",
    val autor: String = ""
)