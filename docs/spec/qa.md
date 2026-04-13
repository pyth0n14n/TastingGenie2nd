# QA Guidelines / Codex Review Findings

This document captures common issues from Codex reviews to prevent regressions. It provides **preventive guidelines** so following them avoids similar issues. Use the Quick Checklist for daily development; refer to details during reviews or when issues arise.

## Quick Checklist (For Daily Use)
- [ ] **ViewModel Lifecycle**: Use SavedStateHandle for navigation-sensitive state; avoid DisposableEffect for visibility.
- [ ] **Data Validation**: Validate all inputs (UI and imports) against app rules; reject invalid data with errors.
- [ ] **Navigation/Settings**: Apply settings reactively in UI; implement all spec-defined routes.
- [ ] **Data Consistency**: Use transactions for multi-table ops; run I/O on Dispatchers.IO.
- [ ] **UI Thread**: Never block main thread with I/O; distinguish cancellation from errors.
- [ ] **Edit/Reload**: Keep load timing and update timing aligned; reload data after mutations and lock edits on load failures.
- [ ] **Review-v2 Structure**: Keep `appearance/aroma/taste/other` ownership explicit in type names, DB columns, backup fields, and UI state.
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
  - Import JSON with viscosity=0 or 6; verify rejection.

### Problem: Uses auto-generated IDs as merge keys, causing data corruption on import.
- **Example**: Importing backup overwrites unrelated records due to ID collision.
- **Root Cause**: From commit `6e964a0` (fix(import-export): avoid corrupt backup merges) - Upserted by numeric ID without considering logical identity.
- **Preventive Measure**: Use business keys (e.g., sake name + grade) for merging; never rely on auto-generated IDs for cross-device sync.
- **Test Coverage**:
  - Import backup into device with existing data; verify no overwrites of unrelated records.
  - Merge backups with conflicting IDs; ensure logical deduplication.

### Problem: Exports non-portable image URIs, breaking restores.
- **Example**: Imported sake records have broken image links on new devices.
- **Root Cause**: From commit `771110e` (fix(import-export): address PR6 review findings) - Serialized content:// URIs directly.
- **Preventive Measure**: Omit image URIs from backups; inform users images are not portable.
- **Test Coverage**:
  - Export/import round-trip; verify images are not restored (show message_no_image).

### Problem: Replacing or deleting images mutates persisted files before save, so cancel or save failure loses the old managed set.
- **Example**: SakeEdit imports a new image immediately and deletes an existing managed file before the form save succeeds.
- **Preventive Measure**: Treat image picks as edit-session state first, and only finalize import during save. On save failure, clean up any newly imported managed images. With the default cleanup policy, save-time replace/delete should only update DB references; unused managed files are removed later by manual cleanup or the auto-cleanup setting.
- **Test Coverage**:
  - Pick a replacement image and cancel edit; verify the persisted image set is unchanged.
  - Simulate save failure after image import; verify the newly imported managed images are deleted.
  - Delete an existing image and save with auto-cleanup OFF; verify the DB reference is removed while the old managed file remains.
### Problem: Duplicate image picks create duplicate preview keys or orphan managed copies.
- **Example**: The same gallery URI is selected twice, `LazyRow` receives duplicate keys, and save imports the same source twice even though only one DB reference survives.
- **Preventive Measure**: Deduplicate image selections in the edit-session state before appending them. Save logic should import each pending source URI at most once, and preview lists should not depend on duplicate-unsafe keys.
- **Test Coverage**:
  - Select the same image URI twice and verify the preview still contains one item.
  - Save after duplicate selection and verify the source is imported once and only one managed URI is persisted.
### Problem: Post-save image cleanup failure is treated as a save failure even after the DB commit succeeded.
- **Example**: SakeEdit saves a replacement image, then cleanup of unused managed files throws and the screen reports `error_save_sake` while the DB already points at the new image set.
- **Preventive Measure**: Mark the DB write as committed before cleanup, and treat committed cleanup as best-effort. Only rollback newly imported images when the save fails before the DB commit.
- **Test Coverage**:
  - Replace an existing image and force unused-image cleanup to fail; verify the form still completes as saved.
  - Delete an existing image and force cleanup to fail; verify the DB save still completes and no false save error is shown.
  - Save metadata only with auto-cleanup ON and verify no image cleanup runs.
