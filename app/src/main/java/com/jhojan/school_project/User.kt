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