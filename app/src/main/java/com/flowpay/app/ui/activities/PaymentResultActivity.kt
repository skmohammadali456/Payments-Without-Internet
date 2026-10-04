// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay

package com.flowpay.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.flowpay.app.MainActivity
import com.flowpay.app.R
import com.flowpay.app.data.TransactionStatus
import com.flowpay.app.helpers.TransactionDetector
import com.flowpay.app.utils.CurrencyFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentResultActivity : AppCompatActivity() {

    private lateinit var statusCircle: View
    private lateinit var tickImageView: ImageView
    private lateinit var statusText: TextView
    private lateinit var statusExplainerText: TextView
    private lateinit var statusHeader: LinearLayout
    private lateinit var amountText: TextView
    private lateinit var detailsCard: CardView
    private lateinit var bankNameText: TextView
    private lateinit var transactionIdText: TextView
    private lateinit var dateTimeText: TextView
    private lateinit var upiIdLayout: LinearLayout
    private lateinit var upiIdText: TextView
    private lateinit var recipientLayout: LinearLayout // NEW
    private lateinit var recipientLabel: TextView // NEW
    private lateinit var recipientText: TextView // NEW
    private lateinit var doneButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set system UI to black theme
        setupSystemUI()

        setContentView(R.layout.activity_payment_success)

        initViews()
        loadTransactionData()
        startAnimations()

        // Replaces the deprecated onBackPressed() override
        onBackPressedDispatcher.addCallback(this) {
            navigateToMain()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // launchMode is singleTask, so a newer payment outcome (e.g. a FAILED
        // confirmation arriving while this screen still shows the previous
        // SUCCESS) is delivered here instead of creating a new instance.
        // Without re-rendering, the screen would keep showing the stale, wrong
        // outcome — contradicting the freshly-posted result notification.
        setIntent(intent)
        resetViewsForAnimation()
        loadTransactionData()
        startAnimations()
    }

    private fun initViews() {
        statusCircle = findViewById(R.id.iv_status_circle)
        tickImageView = findViewById(R.id.iv_success_tick)
        statusText = findViewById(R.id.tv_status)
        statusExplainerText = findViewById(R.id.tv_status_explainer)
        statusHeader = findViewById(R.id.success_container)
        amountText = findViewById(R.id.tv_amount)
        detailsCard = findViewById(R.id.card_details)
        bankNameText = findViewById(R.id.tv_bank_name)
        transactionIdText = findViewById(R.id.tv_transaction_id)
        dateTimeText = findViewById(R.id.tv_date_time)
        upiIdLayout = findViewById(R.id.layout_upi_id)
        upiIdText = findViewById(R.id.tv_upi_id)
        recipientLayout = findViewById(R.id.layout_recipient) // NEW
        recipientLabel = findViewById(R.id.tv_recipient_label) // NEW
        recipientText = findViewById(R.id.tv_recipient_name) // NEW
        doneButton = findViewById(R.id.btn_done)

        resetViewsForAnimation()

        doneButton.setOnClickListener {
            navigateToMain()
        }
    }

    /** Return the animated views to their pre-animation (hidden) state. */
    private fun resetViewsForAnimation() {
        tickImageView.alpha = 0f
        statusHeader.backgroundTintList = null
        statusExplainerText.background = null
        statusExplainerText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        statusExplainerText.compoundDrawablePadding = 0
        statusText.alpha = 0f
        statusExplainerText.alpha = 0f
        amountText.alpha = 0f
        detailsCard.alpha = 0f
        doneButton.alpha = 0f
    }

    private fun loadTransactionData() {
        val transactionId = intent.getStringExtra("transaction_id") ?: "N/A"
        val amount = intent.getStringExtra("amount") ?: "0"
        val status = intent.getStringExtra("status") ?: "UNKNOWN"
        val bankName = intent.getStringExtra("bank_name") ?: "Bank"
        val timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis())
        val upiId = intent.getStringExtra("upi_id")
        val transactionType = intent.getStringExtra("transaction_type") ?: "DEBIT"
        val recipientName = intent.getStringExtra("recipient_name") // NEW
        val phoneNumber = intent.getStringExtra("phone_number") // NEW

        // Get operation type from detector
        val detector = TransactionDetector.getInstance(this)
        val operationType = detector.getOperationType() ?: ""

        // Render the outcome the bank actually reported — this screen is
        // launched for every parsed confirmation, not only successes. Every
        // property is set explicitly (never relying on layout defaults) so an
        // onNewIntent re-render from a different status resets cleanly.
        //
        // The top surface, heading, amount, and icon all reflect the bank's
        // reported outcome. SUCCESS is green; all other statuses use their
        // designated warning, danger, or neutral color.
        when (status.uppercase(Locale.ROOT)) {
            TransactionStatus.FAILED -> {
                statusText.text = getString(R.string.payment_status_failed)
                statusExplainerText.text = getString(R.string.status_explainer_failed)
                statusExplainerText.visibility = View.VISIBLE
                tickImageView.setImageResource(R.drawable.ic_error)
                applyStatusAccent(R.color.error_red)
            }
            TransactionStatus.NEEDS_REVIEW -> {
                statusText.text = getString(R.string.payment_status_needs_review)
                statusExplainerText.text = getString(R.string.status_explainer_needs_review)
                statusExplainerText.visibility = View.VISIBLE
                tickImageView.setImageResource(R.drawable.ic_error)
                applyStatusAccent(R.color.warning_orange)
            }
            TransactionStatus.SUCCESS -> {
                statusText.text = getString(R.string.payment_status_success)
                statusExplainerText.visibility = View.GONE
                tickImageView.setImageResource(R.drawable.ic_check_white)
                applySuccessAccent()
            }
            TransactionStatus.PENDING -> {
                statusText.text = getString(R.string.payment_status_pending)
                statusExplainerText.text = getString(R.string.status_explainer_pending)
                statusExplainerText.visibility = View.VISIBLE
                tickImageView.setImageResource(R.drawable.ic_unverified)
                applyStatusAccent(R.color.warning_orange)
            }
            TransactionStatus.CANCELLED -> {
                statusText.text = getString(R.string.payment_status_cancelled)
                statusExplainerText.text = getString(R.string.status_explainer_cancelled)
                statusExplainerText.visibility = View.VISIBLE
                tickImageView.setImageResource(R.drawable.ic_close)
                applyStatusAccent(R.color.unverified_grey)
            }
            TransactionStatus.UNVERIFIED -> {
                statusText.text = getString(R.string.payment_status_unverified)
                statusExplainerText.text = getString(R.string.status_explainer_unverified)
                statusExplainerText.visibility = View.VISIBLE
                tickImageView.setImageResource(R.drawable.ic_unverified)
                applyStatusAccent(R.color.unverified_grey)
                statusExplainerText.setBackgroundResource(R.drawable.result_unverified_callout_bg)
                val calloutIcon = ContextCompat.getDrawable(this, R.drawable.ic_unverified)?.mutate()
                calloutIcon?.setTint(ContextCompat.getColor(this, R.color.status_neutral))
                statusExplainerText.setCompoundDrawablesWithIntrinsicBounds(
                    calloutIcon,
                    null,
                    null,
                    null
                )
                statusExplainerText.compoundDrawablePadding =
                    (8 * resources.displayMetrics.density).toInt()
            }
            else -> {
                // Unknown values never fall open to the success styling.
                statusText.text = getString(R.string.payment_status_unknown)
                statusExplainerText.text = getString(R.string.status_explainer_unknown)
                statusExplainerText.visibility = View.VISIBLE
                tickImageView.setImageResource(R.drawable.ic_unverified)
                applyStatusAccent(R.color.unverified_grey)
            }
        }
        tickImageView.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
        amountText.text = getString(R.string.amount_rupees, formatAmount(amount))

        // Handle recipient/sender display - UPDATED LOGIC
        when {
            !recipientName.isNullOrEmpty() -> {
                recipientLayout.visibility = View.VISIBLE
                recipientLabel.text = getString(counterpartyLabelFor(status, transactionType))
                recipientText.text = recipientName
            }
            !phoneNumber.isNullOrEmpty() -> {
                recipientLayout.visibility = View.VISIBLE
                recipientLabel.text = getString(
                    if (transactionType == "CREDIT") R.string.recipient_from else R.string.recipient_to
                )
                recipientText.text = phoneNumber
            }
            operationType == "UPI_123" && detector.getPhoneNumber() != null -> {
                recipientLayout.visibility = View.VISIBLE
                recipientLabel.text = getString(R.string.recipient_to)
                recipientText.text = detector.getPhoneNumber()
            }
            else -> {
                recipientLayout.visibility = View.GONE
            }
        }

        // Bank name - show if different from recipient
        bankNameText.text = bankName

        transactionIdText.text = bankReferenceOf(transactionId)
        dateTimeText.text = formatDateTime(timestamp)

        // Show UPI ID if available
        if (!upiId.isNullOrEmpty()) {
            upiIdLayout.visibility = View.VISIBLE
            upiIdText.text = upiId
        } else {
            upiIdLayout.visibility = View.GONE
        }
    }

    /** SUCCESS look: green confirmation heading and amount. */
    private fun applySuccessAccent() {
        statusCircle.backgroundTintList = null
        statusCircle.background = ContextCompat.getDrawable(this, R.drawable.circle_success_bg)
        statusText.setTextColor(ContextCompat.getColor(this, R.color.flowpay_green))
        amountText.setTextColor(ContextCompat.getColor(this, R.color.flowpay_green))
        statusHeader.backgroundTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.status_success_tint)
        )
    }

    /** Non-success look: one status colour across circle, heading and amount. */
    private fun applyStatusAccent(colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        statusCircle.background = ContextCompat.getDrawable(this, R.drawable.circle_status_bg)
        statusCircle.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        statusText.setTextColor(color)
        amountText.setTextColor(color)
        val tintRes = when (colorRes) {
            R.color.error_red -> R.color.status_danger_tint
            R.color.warning_orange -> R.color.status_warning_tint
            else -> R.color.status_neutral_tint
        }
        statusHeader.backgroundTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(this, tintRes)
        )
    }

    private fun formatAmount(amount: String): String = CurrencyFormat.inr(amount)

    /**
     * The label above the counterparty's name, for the outcome being rendered.
     *
     * "Paid to" and "Received from" are past tense: they assert that the money
     * moved, so only a confirmed SUCCESS earns them. Under a red "Payment
     * Failed" heading the assertion is simply false — the card read "Paid to
     * SHARMA STORE" directly below an explainer saying any debited amount is
     * normally auto-reversed, so the screen contradicted itself and the more
     * concrete-looking line is the one a worried user believes. NEEDS_REVIEW
     * and UNVERIFIED have the same problem for the opposite reason: nobody
     * knows yet whether it moved, which is the whole point of those states.
     *
     * The neutral "To"/"From" names the counterparty without claiming an
     * outcome, which is all the row was ever there to do.
     */
    private fun counterpartyLabelFor(status: String, transactionType: String): Int {
        val isCredit = transactionType == "CREDIT"
        return when {
            status != TransactionStatus.SUCCESS ->
                if (isCredit) R.string.recipient_from else R.string.recipient_to
            isCredit -> R.string.recipient_received_from
            else -> R.string.recipient_paid_to
        }
    }

    /**
     * The part of a stored transaction id a user can actually act on.
     *
     * Ids are stored as `<bank reference>_<timestamp>` — the timestamp keeps
     * rows unique when a bank reuses a reference, and is meaningless to the
     * reader. Shown whole it produced "125012501250…85952823651", ellipsised
     * through the middle, which is precisely the half a user needs to match
     * this payment against their bank statement. Generated ids (`TXN…`, no
     * separator) are shown as-is.
     */
    private fun bankReferenceOf(transactionId: String): String =
        transactionId.substringBeforeLast('_').ifBlank { transactionId }

    private fun formatDateTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    private fun startAnimations() {
        val duration = 200L

        statusHeader.alpha = 0f
        statusHeader.animate().alpha(1f).setDuration(duration).start()

        tickImageView.alpha = 0f
        tickImageView.scaleX = 0.9f
        tickImageView.scaleY = 0.9f
        tickImageView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .start()

        detailsCard.alpha = 0f
        detailsCard.translationY = 16f
        detailsCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .start()

        doneButton.alpha = 0f
        doneButton.animate().alpha(1f).setDuration(duration).start()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun setupSystemUI() {
        val surface = ContextCompat.getColor(this, R.color.screen_background)
        window.statusBarColor = surface
        window.navigationBarColor = surface
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupSystemUI()
        }
    }
}