### Problem: Manual/auto cleanup can delete the wrong files when multiple images are attached.
- **Example**: `cleanupUnusedImages()` deletes a still-referenced second image because only the primary image URI is considered, or deletes external URIs selected from outside app storage.
- **Preventive Measure**: Build the referenced set from every `Sake.imageUris` entry, not only the first preview image. Restrict cleanup to the app-managed image directory and never delete external URIs.
- **Test Coverage**:
  - Save a sake with multiple images and run manual cleanup; verify every referenced managed image remains.
  - Keep one image referenced and one unreferenced in the managed directory; verify only the unreferenced file is deleted.
  - Include an external URI in the DB and verify cleanup does not attempt to delete it.
### Problem: Review deletion is easy to mis-tap or hides the list when only the delete action failed.
- **Example**: A user taps the trash icon by mistake, or `deleteReview` fails and ReviewList is replaced by a generic load error even though the existing rows are still valid.
- **Preventive Measure**: Put review deletion behind a confirmation dialog, and keep delete failures separate from load failures so the list stays visible.
- **Test Coverage**:
  - Tap the review delete icon and verify no deletion happens until the confirmation button is pressed.
  - Force `deleteReview` to fail and verify the list remains visible while the delete error is shown.
### Problem: Sake deletion can desynchronize related rows, race across rapid taps, or hide cleanup failures behind nullable exception messages.
- **Example**: `SakeList` deletes the parent row but leaves child reviews behind, a slower earlier trash tap overwrites a later selection, or managed image cleanup throws an exception with `message == null` and the failure is silently lost.
- **Preventive Measure**: Wrap sake + review deletion in a transaction, show the destructive action behind a confirmation dialog with the related review count, cancel or sequence competing delete-selection requests so only the latest tap can win, and expose post-commit image cleanup failures separately from the DB delete result without relying on `Throwable.message` being non-null. Do not convert `CancellationException` into a delete error.
- **Test Coverage**:
  - Delete a sake with reviews and verify the parent row and all child reviews disappear together.
  - Tap two sake delete icons in quick succession and verify the latest target is the one that remains pending for confirmation.
  - Force post-commit image cleanup to fail and verify the sake is still gone while an inline cleanup error is shown.
  - Force post-commit image cleanup to throw with a null message and verify the cleanup failure is still surfaced.
  - Cancel the delete coroutine and verify no generic delete error is shown.

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

### Problem: Empty image list treated as error instead of empty state.
- **Example**: S5 shows error_load_review for a review whose parent sake has no images.
- **Root Cause**: From commit `1f6ba2e` (Fix edit mode lock when review seed load fails) - Threw on null without checking validity.
- **Preventive Measure**: Treat empty image collections as valid empty states; only error on actual load failures.
- **Test Coverage**:
  - Open S5 for review with no images; verify message_no_image displays.

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

### Problem: Load timing and update timing drift apart, leaving visible state stale.
- **Example**: A screen loads an entity on init, a child edit screen updates it, and returning shows the pre-edit snapshot until the user leaves and reopens the screen.
- **Preventive Measure**: For every screen that displays mutable data, define how it refreshes after writes from child routes, sibling flows, or background updates. Do not rely only on an init-time fetch when the destination can remain on the back stack after data changes.
- **Test Coverage**:
  - Open a detail screen, mutate the same record from a child edit flow, return, and verify the visible values refresh immediately.
  - Keep a destination on the back stack while underlying repository data changes, then resume it and verify the screen refreshes or observes the updated data.

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

