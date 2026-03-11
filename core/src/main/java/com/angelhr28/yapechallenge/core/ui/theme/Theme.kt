package com.angelhr28.yapechallenge.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = VaultPrimary,
    secondary = VaultSecondary,
    background = VaultBackground,
    surface = VaultSurface,
    error = VaultError,
    onPrimary = VaultOnPrimary,
    onSecondary = VaultOnSecondary,
    onBackground = VaultOnBackground,
    onSurface = VaultOnSurface,
    onError = VaultOnError
)

private val DarkColorScheme = darkColorScheme(
    primary = VaultPrimaryDark,
    secondary = VaultSecondaryDark,
    background = VaultBackgroundDark,
    surface = VaultSurfaceDark,
    onBackground = VaultOnBackgroundDark,
    onSurface = VaultOnSurfaceDark,
    onError = VaultOnError
)

@Composable
fun YapeChallengeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YapeChallengeTypography,
        content = content
    )
}
