# AGENTS.md

This project follows a specification-first workflow.

---

## Source of Truth

- All functional behavior must follow `docs/spec/`.
- If implementation differs from spec, update the spec in the same PR.
- Do not invent features not described in spec.

---

## Build & Checks

All checks required by CI must pass before merging.

See:
- `.github/workflows/`
- `docs/style/LINTING.md`

---

## Pull Request Rules

- Linear history only (no merge commits).
- Prefer squash merge.
- 1 PR = 1 responsibility.
- No unrelated refactoring or formatting-only changes.

Each PR must include:
- Purpose
- Related spec file(s)
- How to test
- Risk / rollback notes

---

## Coding Principles

Follow project style guides:

- Kotlin/Compose style: `docs/style/CODING_STYLE.md`
- KDoc rules: `docs/style/KDOC_STYLE.md`
- Linting rules: `docs/style/LINTING.md`

Additional constraints:

- Do not change DB schema without a migration plan and tests.
- Images must be handled via URI. No direct filesystem assumptions.
- Do not swallow exceptions; expose failures via UI state.

---

## Architectural Direction (minimum)

- UI: Compose
- State: ViewModel + StateFlow
- Data: Repository pattern
- Persistence: Room