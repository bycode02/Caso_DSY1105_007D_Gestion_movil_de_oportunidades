package com.example.opportunitymanagement.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.opportunitymanagement.ui.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = OnGreen80,
    primaryContainer = GreenContainer30,
    onPrimaryContainer = OnGreenContainer90,
    inversePrimary = Green40,
    secondary = GreenGrey80,
    onSecondary = OnGreenGrey80,
    secondaryContainer = GreenGreyContainer30,
    onSecondaryContainer = OnGreenGreyContainer90,
    tertiary = Amber80,
    onTertiary = OnAmber80,
    tertiaryContainer = AmberContainer30,
    onTertiaryContainer = AmberContainer90,
    background = NeutralDark,
    onBackground = OnNeutralDark,
    surface = NeutralDark,
    onSurface = OnNeutralDark,
    surfaceVariant = NeutralVariantDark,
    onSurfaceVariant = OnNeutralVariantDark,
    surfaceTint = Green80,
    inverseSurface = OnNeutralDark,
    inverseOnSurface = InverseSurfaceLight,
    outline = OutlineDark,
    outlineVariant = NeutralVariantDark,
    surfaceBright = SurfaceBrightDark,
    surfaceDim = NeutralDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = OnGreen40,
    primaryContainer = GreenContainer90,
    onPrimaryContainer = OnGreenContainer10,
    inversePrimary = Green80,
    secondary = GreenGrey40,
    onSecondary = OnGreenGrey40,
    secondaryContainer = GreenGreyContainer90,
    onSecondaryContainer = OnGreenGreyContainer10,
    tertiary = Amber40,
    onTertiary = OnGreen40,
    tertiaryContainer = AmberContainer90,
    onTertiaryContainer = OnAmberContainer10,
    background = NeutralLight,
    onBackground = OnNeutralLight,
    surface = NeutralLight,
    onSurface = OnNeutralLight,
    surfaceVariant = NeutralVariantLight,
    onSurfaceVariant = OnNeutralVariantLight,
    surfaceTint = Green40,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceBright = NeutralLight,
    surfaceDim = SurfaceDimLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight
)

@Composable
fun Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // El color dinámico de Android 12+ reemplazaría el verde del proyecto.
    dynamicColor: Boolean = false,
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
        typography = Typography,
        content = content
    )
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ThemePreview() { Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { androidx.compose.material3.Text("Oportunidades corporativas") } }
