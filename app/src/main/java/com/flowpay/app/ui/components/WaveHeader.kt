// SPDX-License-Identifier: Apache-2.0
package com.flowpay.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import com.flowpay.app.ui.theme.WavePayBrand
import com.flowpay.app.ui.theme.WavePayBrandPressed

@Composable
fun WaveHeader(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.material3.MaterialTheme.shapes.extraLarge)
            .background(WavePayBrand)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val wave = Path().apply {
                moveTo(0f, size.height * 0.68f)
                cubicTo(
                    size.width * 0.22f, size.height * 0.52f,
                    size.width * 0.38f, size.height * 0.93f,
                    size.width * 0.62f, size.height * 0.77f
                )
                cubicTo(
                    size.width * 0.78f, size.height * 0.66f,
                    size.width * 0.90f, size.height * 0.69f,
                    size.width, size.height * 0.56f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            clipRect {
                drawPath(wave, WavePayBrandPressed)
            }
        }
        content()
    }
}