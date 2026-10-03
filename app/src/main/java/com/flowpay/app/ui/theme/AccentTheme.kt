// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay

package com.flowpay.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class FlowpayAccentTheme(
    val primary: Color,
    val primaryDark: Color,
    val headerGradientStart: Color,
    val headerGradientEnd: Color,
    val accent: Color,
    val accentLight: Color
)

val BlueAccentTheme = FlowpayAccentTheme(
    primary = FlowpayAccent,
    primaryDark = FlowpayAccent,
    headerGradientStart = FlowpayAccent,
    headerGradientEnd = FlowpayAccent,
    accent = FlowpayAccent,
    accentLight = FlowpayAccent
)

val LocalFlowpayAccentTheme = compositionLocalOf { BlueAccentTheme }
