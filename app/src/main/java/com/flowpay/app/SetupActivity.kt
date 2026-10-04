// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay

package com.flowpay.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.unit.dp
import com.flowpay.app.helpers.SetupHelper
import com.flowpay.app.ui.components.InfoBanner
import com.flowpay.app.ui.components.InfoBannerTone
import com.flowpay.app.ui.components.PrimaryButton
import com.flowpay.app.ui.components.SectionCard
import com.flowpay.app.ui.components.SecondaryButton
import com.flowpay.app.ui.components.WaveHeader
import com.flowpay.app.ui.theme.Spacing
import com.flowpay.app.ui.theme.WavePayCanvas
import com.flowpay.app.ui.theme.FlowpayTheme
import com.flowpay.app.ui.theme.WavePayBrand
import com.flowpay.app.ui.theme.WavePayBrandTint
import com.flowpay.app.ui.theme.WavePayInk
import com.flowpay.app.ui.theme.WavePayOnBrand
import com.flowpay.app.ui.theme.WavePayOutline
import com.flowpay.app.ui.theme.WavePaySecondaryText
import com.flowpay.app.ui.theme.WavePaySurface

class SetupActivity : ComponentActivity() {
    private lateinit var setupHelper: SetupHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize setup helper
        setupHelper = SetupHelper(
            this,
            object : SetupHelper.UICallback {
                override fun showToast(message: String) {
                    runOnUiThread { Toast.makeText(this@SetupActivity, message, Toast.LENGTH_LONG).show() }
                }

                override fun navigateToTestConfiguration() {
                    val intent = Intent(this@SetupActivity, TestConfigurationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        )

        setTheme(R.style.Theme_Flowpay)
        // Edge-to-edge: Compose insets are the single source of padding (see MainActivity).
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FlowpayTheme {
                SetupScreen(setupHelper = setupHelper)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(setupHelper: SetupHelper) {
    var selectedBank by remember { mutableStateOf("") }
    var selectedPrimarySim by remember { mutableStateOf("") }
    var selectedSecondarySim by remember { mutableStateOf("") }
    var isDualSimEnabled by remember { mutableStateOf(false) }
    var disclaimerAccepted by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }

    val banks = setupHelper.getBanks()
    val simCarriers = setupHelper.getSimCarriers()
    val secondarySimOptions = setupHelper.getSecondarySimOptions(selectedPrimarySim)
    val bankAndSimComplete = selectedBank.isNotBlank() &&
        selectedPrimarySim.isNotBlank() &&
        (!isDualSimEnabled || selectedSecondarySim.isNotBlank())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WavePayCanvas)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.medium, vertical = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.setup_onboarding_step, currentPage + 1),
                style = MaterialTheme.typography.labelLarge,
                color = WavePaySecondaryText
            )
            OnboardingProgress(activeStep = currentPage + 1)

            when (currentPage) {
                0 -> HeaderCard()
                1 -> PermissionInfoPage(
                    title = stringResource(R.string.setup_permission_phone_title),
                    reason = stringResource(R.string.setup_permission_phone_reason),
                    icon = Icons.Rounded.Call
                )
                2 -> PermissionInfoPage(
                    title = stringResource(R.string.setup_permission_sms_title),
                    reason = stringResource(R.string.setup_permission_sms_reason),
                    icon = Icons.Rounded.Sms
                )
                3 -> {
                    BankSelectionSection(
                        banks = banks,
                        selectedBank = selectedBank,
                        onBankSelected = { selectedBank = it }
                    )
                    SimCardSelectionSection(
                        simCarriers = simCarriers,
                        selectedPrimarySim = selectedPrimarySim,
                        selectedSecondarySim = selectedSecondarySim,
                        isDualSimEnabled = isDualSimEnabled,
                        onPrimarySimSelected = { selectedPrimarySim = it },
                        onSecondarySimSelected = { selectedSecondarySim = it },
                        onDualSimToggled = { isDualSimEnabled = it },
                        secondarySimOptions = secondarySimOptions
                    )
                }
                else -> DisclaimerSection(
                    isAccepted = disclaimerAccepted,
                    onAcceptedChange = { disclaimerAccepted = it }
                )
            }

            if (currentPage > 0) {
                SecondaryButton(
                    text = stringResource(R.string.testcfg_back),
                    onClick = { currentPage -= 1 },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val canContinue = when (currentPage) {
                3 -> bankAndSimComplete
                4 -> bankAndSimComplete && disclaimerAccepted
                else -> true
            }
            PrimaryButton(
                text = stringResource(
                    when (currentPage) {
                        0 -> R.string.setup_get_started
                        4 -> R.string.complete_setup
                        else -> R.string.setup_permission_continue
                    }
                ),
                onClick = {
                    if (currentPage < 4) {
                        currentPage += 1
                    } else {
                        setupHelper.completeSetup(
                            SetupHelper.SetupData(
                                selectedBank = selectedBank,
                                selectedPrimarySim = selectedPrimarySim,
                                isDualSimEnabled = isDualSimEnabled,
                                selectedSecondarySim = selectedSecondarySim,
                                disclaimerAccepted = disclaimerAccepted
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue
            )
        }
    }
}

@Composable
private fun OnboardingProgress(activeStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)
    ) {
        repeat(6) { step ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (step < activeStep) WavePayBrand else WavePayOutline,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PermissionInfoPage(
    title: String,
    reason: String,
    icon: ImageVector
) {
    SectionCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(WavePayBrandTint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WavePayBrand,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = WavePayInk)
                Text(reason, style = MaterialTheme.typography.bodyLarge, color = WavePaySecondaryText)
            }
        }
        Spacer(Modifier.height(Spacing.medium))
        InfoBanner(
            message = stringResource(R.string.setup_permission_timing),
            tone = InfoBannerTone.NEUTRAL
        )
    }
}

@Composable
fun HeaderCard() {
    WaveHeader(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(WavePayOnBrand.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = WavePayOnBrand,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = stringResource(R.string.setup_flowpay),
                style = MaterialTheme.typography.headlineLarge,
                color = WavePayOnBrand
            )
            Text(
                text = stringResource(R.string.setup_welcome_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = WavePayOnBrand
            )
        }
    }
}

@Composable
private fun SetupSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(WavePayBrandTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WavePayBrand,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = WavePayInk)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = WavePaySecondaryText)
        }
    }
}

