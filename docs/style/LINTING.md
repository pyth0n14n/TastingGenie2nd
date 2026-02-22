# LINTING.md

## Purpose

This document defines formatting and static analysis rules
enforced locally and in CI.

CI configuration is the ultimate source of truth.

---

## Tools

- ktlint: Kotlin formatting
- detekt: Static code analysis

---

## Local Usage

### Fix & Check

    ./gradlew localFix

---

## CI Required Checks

The following tasks must pass in CI:

    ./gradlew ciCheck

If CI fails, fix locally before pushing again.

---

## Rules

### Formatting

- `.editorconfig` is the formatting authority.
- No wildcard imports.
- Trailing commas allowed if configured.
- Line length and indentation follow ktlint defaults unless overridden.

### Static Analysis

- detekt findings are treated as errors unless explicitly configured.
- Suppressions must include justification.

---

## Policy

- No formatting-only PRs unless explicitly required.
- If formatting changes are needed, include them in the same PR as functional changes.
- Do not disable lint rules to bypass CI without review.