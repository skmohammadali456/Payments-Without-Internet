// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay

package com.flowpay.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.flowpay.app.constants.PermissionConstants
import com.flowpay.app.helpers.SetupHelper
import com.flowpay.app.helpers.TestConfigurationHelper
import com.flowpay.app.managers.CallType
import com.flowpay.app.ui.dialogs.Upi123ProgressDialog
import com.flowpay.app.ui.dialogs.UssdProgressDialog
import com.flowpay.app.ui.components.PrimaryButton
import com.flowpay.app.ui.components.SectionCard
import com.flowpay.app.ui.components.SecondaryButton
import com.flowpay.app.ui.components.WaveHeader
import com.flowpay.app.ui.theme.FlowpayAccentGreen
import com.flowpay.app.ui.theme.FlowpayStatusWarning
import com.flowpay.app.ui.theme.Spacing
import com.flowpay.app.ui.theme.FlowpayTheme
import com.flowpay.app.ui.theme.WavePayBrand
import com.flowpay.app.ui.theme.WavePayBrandTint
import com.flowpay.app.ui.theme.WavePayCanvas
import com.flowpay.app.ui.theme.WavePayInk
import com.flowpay.app.ui.theme.WavePayOnBrand
import com.flowpay.app.ui.theme.WavePayOutline
import com.flowpay.app.ui.theme.WavePaySecondaryText
import com.flowpay.app.ui.theme.WavePaySurface
import kotlinx.coroutines.delay

class TestConfigurationActivity : ComponentActivity() {
    private lateinit var testHelper: TestConfigurationHelper

