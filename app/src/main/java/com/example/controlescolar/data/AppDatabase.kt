package com.example.controlescolar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

@Database(
    entities = [Carrera::class, Materia::class, Alumno::class, Calificacion::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carreraDao(): CarreraDao
    abstract fun materiaDao(): MateriaDao
    abstract fun alumnoDao(): AlumnoDao
    abstract fun calificacionDao(): CalificacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "control_escolar.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadExecutor().execute {
                                prepoblarBaseDatos(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun prepoblarBaseDatos(db: AppDatabase) {
            val c1 = db.carreraDao()
            val m1 = db.materiaDao()

            runBlocking {
                val idSistemas = c1.insertar(Carrera(id = 1, nombre = "Ingeniería en Sistemas Computacionales"))
                val idIndustrial = c1.insertar(Carrera(id = 2, nombre = "Ingeniería Industrial"))
                val idAdmin = c1.insertar(Carrera(id = 3, nombre = "Licenciatura en Administración"))

                m1.insertarTodas(
                    listOf(
                        Materia(carreraId = idSistemas, nombre = "Programación Orientada a Objetos"),
                        Materia(carreraId = idSistemas, nombre = "Estructura de Datos"),
                        Materia(carreraId = idSistemas, nombre = "Bases de Datos"),
                        Materia(carreraId = idSistemas, nombre = "Redes de Computadoras"),
                        Materia(carreraId = idSistemas, nombre = "Ingeniería de Software"),

                        Materia(carreraId = idIndustrial, nombre = "Estadística Industrial"),
                        Materia(carreraId = idIndustrial, nombre = "Control de Calidad"),
                        Materia(carreraId = idIndustrial, nombre = "Investigación de Operaciones"),
                        Materia(carreraId = idIndustrial, nombre = "Logística y Cadenas de Suministro"),

                        Materia(carreraId = idAdmin, nombre = "Contabilidad Financiera"),
                        Materia(carreraId = idAdmin, nombre = "Mercadotecnia"),
                        Materia(carreraId = idAdmin, nombre = "Administración de Recursos Humanos"),
                        Materia(carreraId = idAdmin, nombre = "Finanzas Corporativas")
                    )
                )
            }
        }
    }
}
