// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Flowpay

package com.flowpay.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────
// WavePay design tokens — light theme.
//
// Every Compose screen draws from these; inline Color(0x…) literals outside
// this package are a CI failure (see the palette-gate step in build.yml).
// The ramp consolidates the near-duplicate greys that had accreted across
// screens (0x1E1E1E vs 0x1A1A1A, 0x8A8A8A vs 0x888888, …) into one value
// per visual role, so "card grey" or "secondary text" can be changed in
// exactly one place.
// ─────────────────────────────────────────────────────────────────────────

// WavePay semantic tokens. Keep legacy Flowpay names below as aliases so
// existing screens and resource references remain source-compatible.
val WavePayBrand = Color(0xFF0A6C8C)
val WavePayBrandPressed = Color(0xFF084A61)
val WavePayBrandTint = Color(0xFFE3F2F7)
val WavePayInk = Color(0xFF0B1B26)
val WavePaySecondaryText = Color(0xFF4A5B68)
val WavePayCanvas = Color(0xFFF4F7F9)
val WavePaySurface = Color(0xFFFFFFFF)
val WavePayOutline = Color(0xFFDDE5EA)

val WavePayStatusSuccess = Color(0xFF146C43)
val WavePayStatusSuccessTint = Color(0xFFE6F4EC)
val WavePayStatusDanger = Color(0xFFB42318)
val WavePayStatusDangerTint = Color(0xFFFDECEA)
val WavePayStatusWarning = Color(0xFF8A4B08)
val WavePayStatusWarningTint = Color(0xFFFDF0DC)
val WavePayStatusNeutral = Color(0xFF667085)
val WavePayStatusNeutralTint = Color(0xFFEEF1F4)
val WavePayOnBrand = Color(0xFFFFFFFF)

// Existing tokens retained as aliases for call sites migrated in later phases.
val FlowpaySurface = WavePaySurface
val FlowpaySurfaceDim = WavePayCanvas
val FlowpayMediumGray = WavePayStatusNeutralTint
val FlowpayLightGray = WavePayOutline
val FlowpayOutlineGray = WavePayOutline
val FlowpayDisabledGray = WavePaySecondaryText
val FlowpayTextGray = WavePayStatusNeutral
val FlowpayTextSecondary = WavePaySecondaryText
val FlowpayTextPale = WavePayInk
val FlowpayOnSurface = WavePayInk
val FlowpayCardBackground = WavePaySurface
val FlowpayCardText = WavePayInk
val FlowpayCardSubtext = WavePaySecondaryText
val FlowpayAccent = WavePayBrand
val FlowpayAccentGreen = WavePayStatusSuccess
val FlowpayAccentGreenBright = WavePayStatusSuccess

// ─────────────────────────────────────────────────────────────────────────
// Transaction status palette. One color per outcome, used identically in
// the history list, detail dialog and result screen so a status never
// changes meaning between screens.
// ─────────────────────────────────────────────────────────────────────────

/** SUCCESS — bank confirmed. */
val FlowpayStatusSuccess = WavePayStatusSuccess

/** FAILED / declined, and destructive actions (delete, clear). */
val FlowpayStatusError = WavePayStatusDanger

/** NEEDS_REVIEW / PENDING — user attention required. */
val FlowpayStatusWarning = WavePayStatusWarning

/** UNVERIFIED / CANCELLED — outcome unknown or nothing happened. Neutral:
 *  deliberately neither success-green nor failure-red. */
val FlowpayStatusNeutral = WavePayStatusNeutral

/**
 * The single mapping from a [com.flowpay.app.data.TransactionStatus] string
 * to its display color. Replaces the byte-identical getStatusColor()
 * functions that had been copy-pasted into multiple screens.
 */
fun statusColor(status: String): Color = when (status.uppercase()) {
    "SUCCESS" -> FlowpayStatusSuccess
    "FAILED", "DECLINED" -> FlowpayStatusError
    "PENDING", "NEEDS_REVIEW" -> FlowpayStatusWarning
    else -> FlowpayStatusNeutral // UNVERIFIED, CANCELLED, and unknown values
}
