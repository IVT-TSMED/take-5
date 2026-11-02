package au.com.invitro.take5.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.com.invitro.take5.R

val IvtTeal = Color(0xFF276884)
val IvtNavy = Color(0xFF004F6C)
val IvtBlue = Color(0xFF037AC5)
val IvtGrey = Color(0xFF5F6B70)
val IvtBackground = Color(0xFFF2F2F2)
val IvtRed = Color(0xFFC10841)
val IvtOrange = Color(0xFFF89406)
val IvtGreen = Color(0xFF738D00)

val OpenSans = FontFamily(
    Font(R.font.opensans_regular, FontWeight.Normal),
    Font(R.font.opensans_semibold, FontWeight.SemiBold),
    Font(R.font.opensans_bold, FontWeight.Bold),
)

private val colours = lightColorScheme(
    primary = IvtTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8F0),
    onPrimaryContainer = IvtNavy,
    secondary = IvtNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E8F0),
    onSecondaryContainer = IvtNavy,
    tertiary = IvtBlue,
    background = IvtBackground,
    onBackground = Color(0xFF1E1E1E),
    surface = Color.White,
    onSurface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFFE9EEF0),
    onSurfaceVariant = IvtGrey,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    outline = Color(0xFFB8C4C9),
    error = IvtRed,
)

private val baseTypography = Typography()

private val typography = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = OpenSans),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = OpenSans),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = OpenSans),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = OpenSans),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = OpenSans),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = OpenSans, fontWeight = FontWeight.SemiBold),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = OpenSans, fontWeight = FontWeight.SemiBold),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = OpenSans, fontWeight = FontWeight.SemiBold),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = OpenSans, fontWeight = FontWeight.SemiBold),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = OpenSans),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = OpenSans),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = OpenSans),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = OpenSans, fontWeight = FontWeight.SemiBold),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = OpenSans),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = OpenSans),
)

@Composable
fun IvtTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colours,
        typography = typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(6.dp),
            large = RoundedCornerShape(8.dp),
        ),
        content = content,
    )
}
