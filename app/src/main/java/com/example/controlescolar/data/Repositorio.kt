package com.example.controlescolar.data

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

const val MSG_CARRERA_BLOQUEADA =
    "No se puede cambiar la carrera porque el alumno ya tiene calificaciones registradas."

sealed interface ResultadoAlumno {
    data object Exito : ResultadoAlumno
    data object MatriculaDuplicada : ResultadoAlumno
    data class CarreraBloqueada(val mensaje: String) : ResultadoAlumno
    data class Error(val mensaje: String) : ResultadoAlumno
}

sealed interface ResultadoCalificacion {
    data object Exito : ResultadoCalificacion
    data object Duplicada : ResultadoCalificacion
    data class Error(val mensaje: String) : ResultadoCalificacion
}

class Repositorio(db: AppDatabase) {
    private val carreraDao = db.carreraDao()
    private val materiaDao = db.materiaDao()
    private val alumnoDao = db.alumnoDao()
    private val calificacionDao = db.calificacionDao()

    // ---------- Lecturas observables ----------
    val carreras: Flow<List<Carrera>> = carreraDao.observarTodas()

    /** carreraId == null => todos los alumnos. */
    fun alumnos(carreraId: Long?): Flow<List<AlumnoConCarrera>> =
        if (carreraId == null) alumnoDao.observarTodos() else alumnoDao.observarPorCarrera(carreraId)

    fun detalleAlumno(id: Long): Flow<AlumnoConCarrera?> = alumnoDao.observarDetalle(id)

    fun materiasConCalificacion(alumnoId: Long): Flow<List<MateriaConCalificacion>> =
        calificacionDao.observarMateriasConCalificacion(alumnoId)

    // ---------- Lecturas puntuales ----------
    suspend fun obtenerAlumno(id: Long): Alumno? = alumnoDao.obtenerPorId(id)

    suspend fun tieneCalificaciones(alumnoId: Long): Boolean =
        calificacionDao.contarDeAlumno(alumnoId) > 0

    suspend fun obtenerCalificacion(id: Long): Calificacion? = calificacionDao.obtenerPorId(id)

    suspend fun materiasDeAlumno(alumnoId: Long): List<MateriaConCalificacion> =
        calificacionDao.observarMateriasConCalificacion(alumnoId).first()

    // ---------- Alumnos ----------
    suspend fun guardarAlumno(alumno: Alumno): ResultadoAlumno {
        try {
            if (alumnoDao.contarMatricula(alumno.matricula, alumno.id) > 0)
                return ResultadoAlumno.MatriculaDuplicada
            if (carreraDao.obtenerPorId(alumno.carreraId) == null)
                return ResultadoAlumno.Error("La carrera seleccionada no existe")

            if (alumno.id == 0L) {
                alumnoDao.insertar(alumno)
            } else {
                val actual = alumnoDao.obtenerPorId(alumno.id)
                    ?: return ResultadoAlumno.Error("El alumno ya no existe")
                if (actual.carreraId != alumno.carreraId &&
                    calificacionDao.contarDeAlumno(alumno.id) > 0
                ) return ResultadoAlumno.CarreraBloqueada(MSG_CARRERA_BLOQUEADA)
                alumnoDao.actualizar(alumno)
            }
            return ResultadoAlumno.Exito
        } catch (e: CancellationException) {
            throw e
        } catch (e: SQLiteConstraintException) {
            return ResultadoAlumno.MatriculaDuplicada
        } catch (e: Exception) {
            return ResultadoAlumno.Error("No se pudo guardar el alumno. Intenta de nuevo.")
        }
    }

    /** Las calificaciones se eliminan por ON DELETE CASCADE. */
    suspend fun eliminarAlumno(id: Long): Boolean = try {
        alumnoDao.obtenerPorId(id)?.let { alumnoDao.eliminar(it) }
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }

    // ---------- Calificaciones ----------
    suspend fun guardarCalificacion(c: Calificacion): ResultadoCalificacion {
        try {
            val alumno = alumnoDao.obtenerPorId(c.alumnoId)
                ?: return ResultadoCalificacion.Error("El alumno no existe")
            val materia = materiaDao.obtenerPorId(c.materiaId)
                ?: return ResultadoCalificacion.Error("Selecciona una materia")
            if (materia.carreraId != alumno.carreraId)
                return ResultadoCalificacion.Error("La materia no corresponde a la carrera del alumno")
            if (c.valor.isNaN() || c.valor !in 0.0..100.0)
                return ResultadoCalificacion.Error("La calificación debe estar entre 0 y 100")

            if (c.id == 0L) {
                if (calificacionDao.contarPorAlumnoYMateria(alumno.id, materia.id) > 0)
                    return ResultadoCalificacion.Duplicada
                calificacionDao.insertar(c)
            } else {
                val actual = calificacionDao.obtenerPorId(c.id)
                    ?: return ResultadoCalificacion.Error("La calificación ya no existe")
                // La edición solo cambia el valor: se conservan alumno y materia
                calificacionDao.actualizar(actual.copy(valor = c.valor))
            }
            return ResultadoCalificacion.Exito
        } catch (e: CancellationException) {
            throw e
        } catch (e: SQLiteConstraintException) {
            return ResultadoCalificacion.Duplicada
        } catch (e: Exception) {
            return ResultadoCalificacion.Error("No se pudo guardar la calificación. Intenta de nuevo.")
        }
    }

    suspend fun eliminarCalificacion(id: Long): Boolean = try {
        calificacionDao.obtenerPorId(id)?.let { calificacionDao.eliminar(it) }
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }
}


