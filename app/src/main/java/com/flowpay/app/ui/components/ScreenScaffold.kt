// SPDX-License-Identifier: Apache-2.0
package com.flowpay.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.flowpay.app.R
import com.flowpay.app.ui.theme.Spacing
import com.flowpay.app.ui.theme.WavePayCanvas
import com.flowpay.app.ui.theme.WavePaySurface

@Composable
fun ScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    navigationDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val backDescription = navigationDescription ?: stringResource(R.string.action_back)
    Scaffold(
        modifier = modifier,
        containerColor = WavePayCanvas,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WavePaySurface)
                    .heightIn(min = Spacing.touchTarget)
                    .padding(horizontal = Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(Spacing.touchTarget)
                            .semantics {
                                contentDescription = backDescription
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Spacing.small),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(content = actions)
            }
        },
        content = content
    )
}