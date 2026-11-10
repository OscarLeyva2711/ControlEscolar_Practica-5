package com.example.controlescolar.ui.calificacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controlescolar.ui.AppViewModelProvider
import com.example.controlescolar.ui.componentes.BarraSuperior
import com.example.controlescolar.ui.componentes.BotonesFormulario
import com.example.controlescolar.ui.componentes.CampoSelector
import com.example.controlescolar.ui.componentes.CampoSoloLectura
import com.example.controlescolar.ui.componentes.CampoTexto

@Composable
fun FormularioCalificacionScreen(
    onVolver: () -> Unit,
    viewModel: FormularioCalificacionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(s.finalizado) { if (s.finalizado) onVolver() }
    LaunchedEffect(s.mensaje) {
        s.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = if (s.esEdicion) "Editar calificación" else "Registrar calificación",
                onAtras = onVolver
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (s.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    if (s.esEdicion) "Actualiza la calificación de la materia."
                    else "Registra la calificación de una materia pendiente.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                CampoSoloLectura(etiqueta = "Alumno · Solo lectura", valor = s.alumnoNombre)

                if (s.esEdicion) {
                    CampoSoloLectura(etiqueta = "Materia · Solo lectura", valor = s.materiaNombre)
                } else {
                    CampoSelector(
                        etiqueta = "Materia",
                        seleccion = s.materiaSeleccionada,
                        textoVacio = "Selecciona una materia",
                        opciones = s.materiasPendientes.map { it.materiaId to it.materiaNombre },
                        onSeleccion = viewModel::onMateria,
                        error = s.errorMateria,
                        apoyo = when {
                            s.sinMateriasPendientes -> "Todas las materias ya tienen calificación"
                            s.materiasPendientes.size == 1 -> "Única materia pendiente de registro"
                            else -> "Materias de su carrera sin calificación"
                        }
                    )
                }

                CampoTexto(
                    etiqueta = "Calificación",
                    valor = s.valor,
                    onCambio = viewModel::onValor,
                    error = s.errorValor,
                    apoyo = "De 0 a 100. Admite decimales.",
                    placeholder = "Ingresa una calificación",
                    teclado = KeyboardType.Decimal,
                    accionIme = ImeAction.Done
                )
            }

            BotonesFormulario(
                textoGuardar = if (s.esEdicion) "Guardar cambios" else "Guardar",
                guardando = s.guardando,
                onGuardar = viewModel::guardar,
                onCancelar = onVolver,
                guardarHabilitado = !s.sinMateriasPendientes,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }
}
