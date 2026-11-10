package com.example.controlescolar.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlescolar.data.AlumnoConCarrera
import com.example.controlescolar.data.Carrera
import com.example.controlescolar.data.Repositorio
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ListaUiState(
    val cargando: Boolean = true,
    val carreras: List<Carrera> = emptyList(),
    val carreraSeleccionadaId: Long? = null, // null = "Todos"
    val alumnos: List<AlumnoConCarrera> = emptyList(),
    val alumnoAEliminar: AlumnoConCarrera? = null,
    val mensaje: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ListaAlumnosViewModel(private val repositorio: Repositorio) : ViewModel() {

    private val filtro = MutableStateFlow<Long?>(null)
    private val alumnoAEliminar = MutableStateFlow<AlumnoConCarrera?>(null)
    private val mensaje = MutableStateFlow<String?>(null)

    // El filtro viaja junto con los datos para que chip y lista cambien al mismo tiempo
    private val alumnosFiltrados = filtro.flatMapLatest { carreraId ->
        repositorio.alumnos(carreraId).map { lista -> carreraId to lista }
    }

    val uiState: StateFlow<ListaUiState> = combine(
        repositorio.carreras, alumnosFiltrados, alumnoAEliminar, mensaje
    ) { carreras, (carreraId, alumnos), eliminar, msg ->
        ListaUiState(
            cargando = false,
            carreras = carreras,
            carreraSeleccionadaId = carreraId,
            alumnos = alumnos,
            alumnoAEliminar = eliminar,
            mensaje = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListaUiState())

    fun seleccionarCarrera(carreraId: Long?) {
        filtro.value = carreraId
    }

    fun pedirEliminacion(alumno: AlumnoConCarrera) {
        alumnoAEliminar.value = alumno
    }

    fun cancelarEliminacion() {
        alumnoAEliminar.value = null
    }

    fun confirmarEliminacion() {
        val alumno = alumnoAEliminar.value ?: return
        alumnoAEliminar.value = null
        viewModelScope.launch {
            if (!repositorio.eliminarAlumno(alumno.id)) {
                mensaje.value = "No se pudo eliminar el alumno. Intenta de nuevo."
            }
        }
    }

    fun mensajeMostrado() {
        mensaje.value = null
    }
}
