package com.example.controlescolar.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val FormaCampo = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    onAtras: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            if (onAtras != null) {
                IconButton(onClick = onAtras) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    apoyo: String? = null,
    placeholder: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
    accionIme: ImeAction = ImeAction.Next
) {
    val textoApoyo = error ?: apoyo
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        isError = error != null,
        trailingIcon = if (error != null) {
            { Icon(Icons.Outlined.ErrorOutline, contentDescription = null) }
        } else null,
        supportingText = if (textoApoyo != null) { { Text(textoApoyo) } } else null,
        singleLine = true,
        shape = FormaCampo,
        keyboardOptions = KeyboardOptions(
            keyboardType = teclado,
            capitalization = capitalizacion,
            imeAction = accionIme
        )
    )
}

/** Selector desplegable: solo permite elegir de las opciones (id, nombre). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSelector(
    etiqueta: String,
    seleccion: String?,
    textoVacio: String,
    opciones: List<Pair<Long, String>>,
    onSeleccion: (Long) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    apoyo: String? = null
) {
    var expandido by remember { mutableStateOf(false) }
    val textoApoyo = error ?: apoyo

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccion ?: textoVacio,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = { Text(etiqueta) },
            textStyle = LocalTextStyle.current.copy(
                color = if (seleccion == null) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            ),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (error != null) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                }
            },
            isError = error != null,
            supportingText = if (textoApoyo != null) { { Text(textoApoyo) } } else null,
            singleLine = true,
            shape = FormaCampo
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { (id, nombre) ->
                DropdownMenuItem(
                    text = { Text(nombre) },
                    onClick = {
                        onSeleccion(id)
                        expandido = false
                    }
                )
            }
        }
    }
}

/** Campo no editable: "Solo lectura" (alumno/materia) o bloqueado con candado (carrera). */
@Composable
fun CampoSoloLectura(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    apoyo: String? = null,
    bloqueado: Boolean = false
) {
    val colores = MaterialTheme.colorScheme
    OutlinedTextField(
        value = valor,
        onValueChange = {},
        enabled = false,
        readOnly = true,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        trailingIcon = if (bloqueado) {
            { Icon(Icons.Outlined.Lock, contentDescription = "Campo bloqueado") }
        } else null,
        supportingText = if (apoyo != null) { { Text(apoyo) } } else null,
        singleLine = true,
        shape = FormaCampo,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = colores.onSurface,
            disabledLabelColor = colores.onSurfaceVariant,
            disabledContainerColor = colores.surfaceVariant,
            disabledBorderColor = if (bloqueado) colores.outlineVariant else colores.outline,
            disabledTrailingIconColor = colores.onSurfaceVariant,
            disabledSupportingTextColor = colores.onSurfaceVariant
        )
    )
}

@Composable
fun BotonesFormulario(
    textoGuardar: String,
    guardando: Boolean,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    guardarHabilitado: Boolean = true
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onGuardar,
            enabled = guardarHabilitado && !guardando, // evita envíos repetidos
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = FormaCampo
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
            } else {
                Text(textoGuardar)
            }
        }
        OutlinedButton(
            onClick = onCancelar,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = FormaCampo
        ) {
            Text("Cancelar")
        }
    }
}

@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (accion != null) {
            Spacer(Modifier.height(16.dp))
            accion()
        }
    }
}

@Composable
fun DialogoConfirmacion(
    titulo: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = contenido) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DialogoEliminarAlumno(
    nombre: String,
    matricula: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    DialogoConfirmacion("¿Eliminar alumno?", onConfirmar, onCancelar) {
        Text(nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "Matrícula $matricula",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "También se eliminarán todas sus calificaciones. Esta acción no se puede deshacer.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
