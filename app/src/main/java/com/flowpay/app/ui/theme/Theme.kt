// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay

package com.flowpay.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.flowpay.app.utils.findComponentActivity

private val LightColorScheme = lightColorScheme(
    primary = WavePayBrand,
    onPrimary = WavePayOnBrand,
    secondary = WavePayBrand,
    onSecondary = WavePayOnBrand,
    tertiary = WavePayBrand,
    background = WavePayCanvas,
    onBackground = WavePayInk,
    surface = WavePaySurface,
    onSurface = WavePayInk,
    surfaceVariant = WavePayStatusNeutralTint,
    onSurfaceVariant = WavePaySecondaryText,
    outline = WavePayOutline,
    error = WavePayStatusDanger
)

@Composable
fun FlowpayTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findComponentActivity()?.window ?: return@SideEffect
            window.statusBarColor = WavePayCanvas.toArgb()
            window.navigationBarColor = WavePayCanvas.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = WavePayShapes,
        content = content
    )
}
