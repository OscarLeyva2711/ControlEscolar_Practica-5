package com.example.controlescolar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarreraDao {
    @Query("SELECT * FROM carreras ORDER BY nombre ASC")
    fun observarTodas(): Flow<List<Carrera>>

    @Query("SELECT * FROM carreras WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Carrera?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(carrera: Carrera): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(carreras: List<Carrera>): List<Long>
}

@Dao
interface MateriaDao {
    @Query("SELECT * FROM materias WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Materia?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(materia: Materia): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(materias: List<Materia>): List<Long>
}

@Dao
interface AlumnoDao {
    @Query("""
        SELECT a.id, a.nombre, a.matricula, a.correo, a.semestre, a.carreraId, c.nombre AS carreraNombre
        FROM alumnos a
        INNER JOIN carreras c ON a.carreraId = c.id
        ORDER BY a.nombre ASC
    """)
    fun observarTodos(): Flow<List<AlumnoConCarrera>>

    @Query("""
        SELECT a.id, a.nombre, a.matricula, a.correo, a.semestre, a.carreraId, c.nombre AS carreraNombre
        FROM alumnos a
        INNER JOIN carreras c ON a.carreraId = c.id
        WHERE a.carreraId = :carreraId
        ORDER BY a.nombre ASC
    """)
    fun observarPorCarrera(carreraId: Long): Flow<List<AlumnoConCarrera>>

    @Query("""
        SELECT a.id, a.nombre, a.matricula, a.correo, a.semestre, a.carreraId, c.nombre AS carreraNombre
        FROM alumnos a
        INNER JOIN carreras c ON a.carreraId = c.id
        WHERE a.id = :id
    """)
    fun observarDetalle(id: Long): Flow<AlumnoConCarrera?>

    @Query("SELECT * FROM alumnos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Alumno?

    @Query("SELECT COUNT(*) FROM alumnos WHERE matricula = :matricula AND id != :id")
    suspend fun contarMatricula(matricula: String, id: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(alumno: Alumno): Long

    @Update
    suspend fun actualizar(alumno: Alumno): Int

    @Delete
    suspend fun eliminar(alumno: Alumno): Int
}

@Dao
interface CalificacionDao {
    @Query("""
        SELECT m.id AS materiaId, m.nombre AS materiaNombre, c.id AS calificacionId, c.valor AS valor
        FROM materias m
        INNER JOIN alumnos a ON m.carreraId = a.carreraId
        LEFT JOIN calificaciones c ON c.materiaId = m.id AND c.alumnoId = a.id
        WHERE a.id = :alumnoId
        ORDER BY m.nombre ASC
    """)
    fun observarMateriasConCalificacion(alumnoId: Long): Flow<List<MateriaConCalificacion>>

    @Query("SELECT COUNT(*) FROM calificaciones WHERE alumnoId = :alumnoId")
    suspend fun contarDeAlumno(alumnoId: Long): Int

    @Query("SELECT * FROM calificaciones WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Calificacion?

    @Query("SELECT COUNT(*) FROM calificaciones WHERE alumnoId = :alumnoId AND materiaId = :materiaId")
    suspend fun contarPorAlumnoYMateria(alumnoId: Long, materiaId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(calificacion: Calificacion): Long

    @Update
    suspend fun actualizar(calificacion: Calificacion): Int

    @Delete
    suspend fun eliminar(calificacion: Calificacion): Int
}
