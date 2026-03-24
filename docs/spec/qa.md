# QA Guidelines / Codex Review Findings

This document captures common issues from Codex reviews to prevent regressions. It provides **preventive guidelines** so following them avoids similar issues. Use the Quick Checklist for daily development; refer to details during reviews or when issues arise.

## Quick Checklist (For Daily Use)
- [ ] **ViewModel Lifecycle**: Use SavedStateHandle for navigation-sensitive state; avoid DisposableEffect for visibility.
- [ ] **Data Validation**: Validate all inputs (UI and imports) against app rules; reject invalid data with errors.
- [ ] **Navigation/Settings**: Apply settings reactively in UI; implement all spec-defined routes.
- [ ] **Data Consistency**: Use transactions for multi-table ops; run I/O on Dispatchers.IO.
- [ ] **UI Thread**: Never block main thread with I/O; distinguish cancellation from errors.
- [ ] **Edit/Reload**: Reload data after mutations; lock edits on load failures.
- [ ] **UI Display**: Map enums to localized labels; parse enums safely.
- [ ] **Spec Feasibility**: Ensure specs use valid identifiers; test implementation constraints.

## Operational Guidance
- **During Development**: Use Quick Checklist as a mental reminder. Reference details only for specific concerns.
- **During Testing**: Map test cases to Quick Checklist items; ensure coverage.
- **During Review**: Reviewers check against full details; developers self-check before PR.
- **When Issues Arise**: Add new findings to this doc to prevent recurrence.
- **Context Separation**: Keep development focused on features; use this for quality gates, not design decisions.

## Detailed Guidelines

## 1. ViewModel Lifecycle and Configuration Changes

### Problem: DisposableEffect/onDispose misfires on rotation, clearing UI state prematurely.
- **Example**: Settings screen clears transfer feedback on rotation because `setSettingsVisible(false)` is called.
- **Root Cause**: From commit `58373c4` (fix(settings): preserve background transfer feedback) - ViewModel visibility was tied to composable lifecycle, but rotation recreates composables without actual navigation.
- **Preventive Measure**: Never use DisposableEffect for navigation-sensitive state; use `SavedStateHandle` or lifecycle-aware observers instead.
- **Test Coverage**:
  - Rotate device during background transfer; verify feedback persists.
  - Rotate while transfer in progress; ensure no cancellation or state loss.

### Problem: LaunchedEffect clears state on every reopen, erasing off-screen results.
- **Example**: Import/export completes while on sake list, but reopening settings clears the result.
- **Root Cause**: From commit `f55bbcc` (fix(settings): clear stale transfer feedback on reopen) - Attempted to clear stale state but over-cleared valid results.
- **Preventive Measure**: Distinguish between "first open" and "reopen" using SavedStateHandle flags; only clear on explicit user actions.
- **Test Coverage**:
  - Complete transfer off-screen, reopen settings; verify result displays.
  - Reopen settings after failure; ensure old errors don't persist.

### Problem: ViewModel scoped to wrong back stack entry causes state retention issues.
- **Example**: Settings ViewModel survives popBackStack, showing stale errors on reopen.
- **Root Cause**: From commit `a5145fb` (fix(settings): scope backup jobs beyond settings route) - Moved to sake_list entry to persist across navigation, but retained too much state.
- **Preventive Measure**: Scope ViewModels to the minimal necessary back stack; use explicit state reset on navigation.
- **Test Coverage**:
  - Fail transfer, leave settings, reopen; verify no stale error.
  - Success transfer, leave settings, reopen; verify no stale success message.

## 2. Backup/Import Validation

### Problem: Accepts invalid data that app cannot handle, creating uneditable records.
- **Example**: Blank sake names or invalid viscosity values imported without validation.
- **Root Cause**: From commit `2af8ba1` (fix(import): validate backup sake names and viscosity) - Mapper trusted input without app-level validation.
- **Preventive Measure**: Validate all imported data against the same rules as UI input; reject invalid payloads with clear error.
- **Test Coverage**:
  - Import JSON with blank sake name; verify rejection with error_import_invalid_payload.
  - Import JSON with viscosity=0 or 4; verify rejection.

