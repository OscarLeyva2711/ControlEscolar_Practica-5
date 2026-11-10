package com.example.controlescolar.ui.lista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controlescolar.data.AlumnoConCarrera
import com.example.controlescolar.data.Carrera
import com.example.controlescolar.ui.AppViewModelProvider
import com.example.controlescolar.ui.componentes.BarraSuperior
import com.example.controlescolar.ui.componentes.DialogoEliminarAlumno
import com.example.controlescolar.ui.componentes.EstadoVacio

@Composable
fun ListaAlumnosScreen(
    onNuevoAlumno: () -> Unit,
    onDetalle: (Long) -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: ListaAlumnosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior("Control Escolar") },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoAlumno,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Registrar alumno")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            FiltroCarreras(
                carreras = state.carreras,
                seleccionada = state.carreraSeleccionadaId,
                onSeleccion = viewModel::seleccionarCarrera
            )
            when {
                state.cargando -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.alumnos.isEmpty() && state.carreraSeleccionadaId == null -> EstadoVacio(
                    icono = Icons.Outlined.Groups,
                    titulo = "No hay alumnos registrados",
                    mensaje = "Agrega un alumno para consultar sus materias y calificaciones."
                )

                state.alumnos.isEmpty() -> EstadoVacio(
                    icono = Icons.Outlined.SearchOff,
                    titulo = "No hay alumnos en esta carrera",
                    mensaje = "Prueba con otra carrera o consulta todos los alumnos.",
                    accion = {
                        TextButton(onClick = { viewModel.seleccionarCarrera(null) }) {
                            Text("Mostrar todos")
                        }
                    }
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alumnos", style = MaterialTheme.typography.titleMedium)
                            Text(
                                textoAlumnos(state.alumnos.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(state.alumnos, key = { it.id }) { alumno ->
                        AlumnoCard(
                            alumno = alumno,
                            onClick = { onDetalle(alumno.id) },
                            onEditar = { onEditar(alumno.id) },
                            onEliminar = { viewModel.pedirEliminacion(alumno) }
                        )
                    }
                }
            }
        }
    }

    state.alumnoAEliminar?.let { alumno ->
        DialogoEliminarAlumno(
            nombre = alumno.nombre,
            matricula = alumno.matricula,
            onConfirmar = viewModel::confirmarEliminacion,
            onCancelar = viewModel::cancelarEliminacion
        )
    }
}

private fun textoAlumnos(n: Int) = if (n == 1) "1 alumno" else "$n alumnos"

@Composable
private fun FiltroCarreras(
    carreras: List<Carrera>,
    seleccionada: Long?,
    onSeleccion: (Long?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ChipCarrera("Todos", seleccionada == null) { onSeleccion(null) } }
        items(carreras, key = { it.id }) { carrera ->
            ChipCarrera(carrera.nombre, seleccionada == carrera.id) { onSeleccion(carrera.id) }
        }
    }
}

@Composable
private fun ChipCarrera(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        leadingIcon = if (seleccionado) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else null,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun AlumnoCard(
    alumno: AlumnoConCarrera,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(alumno.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Matrícula ${alumno.matricula}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    alumno.carreraNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Outlined.Edit, contentDescription = "Editar alumno")
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar alumno")
            }
        }
    }
}
