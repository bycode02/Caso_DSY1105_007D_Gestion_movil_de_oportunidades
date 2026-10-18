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
import com.example.opportunitymanagement.ui.theme.Pink40
import com.example.opportunitymanagement.ui.theme.Pink80
import com.example.opportunitymanagement.ui.theme.Purple40
import com.example.opportunitymanagement.ui.theme.Purple80
import com.example.opportunitymanagement.ui.theme.PurpleGrey40
import com.example.opportunitymanagement.ui.theme.PurpleGrey80
import com.example.opportunitymanagement.ui.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
        typography = Typography,
        content = content
    )
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ThemePreview() { Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { androidx.compose.material3.Text("Oportunidades corporativas") } }
