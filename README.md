# cloudflare_library

A sample Android project demonstrating secure transport and data protection techniques: mutual TLS (mTLS), SSL/TLS pinning, and field-level encryption. The repository contains an `app` module (sample app) and an `sslpinning` library module with utilities for certificate handling and encryption.

## Quick overview
- Language: Kotlin
- Build system: Gradle (wrapper included)
- Modules:
  - `app/` - Android application using mTLS, FLE, and DataStore
  - `sslpinning/` - library with helpers for certificate loading, OkHttp setup, and encryption utilities
  - `buildSrc/` - centralized build configuration

## Requirements
- JDK 11 (or compatible with your Gradle wrapper)
- Android Studio Arctic Fox or later (recommended)
- Android SDK & platforms matching the project's `compileSdk` (see `app/build.gradle` / `buildSrc`)
- NDK (if building native parts) — version is specified in `app/build.gradle`/`buildSrc`

## Build & run
From the project root you can use the Gradle wrapper to build and install the debug app:

```bash
# Build debug APK
./gradlew assembleDebug

# Install on a connected device/emulator
./gradlew installDebug
```

Or open the project in Android Studio and run the `app` configuration.

## Tests
Run unit tests and instrumentation tests with Gradle:

```bash
# Unit tests
./gradlew test

# Instrumentation (on a connected device/emulator)
./gradlew connectedAndroidTest
```

## Security notes (important)
- Secrets and long-lived private keys should NOT be committed to the repository. This project is designed to fetch encrypted certificates/keys at runtime (e.g. from a secure server or Firebase).
- The app demonstrates a defense-in-depth approach: mTLS + certificate pinning + field-level encryption + local key protection. Review source files like `sslpinning/src/main/java/yap/sslpinninglibrary/CerOkHttpClient.kt` and `app/src/main/java/com/sslcf/sslpinning/*` for details.
- For production use, add Play Integrity (or vendor attestation), hardware-backed Keystore wrapping, telemetry and secure key rotation workflows.

## Code structure (high level)
- `app/src/main/java` — application entry points, network builder (`YapHttpsBuilder`), encryption/decryption flows
- `sslpinning/src/main/java` — encryption helper classes (`EncryptCloudflareData`, `DecryptCloudflareData`), OkHttp certificate loader (`CerOkHttpClient`), utility classes
- `app/src/main/res/raw` — sample certificates and keys used for local development (do not ship secrets in public repos)

## Contributing
- Create a feature branch from `main` (or `develop`) for changes.
- Run unit tests and lint checks before submitting a PR:

```bash
./gradlew check
./gradlew test
```

- Include a short description of your changes and any security implications in the PR description.

## Troubleshooting
- If the build fails due to SDK/NDK versions, make sure the Android SDK and NDK referenced in `buildSrc` are installed and match the `compileSdk`/`ndkVersion` values.
- Certificate related errors often indicate incorrect password, corrupted PKCS12 file, or mismatched certificate format. Check logs from `CerOkHttpClient` and ensure the certificate is valid.

## License & contact
- This project has no license file in the repository. Add a `LICENSE` file if you plan to open-source it.
- For questions, contact the repository owner or maintainers listed in the project settings.

---

(Updated README with basic project information and common commands.)
