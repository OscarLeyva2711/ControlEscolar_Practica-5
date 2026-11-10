package com.example.controlescolar

import android.app.Application
import com.example.controlescolar.data.AppDatabase
import com.example.controlescolar.data.Repositorio

class ControlEscolarApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repositorio by lazy { Repositorio(database) }
}
