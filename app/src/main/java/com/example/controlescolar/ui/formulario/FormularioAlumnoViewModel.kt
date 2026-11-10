package com.example.controlescolar.ui.formulario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlescolar.data.Alumno
import com.example.controlescolar.data.Carrera
import com.example.controlescolar.data.Repositorio
import com.example.controlescolar.data.ResultadoAlumno
import com.example.controlescolar.data.Validaciones
import com.example.controlescolar.ui.navigation.Argumentos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioAlumnoUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val nombre: String = "",
    val matricula: String = "",
    val correo: String = "",
    val semestre: String = "",
    val carreraId: Long? = null,
    val carreras: List<Carrera> = emptyList(),
    val errorNombre: String? = null,
    val errorMatricula: String? = null,
    val errorCorreo: String? = null,
    val errorSemestre: String? = null,
    val errorCarrera: String? = null,
    val carreraBloqueada: Boolean = false, // alumno con calificaciones: no puede cambiar de carrera
    val guardando: Boolean = false,
    val finalizado: Boolean = false,
    val mensaje: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(errorNombre, errorMatricula, errorCorreo, errorSemestre, errorCarrera)
            .any { it != null }
}

class FormularioAlumnoViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositorio: Repositorio
) : ViewModel() {

    private val alumnoId: Long = savedStateHandle.get<Long>(Argumentos.ALUMNO_ID) ?: -1L
    private val esEdicion = alumnoId > 0

    private val _uiState = MutableStateFlow(
        FormularioAlumnoUiState(esEdicion = esEdicion, cargando = esEdicion)
    )
    val uiState: StateFlow<FormularioAlumnoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.carreras.collect { lista -> _uiState.update { it.copy(carreras = lista) } }
        }
        if (esEdicion) cargarAlumno()
    }

    private fun cargarAlumno() {
        viewModelScope.launch {
            val alumno = repositorio.obtenerAlumno(alumnoId)
            if (alumno == null) {
                _uiState.update { it.copy(cargando = false, finalizado = true) }
                return@launch
            }
            val bloqueada = repositorio.tieneCalificaciones(alumnoId)
            _uiState.update {
                it.copy(
                    cargando = false,
                    nombre = alumno.nombre,
                    matricula = alumno.matricula,
                    correo = alumno.correo,
                    semestre = alumno.semestre.toString(),
                    carreraId = alumno.carreraId,
                    carreraBloqueada = bloqueada
                )
            }
        }
    }

    fun onNombre(v: String) = _uiState.update { it.copy(nombre = v, errorNombre = null) }
    fun onMatricula(v: String) = _uiState.update { it.copy(matricula = v, errorMatricula = null) }
    fun onCorreo(v: String) = _uiState.update { it.copy(correo = v, errorCorreo = null) }
    fun onSemestre(v: String) = _uiState.update { it.copy(semestre = v, errorSemestre = null) }
    fun onCarrera(id: Long) = _uiState.update { it.copy(carreraId = id, errorCarrera = null) }
    fun mensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun guardar() {
        val actual = _uiState.value
        if (actual.guardando || actual.cargando) return // evita envíos repetidos

        // 1) Quitar espacios al inicio y al final
        val nombre = actual.nombre.trim()
        val matricula = actual.matricula.trim()
        val correo = actual.correo.trim()
        val semestre = actual.semestre.trim()

        // 2) Validar
        val validado = actual.copy(
            nombre = nombre,
            matricula = matricula,
            correo = correo,
            semestre = semestre,
            errorNombre = Validaciones.nombre(nombre),
            errorMatricula = Validaciones.matricula(matricula),
            errorCorreo = Validaciones.correo(correo),
            errorSemestre = Validaciones.semestre(semestre),
            errorCarrera = Validaciones.carrera(actual.carreraId)
        )
        if (validado.hayErrores) {
            _uiState.value = validado
            return
        }

        // 3) Guardar desde una corrutina
        _uiState.value = validado.copy(guardando = true)
        val alumno = Alumno(
            id = if (esEdicion) alumnoId else 0L,
            nombre = nombre,
            matricula = matricula,
            correo = correo,
            semestre = semestre.toInt(),
            carreraId = checkNotNull(validado.carreraId)
        )
        viewModelScope.launch {
            when (val r = repositorio.guardarAlumno(alumno)) {
                ResultadoAlumno.Exito ->
                    _uiState.update { it.copy(guardando = false, finalizado = true) }

                ResultadoAlumno.MatriculaDuplicada ->
                    _uiState.update {
                        it.copy(guardando = false, errorMatricula = "La matrícula ya está registrada")
                    }

                is ResultadoAlumno.CarreraBloqueada ->
                    _uiState.update {
                        it.copy(guardando = false, carreraBloqueada = true, errorCarrera = r.mensaje)
                    }

                is ResultadoAlumno.Error ->
                    _uiState.update { it.copy(guardando = false, mensaje = r.mensaje) }
            }
        }
    }
}
