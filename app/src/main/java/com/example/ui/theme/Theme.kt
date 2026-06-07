package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    isTeacherMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isTeacherMode) {
        lightColorScheme(
            primary = PrimaryiOSBlue,
            secondary = ComfortSuccessGreen,
            tertiary = SoftLavender,
            background = PrimaryBackgroundWarmWhite,
            surface = SolidCardWhite,
            onBackground = TextDeepCharcoal,
            onSurface = TextDeepCharcoal,
            primaryContainer = SoftLilac,
            secondaryContainer = SoftGreenCard,
            surfaceVariant = SecondarySurfaceSoftIvory,
            onSurfaceVariant = TextDeepCharcoal,
            outline = BorderLightSystem
        )
    } else {
        lightColorScheme(
            primary = PrimaryiOSBlue,
            secondary = ComfortSuccessGreen,
            tertiary = SoftLavender,
            background = PrimaryBackgroundWarmWhite,
            surface = SolidCardWhite,
            onBackground = TextDeepCharcoal,
            onSurface = TextDeepCharcoal,
            primaryContainer = BlushPink,
            secondaryContainer = SoftGreenCard,
            surfaceVariant = SecondarySurfaceSoftIvory,
            onSurfaceVariant = TextDeepCharcoal,
            outline = BorderLightSystem
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
