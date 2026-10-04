// SPDX-License-Identifier: Apache-2.0
package com.flowpay.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.flowpay.app.ui.theme.WavePayInk

@Composable
fun AmountText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = WavePayInk
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color
    )
}