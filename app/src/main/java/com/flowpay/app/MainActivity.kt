// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay
//
// Home screen: the entry point to both payment rails (Scan QR and Pay
// Contact), recent payments, and settings. UI only — the transfer
// orchestration and permission gating live in MainActivityHelper.

package com.flowpay.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PermContactCalendar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flowpay.app.constants.AppConstants
import com.flowpay.app.constants.PermissionConstants
import com.flowpay.app.data.PaymentDetails
import com.flowpay.app.data.PaymentStatus
import com.flowpay.app.data.TestResultsManager
import com.flowpay.app.helpers.MainActivityHelper
import com.flowpay.app.managers.PermissionManager
import com.flowpay.app.payment.Upi123CallStringBuilder
import com.flowpay.app.payment.messageFor
import com.flowpay.app.ui.activities.SettingsActivity
import com.flowpay.app.ui.activities.TransactionHistoryActivity
import com.flowpay.app.ui.components.TransactionDetailDialog
import com.flowpay.app.ui.components.StatusIndicator
import com.flowpay.app.ui.dialogs.ContactPickerDialog
import com.flowpay.app.ui.theme.BlueAccentTheme
import com.flowpay.app.ui.theme.FlowpaySurface
import com.flowpay.app.ui.theme.FlowpayOnSurface
import com.flowpay.app.ui.theme.FlowpayAccent
import com.flowpay.app.ui.theme.FlowpayLightGray
import com.flowpay.app.ui.theme.FlowpayMediumGray
import com.flowpay.app.ui.theme.FlowpayOutlineGray
import com.flowpay.app.ui.theme.FlowpayStatusError
import com.flowpay.app.ui.theme.FlowpaySurfaceDim
import com.flowpay.app.ui.theme.FlowpayTextGray
import com.flowpay.app.ui.theme.FlowpayTextSecondary
import com.flowpay.app.ui.theme.FlowpayTextPale
import com.flowpay.app.ui.theme.FlowpayTheme
import com.flowpay.app.ui.theme.LocalFlowpayAccentTheme
import com.flowpay.app.ui.theme.Spacing
import com.flowpay.app.utils.CurrencyFormat
import com.flowpay.app.utils.findComponentActivity
import com.flowpay.app.viewmodel.MainUiEvent
import com.flowpay.app.viewmodel.MainViewModel
import com.flowpay.app.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "Flowpay"
    }

    // Helper for all business logic
    private lateinit var helper: MainActivityHelper

    // Shared with MainScreen (same instance via Compose viewModel()); carries
    // one-shot Activity -> Compose events, replacing the old static callbacks.
    private val mainViewModel: MainViewModel by viewModels()

    // Launches QRScannerActivity and, on return, un-sticks the QR button's
    // "Opening..." state via a QrScannerClosed event.
    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        mainViewModel.onQrScannerClosed()
    }

    // Launches the system "draw over other apps" settings screen; on return,
    // re-checks the permission and reports the outcome via toast (there is
    // no reliable resultCode for this settings screen, so re-checking is
    // the only correct way to know what happened).
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val granted = PermissionManager.canDrawOverlays(this)
        val message = if (granted) {
            getString(R.string.overlay_permission_granted)
        } else {
            getString(R.string.overlay_permission_required)
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Phone-call permission group, requested before dialing or QR scanning.
    // There is no auto-retry: the user re-taps the action once granted.
    private val phonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        if (!granted) {
            Toast.makeText(this, R.string.error_permissions_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_Flowpay)

        // Draw edge-to-edge so Compose's statusBarsPadding()/navigationBarsPadding()
        // are the single source of inset padding. The theme previously also set
        // android:fitsSystemWindows=true, which made the decor pad the content as
        // well — a double inset that, depending on inset-dispatch timing, showed
        // intermittent black bars at the top and bottom.
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        // Light neutral system bars from the first frame.
        val systemSurface = ContextCompat.getColor(this, R.color.screen_background)
        window.statusBarColor = systemSurface
        window.navigationBarColor = systemSurface
        window.setBackgroundDrawableResource(R.color.screen_background)
        window.decorView.setBackgroundColor(systemSurface)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.decorView.post { enforceBlackStatusBar() }

        // Initialize helper with UI callbacks (matches the 6-method UICallback)
        helper = MainActivityHelper(
            this,
            object : MainActivityHelper.UICallback {
                override fun showToast(message: String) {
                    runOnUiThread { Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show() }
                }

                override fun updatePaymentState(paymentState: com.flowpay.app.states.PaymentState) {
                    Log.d(TAG, "Payment state updated: ${paymentState::class.simpleName}")
                }

                override fun navigateToSetup() {
                    startActivity(Intent(this@MainActivity, SetupActivity::class.java))
                    finish()
                }

                override fun navigateToTestConfiguration() {
                    startActivity(Intent(this@MainActivity, TestConfigurationActivity::class.java))
                    finish()
                }

                override fun finishActivity() {
                    finish()
                }

                override fun showOverlayPermissionExplanation() {
                    mainViewModel.onOverlayPermissionNeeded()
                }

                override fun launchQRScanner(intent: Intent) {
                    qrScannerLauncher.launch(intent)
                }

                override fun requestPhonePermissions() {
                    phonePermissionLauncher.launch(PermissionConstants.PHONE_PERMISSIONS)
                }
            }
        )

        helper.initialize()

        // The launch gate must know which screen to show before rendering, so
        // these two SharedPreferences flags are read synchronously. Reading
        // them faults the prefs file in once (a small, one-time disk read);
        // annotate it as permitted so the debug StrictMode tripwire stays
        // sharp for genuinely unexpected main-thread disk I/O instead of
        // crying wolf on this known-safe read every launch.
        val setupCompleted: Boolean
        val testCompleted: Boolean
        val oldPolicy = android.os.StrictMode.allowThreadDiskReads()
        try {
            setupCompleted = helper.isSetupCompleted()
            testCompleted = helper.isTestCompleted()
        } finally {
            android.os.StrictMode.setThreadPolicy(oldPolicy)
        }

        if (!setupCompleted) {
            helper.navigateToSetup()
            return
        }
        if (!testCompleted) {
            helper.navigateToTestConfiguration()
            return
        }

        setContent {
            CompositionLocalProvider(LocalFlowpayAccentTheme provides BlueAccentTheme) {
                FlowpayTheme {
                    MainScreen(
                        onInitiateTransfer = { phoneNumber, amount ->
                            helper.initiateTransfer(phoneNumber, amount)
                        },
                        onQRScanClick = {
                            helper.startQRScanning()
                        },
                        onRequestOverlayPermission = {
                            PermissionManager(this).overlayPermissionSettingsIntent()?.let {
                                overlayPermissionLauncher.launch(it)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        helper.onPause()
    }

    override fun onResume() {
        super.onResume()
        helper.onResume()
        enforceBlackStatusBar()
    }

    private fun enforceBlackStatusBar() {
        val systemSurface = ContextCompat.getColor(this, R.color.screen_background)
        window.statusBarColor = systemSurface
        window.navigationBarColor = systemSurface
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    override fun onStop() {
        super.onStop()
        helper.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        helper.onDestroy()
    }
}

// Payment Action Buttons - QR scan + Pay Contact.
//
// Each rail is locked until its own connectivity test has passed: Scan QR
// dials *99#, Pay Contact dials the UPI 123 IVR. One composable per button,
// because the locked/unlocked branches on both pushed the combined function
// past detekt's complexity and parameter limits.
@Composable
fun PaymentActionButtons(
    onQRScanClick: () -> Unit,
    onPayContactClick: () -> Unit,
    isUpi123Ready: Boolean,
    isUssdReady: Boolean,
    isScanning: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        ScanQrButton(onQRScanClick, isUssdReady, isScanning)
        PayContactButton(onPayContactClick, isUpi123Ready)
    }
}

/** Scan QR — locked until the *99# connectivity test has passed. */
@Composable
private fun ScanQrButton(
    onQRScanClick: () -> Unit,
    isUssdReady: Boolean,
    isScanning: Boolean
) {
    val label = when {
        !isUssdReady -> stringResource(R.string.home_setup_ussd)
        isScanning -> stringResource(R.string.home_scan_opening)
        else -> stringResource(R.string.home_scan_qr)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.touchTarget)
            .clip(RoundedCornerShape(12.dp))
            .background(FlowpayAccent)
            .clickable(enabled = !isScanning, onClick = onQRScanClick)
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isUssdReady) Icons.Default.QrCodeScanner else Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}

/** Pay Contact — locked until the UPI 123 IVR connectivity test has passed. */
@Composable
private fun PayContactButton(
    onPayContactClick: () -> Unit,
    isUpi123Ready: Boolean
) {
    val accent = LocalFlowpayAccentTheme.current.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.touchTarget)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, FlowpayLightGray, RoundedCornerShape(12.dp))
            .clickable(onClick = onPayContactClick)
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isUpi123Ready) Icons.Default.Person else Icons.Default.Lock,
            contentDescription = null,
            tint = accent
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = stringResource(
                if (isUpi123Ready) R.string.home_pay_by_number else R.string.upi123_dlg_already_set_up
            ),
            style = MaterialTheme.typography.titleMedium,
            color = accent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onInitiateTransfer: (String, String) -> Unit,
    onQRScanClick: () -> Unit,
    onRequestOverlayPermission: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var savedBank by remember {
        mutableStateOf(sharedPreferences.getString(AppConstants.KEY_SELECTED_BANK, "hdfc") ?: "hdfc")
    }

    // Pay Contact dials the UPI 123 IVR, so it stays inactive until the
    // UPI 123 configuration test has passed (re-checked on every resume so
    // completing the test activates it immediately).
    val testResultsManager = remember { TestResultsManager(context) }
    var isUpi123Ready by remember {
        mutableStateOf(testResultsManager.getTestResults()?.upi123Enabled == true)
    }

    // Scan QR dials *99#, so it stays locked until the *99# test has passed —
    // the same gate Pay Contact has always had for the UPI 123 IVR. Without
    // it the scanner opened, read a QR, and only then dialled a rail this SIM
    // may not support.
    var isUssdReady by remember {
        mutableStateOf(testResultsManager.getTestResults()?.ussdEnabled == true)
    }

    LaunchedEffect(lifecycle) {
        snapshotFlow { lifecycle.currentState }.collect { state ->
            if (state == Lifecycle.State.RESUMED) {
                savedBank = sharedPreferences.getString(AppConstants.KEY_SELECTED_BANK, "hdfc") ?: "hdfc"
                isUpi123Ready = testResultsManager.getTestResults()?.upi123Enabled == true
                isUssdReady = testResultsManager.getTestResults()?.ussdEnabled == true
            }
        }
    }

    val transactionViewModel: TransactionViewModel = viewModel()
    val recentPayments by transactionViewModel.recentTransactions.collectAsState()
    val recentPayees = remember(recentPayments) {
        recentPayments
            .filter { it.phoneNumber.isNotBlank() }
            .distinctBy { it.phoneNumber }
            .take(6)
    }
    val selectedTransaction by transactionViewModel.selectedTransaction.collectAsState()
    val isLoading by transactionViewModel.isLoading.collectAsState()
    val error by transactionViewModel.error.collectAsState()

    var showPayContact by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var prefilledPayee by remember { mutableStateOf<Pair<String?, String?>?>(null) }

    var showSmsPermissionDialog by remember { mutableStateOf(false) }
    var pendingSmsAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    val hostActivity = remember(context) { context.findComponentActivity() }
    val permissionManager = remember(hostActivity) {
        hostActivity?.let { PermissionManager(it) }
    }

    // Runs the queued action (start scan / open pay dialog / initiate transfer)
    // once RECEIVE_SMS is granted; the launcher stays Compose-scoped so no
    // Activity-level callback bridge is needed.
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingSmsAction?.invoke()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.error_sms_permission),
                Toast.LENGTH_LONG
            ).show()
        }
        pendingSmsAction = null
    }

    // POST_NOTIFICATIONS backs the payment-outcome notification — the
    // fallback the result screen relies on when its direct background launch
    // is blocked (see PaymentResultNotifier). It is best-effort, not a
    // prerequisite, so we ask ONCE at the first payment and never block on the
    // result: the payment proceeds whether granted or not. Settings offers a
    // later toggle for anyone who declines.
    val postNotificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* best-effort: outcome recorded by the OS; nothing to do here */ }
    val maybeAskNotifications = remember(context) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val alreadyAsked = sharedPreferences.getBoolean(AppConstants.KEY_NOTIFICATIONS_ASKED, false)
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!alreadyAsked && !granted) {
                    sharedPreferences.edit()
                        .putBoolean(AppConstants.KEY_NOTIFICATIONS_ASKED, true)
                        .apply()
                    postNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    fun requestPayContact(payee: PaymentDetails? = null) {
        prefilledPayee = payee?.let { it.recipientName to it.phoneNumber }
        maybeAskNotifications()
        val hasSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        when {
            !isUpi123Ready -> context.startActivity(
                Intent(context, TestConfigurationActivity::class.java)
            )
            !PermissionManager.canDrawOverlays(context) -> showOverlayPermissionDialog = true
            !hasSms -> {
                pendingSmsAction = { showPayContact = true }
                showSmsPermissionDialog = true
            }
            else -> showPayContact = true
        }
    }

    // One-shot events from MainActivity (launchers + business-logic helper),
    // replacing the former static @Volatile callbacks on its companion.
    val mainViewModel: MainViewModel = viewModel()
    LaunchedEffect(Unit) {
        mainViewModel.events.collect { event ->
            when (event) {
                MainUiEvent.QrScannerClosed -> isScanning = false
                MainUiEvent.OverlayPermissionNeeded -> showOverlayPermissionDialog = true
            }
        }
    }

    // Reset scanning + refresh list whenever the app resumes
    LaunchedEffect(lifecycle) {
        snapshotFlow { lifecycle.currentState }.collect { state ->
            if (state == Lifecycle.State.RESUMED) {
                isScanning = false
                transactionViewModel.refresh()
            }
        }
    }

    // Safety: never leave the QR button stuck in "Opening..."
    LaunchedEffect(isScanning) {
        if (isScanning) {
            kotlinx.coroutines.delay(AppConstants.USSD_SESSION_TIMEOUT)
            if (isScanning) isScanning = false
        }
    }

    val selectedBankName = when (savedBank) {
        "sbi" -> "State Bank of India"
        "hdfc" -> "HDFC Bank"
        "icici" -> "ICICI Bank"
        "axis" -> "Axis Bank"
        "kotak" -> "Kotak Mahindra Bank"
        "pnb" -> "Punjab National Bank"
        "bob" -> "Bank of Baroda"
        "yes" -> "Yes Bank"
        "idbi" -> "IDBI Bank"
        "canara" -> "Canara Bank"
        else -> "HDFC Bank"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FlowpaySurfaceDim
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FlowpaySurfaceDim)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 480.dp)
                    .align(Alignment.Center)
                    .background(FlowpaySurfaceDim)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(Spacing.medium))

                // Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(min = 168.dp)
                        .border(1.dp, FlowpayLightGray, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, shape = RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.medium)
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.home_title),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = FlowpayOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stringResource(R.string.home_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FlowpayTextSecondary
                                    )
                                }

                                // Settings Button
                                Box(
                                    modifier = Modifier
                                        .size(Spacing.touchTarget)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, FlowpayLightGray, RoundedCornerShape(12.dp))
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            context.startActivity(Intent(context, SettingsActivity::class.java))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = stringResource(R.string.settings_title),
                                        tint = FlowpayOnSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.medium))

                            // Bank Info Section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FlowpaySurfaceDim)
                                    .padding(horizontal = Spacing.medium, vertical = Spacing.small)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.home_connected_bank),
                                            fontSize = 12.sp,
                                            color = FlowpayTextSecondary,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = selectedBankName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FlowpayOnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(Spacing.touchTarget)
                                            .clip(CircleShape)
                                            .background(FlowpayMediumGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = FlowpayAccent,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = FlowpayTextGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(Spacing.small))
                    Text(
                        text = stringResource(R.string.home_works_offline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FlowpayTextGray
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                PaymentActionButtons(
                    onQRScanClick = {
                        maybeAskNotifications()
                        val hasSms = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECEIVE_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                        when {
                            // *99# not verified yet — the button is in its
                            // "Set up *99#" state; take the user to the test
                            // screen rather than opening a scanner whose
                            // payment rail has not been shown to work.
                            !isUssdReady -> {
                                context.startActivity(
                                    Intent(context, TestConfigurationActivity::class.java)
                                )
                            }
                            !hasSms -> {
                                pendingSmsAction = {
                                    isScanning = true
                                    onQRScanClick()
                                }
                                showSmsPermissionDialog = true
                            }
                            else -> {
                                isScanning = true
                                onQRScanClick()
                            }
                        }
                    },
                    onPayContactClick = {
                        requestPayContact()
                    },
                    isUpi123Ready = isUpi123Ready,
                    isUssdReady = isUssdReady,
                    isScanning = isScanning
                )

                if (recentPayees.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.medium)
                    ) {
                        Text(
                            text = stringResource(R.string.home_recent_payees),
                            style = MaterialTheme.typography.titleMedium,
                            color = FlowpayOnSurface
                        )
                        Spacer(Modifier.height(Spacing.small))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                            items(recentPayees) { payee ->
                                val name = payee.recipientName
                                    ?.takeIf(String::isNotBlank)
                                    ?: payee.phoneNumber
                                Surface(
                                    modifier = Modifier
                                        .heightIn(min = Spacing.touchTarget)
                                        .clickable { requestPayContact(payee) },
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        FlowpayLightGray
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = Spacing.medium,
                                            vertical = Spacing.small
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = FlowpayAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(Spacing.small))
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = FlowpayOnSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                // Recent Transactions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(Spacing.medium)
                            .border(0.dp, Color.Transparent, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(Spacing.touchTarget)
                                        .background(FlowpayMediumGray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = FlowpayAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.home_recent_payments),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FlowpayOnSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.home_recent_payments_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FlowpayTextSecondary
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    context.startActivity(Intent(context, TransactionHistoryActivity::class.java))
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.home_view_all),
                                    color = LocalFlowpayAccentTheme.current.accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when {
                            isLoading -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = FlowpayAccent,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = stringResource(R.string.home_loading_transactions),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FlowpayTextSecondary
                                    )
                                }
                            }

                            error != null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_failed_to_load),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FlowpayOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = error.orEmpty(),
                                        color = FlowpayTextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(onClick = { transactionViewModel.refresh() }) {
                                        Text(
                                            text = stringResource(R.string.home_retry),
                                            color = FlowpayAccent
                                        )
                                    }
                                }
                            }

                            recentPayments.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(FlowpayMediumGray, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = FlowpayTextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = stringResource(R.string.home_no_transactions),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FlowpayOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stringResource(R.string.home_history_empty),
                                        color = FlowpayTextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            else -> {
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                                    recentPayments.take(3).forEach { payment ->
                                        TransactionItem(
                                            payment = payment,
                                            onClick = {
                                                transactionViewModel.selectTransaction(payment.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Detail dialog for a tapped Recent Payments row — the same
            // surface the history screen shows.
            selectedTransaction?.let { transaction ->
                TransactionDetailDialog(
                    transaction = transaction,
                    onDismiss = { transactionViewModel.clearSelectedTransaction() },
                    onDelete = {
                        transactionViewModel.deleteTransaction(transaction)
                        transactionViewModel.clearSelectedTransaction()
                    }
                )
            }

            // Dialogs
            if (showPayContact) {
                PayContactDialog(
                    initialPhoneNumber = prefilledPayee?.second.orEmpty(),
                    initialRecipientName = prefilledPayee?.first,
                    onDismiss = { showPayContact = false },
                    onConfirm = { phone, amt ->
                        if (permissionManager?.checkSMSPermissions() != true) {
                            pendingSmsAction = {
                                showPayContact = false
                                onInitiateTransfer(phone, amt)
                            }
                            showSmsPermissionDialog = true
                        } else {
                            showPayContact = false
                            onInitiateTransfer(phone, amt)
                        }
                    }
                )
            }

            if (showOverlayPermissionDialog) {
                PermissionExplanationDialog(
                    title = stringResource(R.string.permission_overlay_title),
                    message = stringResource(R.string.permission_overlay_reason),
                    confirmButtonText = stringResource(R.string.permission_grant),
                    onConfirm = {
                        showOverlayPermissionDialog = false
                        onRequestOverlayPermission()
                    },
                    onDismiss = { showOverlayPermissionDialog = false }
                )
            }

            if (showSmsPermissionDialog) {
                PermissionExplanationDialog(
                    title = stringResource(R.string.permission_sms_title),
                    message = stringResource(R.string.permission_sms_reason),
                    confirmButtonText = stringResource(R.string.permission_grant),
                    onConfirm = {
                        showSmsPermissionDialog = false
                        // Only RECEIVE_SMS is declared in the manifest and
                        // needed (the app never reads the inbox). The launcher's
                        // callback runs pendingSmsAction once granted.
                        smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                    },
                    onDismiss = {
                        showSmsPermissionDialog = false
                        pendingSmsAction = null
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionItem(payment: PaymentDetails, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Spacing.small),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = payment.recipientName?.takeIf(String::isNotBlank) ?: payment.phoneNumber,
                    style = MaterialTheme.typography.titleSmall,
                    color = FlowpayOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(payment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = FlowpayTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
            }

            TransactionItemAmount(amount = payment.amount, status = payment.status)
        }
    }
}

@Composable
private fun TransactionItemAmount(amount: Double, status: PaymentStatus) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
        modifier = Modifier.padding(start = Spacing.small)
    ) {
        Text(
            text = stringResource(R.string.amount_rupees, CurrencyFormat.inr(amount)),
            style = MaterialTheme.typography.titleSmall,
            color = FlowpayOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        StatusIndicator(status = status.name)
    }
}

@Composable
fun PayContactDialog(
    initialPhoneNumber: String = "",
    initialRecipientName: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val context = LocalContext.current
    val hostActivity = remember(context) { context.findComponentActivity() }
    var phoneNumber by remember(initialPhoneNumber) { mutableStateOf(initialPhoneNumber) }
    var amount by remember { mutableStateOf("") }
    // UPI 123Pay's IVR will not accept 5000 or more (AppConstants
    // .UPI123PAY_MAX_AMOUNT = 4999), so the cap is enforced before dialling.
    val isOverCap = (amount.toLongOrNull() ?: 0L) > AppConstants.UPI123PAY_MAX_AMOUNT.toLong()
    var selectedContactName by remember(initialRecipientName) { mutableStateOf(initialRecipientName) }
    var showContactPicker by remember { mutableStateOf(false) }
    var showContactPermissionDialog by remember { mutableStateOf(false) }
    val permissionManager = remember(hostActivity) {
        hostActivity?.let { PermissionManager(it) }
    }
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showContactPicker = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FlowpaySurface,
        title = {
            Text(
                text = stringResource(R.string.home_pay_by_number),
                color = FlowpayOnSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                selectedContactName?.let { name ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = LocalFlowpayAccentTheme.current.accent.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LocalFlowpayAccentTheme.current.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.pay_contact_sending_to, name),
                                color = LocalFlowpayAccentTheme.current.accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                phoneNumber = it
                                selectedContactName = null
                            }
                        },
                        label = {
                            Text(stringResource(R.string.home_field_mobile_label), color = FlowpayTextSecondary)
                        },
                        placeholder = {
                            Text(stringResource(R.string.home_field_mobile_hint), color = FlowpayTextGray)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FlowpayOnSurface,
                            unfocusedTextColor = FlowpayOnSurface,
                            focusedBorderColor = FlowpayOutlineGray,
                            unfocusedBorderColor = FlowpayLightGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    IconButton(
                        onClick = {
                            val pm = permissionManager
                            if (pm == null) {
                                Toast.makeText(
                                    context.applicationContext,
                                context.getString(R.string.contacts_picker_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@IconButton
                            }
                            if (pm.hasContactPermission()) {
                                showContactPicker = true
                            } else {
                                showContactPermissionDialog = true
                            }
                        },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(48.dp)
                            .background(
                                color = LocalFlowpayAccentTheme.current.accent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PermContactCalendar,
                            contentDescription = context.getString(R.string.home_select_contact),
                            tint = LocalFlowpayAccentTheme.current.accent
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 6) {
                            amount = it
                        }
                    },
                    label = {
                        Text(stringResource(R.string.home_field_amount_label), color = FlowpayTextSecondary)
                    },
                    placeholder = {
                        Text(stringResource(R.string.home_field_amount_hint), color = FlowpayTextGray)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isOverCap,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FlowpayOnSurface,
                        unfocusedTextColor = FlowpayOnSurface,
                        focusedBorderColor = if (isOverCap) FlowpayStatusError else FlowpayOutlineGray,
                        unfocusedBorderColor = if (isOverCap) FlowpayStatusError else FlowpayLightGray,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                // Told here, in the dialog, while the number can still be
                // corrected. The IVR itself only rejects an over-cap amount
                // mid-call, after the user has already dialled.
                if (isOverCap) {
                    Text(
                        text = Upi123CallStringBuilder.Reason.AMOUNT_ABOVE_CAP.messageFor(context),
                        color = FlowpayStatusError,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            val canTransfer = phoneNumber.length == 10 && amount.isNotEmpty() &&
                amount != "0" && !isOverCap
            TextButton(
                onClick = { onConfirm(phoneNumber, amount) },
                enabled = canTransfer
            ) {
                Text(
                    stringResource(R.string.action_transfer),
                    color = if (canTransfer) {
                        LocalFlowpayAccentTheme.current.accent
                    } else {
                        FlowpayTextGray
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = FlowpayTextSecondary)
            }
        }
    )

    if (showContactPicker) {
        ContactPickerDialog(
            onDismiss = { showContactPicker = false },
            onContactSelected = { contact ->
                phoneNumber = contact.phoneNumber
                selectedContactName = contact.name
                showContactPicker = false
            }
        )
    }

    if (showContactPermissionDialog) {
        PermissionExplanationDialog(
            title = stringResource(R.string.permission_contacts_title),
            message = stringResource(R.string.permission_contacts_reason),
            confirmButtonText = stringResource(R.string.permission_grant),
            onConfirm = {
                showContactPermissionDialog = false
                contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            },
            onDismiss = { showContactPermissionDialog = false }
        )
    }
}

@Composable
fun PermissionExplanationDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FlowpaySurface,
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = FlowpayOnSurface
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = FlowpayTextPale,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LocalFlowpayAccentTheme.current.accent
                )
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_not_now), color = FlowpayTextSecondary)
            }
        }
    )
}

// Utility functions
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale("en", "IN"))
    return formatter.format(Date(timestamp))
}
