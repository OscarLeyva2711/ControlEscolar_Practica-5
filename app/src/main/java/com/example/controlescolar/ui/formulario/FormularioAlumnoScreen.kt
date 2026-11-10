package com.example.controlescolar.ui.formulario

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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controlescolar.data.MSG_CARRERA_BLOQUEADA
import com.example.controlescolar.ui.AppViewModelProvider
import com.example.controlescolar.ui.componentes.BarraSuperior
import com.example.controlescolar.ui.componentes.BotonesFormulario
import com.example.controlescolar.ui.componentes.CampoSelector
import com.example.controlescolar.ui.componentes.CampoSoloLectura
import com.example.controlescolar.ui.componentes.CampoTexto

@Composable
fun FormularioAlumnoScreen(
    onVolver: () -> Unit,
    viewModel: FormularioAlumnoViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
                titulo = if (s.esEdicion) "Editar alumno" else "Registrar alumno",
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

        val subtitulo = when {
            s.hayErrores -> "Revisa los campos para continuar."
            s.esEdicion -> "Actualiza los datos personales del alumno."
            else -> "Completa los datos del nuevo alumno."
        }
        val carreraSeleccionada = s.carreras.firstOrNull { it.id == s.carreraId }?.nombre

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
                    subtitulo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                CampoTexto(
                    etiqueta = "Nombre",
                    valor = s.nombre,
                    onCambio = viewModel::onNombre,
                    error = s.errorNombre,
                    capitalizacion = KeyboardCapitalization.Words
                )
                CampoTexto(
                    etiqueta = "Matrícula",
                    valor = s.matricula,
                    onCambio = viewModel::onMatricula,
                    error = s.errorMatricula
                )
                CampoTexto(
                    etiqueta = "Correo",
                    valor = s.correo,
                    onCambio = viewModel::onCorreo,
                    error = s.errorCorreo,
                    teclado = KeyboardType.Email
                )
                CampoTexto(
                    etiqueta = "Semestre",
                    valor = s.semestre,
                    onCambio = viewModel::onSemestre,
                    error = s.errorSemestre,
                    apoyo = "Número entero mayor que 0",
                    teclado = KeyboardType.Number,
                    accionIme = ImeAction.Done
                )

                if (s.carreraBloqueada) {
                    CampoSoloLectura(
                        etiqueta = "Carrera",
                        valor = carreraSeleccionada.orEmpty(),
                        apoyo = s.errorCarrera ?: MSG_CARRERA_BLOQUEADA,
                        bloqueado = true
                    )
                } else {
                    CampoSelector(
                        etiqueta = "Carrera",
                        seleccion = carreraSeleccionada,
                        textoVacio = "Selecciona una carrera",
                        opciones = s.carreras.map { it.id to it.nombre },
                        onSeleccion = viewModel::onCarrera,
                        error = s.errorCarrera,
                        apoyo = "Catálogo institucional: ${s.carreras.size} carreras disponibles."
                    )
                }
            }

            BotonesFormulario(
                textoGuardar = if (s.esEdicion) "Guardar cambios" else "Guardar",
                guardando = s.guardando,
                onGuardar = viewModel::guardar,
                onCancelar = onVolver,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }
}
