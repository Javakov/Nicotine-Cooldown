package org.javakov.antyvkid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val display = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Black,
    letterSpacing = (-1.6).sp
)

private val title = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = (-0.2).sp
)

private val body = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.2.sp
)

private val label = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    letterSpacing = 1.6.sp
)

val Typography = Typography(
    displayLarge = display.copy(fontSize = 64.sp, lineHeight = 68.sp),
    displayMedium = display.copy(fontSize = 56.sp, lineHeight = 60.sp),
    displaySmall = display.copy(fontSize = 44.sp, lineHeight = 48.sp),
    headlineLarge = title.copy(fontSize = 32.sp, lineHeight = 36.sp),
    headlineMedium = title.copy(fontSize = 26.sp, lineHeight = 30.sp),
    headlineSmall = title.copy(fontSize = 22.sp, lineHeight = 26.sp),
    titleLarge = title.copy(fontSize = 20.sp, lineHeight = 24.sp),
    titleMedium = title.copy(fontSize = 16.sp, lineHeight = 20.sp),
    titleSmall = title.copy(fontSize = 14.sp, lineHeight = 18.sp),
    bodyLarge = body.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = body.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = body.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = label.copy(fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = label.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = label.copy(fontSize = 11.sp, lineHeight = 14.sp)
)
