package com.angelhr28.yapechallenge.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Clase utilitaria para gestionar la autenticacion biometrica del dispositivo.
 */
class BiometricHelper {

    /**
     * Verifica si el dispositivo soporta autenticacion biometrica.
     *
     * @param activity la actividad desde la cual se consulta.
     * @return `true` si la autenticacion biometrica esta disponible.
     */
    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Inicia el flujo de autenticacion biometrica.
     *
     * @param activity la actividad host del prompt biometrico.
     * @param title titulo del dialogo biometrico.
     * @param subtitle subtitulo del dialogo biometrico.
     * @param negativeButtonText texto del boton de cancelar.
     * @param onSuccess callback invocado tras autenticacion exitosa.
     * @param onError callback invocado con el mensaje de error.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Autenticación requerida",
        subtitle: String = "Verifica tu identidad para continuar",
        negativeButtonText: String = "Cancelar",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Autenticación fallida")
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
