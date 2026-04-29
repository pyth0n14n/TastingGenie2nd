# AGENTS.md

This project follows a specification-first workflow.

---

## Source of Truth

- All functional behavior must follow `docs/spec/`.
- If implementation differs from spec, update the spec in the same PR.
- Do not invent features not described in spec.
- If code review points out issues, update relevant docs (e.g., `docs/spec/`, `docs/style/`) to prevent similar issues in the future.
- For QA guidelines and preventing regressions: See `docs/spec/qa.md`.

---

## Build & Checks

All checks required by CI must pass before merging.

- Local development: `./gradlew localFix` is the default verification command. Use this first for pre-commit verification and autofix.
- Do not replace `./gradlew localFix` in normal workflow with ad hoc partial commands such as a single compile task or a narrow test target unless you are diagnosing a specific blocker after `localFix`.
- If `localFix` cannot be completed, state that explicitly and then list any supplemental commands you ran for diagnosis.
- CI / pull request gate: `./gradlew ciCheck`.
- Include expected scope of tests in PR description (unit/test/androidTest status, and any currently skipped tests). In `How to test`, list `./gradlew localFix` as the standard local verification step unless blocked.

See:
- `.github/workflows/`
- `docs/style/LINTING.md`

---

## Pull Request Rules

- Linear history only (no merge commits).
- Prefer squash merge.
- 1 PR = 1 responsibility.
- Commit in small logical units. Do not accumulate large batches of unrelated or weakly-related changes before committing.
  - Target: <= 20 files / 800 lines per PR when feasible.
  - Each commit: target <= 10 files / 300 lines.
  - 1 logical change per commit (implementation, tests, docs should be separate when possible).
- No unrelated refactoring or formatting-only changes.
- Avoid “giant PR syndrome”: if a PR would touch >40 files or appears to encompass multiple features, split into multiple PRs.

Each PR must include:
- Purpose
- Related spec file(s)
- How to test
  - Default: `./gradlew localFix`
  - If not run, explain why
  - If supplemental commands were used, describe them as secondary diagnostics rather than the primary verification step
- Risk / rollback notes

---

## Coding Principles

Follow project style guides:

- Kotlin/Compose style: `docs/style/CODING_STYLE.md`
- KDoc rules: `docs/style/KDOC_STYLE.md`
- Linting rules: `docs/style/LINTING.md`
- UI design constraints: `docs/style/UI_DESIGN_CONSTRAINTS.md`

Additional constraints:

- Do not change DB schema without a migration plan and tests.
- Images must be handled via URI. No direct filesystem assumptions.
- Do not swallow exceptions; expose failures via UI state.

---

## UI / Design Workflow

- For Figma-driven UI work, treat the target Figma screen frame as the visual source of truth.
- Record the target Figma frame name or URL in the PLAN or PR description.
- If a screen has no completed Figma frame, keep the current UI or explicitly scope the design decision before changing it.
- Prefer Material Design 3 components and project theme tokens over hardcoded colors, typography, or spacing.
- Preserve existing behavior, navigation, validation, and data compatibility unless the UI change requires a minimal supporting state or repository change.
- Do not remove existing secondary actions just because they are absent from Figma; move them to overflow or another secondary action area.

---

## Architectural Direction (minimum)

- UI: Compose
- State: ViewModel + StateFlow
- Data: Repository pattern
- Persistence: Room