### Problem: Review section switching depends on vertical scroll, hiding navigation and save actions mid-form.
- **Example**: `ReviewEdit` puts the section tabs and save button inside the scrolling column, so long forms can hide both the active-section affordance and the save action.
- **Preventive Measure**: Keep review section tabs outside the section list and sync them with a pager so both tap and horizontal swipe navigate sections. Keep the review save action in a fixed bottom bar that remains visible while the section content scrolls and while the IME is shown.
- **Test Coverage**:
  - Swipe `ReviewEdit` and `ReviewDetail` horizontally and verify the visible section changes while the selected tab stays in sync.
  - Scroll or swipe within `ReviewEdit` and verify the save button remains visible and enabled/disabled state is preserved.

### Problem: Sake edit save action disappears at the end of a long form.
- **Example**: `SakeEdit` keeps the error text and save button inside the `LazyColumn`, so scrolling through metadata fields can hide the primary action entirely.
- **Preventive Measure**: Keep `SakeEdit` save/error feedback in a fixed bottom bar, not as the last rows of the form list. The form may scroll independently, but the primary save action should remain visible while editing and while the IME is shown.
- **Test Coverage**:
  - Scroll `SakeEdit` through lower metadata fields and verify the save button remains visible.
  - Trigger an edit error state and verify the error message is shown in the fixed bottom bar together with the save action.

### Problem: Optional dropdowns cannot be cleared to null.
- **Example**: Once selected, cannot deselect optional review fields.
- **Root Cause**: From commit `503eb59` (test(review): cover PR4 viewmodel flows) - No "none" option in dropdowns.
- **Preventive Measure**: Add explicit "not selected" option for nullable fields.
- **Test Coverage**:
  - Select value, then clear; verify can set back to null.

### Problem: Review-v2 fields drift across layers, so values land in the wrong tab or the wrong backup key.
- **Example**: `aromaIntensity` is saved under a legacy `intensity` field while detail UI expects the prefixed field, or `tasteInPalateAroma` is rendered in the aroma tab because old naming leaked through.
- **Preventive Measure**: Keep the same `appearanceXxx / aromaXxx / tasteXxx / otherXxx` names across domain model, Room entity, backup schema, and UI state. Do not reintroduce legacy unprefixed review field names after the migration PR.
- **Test Coverage**:
  - Save and reload one populated review covering every prefix group; verify all values return to the same tab/section.
  - Export and import the same review; verify the backup keys round-trip without renaming drift.

### Problem: Legacy aroma fields are migrated inconsistently, leaving removed fields or misfiled examples behind.
- **Example**: `scentBase` remains in the DB after the review-v2 migration, or old `scentMouth` data is still shown in the aroma tab instead of the taste tab.
- **Preventive Measure**: Remove `scentBase` completely during the review-v2 schema change. Migrate old `scentTop` to `aromaExamples`, `scentMouth` to `tasteInPalateAroma`, `sharp` to `tasteAftertaste`, and `comment` to `otherCautions`.
- **Test Coverage**:
  - Migrate a pre-v2 database and verify `scentBase` no longer exists.
  - Verify migrated `scentTop`, `scentMouth`, `sharp`, and `comment` appear in the intended new fields.

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

### Problem: Navigation briefly shows a white flash between screens.
- **Example**: Moving between sake list, edit, detail, and settings shows a momentary white frame before the next screen draws.
- **Preventive Measure**: Keep the activity `windowBackground`, the Compose root container, and the navigation host fill/background aligned to the same app background color. Do not leave the nav root transparent against the platform default window color. If the app uses dynamic color, also update the activity window background at runtime so Android 12+ devices do not flash a mismatched fallback color.
- **Test Coverage**:
  - Manually traverse `SakeList -> SakeEdit -> ReviewList -> ReviewEdit -> Settings` in both light and dark theme; verify no white flash appears during transitions.
  - Repeat the same transition check on an Android 12+ device with dynamic color enabled; verify the flash does not change to a mismatched Monet color.

### Problem: Review date input allows free-form typing, causing invalid formats and unnecessary keyboard input.
- **Example**: `ReviewEdit` opens a generic keyboard for the date field and relies on the user to type `YYYY-MM-DD` exactly.
- **Preventive Measure**: Use a date picker for review dates and let the UI generate the persisted `YYYY-MM-DD` text. New reviews should default the field to the current day. Do not require manual date-format entry for review forms.
- **Test Coverage**:
  - Trigger the picker with a touch-style Compose test interaction, not only a semantics `performClick()`, so text-field gesture regressions are caught.
  - Open a new review form and verify the date field is prefilled with the current day.
  - Select a review date from the picker and verify the field shows the generated `YYYY-MM-DD` value.
  - Save a review after picker selection and verify the stored `LocalDate` matches the chosen day.

