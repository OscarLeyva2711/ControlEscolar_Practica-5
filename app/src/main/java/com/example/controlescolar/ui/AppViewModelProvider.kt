package com.example.controlescolar.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.controlescolar.ControlEscolarApp
import com.example.controlescolar.ui.calificacion.FormularioCalificacionViewModel
import com.example.controlescolar.ui.detalle.DetalleAlumnoViewModel
import com.example.controlescolar.ui.formulario.FormularioAlumnoViewModel
import com.example.controlescolar.ui.lista.ListaAlumnosViewModel

private fun CreationExtras.app(): ControlEscolarApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ControlEscolarApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { ListaAlumnosViewModel(app().repositorio) }
        initializer { FormularioAlumnoViewModel(createSavedStateHandle(), app().repositorio) }
        initializer { DetalleAlumnoViewModel(createSavedStateHandle(), app().repositorio) }
        initializer { FormularioCalificacionViewModel(createSavedStateHandle(), app().repositorio) }
    }
}
