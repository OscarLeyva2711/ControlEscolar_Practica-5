package com.example.controlescolar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.controlescolar.ui.calificacion.FormularioCalificacionScreen
import com.example.controlescolar.ui.detalle.DetalleAlumnoScreen
import com.example.controlescolar.ui.formulario.FormularioAlumnoScreen
import com.example.controlescolar.ui.lista.ListaAlumnosScreen

object Argumentos {
    const val ALUMNO_ID = "alumnoId"
    const val CALIFICACION_ID = "calificacionId"
    const val MATERIA_ID = "materiaId"
}

object Rutas {
    const val LISTA = "lista"
    const val ALUMNO_FORM = "alumno_form?${Argumentos.ALUMNO_ID}={${Argumentos.ALUMNO_ID}}"
    const val DETALLE = "detalle/{${Argumentos.ALUMNO_ID}}"
    const val CALIFICACION_FORM =
        "calificacion_form/{${Argumentos.ALUMNO_ID}}" +
            "?${Argumentos.CALIFICACION_ID}={${Argumentos.CALIFICACION_ID}}" +
            "&${Argumentos.MATERIA_ID}={${Argumentos.MATERIA_ID}}"

    /** alumnoId == null => registro; con id => edición. */
    fun alumnoForm(alumnoId: Long? = null) = "alumno_form?${Argumentos.ALUMNO_ID}=${alumnoId ?: -1L}"
    fun detalle(alumnoId: Long) = "detalle/$alumnoId"
    fun calificacionForm(alumnoId: Long, calificacionId: Long? = null, materiaId: Long? = null) =
        "calificacion_form/$alumnoId" +
            "?${Argumentos.CALIFICACION_ID}=${calificacionId ?: -1L}" +
            "&${Argumentos.MATERIA_ID}=${materiaId ?: -1L}"
}

private fun argLargo(nombre: String, porDefecto: Long? = null) = navArgument(nombre) {
    type = NavType.LongType
    if (porDefecto != null) defaultValue = porDefecto
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Rutas.LISTA) {

        composable(Rutas.LISTA) {
            ListaAlumnosScreen(
                onNuevoAlumno = { navController.navigate(Rutas.alumnoForm()) },
                onDetalle = { navController.navigate(Rutas.detalle(it)) },
                onEditar = { navController.navigate(Rutas.alumnoForm(it)) }
            )
        }

        composable(Rutas.ALUMNO_FORM, arguments = listOf(argLargo(Argumentos.ALUMNO_ID, -1L))) {
            FormularioAlumnoScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.DETALLE, arguments = listOf(argLargo(Argumentos.ALUMNO_ID))) { entrada ->
            val alumnoId = entrada.arguments?.getLong(Argumentos.ALUMNO_ID) ?: -1L
            DetalleAlumnoScreen(
                onVolver = { navController.popBackStack() },
                onEditarAlumno = { navController.navigate(Rutas.alumnoForm(alumnoId)) },
                onRegistrarCalificacion = { materiaId ->
                    navController.navigate(Rutas.calificacionForm(alumnoId, materiaId = materiaId))
                },
                onEditarCalificacion = { calificacionId ->
                    navController.navigate(Rutas.calificacionForm(alumnoId, calificacionId = calificacionId))
                }
            )
        }

        composable(
            Rutas.CALIFICACION_FORM,
            arguments = listOf(
                argLargo(Argumentos.ALUMNO_ID),
                argLargo(Argumentos.CALIFICACION_ID, -1L),
                argLargo(Argumentos.MATERIA_ID, -1L)
            )
        ) {
            FormularioCalificacionScreen(onVolver = { navController.popBackStack() })
        }
    }
}