### Problem: Major review ratings still use popup selectors, making repeated scoring slow and hiding the meaning of the chosen value.
- **Example**: `appearanceViscosity`, `aromaIntensity`, taste levels, or `otherOverallReview` still open dropdown menus instead of exposing the score directly in the form.
- **Preventive Measure**: Use in-place staged controls for the major review ratings. `appearanceViscosity`, `aromaIntensity`, `tasteSweetness`, `tasteSourness`, `tasteBitterness`, `tasteUmami`, and `tasteAftertaste` should use a discrete staged selector such as a stepped slider with a visible current label and clear action. `otherOverallReview` should use star selection plus a visible text label that explains the current star meaning.
- **Layout Note**: Keep the clear-action slot stable even when the current value is `null`; avoid headers that grow later and push the slider or stars downward after selection.
- **Visual Note**: Unselected staged controls should look visibly different from selected ones, for example by dimming the current-value text and using lower-contrast slider/star colors until the user picks a value.
- **Failure Note**: Staged controls must tolerate empty option lists during load-failure rendering. Error states may still compose the screen shell, so the control should degrade safely instead of throwing when master data failed to load.
- **Test Coverage**:
  - Move each staged selector and verify the committed value is reflected in UI state.
  - Clear an optional staged selector and verify it returns to `null`.
  - Render the edit screen with a load error and empty staged-option lists; verify the error is shown and composition does not crash.
  - Select a star rating and verify both the stored enum and the visible meaning label update together.

### Problem: Hidden soundness fields leak nulls or stale values instead of defaulting to healthy.
- **Example**: A review saved while `showReviewSoundness` is off stores `null` for one section, or an old `UNSOUND` value remains hidden and surprises the user after re-enabling the setting.
- **Preventive Measure**: Treat `appearanceSoundness`, `aromaSoundness`, and `tasteSoundness` as non-null review fields with a default of `SOUND`. Hiding the UI changes visibility only; it must not create null state or skip persistence.
- **Test Coverage**:
  - Save a review with soundness hidden and verify all three persisted values are `SOUND`.
  - Toggle the setting off and on around edit flows; verify the restored values are deterministic and non-null.

### Problem: Derived flavor-profile classification gets persisted separately and drifts away from the source axes.
- **Example**: The 4-type `薫酒/爽酒/熟酒/醇酒` badge still shows the old type after `aromaIntensity` or `tasteComplexity` changed because a stale field was stored independently.
- **Preventive Measure**: Keep the flavor-profile type derived from `aromaIntensity` and `tasteComplexity`. Grid taps should update those source fields, and source-field edits should update the grid. Do not persist a separate flavor-profile column.
- **Test Coverage**:
  - Change `aromaIntensity` or `tasteComplexity` directly and verify the grid position and type label update.
  - Tap a grid cell and verify both source fields update to the expected values.
  - Verify the center line (`MEDIUM`) is still classified deterministically and no cell is left unlabeled.

### Problem: Destructive image actions fire immediately from the form, making accidental taps expensive.
- **Example**: A user taps image delete while editing a sake and the image disappears without confirmation.
- **Preventive Measure**: Gate image deletion behind a confirmation dialog, and keep the delete control disabled while save is in progress.
- **Test Coverage**:
  - Open SakeEdit with an existing image, tap delete, and verify a confirmation dialog appears.
  - Cancel the dialog and verify the image preview remains.
  - Confirm the dialog and verify the form reflects a pending image removal.

