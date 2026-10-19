# PhoneDex

Journal de bord des smartphones repérés dans la vie réelle · A log of the smartphones you spot in real life.

Kotlin · Jetpack Compose · Material 3 · Room · osmdroid (OpenStreetMap). No account, no ads, no tracking, no Google services. Data stays on your device.

## Build

```
./gradlew assembleRelease     # unsigned APK in app/build/outputs/apk/release/ (no keys needed)
./gradlew testDebugUnitTest   # unit tests
```

Requires JDK 17 and the Android SDK (API 35). The Gradle wrapper can be generated with the
"Generate Gradle wrapper" GitHub workflow, or with `gradle wrapper --gradle-version 8.9`.

Optional signing: set `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` before building.

## Network use

Map tiles are downloaded from OpenStreetMap (© OpenStreetMap contributors) when the Map tab is shown,
and the system geocoder may contact its provider to turn GPS coordinates into an address.
Everything else works offline.

## License

Copyright (C) 2026 Tz0P

PhoneDex is free software: GPL-3.0-or-later. See [LICENSE](LICENSE).
Third-party components and their licenses: [THIRD_PARTY.md](THIRD_PARTY.md).
