package com.example.weatherapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.weatherapp.R

val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

val baseTypography = Typography()

val Typography = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = Montserrat),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = Montserrat),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = Montserrat),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = Montserrat),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = Montserrat),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = Montserrat),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = Montserrat),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = Montserrat),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = Montserrat),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = Montserrat),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = Montserrat),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = Montserrat),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = Montserrat),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = Montserrat),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = Montserrat)
)
