package com.example.controlescolar.ui.detalle

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.example.controlescolar.data.MateriaConCalificacion
import com.example.controlescolar.data.Validaciones
import com.example.controlescolar.ui.AppViewModelProvider
import com.example.controlescolar.ui.componentes.BarraSuperior
import com.example.controlescolar.ui.componentes.DialogoConfirmacion
import com.example.controlescolar.ui.componentes.DialogoEliminarAlumno

@Composable
fun DetalleAlumnoScreen(
    onVolver: () -> Unit,
    onEditarAlumno: () -> Unit,
    onRegistrarCalificacion: (materiaId: Long) -> Unit,
    onEditarCalificacion: (calificacionId: Long) -> Unit,
    viewModel: DetalleAlumnoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val alumno = s.alumno

    LaunchedEffect(s.alumnoEliminado) { if (s.alumnoEliminado) onVolver() }
    LaunchedEffect(s.mensaje) {
        s.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(titulo = "Detalle del alumno", onAtras = onVolver) {
                if (alumno != null) {
                    IconButton(onClick = onEditarAlumno) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar alumno")
                    }
                    IconButton(onClick = viewModel::pedirEliminarAlumno) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Eliminar alumno")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (alumno == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                if (s.cargando) CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DatosPersonales(alumno) }
            item { TarjetaPromedio(s.promedio != null, s.textoPromedio) }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Materias", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${s.registradas} de ${s.materias.size} registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(s.materias, key = { it.materiaId }) { materia ->
                TarjetaMateria(
                    materia = materia,
                    onRegistrar = { onRegistrarCalificacion(materia.materiaId) },
                    onEditar = { materia.calificacionId?.let(onEditarCalificacion) },
                    onEliminar = { viewModel.pedirEliminarCalificacion(materia) }
                )
            }
        }
    }

    when (val d = s.dialogo) {
        DialogoDetalle.EliminarAlumno -> alumno?.let {
            DialogoEliminarAlumno(
                nombre = it.nombre,
                matricula = it.matricula,
                onConfirmar = viewModel::confirmarEliminacion,
                onCancelar = viewModel::cancelarDialogo
            )
        }

        is DialogoDetalle.EliminarCalificacion -> DialogoConfirmacion(
            titulo = "¿Eliminar calificación?",
            onConfirmar = viewModel::confirmarEliminacion,
            onCancelar = viewModel::cancelarDialogo
        ) {
            Text(
                d.materia.materiaNombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Calificación: ${Validaciones.formato2Decimales(d.materia.valor ?: 0.0)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Se eliminará esta calificación del historial de ${alumno?.nombre.orEmpty()}.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        null -> Unit
    }
}

@Composable
private fun DatosPersonales(alumno: AlumnoConCarrera) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(alumno.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Matrícula ${alumno.matricula} · Semestre ${alumno.semestre}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                alumno.correo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(alumno.carreraNombre, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun TarjetaPromedio(hayPromedio: Boolean, texto: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            if (hayPromedio) {
                Text("Promedio: $texto", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            } else {
                Text(texto, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun TarjetaMateria(
    materia: MateriaConCalificacion,
    onRegistrar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp, end = 4.dp)) {
            Text(materia.materiaNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (materia.valor != null) {
                    Text(
                        Validaciones.formato2Decimales(materia.valor),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row {
                        IconButton(onClick = onEditar) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar calificación de ${materia.materiaNombre}")
                        }
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar calificación de ${materia.materiaNombre}")
                        }
                    }
                } else {
                    Text(
                        "Sin registrar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onRegistrar) { Text("Registrar calificación") }
                }
            }
        }
    }
}
