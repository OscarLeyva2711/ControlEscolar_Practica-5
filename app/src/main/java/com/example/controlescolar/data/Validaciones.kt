package com.example.controlescolar.data

import android.util.Patterns
import java.util.Locale

/** Validaciones de formularios. Devuelven el mensaje de error o null si el valor es válido. */
object Validaciones {

    fun nombre(valor: String): String? =
        if (valor.trim().isEmpty()) "El nombre es obligatorio" else null

    fun matricula(valor: String): String? =
        if (valor.trim().isEmpty()) "La matrícula es obligatoria" else null

    fun correo(valor: String): String? {
        val v = valor.trim()
        return if (v.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(v).matches()) "Correo no válido" else null
    }

    fun semestre(valor: String): String? {
        val n = valor.trim().toIntOrNull()
        return if (n == null || n <= 0) "El semestre debe ser un entero mayor que 0" else null
    }

    fun carrera(id: Long?): String? = if (id == null) "Selecciona una carrera" else null

    fun materia(id: Long?): String? = if (id == null) "Selecciona una materia" else null

    fun calificacion(valor: String): String? {
        val n = valor.trim().replace(',', '.').toDoubleOrNull()
        if (n == null || n.isNaN() || n.isInfinite()) return "Ingresa un valor numérico"
        return if (n < 0.0 || n > 100.0) "La calificación debe estar entre 0 y 100" else null
    }

    /** Convierte el texto ya validado a Double (acepta coma o punto decimal). */
    fun aDouble(valor: String): Double = valor.trim().replace(',', '.').toDouble()

    fun formato2Decimales(valor: Double): String = String.format(Locale.US, "%.2f", valor)

    fun formatoPromedio(promedio: Double?): String =
        promedio?.let { formato2Decimales(it) } ?: "Sin calificaciones"
}
