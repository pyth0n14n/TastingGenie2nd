# CODING_STYLE.md

## Kotlin
- Prefer `val` over `var`.
- Avoid nullable types when possible; use explicit domain defaults.
- Use early returns to reduce nesting.
- Prefer sealed types for UI state and one-off events.
- No wildcard imports.

## Naming
- Classes: `PascalCase`
- Functions/vars: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Compose:
  - Route (state/DI): `XxxRoute`
  - UI (pure): `XxxScreen`
  - Reusable pieces: `XxxSection`, `XxxRow`, etc.

## Architecture (minimum)
- UI: Compose
- State: ViewModel + `StateFlow<UiState>`
- Data: Repository pattern
- DB: Room (migration required for schema changes)

## Compose Guidelines
- Keep Composables small and pure.
- No side effects in UI functions; use `LaunchedEffect` / `remember` properly.
- Route vs Screen separation:
  - `Route`: collects flows, handles navigation callbacks, DI entry.
  - `Screen`: takes `UiState` and callbacks only.
- All user-facing strings must go through `stringResource` (later i18n-ready).

## Error & Loading UI
- `UiState` must include at least:
  - `isLoading: Boolean`
  - `error: UiError?` (or sealed)
- Errors must be renderable and testable.

## Data/Time
- Store timestamps as `Instant` (or epoch millis) consistently.
- Do not use device locale-dependent formats for storage.

## Logging
- Do not log PII (notes, images, location).