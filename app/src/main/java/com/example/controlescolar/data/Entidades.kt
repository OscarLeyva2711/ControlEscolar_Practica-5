package com.example.controlescolar.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "carreras")
data class Carrera(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String
)

@Entity(
    tableName = "materias",
    foreignKeys = [
        ForeignKey(
            entity = Carrera::class,
            parentColumns = ["id"],
            childColumns = ["carreraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("carreraId")]
)
data class Materia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carreraId: Long,
    val nombre: String
)

@Entity(
    tableName = "alumnos",
    foreignKeys = [
        ForeignKey(
            entity = Carrera::class,
            parentColumns = ["id"],
            childColumns = ["carreraId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["matricula"], unique = true),
        Index("carreraId")
    ]
)
data class Alumno(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val matricula: String,
    val correo: String,
    val semestre: Int,
    val carreraId: Long
)

@Entity(
    tableName = "calificaciones",
    foreignKeys = [
        ForeignKey(
            entity = Alumno::class,
            parentColumns = ["id"],
            childColumns = ["alumnoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Materia::class,
            parentColumns = ["id"],
            childColumns = ["materiaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["alumnoId", "materiaId"], unique = true),
        Index("alumnoId"),
        Index("materiaId")
    ]
)
data class Calificacion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alumnoId: Long,
    val materiaId: Long,
    val valor: Double
)

data class AlumnoConCarrera(
    val id: Long,
    val nombre: String,
    val matricula: String,
    val correo: String,
    val semestre: Int,
    val carreraId: Long,
    val carreraNombre: String
)

data class MateriaConCalificacion(
    val materiaId: Long,
    val materiaNombre: String,
    val calificacionId: Long?,
    val valor: Double?
)
