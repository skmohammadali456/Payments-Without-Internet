// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay

package com.flowpay.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val WavePayRadiusSmall = 12.dp
val WavePayRadiusMedium = 20.dp
val WavePayRadiusLarge = 28.dp

val WavePayShapes = Shapes(
    extraSmall = RoundedCornerShape(WavePayRadiusSmall),
    small = RoundedCornerShape(WavePayRadiusSmall),
    medium = RoundedCornerShape(WavePayRadiusSmall),
    large = RoundedCornerShape(WavePayRadiusMedium),
    extraLarge = RoundedCornerShape(WavePayRadiusLarge)
)