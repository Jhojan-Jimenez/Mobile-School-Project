package com.jhojan.school_project

data class User(
    val id: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val rol: String = "",
    val telefono: String = "",
    val Direccion: String = "",
    val correo: String = ""
)

data class Student(
    val grado: String = "",
    val grupo: String = "",
    val user: User = User()
)
data class Guardian(
    val parentesco: String ="",
    val user: User = User()
)
data class Teacher(
    val departamento:String="",
    val asignatura:String="",
    val user: User = User()
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
    val grado: String = "",
    val grupo: String = "",
    val descripcion: String = ""
)

data class Event(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val lugar: String = ""
)

data class Subject(
    val id: String = "",
    val nombre: String = "",
    val codigo: String = "",
    val area: String = ""
)

data class News(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val fecha: String = "",
    val autor: String = ""
)