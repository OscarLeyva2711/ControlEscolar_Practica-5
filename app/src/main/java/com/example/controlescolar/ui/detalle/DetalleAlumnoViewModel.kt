package com.example.controlescolar.ui.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlescolar.data.AlumnoConCarrera
import com.example.controlescolar.data.MateriaConCalificacion
import com.example.controlescolar.data.Repositorio
import com.example.controlescolar.data.Validaciones
import com.example.controlescolar.ui.navigation.Argumentos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DialogoDetalle {
    data object EliminarAlumno : DialogoDetalle
    data class EliminarCalificacion(val materia: MateriaConCalificacion) : DialogoDetalle
}

data class DetalleUiState(
    val cargando: Boolean = true,
    val alumno: AlumnoConCarrera? = null,
    val materias: List<MateriaConCalificacion> = emptyList(),
    val promedio: Double? = null, // null => "Sin calificaciones"
    val dialogo: DialogoDetalle? = null,
    val alumnoEliminado: Boolean = false,
    val mensaje: String? = null
) {
    val registradas: Int get() = materias.count { it.valor != null }
    val textoPromedio: String get() = Validaciones.formatoPromedio(promedio)
}

class DetalleAlumnoViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositorio: Repositorio
) : ViewModel() {

    private val alumnoId: Long = savedStateHandle.get<Long>(Argumentos.ALUMNO_ID) ?: -1L

    private val dialogo = MutableStateFlow<DialogoDetalle?>(null)
    private val eliminado = MutableStateFlow(false)
    private val mensaje = MutableStateFlow<String?>(null)

    private val datos = combine(
        repositorio.detalleAlumno(alumnoId),
        repositorio.materiasConCalificacion(alumnoId)
    ) { alumno, materias -> alumno to materias }

    val uiState: StateFlow<DetalleUiState> = combine(
        datos, dialogo, eliminado, mensaje
    ) { (alumno, materias), d, e, msg ->
        // Promedio = suma de calificaciones registradas / cantidad registradas.
        // Los ceros cuentan; las materias sin calificación no se consideran.
        val valores = materias.mapNotNull { it.valor }
        DetalleUiState(
            cargando = false,
            alumno = alumno,
            materias = materias,
            promedio = if (valores.isEmpty()) null else valores.sum() / valores.size,
            dialogo = d,
            alumnoEliminado = e,
            mensaje = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState())

    fun pedirEliminarAlumno() {
        dialogo.value = DialogoDetalle.EliminarAlumno
    }

    fun pedirEliminarCalificacion(materia: MateriaConCalificacion) {
        dialogo.value = DialogoDetalle.EliminarCalificacion(materia)
    }

    fun cancelarDialogo() {
        dialogo.value = null
    }

    fun confirmarEliminacion() {
        val actual = dialogo.value ?: return
        dialogo.value = null
        viewModelScope.launch {
            when (actual) {
                DialogoDetalle.EliminarAlumno -> {
                    if (repositorio.eliminarAlumno(alumnoId)) eliminado.value = true
                    else mensaje.value = "No se pudo eliminar el alumno. Intenta de nuevo."
                }

                is DialogoDetalle.EliminarCalificacion -> {
                    val id = actual.materia.calificacionId
                    if (id == null || !repositorio.eliminarCalificacion(id)) {
                        mensaje.value = "No se pudo eliminar la calificación. Intenta de nuevo."
                    }
                }
            }
        }
    }

    fun mensajeMostrado() {
        mensaje.value = null
    }
}
