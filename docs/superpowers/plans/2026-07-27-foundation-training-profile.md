# Foundation and Training Profile Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the first working Gym App slice: an athlete creates an account, defines a segmented training profile, creates a manual routine, and records a workout session from native iOS and Android apps.

**Architecture:** A Kotlin/Spring Boot modular monolith exposes a versioned REST API backed by PostgreSQL. Native SwiftUI and Jetpack Compose clients implement the same onboarding, profile, routine, and session flows through that API. Web administration, AI, Premium, payments, gym links, QR, photos, and social login are outside this delivery.

**Tech Stack:** Kotlin + Spring Boot, PostgreSQL + Flyway, OpenAPI, Testcontainers, SwiftUI, Kotlin + Jetpack Compose, Docker Compose, GitHub Actions.

## Global Constraints

- API base path is `/api/v1`; all dates are UTC ISO-8601 and exposed IDs are UUIDs.
- One required primary profile: `GENERAL_FITNESS`, `BODYBUILDING`, `POWERLIFTING`, `RUNNING`, `CROSSFIT`, or `CALISTHENICS`.
- Secondary interests use the same catalogue, cannot repeat the primary profile, and are limited to two.
- Availability accepts 1–7 days and 15–240 minutes per session; Spanish (Colombia) copy and accessible labels are mandatory.
- Money, payments, AI, memberships, gyms, photos, and Premium are not created in this slice.

---

## File Structure

```text
backend/src/main/kotlin/com/gymapp/{auth,profile,training,shared}/
backend/src/main/resources/db/migration/
backend/src/test/kotlin/com/gymapp/
android/app/src/{main,test,androidTest}/
ios/GymApp/{App,Networking,Features}/
ios/GymAppTests/
contracts/openapi.yaml
infra/docker-compose.yml
.github/workflows/ci.yml
```

`auth` owns credentials and tokens; `profile` owns segmentation; `training` owns exercises, plans, sessions, and sets. Mobile views depend on feature repositories/API clients, never on persistence details.

## Task 1: Bootstrap backend, database, API contract, and CI

**Files:**
- Create: `backend/settings.gradle.kts`, `backend/build.gradle.kts`, `backend/src/main/kotlin/com/gymapp/GymAppApplication.kt`
- Create: `infra/docker-compose.yml`, `contracts/openapi.yaml`, `.github/workflows/ci.yml`
- Test: `backend/src/test/kotlin/com/gymapp/GymAppApplicationTest.kt`

**Produces:** PostgreSQL local service, Spring application, `GET /api/v1/health`, initial OpenAPI, and CI.

- [ ] **Step 1: Write the failing context test.**

```kotlin
@SpringBootTest
class GymAppApplicationTest {
    @Test fun contextLoads() = Unit
}
```

- [ ] **Step 2: Run `cd backend; ./gradlew test --tests com.gymapp.GymAppApplicationTest`; verify failure because the project does not exist.**
- [ ] **Step 3: Add the application and health endpoint.**

```kotlin
@SpringBootApplication
class GymAppApplication
fun main(args: Array<String>) = runApplication<GymAppApplication>(*args)

@RestController
@RequestMapping("/api/v1")
class HealthController {
    @GetMapping("/health") fun health() = mapOf("status" to "ok")
}
```

- [ ] **Step 4: Add PostgreSQL to Docker Compose.**

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: gym_app
      POSTGRES_USER: gym_app
      POSTGRES_PASSWORD: gym_app
    ports: ["5432:5432"]
```

- [ ] **Step 5: Add the health path to OpenAPI; run `./gradlew test` and `docker compose -f infra/docker-compose.yml config`.**
- [ ] **Step 6: Commit `chore: bootstrap backend foundation`.**

## Task 2: Implement authenticated accounts

**Files:**
- Create: `backend/src/main/kotlin/com/gymapp/auth/{AuthController,AuthService,User,UserRepository,AuthDtos,JwtService}.kt`
- Create: `backend/src/main/resources/db/migration/V001__auth.sql`
- Test: `backend/src/test/kotlin/com/gymapp/auth/AuthControllerIT.kt`

**Interfaces:**
- `POST /api/v1/auth/register` accepts `email`, `password`, `acceptedTermsAt`; returns `userId` and `accessToken`.
- `POST /api/v1/auth/login` accepts `email`, `password`; returns the same shape.
- `GET /api/v1/me` returns authenticated `userId` and `email`.

- [ ] **Step 1: Write failing integration tests for registration, duplicate email, login, and unauthenticated `GET /me`.**

```kotlin
webTestClient.post().uri("/api/v1/auth/register")
  .bodyValue(mapOf("email" to "ana@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-07-27T00:00:00Z"))
  .exchange().expectStatus().isCreated
