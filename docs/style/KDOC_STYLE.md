# KDOC_STYLE.md

## What needs KDoc
- Public APIs (public classes, public functions, public properties)
- Interfaces and repositories
- Use-case-like domain logic
- Anything with non-obvious behavior or side effects

## What does NOT need KDoc
- Private/internal helpers unless the intent is non-obvious
- Trivial getters/setters
- Pure UI composables that are self-explanatory

## Format (short and consistent)
Use 1–3 lines by default.

Example:

/**
 * Adds a tasting note and returns the new note id.
 *
 * @throws IllegalArgumentException when title is blank.
 */
suspend fun addNote(title: String, rating: Int): Long

## Compose KDoc
For Routes/Screens:

/**
 * Route for the note list screen.
 *
 * Collects state from ViewModel and delegates rendering to [NoteListScreen].
 */
@Composable fun NoteListRoute(...)

## Do not lie
KDoc must match behavior. If behavior changes, update KDoc in the same PR.