package com.angelhr28.yapechallenge.core.extensions

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Muestra un mensaje Toast de forma abreviada.
 *
 * @param message texto del mensaje a mostrar.
 * @param duration duracion del Toast.
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Convierte un timestamp en milisegundos a fecha formateada (dd/MM/yyyy HH:mm:ss) con locale Peru.
 */
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "PE"))
    return sdf.format(Date(this))
}

/**
 * Convierte un timestamp en milisegundos a fecha corta (dd/MM/yyyy) con locale Peru.
 */
fun Long.toShortDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE"))
    return sdf.format(Date(this))
}
