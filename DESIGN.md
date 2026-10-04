# WavePay UI redesign

## Scope and safety

- Redesign presentation only. Keep payment, persistence, permission timing, and navigation behavior unchanged.
- Preserve existing resource names, Compose callbacks, intent-extra keys, view IDs, and any `FLAG_SECURE` usage.
- Do not edit payment, helper, data, repository, receiver, manager, service, manifest, Gradle, or test files. The QR scanner activity remains unchanged; its permitted XML and drawable resources provide its visual treatment.
- Keep the UI light-only and offline-first. Add no network permission, network/image-loading dependency, invented production data, or text baked into images.
- Put all user-facing copy in `strings.xml`. Use Compose previews only for sample content.

## Design tokens

Keep all existing color resource and Kotlin token names; add semantic aliases/tokens rather than renaming them. Screens use theme tokens, not inline hex values.

| Role | Value |
| --- | --- |
| Brand | `#0A6C8C` |
| Pressed brand | `#084A61` |
| Brand tint | `#E3F2F7` |
| Ink | `#0B1B26` |
| Secondary text | `#4A5B68` |
| Canvas | `#F4F7F9` |
| Surface | `#FFFFFF` |
| Outline | `#DDE5EA` |
| Success / tint | `#146C43` / `#E6F4EC` |
| Danger / tint | `#B42318` / `#FDECEA` |
| Warning / tint | `#8A4B08` / `#FDF0DC` |
| Neutral / tint | `#667085` / `#EEF1F4` |

Only `SUCCESS` is green. `UNVERIFIED` and `CANCELLED` are neutral; `NEEDS_REVIEW` and `PENDING` are amber; `FAILED` is red. Every transaction status includes an icon and label. An unverified result prominently warns: “Do not pay again until you have checked your bank statement or SMS inbox.”

Bundle Plus Jakarta Sans under its OFL license, with 400/500/600/700 weights. Use display 32/40, title 22/28, body 16/24, label 14/20, caption 12/16, and tabular figures for amounts. Spacing is 4/8/12/16/24/32dp; card radii are 12/20/28dp. Use flat cards with 1dp outlines. Motion is limited to 150–250ms state changes and respects the system animation scale.

The one decorative motif is a soft wave drawn with Compose Canvas, used only in the home header and onboarding heroes. Use Material Rounded icons from the already-present extended icon set.

## Shared components

- `ScreenScaffold`: consistent light canvas, safe insets, width handling, and scroll behavior.
- `PrimaryButton` and `SecondaryButton`: 48dp minimum touch targets and accessible enabled/disabled states.
- `StatusChip`: icon, localized label, and the fixed status palette.
- `SectionCard`: white surface with a 1dp outline and 12/20/28dp shape tokens.
- `InfoBanner`: accessible informational, warning, and error treatments.
- `EmptyState`: localized title/body and a rounded Material icon.
- `WaveHeader`: Canvas wave motif, restricted to the home header and onboarding heroes.
- Existing transaction detail, contact picker, permission, and payment-setup dialogs are restyled to match these components.

## Screen plan

1. Onboarding: six concise pages for welcome, phone permission, SMS permission, bank/SIM selection, disclaimer summary with expandable text, and a USSD/UPI 123 test stepper. Retain existing permission timing and setup state.
2. Home and payment entry: wordmark/settings row, wave header, scan/pay actions, offline and bank chips, unfinished-setup banner, real recent payees/payments only, and an empty state. Restyle the existing pay-by-number sheet without changing transfer behavior.
3. QR and call overlay: retain the dark camera preview, add a corner frame and accessible 56dp controls, and restyle existing overlay IDs with readable steps, the bank-screen PIN warning, and a prominent terminate action.
4. Result, history, settings, and dialogs: status-truthful result layout with the unverified warning first; filterable/grouped history and a bottom-sheet detail view; grouped settings with live permission states and explanations; restyled payment and contact dialogs.
5. App identity assets: a WavePay adaptive vector symbol with monochrome variant and brand-color splash. Remove the black PNG wordmark icon; do not embed text in the icon.

## Files to touch by phase

### Phase 0 — design specification
- `DESIGN.md`

### Phase 1 — theme, font, and shared components
- `app/src/main/java/com/flowpay/app/ui/theme/Color.kt`
- `app/src/main/java/com/flowpay/app/ui/theme/Theme.kt`
- `app/src/main/java/com/flowpay/app/ui/theme/Type.kt`
- `app/src/main/java/com/flowpay/app/ui/theme/Shape.kt`
- `app/src/main/java/com/flowpay/app/ui/theme/Spacing.kt`
- `app/src/main/java/com/flowpay/app/ui/components/` (shared components)
- `app/src/main/res/font/` (Plus Jakarta Sans and OFL license)
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values-v31/themes.xml`

### Phase 2 — onboarding and test screen
- `app/src/main/java/com/flowpay/app/SetupActivity.kt`
- `app/src/main/java/com/flowpay/app/TestConfigurationActivity.kt`
- `app/src/main/res/values/strings.xml`

### Phase 3 — home, pay sheet, QR, and call overlay
- `app/src/main/java/com/flowpay/app/MainActivity.kt`
- `app/src/main/res/layout/activity_qr_scanner.xml`
- `app/src/main/res/layout/call_overlay_flowpay.xml`
- `app/src/main/res/drawable/`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/dimens.xml`

### Phase 4 — result, history, settings, detail, and dialogs
- `app/src/main/java/com/flowpay/app/ui/activities/PaymentResultActivity.kt`
- `app/src/main/java/com/flowpay/app/ui/activities/TransactionHistoryActivity.kt`
- `app/src/main/java/com/flowpay/app/ui/activities/SettingsActivity.kt`
- `app/src/main/java/com/flowpay/app/ui/components/StatusIndicator.kt`
- `app/src/main/java/com/flowpay/app/ui/components/TransactionDetailDialog.kt`
- `app/src/main/java/com/flowpay/app/ui/dialogs/ContactPickerDialog.kt`
- `app/src/main/java/com/flowpay/app/ui/dialogs/Upi123ProgressDialog.kt`
- `app/src/main/java/com/flowpay/app/ui/dialogs/UssdProgressDialog.kt`
- `app/src/main/java/com/flowpay/app/MainActivity.kt` (shared permission/payment dialogs only)
- `app/src/main/res/layout/activity_payment_success.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values-v31/themes.xml`

### Phase 5 — icon and splash
- `app/src/main/res/drawable/` (vector symbol and splash assets)
- `app/src/main/res/mipmap-anydpi-v26/`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values-v31/themes.xml`

## Verification

After each phase, run the checks available in the workspace, inspect the diff for prohibited-path or behavior changes, and commit/push that phase separately. The Android SDK is not currently configured in this workspace, so a successful APK build may require SDK setup before compiler/resource verification.