@Composable
private fun SetupFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = WavePaySecondaryText,
        modifier = Modifier.padding(bottom = Spacing.small)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSelectionSection(
    banks: List<Pair<String, String>>,
    selectedBank: String,
    onBankSelected: (String) -> Unit
) {
    SectionCard {
        SetupSectionHeader(
            icon = Icons.Rounded.AccountBalance,
            title = stringResource(R.string.bank_selection),
            subtitle = stringResource(R.string.choose_primary_bank)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SetupFieldLabel(stringResource(R.string.setup_select_bank))

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = banks.find { it.first == selectedBank }?.second
                    ?: stringResource(R.string.setup_choose_your_bank),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WavePayBrand,
                    unfocusedBorderColor = WavePayOutline,
                    focusedContainerColor = WavePaySurface,
                    unfocusedContainerColor = WavePaySurface,
                    focusedTextColor = WavePayInk,
                    unfocusedTextColor = WavePayInk,
                    focusedTrailingIconColor = WavePaySecondaryText,
                    unfocusedTrailingIconColor = WavePaySecondaryText
                ),
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.bodyLarge,
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(WavePaySurface)
            ) {
                banks.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                label,
                                color = WavePayInk,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onBankSelected(value)
                            expanded = false
                        },
                        modifier = Modifier.background(WavePaySurface)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimCardSelectionSection(
    simCarriers: List<Pair<String, String>>,
    selectedPrimarySim: String,
    selectedSecondarySim: String,
    isDualSimEnabled: Boolean,
    onPrimarySimSelected: (String) -> Unit,
    onSecondarySimSelected: (String) -> Unit,
    onDualSimToggled: (Boolean) -> Unit,
    secondarySimOptions: List<Pair<String, String>>
) {
    SectionCard {
        SetupSectionHeader(
            icon = Icons.Rounded.SimCard,
            title = stringResource(R.string.sim_card_selection),
            subtitle = stringResource(R.string.configure_sim_cards)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SetupFieldLabel(stringResource(R.string.setup_primary_sim))

        // Primary SIM Selection
        var primaryExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = primaryExpanded,
            onExpandedChange = { primaryExpanded = !primaryExpanded }
        ) {
            OutlinedTextField(
                value = simCarriers.find { it.first == selectedPrimarySim }?.second
                    ?: stringResource(R.string.setup_select_primary_sim),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = primaryExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WavePayBrand,
                    unfocusedBorderColor = WavePayOutline,
                    focusedContainerColor = WavePaySurface,
                    unfocusedContainerColor = WavePaySurface,
                    focusedTextColor = WavePayInk,
                    unfocusedTextColor = WavePayInk,
                    focusedTrailingIconColor = WavePaySecondaryText,
                    unfocusedTrailingIconColor = WavePaySecondaryText
                ),
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.bodyLarge,
            )

            ExposedDropdownMenu(
                expanded = primaryExpanded,
                onDismissRequest = { primaryExpanded = false },
                modifier = Modifier.background(WavePaySurface)
            ) {
                simCarriers.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                label,
                                color = WavePayInk,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onPrimarySimSelected(value)
                            primaryExpanded = false
                        },
                        modifier = Modifier.background(WavePaySurface)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dual SIM Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Spacing.touchTarget)
                .toggleable(
                    value = isDualSimEnabled,
                    role = Role.Checkbox,
                    onValueChange = onDualSimToggled
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDualSimEnabled,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = WavePayBrand,
                    uncheckedColor = WavePayOutline
                )
            )
            Text(
                text = stringResource(R.string.enable_dual_sim),
                style = MaterialTheme.typography.bodyLarge,
                color = WavePayInk
            )
        }

        // Secondary SIM Section
        if (isDualSimEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = WavePayOutline,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            SetupFieldLabel(stringResource(R.string.secondary_sim))

            // Secondary SIM Selection
            var secondaryExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = secondaryExpanded,
                onExpandedChange = { secondaryExpanded = !secondaryExpanded }
            ) {
                OutlinedTextField(
                    value = secondarySimOptions.find { it.first == selectedSecondarySim }?.second
                        ?: stringResource(R.string.setup_select_secondary_sim),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = secondaryExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WavePayBrand,
                        unfocusedBorderColor = WavePayOutline,
                        focusedContainerColor = WavePaySurface,
                        unfocusedContainerColor = WavePaySurface,
                        focusedTextColor = WavePayInk,
                        unfocusedTextColor = WavePayInk,
                        focusedTrailingIconColor = WavePaySecondaryText,
                        unfocusedTrailingIconColor = WavePaySecondaryText
                    ),
                    shape = MaterialTheme.shapes.medium,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )

                ExposedDropdownMenu(
                    expanded = secondaryExpanded,
                    onDismissRequest = { secondaryExpanded = false },
                    modifier = Modifier.background(WavePaySurface)
                ) {
                    secondarySimOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    label,
                                    color = WavePayInk,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onSecondarySimSelected(value)
                                secondaryExpanded = false
                            },
                            modifier = Modifier.background(WavePaySurface)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DisclaimerSection(
    isAccepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    SectionCard {
        SetupSectionHeader(
            icon = Icons.Rounded.Info,
            title = stringResource(R.string.disclaimer),
            subtitle = stringResource(R.string.setup_please_read_before_continuing)
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Checkbox(
                checked = isAccepted,
                onCheckedChange = onAcceptedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = WavePayBrand,
                    uncheckedColor = WavePayOutline
                )
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text(
                    text = stringResource(
                        if (isExpanded) R.string.disclaimer_text else R.string.disclaimer_summary
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = WavePaySecondaryText
                )
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(
                        text = stringResource(
                            if (isExpanded) R.string.setup_show_less else R.string.setup_show_more
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = WavePayBrand
                    )
                }
            }
        }
    }
}

@Composable
fun CompleteSetupButton(
    enabled: Boolean,
    onCompleteSetup: () -> Unit
) {
    PrimaryButton(
        text = stringResource(R.string.complete_setup),
        onClick = onCompleteSetup,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}