webTestClient.get().uri("/api/v1/me").exchange().expectStatus().isUnauthorized
```

- [ ] **Step 2: Run `./gradlew test --tests com.gymapp.auth.AuthControllerIT`; verify failure.**
- [ ] **Step 3: Create `users` and `consents` migrations; hash passwords with BCrypt and issue signed short-lived bearer tokens.**

```sql
create table users (
  id uuid primary key,
  email varchar(320) not null unique,
  password_hash varchar(255) not null,
  created_at timestamptz not null
);
```

- [ ] **Step 4: Return 409 for duplicate email and 401 for invalid credentials; exclude password fields from responses and logs.**
- [ ] **Step 5: Update OpenAPI, rerun integration tests, and commit `feat: add authenticated user accounts`.**

## Task 3: Implement segmented training profiles

**Files:**
- Create: `backend/src/main/kotlin/com/gymapp/profile/{TrainingProfile,ProfileController,ProfileService,ProfileDtos,ProfileRules}.kt`
- Create: `backend/src/main/resources/db/migration/V002__training_profiles.sql`
- Test: `backend/src/test/kotlin/com/gymapp/profile/ProfileControllerIT.kt`

**Interfaces:**
- `PUT /api/v1/me/training-profile` accepts `experienceLevel`, `primaryProfile`, `secondaryProfiles`, `goal`, `availabilityBand`, `availableDaysPerWeek`, and `sessionDurationMinutes`.
- `GET /api/v1/me/training-profile` returns the saved profile.

- [ ] **Step 1: Write failing tests for a valid beginner-calistenia profile, three secondary profiles, duplicated primary/secondary, 0 days, and 241 minutes.**

```kotlin
assertThatThrownBy {
  rules.validate(CALISTHENICS, listOf(RUNNING, CROSSFIT, POWERLIFTING), 3, 60)
}.isInstanceOf(ValidationException::class.java)
```

- [ ] **Step 2: Run profile tests and verify failure.**
- [ ] **Step 3: Add `training_profiles` and `profile_secondary_interests`; use one profile per user and unique `(training_profile_id, profile_code)`.**
- [ ] **Step 4: Implement the rules and return 422 with field errors; update OpenAPI.**
- [ ] **Step 5: Rerun tests and commit `feat: add segmented training profiles`.**

## Task 4: Import and curate the exercise catalogue

**Files:**
- Create: `backend/src/main/kotlin/com/gymapp/catalog/{ExerciseDatasetImporter,ExerciseDatasetRecord,ExerciseCatalogueService}.kt`
- Create: `backend/src/main/resources/catalog/profile-mappings.yaml`, `backend/src/main/resources/catalog/source-manifest.yaml`
- Create: `backend/src/main/resources/db/migration/V003__exercise_catalog.sql`
- Test: `backend/src/test/kotlin/com/gymapp/catalog/ExerciseDatasetImporterTest.kt`

**Interfaces:** `ExerciseDatasetImporter.import(sourceFile, schemaFile, manifest)` validates a pinned JSON file and writes only curated, published exercises. `GET /api/v1/exercises?profile=CALISTHENICS` returns only published exercises mapped to the requested profile.

- [ ] **Step 1: Write failing importer tests for a valid Spanish instruction, duplicate source ID, missing Spanish instructions, and an unmapped profile.**

```kotlin
val report = importer.import(validDataset, validSchema, manifest)
assertThat(report.published).isEqualTo(1)
assertThat(report.excludedByReason["MISSING_ES_INSTRUCTIONS"]).isEqualTo(1)
```

- [ ] **Step 2: Run `./gradlew test --tests com.gymapp.catalog.ExerciseDatasetImporterTest`; verify failure.**
- [ ] **Step 3: Add exercises and exercise-training-profile migrations with source commit, SHA-256, attribution, publication status, and unique source identity.**
- [ ] **Step 4: Implement schema validation, SHA-256 verification, source-to-internal transformation, profile mappings, and a report with imported/published/excluded counts. Do not download or store source images/GIFs.**
- [ ] **Step 5: Pin the approved source commit and manifest; map the initial published set for all six profiles.**
- [ ] **Step 6: Rerun importer tests and commit `feat: import curated exercise catalogue`.**

## Task 5: Implement manual routines and session logs

**Files:**
- Create: `backend/src/main/kotlin/com/gymapp/training/{WorkoutPlan,WorkoutSession,TrainingController,TrainingService,TrainingDtos}.kt`
- Create: `backend/src/main/resources/db/migration/V005__training.sql` (V004 is reserved for immutable catalogue-source metadata after V003 was applied locally.)
- Test: `backend/src/test/kotlin/com/gymapp/training/TrainingControllerIT.kt`

**Interfaces:**
- `GET /api/v1/exercises?profile=CALISTHENICS`
- `POST /api/v1/workout-plans`
- `POST /api/v1/workout-plans/{planId}/sessions`

- [ ] **Step 1: Write failing tests for a calisthenics plan, an incompatible exercise, plan ownership, and two logged sets.**

```kotlin
val request = CreateWorkoutPlanRequest(
  name = "Base calistenia",
  days = listOf(WorkoutDayRequest("A", listOf(ExerciseRequest("PUSH_UP", 3, 8, 12))))
)
```

- [ ] **Step 2: Run training tests and verify failure.**
- [ ] **Step 3: Add tables for plans, plan days, plan exercises, sessions, and set logs; consume only the published exercises imported in Task 4.**
- [ ] **Step 4: Permit only owner access, positive sets/repetitions, and exercises tagged for the primary profile.**
- [ ] **Step 5: Document requests/responses in OpenAPI, rerun tests, and commit `feat: add manual routines and session logs`.**

## Task 6: Build the Android flow

**Files:**
- Create: `android/app/src/main/java/com/gymapp/{network,auth,profile,training}/`
- Create: `android/app/src/test/java/com/gymapp/profile/TrainingProfileViewModelTest.kt`
- Test: `android/app/src/androidTest/java/com/gymapp/OnboardingFlowTest.kt`

**Interfaces:** `GymApi` calls Tasks 2–4. `TrainingProfileViewModel.save(request)` sends `PUT /me/training-profile`.

- [ ] **Step 1: Write the failing duplicate-secondary validation test.**

```kotlin
viewModel.updatePrimary(CALISTHENICS)
viewModel.toggleSecondary(CALISTHENICS)
assertThat(viewModel.state.value.fieldError)
  .isEqualTo("El interés secundario no puede repetir el perfil principal")
