# Apply Session References on Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a person explicitly apply an exercise's most recent private reference to only its unfilled sets before saving a workout session.

**Architecture:** Keep the existing backend reference endpoint and Android loading lifecycle. Add pure draft-state helpers that selectively fill blank set fields, then expose one Compose action per referenced exercise group so the rendered values remain normal editable draft inputs.

**Tech Stack:** Kotlin, Android Compose, JUnit 4, existing Retrofit API client.

## Global Constraints

- References come only from the authenticated user's already-loaded current-routine data.
- Opening a session never mutates draft values; applying is an explicit user action.
- Only blank fields on sets of the selected exercise can change; saving and offline behavior stay unchanged.

---

### Task 1: Selectively apply a reference in draft state

**Files:**
- Modify: `android/app/src/main/java/com/gymapp/sessions/SessionDraftState.kt`
- Test: `android/app/src/test/java/com/gymapp/sessions/SessionReferenceStateTest.kt`

- [ ] **Step 1: Write failing tests** for applying a reference only to blank fields of matching exercise sets and returning an unchanged draft when no reference is present.
- [ ] **Step 2: Run** `./gradlew.bat testDebugUnitTest --tests com.gymapp.sessions.SessionReferenceStateTest` and observe the missing helper failure.
- [ ] **Step 3: Implement** `SessionDraftState.applyReference(exerciseId, reference)` to fill blank repetitions and blank load only for matching sets, preserving manually entered values and all other sets.
- [ ] **Step 4: Run** the focused Android unit test and confirm it passes.

### Task 2: Add the explicit session action

**Files:**
- Modify: `android/app/src/main/java/com/gymapp/sessions/SessionScreen.kt`
- Modify: `android/app/src/main/java/com/gymapp/MainActivity.kt`

- [ ] **Step 1: Add** a per-exercise action visible only when the set is the first set for an available reference.
- [ ] **Step 2: Wire** the action to replace the in-memory draft with `applyReference`; do not call the API or save a session.
- [ ] **Step 3: Compile and run** Android unit tests to verify the Compose callback wiring.

### Task 3: Verify the delivery

**Files:**
- Test: `android/app/src/test/java/com/gymapp/sessions/SessionReferenceStateTest.kt`
- Test: `backend/src/test/kotlin/com/gymapp/training/TrainingControllerIT.kt`

- [ ] **Step 1: Run** `./gradlew.bat testDebugUnitTest` from `android`.
- [ ] **Step 2: Run** `./gradlew.bat test` from `backend`.
- [ ] **Step 3: Inspect** `git diff --check` and the changed-file diff against every acceptance criterion.