    // Phone-call permission group, requested before a test dial. No auto-retry:
    // the user re-taps the test action once granted.
    private val phonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        testHelper.onPhonePermissionsResult(results.values.all { it })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize test helper
        testHelper = TestConfigurationHelper(
            this,
            object : TestConfigurationHelper.UICallback {
                override fun showToast(message: String) {
                    runOnUiThread {
                        android.widget.Toast.makeText(this@TestConfigurationActivity, message, android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                override fun updateUssdTesting(isTesting: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateUssdDialog(show: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateUssdTestCompleted(completed: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateUpi123Testing(isTesting: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateUpi123TestCompleted(completed: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateUpi123Dialog(show: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateUpi123ConfigurationOptions(show: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateVoiceTesting(isTesting: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateVoiceDialog(show: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateVoiceTestCompleted(completed: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateCallCompleteButton(show: Boolean) {
                    // State will be managed by the composable
                }

                override fun updateUssdProgressMessage(message: String) {
                    // State will be managed by the composable
                }

                override fun updateUssdConfigurationOptions(show: Boolean) {
                    // State will be managed by the composable
                }

                override fun navigateToMain() {
                    val intent = Intent(this@TestConfigurationActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun requestPhonePermissions() {
                    phonePermissionLauncher.launch(PermissionConstants.PHONE_PERMISSIONS)
                }
            }
        )

        // Initialize the helper
        testHelper.initialize()

        setTheme(R.style.Theme_Flowpay)
        // Edge-to-edge: Compose insets are the single source of padding (see MainActivity).
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FlowpayTheme {
                TestConfigurationScreen(testHelper = testHelper)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregisters the CallManager's PhoneStateListener and cancels
        // pending timeout runnables — without this, a listener registered
        // for an in-flight test call leaks past the screen.
        if (::testHelper.isInitialized) {
            testHelper.cleanup()
        }
    }
}

@Composable
fun TestConfigurationScreen(testHelper: TestConfigurationHelper) {
    val context = LocalContext.current

    // Get test states from helper
    val testStates = testHelper.getTestStates()
    var ussdTestCompleted by remember { mutableStateOf(testStates.ussdTestCompleted) }
    var upi123TestCompleted by remember { mutableStateOf(testStates.upi123TestCompleted) }
    var ussdTesting by remember { mutableStateOf(testStates.ussdTesting) }
    var upi123Testing by remember { mutableStateOf(testStates.upi123Testing) }
    // Holds the pending real-call dial action until the user consents (a test
    // dial places a real *99#/UPI 123 call that may incur carrier charges).
    var pendingDial by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showUssdDialog by remember { mutableStateOf(testStates.showUssdDialog) }
    var showUpi123Dialog by remember { mutableStateOf(testStates.showUpi123Dialog) }
    var showUssdConfigurationOptions by remember { mutableStateOf(testStates.showUssdConfigurationOptions) }
    var showUpi123ConfigurationOptions by remember { mutableStateOf(testStates.showUpi123ConfigurationOptions) }
    var ussdProgressMessage by remember { mutableStateOf(testStates.ussdProgressMessage) }
    var showCallCompleteButton by remember { mutableStateOf(testStates.showCallCompleteButton) }
    var currentTestStep by remember { mutableStateOf(0) }
    val isJioSim = !SetupHelper.isPrimarySimUssdCapable(context)
    val userReportedUssdIssue = SetupHelper.hasUserReportedUssdNotWorking(context)

    // Load existing test results
    LaunchedEffect(Unit) {
        val existingResults = testHelper.getTestResults()
        if (existingResults != null) {
            ussdTestCompleted = existingResults.ussdEnabled
            upi123TestCompleted = existingResults.upi123Enabled
        }
    }

    // Update states when helper states change
    LaunchedEffect(testStates) {
        ussdTestCompleted = testStates.ussdTestCompleted
        upi123TestCompleted = testStates.upi123TestCompleted
        ussdTesting = testStates.ussdTesting
        upi123Testing = testStates.upi123Testing
        showUssdDialog = testStates.showUssdDialog
        showUpi123Dialog = testStates.showUpi123Dialog
        showUssdConfigurationOptions = testStates.showUssdConfigurationOptions
        showUpi123ConfigurationOptions = testStates.showUpi123ConfigurationOptions
        ussdProgressMessage = testStates.ussdProgressMessage
        showCallCompleteButton = testStates.showCallCompleteButton
    }

    // Add a periodic state check to ensure UI updates
    LaunchedEffect(Unit) {
        while (true) {
            delay(100) // Check every 100ms
            val currentStates = testHelper.getTestStates()
            ussdTestCompleted = currentStates.ussdTestCompleted
            upi123TestCompleted = currentStates.upi123TestCompleted
            ussdTesting = currentStates.ussdTesting
            upi123Testing = currentStates.upi123Testing
            showUssdDialog = currentStates.showUssdDialog
            showUpi123Dialog = currentStates.showUpi123Dialog
            showUssdConfigurationOptions = currentStates.showUssdConfigurationOptions
            showUpi123ConfigurationOptions = currentStates.showUpi123ConfigurationOptions
            ussdProgressMessage = currentStates.ussdProgressMessage
            showCallCompleteButton = currentStates.showCallCompleteButton
        }
    }

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
                .padding(horizontal = Spacing.medium, vertical = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            TextButton(
                onClick = {
                    context.startActivity(Intent(context, SetupActivity::class.java))
                    (context as? android.app.Activity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start),
                contentPadding = PaddingValues(horizontal = Spacing.small, vertical = Spacing.tiny)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = WavePaySecondaryText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = stringResource(R.string.testcfg_back_to_setup),
                    style = MaterialTheme.typography.labelLarge,
                    color = WavePaySecondaryText
                )
            }

            TestHeaderCard()
            TestInstructions()

            Text(
                text = stringResource(R.string.testcfg_substep, currentTestStep + 1),
                style = MaterialTheme.typography.labelLarge,
                color = WavePaySecondaryText
            )
            TestSubstepProgress(currentStep = currentTestStep)

            if (currentTestStep == 0) {
                Text(
                    text = stringResource(R.string.testcfg_step_ussd_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = WavePayInk
                )
                TestButton(
                    title = stringResource(R.string.testcfg_set_up),
                    code = stringResource(R.string.testcfg_code_ussd),
                    description = when {
                        isJioSim -> stringResource(R.string.testcfg_jio_no_ussd)
                        userReportedUssdIssue -> stringResource(R.string.testcfg_ussd_reported_issue)
                        else -> stringResource(R.string.testcfg_enable_scan_payments)
                    },
                    isCompleted = ussdTestCompleted || isJioSim || userReportedUssdIssue,
                    isTesting = ussdTesting,
                    isUnsupported = isJioSim,
                    onClick = {
                        if (isJioSim) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.testcfg_jio_no_ussd),
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (!ussdTestCompleted && !ussdTesting) {
                            // Ask before placing a real *99# call. The consent
                            // dialog runs this action on confirm; ussdTesting is
                            // flipped there to guard against a double-dial.
                            pendingDial = {
                                ussdTesting = true
                                testHelper.initiateCall(CallType.USSD)
                            }
                        }
                    }
                )
                PrimaryButton(
                    text = stringResource(R.string.testcfg_next_upi123),
                    onClick = { currentTestStep = 1 },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = stringResource(R.string.testcfg_step_upi123_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = WavePayInk
                )
                TestButton(
                    title = stringResource(R.string.testcfg_set_up),
                    code = stringResource(R.string.testcfg_code_upi123),
                    description = stringResource(R.string.testcfg_enable_manual_payments),
                    isCompleted = upi123TestCompleted,
                    isTesting = upi123Testing,
                    onClick = {
                        if (!upi123TestCompleted && !upi123Testing) {
                            pendingDial = {
                                upi123Testing = true
                                testHelper.initiateUpi123Test()
                            }
                        }
                    }
                )
                SecondaryButton(
                    text = stringResource(R.string.testcfg_back_to_ussd),
                    onClick = { currentTestStep = 0 },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val canContinue = testHelper.canContinue()
            val allTestsCompleted = testHelper.allTestsCompleted()
            PrimaryButton(
                text = stringResource(
                    when {
                        allTestsCompleted -> R.string.testcfg_all_tests_passed_continue
                        canContinue -> R.string.testcfg_continue_partial
                        else -> R.string.testcfg_complete_tests_to_continue
                    }
                ),
                onClick = { testHelper.continueToMain() },
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue
            )

            // Skip path: a failed/hanging *99# test must never trap the
            // user on this screen. Skipping persists completion and the
            // tests can be re-run later from Settings > Reconfigure.
            TextButton(
                onClick = { testHelper.skipTests() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.testcfg_skip_for_now),
                    style = MaterialTheme.typography.labelLarge,
                    color = WavePaySecondaryText
                )
            }
        }

        // USSD Progress Dialog
        UssdProgressDialog(
            isVisible = showUssdDialog || showUssdConfigurationOptions,
            progressMessage = ussdProgressMessage,
            showConfigurationOptions = showUssdConfigurationOptions,
            onConfigured = { testHelper.handleUssdConfigurationConfirmation(true) },
            onNotConfigured = { testHelper.handleUssdConfigurationConfirmation(false) },
            onDismiss = { testHelper.dismissUssdDialog(fromDoesNotWork = false) },
            onDoesNotWork = { testHelper.dismissUssdDialog(fromDoesNotWork = true) }
        )

        // UPI123 Progress Dialog
        Upi123ProgressDialog(
            isVisible = showUpi123Dialog || showUpi123ConfigurationOptions,
            showConfigurationOptions = showUpi123ConfigurationOptions,
            onConfigured = { testHelper.handleUpi123ConfigurationConfirmation(true) },
            onNotConfigured = { testHelper.handleUpi123ConfigurationConfirmation(false) },
            onDismiss = { testHelper.dismissUpi123Dialog() }
        )

        // Consent before placing a real test call to the carrier.
        pendingDial?.let { dial ->
            AlertDialog(
                onDismissRequest = { pendingDial = null },
                containerColor = WavePaySurface,
                titleContentColor = WavePayInk,
                textContentColor = WavePaySecondaryText,
                title = {
                    Text(
                        stringResource(R.string.testcfg_consent_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.testcfg_call_consent_body),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        pendingDial = null
                        dial()
                    }) {
                        Text(
                            stringResource(R.string.action_continue),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDial = null }) {
                        Text(
                            stringResource(R.string.action_cancel),
                            color = WavePaySecondaryText,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun TestHeaderCard() {
    WaveHeader(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(WavePayOnBrand.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = WavePayOnBrand,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.tiny)
                ) {
                    Text(
                        text = stringResource(R.string.testcfg_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = WavePayOnBrand
                    )
                    Text(
                        text = stringResource(R.string.testcfg_step),
                        style = MaterialTheme.typography.labelLarge,
                        color = WavePayOnBrand.copy(alpha = 0.86f)
                    )
                }
            }

            Text(
                text = stringResource(R.string.testcfg_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = WavePayOnBrand
            )
        }
    }
}

@Composable
private fun TestSubstepProgress(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)
    ) {
        repeat(2) { step ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (step <= currentStep) WavePayBrand else WavePayOutline,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun TestInstructions() {
    SectionCard {
        Text(
            text = stringResource(R.string.testcfg_instructions_title),
            style = MaterialTheme.typography.titleMedium,
            color = WavePayInk
        )
        Text(
            text = stringResource(R.string.testcfg_instructions_body),
            style = MaterialTheme.typography.bodyLarge,
            color = WavePaySecondaryText
        )
    }
}

@Composable
fun TestButton(
    title: String,
    code: String,
    description: String,
    isCompleted: Boolean,
    isTesting: Boolean,
    isUnsupported: Boolean = false,
    onClick: () -> Unit
) {
    val iconBgColor = when {
        isUnsupported -> FlowpayStatusWarning.copy(alpha = 0.15f)
        isCompleted -> FlowpayAccentGreen.copy(alpha = 0.15f)
        else -> WavePayBrandTint
    }
    val iconTint = when {
        isUnsupported -> FlowpayStatusWarning
        isCompleted -> FlowpayAccentGreen
        else -> WavePayBrand
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, WavePayOutline),
        colors = CardDefaults.cardColors(containerColor = WavePaySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Spacing.touchTarget)
                .padding(horizontal = Spacing.medium, vertical = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBgColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (code == stringResource(R.string.testcfg_code_ussd)) {
                        Icons.Rounded.Call
                    } else {
                        Icons.Rounded.AccountBalanceWallet
                    },
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.tiny)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isUnsupported) FlowpayStatusWarning else WavePayInk
                )
                Text(
                    text = code,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isUnsupported) FlowpayStatusWarning else WavePayBrand,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnsupported) {
                        FlowpayStatusWarning
                    } else {
                        WavePaySecondaryText
                    }
                )
            }

            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isTesting -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = WavePayBrand,
                            strokeWidth = 2.5.dp,
                            trackColor = WavePayOutline
                        )
                    }
                    isUnsupported -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(FlowpayStatusWarning, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.testcfg_unsupported_symbol),
                                style = MaterialTheme.typography.labelLarge,
                                color = WavePayInk
                            )
                        }
                    }
                    isCompleted -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(FlowpayAccentGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.testcfg_status_completed),
                                tint = WavePayOnBrand,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(1.5.dp, WavePayOutline, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

// Configuration Dialogs - Now integrated into progress dialogs

// Custom Icons
val CheckCircleIcon: ImageVector
    get() {
        return ImageVector.Builder(
            name = "check_circle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Full circle centered at (12,12)
            path(
                fill = null,
                stroke = androidx.compose.ui.graphics.SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            ) {
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, false, true, 3f, 12f)
                arcTo(9f, 9f, 0f, false, true, 21f, 12f)
                close()
            }
            // Centered checkmark
            path(
                fill = null,
                stroke = androidx.compose.ui.graphics.SolidColor(Color.White),
                strokeLineWidth = 2.5f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            ) {
                moveTo(8f, 12f)
                lineTo(11f, 15f)
                lineTo(16f, 9f)
            }
        }.build()
    }

val UssdIcon: ImageVector
    get() {
        return ImageVector.Builder(
            name = "ussd_phone",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Material Design phone icon (filled)
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.White)
            ) {
                moveTo(6.62f, 10.79f)
                curveTo(8.06f, 13.62f, 10.38f, 15.93f, 13.21f, 17.38f)
                lineTo(15.41f, 15.18f)
                curveTo(15.68f, 14.91f, 16.08f, 14.82f, 16.43f, 14.94f)
                curveTo(17.55f, 15.31f, 18.76f, 15.51f, 20f, 15.51f)
                curveTo(20.55f, 15.51f, 21f, 15.96f, 21f, 16.51f)
                lineTo(21f, 20f)
                curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
                curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
                curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f)
                lineTo(7.5f, 3f)
                curveTo(8.05f, 3f, 8.5f, 3.45f, 8.5f, 4f)
                curveTo(8.5f, 5.25f, 8.7f, 6.45f, 9.07f, 7.57f)
                curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f)
                lineTo(6.62f, 10.79f)
                close()
            }
        }.build()
    }

val UpiIcon: ImageVector
    get() {
        return ImageVector.Builder(
            name = "upi_payment",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Material Design credit card / payment icon (filled)
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.White)
            ) {
                moveTo(20f, 4f)
                lineTo(4f, 4f)
                curveTo(2.89f, 4f, 2.01f, 4.89f, 2.01f, 6f)
                lineTo(2f, 18f)
                curveTo(2f, 19.11f, 2.89f, 20f, 4f, 20f)
                lineTo(20f, 20f)
                curveTo(21.11f, 20f, 22f, 19.11f, 22f, 18f)
                lineTo(22f, 6f)
                curveTo(22f, 4.89f, 21.11f, 4f, 20f, 4f)
                close()
                moveTo(20f, 18f)
                lineTo(4f, 18f)
                lineTo(4f, 12f)
                lineTo(20f, 12f)
                lineTo(20f, 18f)
                close()
                moveTo(20f, 8f)
                lineTo(4f, 8f)
                lineTo(4f, 6f)
                lineTo(20f, 6f)
                lineTo(20f, 8f)
                close()
            }
        }.build()
    }

val CheckIcon: ImageVector
    get() {
        return ImageVector.Builder(
            name = "check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = androidx.compose.ui.graphics.SolidColor(Color.White),
                strokeLineWidth = 3f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            ) {
                moveTo(6f, 12f)
                lineTo(10f, 16f)
                lineTo(18f, 8f)
            }
        }.build()
    }
