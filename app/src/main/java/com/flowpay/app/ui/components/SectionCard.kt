// SPDX-License-Identifier: Apache-2.0
package com.flowpay.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.flowpay.app.ui.theme.Spacing
import com.flowpay.app.ui.theme.WavePayOutline
import com.flowpay.app.ui.theme.WavePaySurface

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Spacing.medium),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = WavePaySurface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, WavePayOutline),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}