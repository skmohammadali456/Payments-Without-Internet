// SPDX-License-Identifier: Apache-2.0
package com.flowpay.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flowpay.app.ui.theme.Spacing
import com.flowpay.app.ui.theme.WavePayBrand
import com.flowpay.app.ui.theme.WavePaySecondaryText

@Composable
fun EmptyState(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = 320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WavePayBrand,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(Spacing.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.tiny))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = WavePaySecondaryText,
            textAlign = TextAlign.Center
        )
    }
}