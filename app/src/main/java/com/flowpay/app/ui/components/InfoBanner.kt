// SPDX-License-Identifier: Apache-2.0
package com.flowpay.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
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
import com.flowpay.app.ui.theme.Spacing
import com.flowpay.app.ui.theme.WavePayBrand
import com.flowpay.app.ui.theme.WavePayBrandTint
import com.flowpay.app.ui.theme.WavePayInk
import com.flowpay.app.ui.theme.WavePayStatusDanger
import com.flowpay.app.ui.theme.WavePayStatusDangerTint
import com.flowpay.app.ui.theme.WavePayStatusNeutral
import com.flowpay.app.ui.theme.WavePayStatusNeutralTint
import com.flowpay.app.ui.theme.WavePayStatusWarning
import com.flowpay.app.ui.theme.WavePayStatusWarningTint

enum class InfoBannerTone {
    INFO,
    NEUTRAL,
    WARNING,
    ERROR
}

private data class BannerStyle(
    val icon: ImageVector,
    val foreground: Color,
    val background: Color
)

private fun bannerStyle(tone: InfoBannerTone) = when (tone) {
    InfoBannerTone.INFO -> BannerStyle(Icons.Rounded.Info, WavePayBrand, WavePayBrandTint)
    InfoBannerTone.NEUTRAL ->
        BannerStyle(Icons.Rounded.Info, WavePayStatusNeutral, WavePayStatusNeutralTint)
    InfoBannerTone.WARNING ->
        BannerStyle(Icons.Rounded.WarningAmber, WavePayStatusWarning, WavePayStatusWarningTint)
    InfoBannerTone.ERROR ->
        BannerStyle(Icons.Rounded.ErrorOutline, WavePayStatusDanger, WavePayStatusDangerTint)
}

@Composable
fun InfoBanner(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    tone: InfoBannerTone = InfoBannerTone.INFO
) {
    val style = bannerStyle(tone)
    Surface(
        modifier = modifier,
        color = style.background,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = Spacing.touchTarget)
                .padding(horizontal = Spacing.medium, vertical = Spacing.compact),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.foreground
            )
            Spacer(Modifier.width(Spacing.small))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        color = WavePayInk,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = message,
                    color = WavePayInk,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}