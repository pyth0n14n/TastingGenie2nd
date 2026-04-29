# UI_DESIGN_CONSTRAINTS.md

## Source of Truth
- Figma `Tasting genie 2nd` is the visual source of truth for screens that have a completed frame.
- Use one Figma frame per implementation pass. Record the target frame name or URL in the PLAN or PR description.
- If a screen has no Figma frame, keep the current UI unless the PR explicitly scopes a new design decision.

## Material Design 3
- Prefer Material Design 3 components and interaction patterns.
- Use the project theme tokens for colors, typography, and surface treatment instead of hardcoded values.
- Figma-aligned screens use the fixed light color scheme. Do not use Android dynamic color as the visual baseline.
- Preserve accessibility labels when replacing visible text actions with icons or overflow actions.

## Existing Behavior
- Preserve existing navigation, save behavior, validation, and data compatibility unless the UI design cannot work without a small behavior change.
- Keep existing secondary actions available. If Figma does not show them, move them to an overflow menu or secondary action area rather than removing them.
- Images must continue to use URI-based handling. Do not add direct filesystem assumptions.

## Scope Control
- One UI PR should focus on one screen or one shared foundation.
- Avoid unrelated refactors, formatting-only churn, and DB schema changes in UI PRs.
- Schema changes require an explicit migration plan and tests, and should only happen when directly required by the UI work.

## Verification
- Compare implementation against the target Figma frame for layout, spacing, typography, color, and interaction states.
- Prefer semantics-driven UI tests for navigation and behavior so tests survive visual refactors.
- Run `./gradlew localFix` as the standard local verification unless blocked.