### Problem: Uses auto-generated IDs as merge keys, causing data corruption on import.
- **Example**: Importing backup overwrites unrelated records due to ID collision.
- **Root Cause**: From commit `6e964a0` (fix(import-export): avoid corrupt backup merges) - Upserted by numeric ID without considering logical identity.
- **Preventive Measure**: Use business keys (e.g., sake name + grade) for merging; never rely on auto-generated IDs for cross-device sync.
- **Test Coverage**:
  - Import backup into device with existing data; verify no overwrites of unrelated records.
  - Merge backups with conflicting IDs; ensure logical deduplication.

### Problem: Exports non-portable image URIs, breaking restores.
- **Example**: Imported reviews have broken image links on new devices.
- **Root Cause**: From commit `771110e` (fix(import-export): address PR6 review findings) - Serialized content:// URIs directly.
- **Preventive Measure**: Omit image URIs from backups; inform users images are not portable.
- **Test Coverage**:
  - Export/import round-trip; verify images are not restored (show message_no_image).

## 3. Navigation and Settings Application

### Problem: Settings not applied to UI, misleading users.
- **Example**: Help hints or image preview settings ignored in routes.
- **Root Cause**: From commit `355428c` (fix(review): align image viewer flow with spec) - Settings persisted but not consulted in rendering logic.
- **Preventive Measure**: Always check settings state in composables; use Flow-based settings for reactive updates.
- **Test Coverage**:
  - Disable help hints; verify help action hidden in sake list.
  - Disable image preview; verify image action hidden in review list.

### Problem: Navigation flow deviates from spec.
- **Example**: Image viewer not reachable from review list as per navigation.md.
- **Root Cause**: From commit `9ccbe26` (test(review): verify dummy image renders in viewer) - Wired navigation only from detail route.
- **Preventive Measure**: Implement all spec-defined routes; test navigation graphs against spec.
- **Test Coverage**:
  - From review list, tap image action; verify navigates to S5 and back to S2.

### Problem: Null imageUri treated as error instead of empty state.
- **Example**: S5 shows error_load_review for valid null imageUri.
- **Root Cause**: From commit `1f6ba2e` (Fix edit mode lock when review seed load fails) - Threw on null without checking validity.
- **Preventive Measure**: Treat nullable fields as valid empty states; only error on actual load failures.
- **Test Coverage**:
  - Open S5 for review with no image; verify message_no_image displays.

## 4. Data Consistency and Transactions

### Problem: Export reads inconsistent state due to lack of transaction.
- **Example**: Backup contains review without parent sake if write lands between queries.
- **Root Cause**: From commit `5f4e6de` (test(settings): cover PR6 transfer feedback flows) - Independent queries without snapshot isolation.
- **Preventive Measure**: Wrap multi-table reads in `database.withTransaction { ... }` for consistency.
- **Test Coverage**:
  - Export during concurrent writes; verify backup integrity.

## 5. UI Thread Blocking

### Problem: SAF I/O blocks main thread, causing ANR.
- **Example**: Large backup freezes settings screen.
- **Root Cause**: From commit `771110e` (fix(import-export): address PR6 review findings) - Stream operations on main dispatcher.
- **Preventive Measure**: Run all I/O on Dispatchers.IO; use viewModelScope or repository for orchestration.
- **Test Coverage**:
  - Export large backup; verify UI remains responsive.

### Problem: Transfer jobs cancelled on navigation, but treated as failure.
- **Example**: Back navigation during transfer shows error instead of cancellation.
- **Root Cause**: From commit `3dc63df` (fix(settings): preserve cancellation during SAF transfers) - runCatching caught CancellationException.
- **Preventive Measure**: Let CancellationException propagate; distinguish cancellation from actual errors.
- **Test Coverage**:
  - Cancel transfer via navigation; verify no error shown.

## 6. Edit and Reload Issues

### Problem: Detail screen shows stale data after edit.
- **Example**: Edit review, save, back to detail; old values still shown.
- **Root Cause**: From commit `1f6ba2e` (Fix edit mode lock when review seed load fails) - Only loaded once on init.
- **Preventive Measure**: Reload data after edit saves; use event-driven updates or refresh on resume.
- **Test Coverage**:
  - Edit review, save, back; verify detail shows updated values.

