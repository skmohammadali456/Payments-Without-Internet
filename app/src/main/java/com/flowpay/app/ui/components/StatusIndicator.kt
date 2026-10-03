// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay

package com.flowpay.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flowpay.app.R
import com.flowpay.app.ui.theme.FlowpayStatusError
import com.flowpay.app.ui.theme.FlowpayStatusNeutral
import com.flowpay.app.ui.theme.FlowpayStatusSuccess
import com.flowpay.app.ui.theme.FlowpayStatusWarning

@Composable
fun StatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val (label, color, icon) = when (status.uppercase()) {
        "SUCCESS" ->
            Triple(R.string.status_label_success, FlowpayStatusSuccess, Icons.Default.CheckCircle)
        "FAILED", "DECLINED" ->
            Triple(R.string.status_label_failed, FlowpayStatusError, Icons.Default.ErrorOutline)
        "NEEDS_REVIEW" ->
            Triple(R.string.status_label_needs_review, FlowpayStatusWarning, Icons.Default.Warning)
        "PENDING" ->
            Triple(R.string.status_label_pending, FlowpayStatusWarning, Icons.Default.Schedule)
        "CANCELLED" ->
            Triple(R.string.status_label_cancelled, FlowpayStatusNeutral, Icons.Default.Cancel)
        "UNVERIFIED" ->
            Triple(R.string.status_label_unverified, FlowpayStatusNeutral, Icons.Default.HelpOutline)
        else ->
            Triple(R.string.status_label_unknown, FlowpayStatusNeutral, Icons.Default.HelpOutline)
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.10f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 28.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
            )
            Text(
                text = stringResource(label),
                color = color,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}