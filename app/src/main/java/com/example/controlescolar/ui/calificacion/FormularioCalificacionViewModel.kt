package com.example.controlescolar.ui.calificacion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlescolar.data.Calificacion
import com.example.controlescolar.data.MateriaConCalificacion
import com.example.controlescolar.data.Repositorio
import com.example.controlescolar.data.ResultadoCalificacion
import com.example.controlescolar.data.Validaciones
import com.example.controlescolar.ui.navigation.Argumentos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioCalificacionUiState(
    val cargando: Boolean = true,
    val esEdicion: Boolean = false,
    val alumnoNombre: String = "",
    /** Registro: solo materias de la carrera del alumno que aún no tienen calificación. */
    val materiasPendientes: List<MateriaConCalificacion> = emptyList(),
    val materiaId: Long? = null,
    /** Edición: materia fija (solo lectura). */
    val materiaNombre: String = "",
    val valor: String = "",
    val errorMateria: String? = null,
    val errorValor: String? = null,
    val guardando: Boolean = false,
    val finalizado: Boolean = false,
    val mensaje: String? = null
) {
    val materiaSeleccionada: String?
        get() = materiasPendientes.firstOrNull { it.materiaId == materiaId }?.materiaNombre
    val sinMateriasPendientes: Boolean
        get() = !esEdicion && materiasPendientes.isEmpty()
}

class FormularioCalificacionViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositorio: Repositorio
) : ViewModel() {

    private val alumnoId: Long = savedStateHandle.get<Long>(Argumentos.ALUMNO_ID) ?: -1L
    private val calificacionId: Long = savedStateHandle.get<Long>(Argumentos.CALIFICACION_ID) ?: -1L
    private val materiaIdInicial: Long = savedStateHandle.get<Long>(Argumentos.MATERIA_ID) ?: -1L
    private val esEdicion = calificacionId > 0

    private val _uiState = MutableStateFlow(FormularioCalificacionUiState(esEdicion = esEdicion))
    val uiState: StateFlow<FormularioCalificacionUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val alumno = repositorio.obtenerAlumno(alumnoId)
            if (alumno == null) { // el alumno ya no existe: se cierra la pantalla
                _uiState.update { it.copy(cargando = false, finalizado = true) }
                return@launch
            }
            val materias = repositorio.materiasDeAlumno(alumnoId)

            if (esEdicion) {
                val calificacion = repositorio.obtenerCalificacion(calificacionId)
                val materia = materias.firstOrNull { it.calificacionId == calificacionId }
                if (calificacion == null || materia == null) {
                    _uiState.update { it.copy(cargando = false, finalizado = true) }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        cargando = false,
                        alumnoNombre = alumno.nombre,
                        materiaId = materia.materiaId,
                        materiaNombre = materia.materiaNombre,
                        valor = Validaciones.formato2Decimales(calificacion.valor)
                    )
                }
            } else {
                val pendientes = materias.filter { it.calificacionId == null }
                val preseleccion = materiaIdInicial.takeIf { id -> pendientes.any { it.materiaId == id } }
                    ?: pendientes.singleOrNull()?.materiaId
                _uiState.update {
                    it.copy(
                        cargando = false,
                        alumnoNombre = alumno.nombre,
                        materiasPendientes = pendientes,
                        materiaId = preseleccion
                    )
                }
            }
        }
    }

    fun onMateria(id: Long) = _uiState.update { it.copy(materiaId = id, errorMateria = null) }
    fun onValor(v: String) = _uiState.update { it.copy(valor = v, errorValor = null) }
    fun mensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun guardar() {
        val s = _uiState.value
        if (s.guardando || s.cargando || s.finalizado) return // evita envíos repetidos

        val errMateria = Validaciones.materia(s.materiaId)
        val errValor = Validaciones.calificacion(s.valor)
        if (errMateria != null || errValor != null) {
            _uiState.update { it.copy(errorMateria = errMateria, errorValor = errValor) }
            return
        }

        val materiaId = checkNotNull(s.materiaId)
        _uiState.update { it.copy(guardando = true, errorMateria = null, errorValor = null) }

        viewModelScope.launch {
            val calificacion = Calificacion(
                id = if (esEdicion) calificacionId else 0L,
                alumnoId = alumnoId,
                materiaId = materiaId,
                valor = Validaciones.aDouble(s.valor)
            )
            when (val r = repositorio.guardarCalificacion(calificacion)) {
                ResultadoCalificacion.Exito ->
                    _uiState.update { it.copy(guardando = false, finalizado = true) }

                ResultadoCalificacion.Duplicada ->
                    _uiState.update {
                        it.copy(guardando = false, errorMateria = "Ya existe una calificación para esta materia")
                    }

                is ResultadoCalificacion.Error ->
                    _uiState.update { it.copy(guardando = false, mensaje = r.mensaje) }
            }
        }
    }
}