### Problem: Edit mode allows save on load failure, creating duplicates.
- **Example**: Load failure leaves form editable, save creates new record.
- **Root Cause**: From commit `503eb59` (test(review): cover PR4 viewmodel flows) - Failure branch kept form editable.
- **Preventive Measure**: Lock edit mode on load failure; require successful seed load for edits.
- **Test Coverage**:
  - Simulate load failure; verify save disabled or shows error.

### Problem: Optional dropdowns cannot be cleared to null.
- **Example**: Once selected, cannot deselect optional review fields.
- **Root Cause**: From commit `503eb59` (test(review): cover PR4 viewmodel flows) - No "none" option in dropdowns.
- **Preventive Measure**: Add explicit "not selected" option for nullable fields.
- **Test Coverage**:
  - Select value, then clear; verify can set back to null.

## 7. UI Display and Localization

### Problem: Displays raw enum constants instead of localized labels, confusing users.
- **Example**: Sake list shows "JUNMAI" instead of "純米".
- **Root Cause**: From commit `c6e3617556` - Directly used enum.name without mapping to master labels.
- **Preventive Measure**: Always map enum values to localized display strings from master data before rendering.
- **Test Coverage**:
  - Sake list displays localized grade labels (e.g., "純米" not "JUNMAI").
  - Review details show localized values for all enum fields.

### Problem: Dropdown-style popups open away from the invoking field, making selection hard.
- **Example**: Temperature or color menus appear near the left edge instead of the tapped field.
- **Preventive Measure**: For form selectors, use anchored Material 3 components such as `ExposedDropdownMenuBox`; avoid detached `DropdownMenu` triggered from plain buttons.
- **Test Coverage**:
  - Open sake/review selector fields and verify the menu is attached to the field.
  - Select an option and confirm the same field reflects the chosen label.

## 8. Validation and Error Handling

### Problem: Invalid input sets error but keeps stale state, allowing silent saves.
- **Example**: Invalid grade selection shows error but retains old grade, save succeeds with wrong value.
- **Root Cause**: From commit `f8eecae022` - Error state set without resetting invalid fields.
- **Preventive Measure**: On validation failure, reset invalid fields to null or safe default; block save while errors active.
- **Test Coverage**:
  - Select invalid grade; verify grade resets and save disabled.
  - Enter invalid data; ensure cannot save until corrected.

### Problem: Missing edit target treated as create, causing duplicates.
- **Example**: Edit route with invalid sakeId creates new sake instead of error.
- **Root Cause**: From commit `f8eecae022` - Null getSake() result dropped into create mode.
- **Preventive Measure**: Treat missing edit targets as load errors; show error and disable editing.
- **Test Coverage**:
  - Navigate to edit with invalid ID; verify error_load_sake and no save option.

### Problem: Unsafe enum parsing crashes on invalid master data.
- **Example**: onGradeSelected throws IllegalArgumentException on unexpected string.
- **Root Cause**: From commit `c6e3617556` - Used enumValueOf directly without try-catch.
- **Preventive Measure**: Parse enums defensively; convert invalid values to UI error state.
- **Test Coverage**:
  - Simulate malformed master data; verify no crash, shows error instead.

## 9. Spec Implementation Feasibility

### Problem: Spec defines invalid enum identifiers, forcing ad-hoc workarounds.
- **Example**: Prefecture regions use "-" in names, invalid for Kotlin enums.
- **Root Cause**: From commit `7145532d99` - Spec written without considering implementation constraints.
- **Preventive Measure**: Ensure all spec enums use valid Kotlin identifiers (underscore only); update spec before implementation.
- **Test Coverage**:
  - All master data enums parse without errors.
  - Spec examples compile as Kotlin code.

## General Preventive Measures

- Always validate imports against UI validation rules.
- Use transactions for multi-table operations.
- Run I/O off main thread.
- Scope ViewModels minimally; reset state explicitly.
- Test configuration changes and navigation flows.
- Distinguish cancellation from errors.
- Reload data after mutations.
- Apply settings reactively in UI.
- Map enums to localized labels for display.
- Reset invalid fields on validation failure.
- Treat missing data as errors, not defaults.
- Parse enums safely; handle invalid inputs gracefully.
- Write specs with implementation constraints in mind.
