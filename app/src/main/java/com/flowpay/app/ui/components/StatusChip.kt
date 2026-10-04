// SPDX-License-Identifier: Apache-2.0
package com.flowpay.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.flowpay.app.ui.theme.WavePayStatusDanger
import com.flowpay.app.ui.theme.WavePayStatusDangerTint
import com.flowpay.app.ui.theme.WavePayStatusNeutral
import com.flowpay.app.ui.theme.WavePayStatusNeutralTint
import com.flowpay.app.ui.theme.WavePayStatusSuccess
import com.flowpay.app.ui.theme.WavePayStatusSuccessTint
import com.flowpay.app.ui.theme.WavePayStatusWarning
import com.flowpay.app.ui.theme.WavePayStatusWarningTint

private data class ChipStyle(
    val foreground: Color,
    val background: Color,
    val icon: ImageVector
)

private fun chipStyle(status: String): ChipStyle = when (status.uppercase()) {
    "SUCCESS" -> ChipStyle(WavePayStatusSuccess, WavePayStatusSuccessTint, Icons.Rounded.CheckCircle)
    "FAILED", "DECLINED" ->
        ChipStyle(WavePayStatusDanger, WavePayStatusDangerTint, Icons.Rounded.ErrorOutline)
    "NEEDS_REVIEW" ->
        ChipStyle(WavePayStatusWarning, WavePayStatusWarningTint, Icons.Rounded.WarningAmber)
    "PENDING" -> ChipStyle(WavePayStatusWarning, WavePayStatusWarningTint, Icons.Rounded.Schedule)
    "CANCELLED" -> ChipStyle(WavePayStatusNeutral, WavePayStatusNeutralTint, Icons.Rounded.Cancel)
    else -> ChipStyle(WavePayStatusNeutral, WavePayStatusNeutralTint, Icons.Rounded.HelpOutline)
}

@Composable
fun StatusChip(
    status: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val style = chipStyle(status)
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = label
        },
        color = style.background,
        shape = RoundedCornerShape(100.dp)
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 32.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.foreground,
                modifier = Modifier.defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
            )
            Text(
                text = label,
                color = style.foreground,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}