```

- [ ] **Step 2: Scaffold Compose app, API client, and secure token storage.**
- [ ] **Step 3: Implement registration/login and onboarding: level, primary profile, two secondary interests, goal, and availability.**
- [ ] **Step 4: Implement catalogue, manual plan, and set-logging screens with accessibility labels.**
- [ ] **Step 5: Add an instrumented registration-to-completed-session test; run unit and instrumented tests; commit `feat: add Android training onboarding`.**

## Task 7: Build the iOS flow

**Files:**
- Create: `ios/GymApp/{App,Networking,Features/Auth,Features/Profile,Features/Training}/`
- Create: `ios/GymAppTests/TrainingProfileViewModelTests.swift`
- Test: `ios/GymAppUITests/OnboardingFlowUITests.swift`

**Interfaces:** `GymAPIClient` mirrors Android and uses Keychain token storage.

- [ ] **Step 1: Write the failing two-secondary-interest XCTest.**

```swift
viewModel.toggleSecondary(.running)
viewModel.toggleSecondary(.crossfit)
viewModel.toggleSecondary(.powerlifting)
XCTAssertEqual(viewModel.secondaryProfiles.count, 2)
XCTAssertEqual(viewModel.validationMessage, "Puedes elegir hasta dos intereses secundarios")
```

- [ ] **Step 2: Scaffold SwiftUI app, Keychain storage, and API client.**
- [ ] **Step 3: Implement registration/login and Dynamic Type-compatible onboarding.**
- [ ] **Step 4: Implement catalogue, manual plan, and set logging screens.**
- [ ] **Step 5: Add the account-to-completed-session UI test; run XCTest/UI tests; commit `feat: add iOS training onboarding`.**

## Task 8: Verify API contract and delivery acceptance

**Files:**
- Modify: `contracts/openapi.yaml`, `.github/workflows/ci.yml`
- Create: `backend/src/test/kotlin/com/gymapp/contract/OpenApiContractTest.kt`
- Create: `docs/First_Release_Acceptance.md`

- [ ] **Step 1: Write a failing contract test asserting every implemented API route exists in OpenAPI.**
- [ ] **Step 2: Make CI run backend tests, OpenAPI validation, Android tests, and iOS tests on supported runners.**
- [ ] **Step 3: Write acceptance checks for the catalog import report, beginner calisthenics, runner plus strength interest, invalid combination, manual plan, and session persistence.**
- [ ] **Step 4: Run all checks, record command/result in the acceptance document, and commit `test: verify first training vertical slice`.**

## Plan Self-Review

- Covers the approved first vertical slice and deliberately excludes later roadmap domains.
- Covers all five segmentation dimensions, ownership, validation errors, and accessible native clients.
- Every public API used by a client is defined before that client task.