### Problem: Sake list preview cards ignore the image-preview setting or collapse missing images into blank space.
- **Example**: S0 keeps rendering thumbnails after `setting_image_preview` is disabled, or cards with no image show an empty top area with no placeholder.
- **Preventive Measure**: Make SakeList observe `showImagePreview` reactively. Render the image area only when the setting is enabled, and show a clear placeholder when previews are enabled but a sake has no image.
- **Test Coverage**:
  - Disable image preview and verify SakeList cards render without thumbnails or placeholders.
  - Leave image preview enabled and verify image-less sakes show the placeholder text instead of a blank region.

### Problem: Grouped masters get flattened in UI, making spec-defined hierarchy and optional clears disappear.
- **Example**: Sake classification loses its category structure, or prefecture selection cannot be cleared once chosen.
- **Preventive Measure**: When a master spec defines category or region groupings, keep that grouping in the selector UI and preserve null/clear flows for optional single-select fields.
- **Test Coverage**:
  - Expand classification groups and verify options are shown under the correct headings.
  - Select `OTHER` in sake classification and verify the free-text field appears.
  - Select `OTHER` in sake grade/type and verify the free-text field is rendered immediately below the grade selector, not below unrelated fields such as the image.
  - Select and clear prefecture; verify the field returns to `未選択`.

### Problem: Sake type masters regress when a non-special-designation option is added.
- **Example**: `普通酒` is forced into `OTHER` free text because the master list does not expose it as a first-class option.
- **Preventive Measure**: When the source-of-truth master adds a named sake type such as `FUTSUSHU`, update the enum, asset master, fake master data, and save/load tests in the same PR.
- **Test Coverage**:
  - Select `普通酒` in sake type and verify save/load round-trips without using `種別（その他）`.

## 8. Validation and Error Handling

### Problem: Invalid input sets error but keeps stale state, allowing silent saves.
- **Example**: Invalid grade selection shows error but retains old grade, save succeeds with wrong value.
- **Root Cause**: From commit `f8eecae022` - Error state set without resetting invalid fields.
- **Preventive Measure**: On validation failure, reset invalid fields to null or safe default; block save while errors active.
- **Test Coverage**:
  - Select invalid grade; verify grade resets and save disabled.
  - Enter invalid data; ensure cannot save until corrected.

### Problem: Optional numeric form fields coerce invalid text or lose the typed value before save.
- **Example**: `16%` や `3..5` を入力しても silently null にされ、どの項目が悪いか分からないまま保存が通る。
- **Preventive Measure**: Keep optional numeric inputs as raw text in UI state, validate them only on save, and reject the save when any numeric field is invalid. Do not auto-coerce malformed text to `null`.
- **Test Coverage**:
  - Enter invalid numeric text in sake/review forms; verify save is blocked and an error is shown.
  - Reopen an existing item with valid numeric values; verify the exact formatted text is restored to the fields.
  - Enter `0`, `-1`, or values above the agreed review bounds (`1,000,001` price / `25,001` mL volume); verify save is blocked and no crash occurs.

### Problem: Range-bounded numeric fields accept impossible values because only parsing is validated.
- **Example**: 精米歩合に `101` を入れても整数として通ってしまう。
- **Preventive Measure**: For numeric fields with domain bounds, validate both parseability and range before save. Keep those bounds close to the mapper or ViewModel save validation so UI and import logic can share the same rule later.
- **Test Coverage**:
  - Enter `101` or `-1` into a polish ratio field; verify save is blocked.
  - Enter boundary values such as `0` and `100`; verify the intended acceptance behavior is covered explicitly.

### Problem: Float parsing accepts non-finite values that should never be persisted.
- **Example**: `NaN` や `1e50` を日本酒度/酸度に入れると `Float` 変換だけは通って保存される。
- **Preventive Measure**: Treat float fields as valid only when parsing succeeds and the resulting value is finite. Do not rely on `toFloatOrNull()` alone for save-time validation.
- **Test Coverage**:
  - Enter `NaN`, `Infinity`, or overflowed scientific notation such as `1e50`; verify save is blocked.
  - Reconfirm ordinary finite values such as `+3.5` and `1.4` still round-trip through save and edit.

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
- Keep image ownership on `Sake`; review flows may display that image but must not persist their own image URI.
- Delete only app-managed image URIs; never assume arbitrary external URIs are safe to remove.
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
