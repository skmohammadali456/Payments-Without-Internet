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

// Surfaces (darkest → lightest)
val FlowpaySurface = Color(0xFFFFFFFF)

/** Screen background behind cards/lists. */
val FlowpaySurfaceDim = Color(0xFFF5F7F8)

/** Elevated surface: input fields, chips, avatars. */
val FlowpayMediumGray = Color(0xFFEEF2F4)

/** Borders, dividers, inactive track. */
val FlowpayLightGray = Color(0xFFD0D8DC)

/** Stronger outline / disabled container. */
val FlowpayOutlineGray = Color(0xFF667780)

/** Disabled content / faint hint. */
val FlowpayDisabledGray = Color(0xFF667780)

// Text (dimmest → brightest)
/** Placeholder / hint text. */
val FlowpayTextGray = Color(0xFF667085)

/** Secondary text: captions, labels, timestamps. */
val FlowpayTextSecondary = Color(0xFF475467)

/** Long-form body text on dark dialogs. */
val FlowpayTextPale = Color(0xFF344054)

val FlowpayOnSurface = Color(0xFF101828)

// Card Colors (light card variant)
val FlowpayCardBackground = Color(0xFFFFFFFF)
val FlowpayCardText = Color(0xFF101828)
val FlowpayCardSubtext = Color(0xFF475467)

// Accents
val FlowpayAccent = Color(0xFF155B73)
val FlowpayAccentGreen = Color(0xFF146C43)
val FlowpayAccentGreenBright = Color(0xFF146C43)

// ─────────────────────────────────────────────────────────────────────────
// Transaction status palette. One color per outcome, used identically in
// the history list, detail dialog and result screen so a status never
// changes meaning between screens.
// ─────────────────────────────────────────────────────────────────────────

/** SUCCESS — bank confirmed. */
val FlowpayStatusSuccess = Color(0xFF146C43)

/** FAILED / declined, and destructive actions (delete, clear). */
val FlowpayStatusError = Color(0xFFB42318)

/** NEEDS_REVIEW / PENDING — user attention required. */
val FlowpayStatusWarning = Color(0xFF8A4B08)

/** UNVERIFIED / CANCELLED — outcome unknown or nothing happened. Neutral:
 *  deliberately neither success-green nor failure-red. */
val FlowpayStatusNeutral = Color(0xFF667085)

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
