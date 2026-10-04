// SPDX-License-Identifier: Apache-2.0
package com.flowpay.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.flowpay.app.ui.theme.Spacing
import com.flowpay.app.ui.theme.WavePayBrand
import com.flowpay.app.ui.theme.WavePayBrandPressed
import com.flowpay.app.ui.theme.WavePayOnBrand
import com.flowpay.app.ui.theme.WavePayOutline
import com.flowpay.app.ui.theme.WavePayStatusNeutral
import com.flowpay.app.ui.theme.WavePayStatusNeutralTint

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = Spacing.touchTarget),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) WavePayBrandPressed else WavePayBrand,
            contentColor = WavePayOnBrand,
            disabledContainerColor = WavePayStatusNeutralTint,
            disabledContentColor = WavePayStatusNeutral
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null
                )
                Spacer(Modifier.width(Spacing.small))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = Spacing.touchTarget),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPressed) WavePayBrandPressed else WavePayOutline
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isPressed) WavePayBrandPressed else WavePayBrand,
            disabledContentColor = WavePayStatusNeutral
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(imageVector = leadingIcon, contentDescription = null)
                Spacer(Modifier.width(Spacing.small))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}