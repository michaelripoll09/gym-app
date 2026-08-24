# Gym App

Gym App is a work-in-progress fitness platform with a Kotlin/Spring Boot API, PostgreSQL, and an Android client. It supports personal training workflows and optional gym operations.

## Status

Active development. The API and Android app are not production-ready.

## Prerequisites

- Docker Desktop with Docker Compose
- JDK 21 for the backend
- Android Studio with an Android SDK (API 35) and JDK 17-compatible Android toolchain

## Quick start

### 1. Start PostgreSQL

From the repository root:

```powershell
docker compose -f infra/docker-compose.yml up -d
```

This starts PostgreSQL on `localhost:5432` with database, user, and password all set to `gym_app`.

> These are **development-only** defaults. Replace them and set a strong, unique `GYM_JWT_SECRET` before deploying anywhere outside local development.

### 2. Run the backend

In PowerShell:

```powershell
cd backend
$env:GYM_JWT_SECRET = [Convert]::ToBase64String([byte[]](1..32))
.\gradlew.bat bootRun
```

The API runs on port `8080`. Database migrations run automatically through Flyway.

### 3. Run the Android app

Open `android/` in Android Studio, choose an emulator, and run the `app` configuration. The debug build uses `http://10.0.2.2:8080/api/v1`, which reaches the backend running on the host machine from the Android emulator.

Or install the debug build from PowerShell:

```powershell
cd android
.\gradlew.bat installDebug
```

## Tests

```powershell
cd backend
.\gradlew.bat test

cd ..\android
.\gradlew.bat test
.\gradlew.bat connectedDebugAndroidTest  # requires a running emulator or device
```

## Repository layout

- `android/` — Android application
- `backend/` — Kotlin/Spring Boot API and Flyway migrations
- `infra/` — local infrastructure, including PostgreSQL Compose configuration
- `contracts/` — shared API contracts
- `docs/` — public product, architecture, operations, and security documentation

## Documentation

Start with the [documentation index](docs/README.md). Useful entry points include [architecture](docs/Architecture_Documentation.md), [backend](docs/Backend_Documentation.md), [frontend](docs/Frontend_Documentation.md), and [security and privacy](docs/Security_Privacy_Documentation.md).

## Contributing and security

Please open an issue or pull request with a focused description and relevant tests. For security and privacy guidance, see the [security documentation](docs/Security_Privacy_Documentation.md); do not post sensitive details in public issues.

## License

No license file is currently included. Do not assume permission to redistribute or reuse this project.
