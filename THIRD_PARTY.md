# Third-party components

PhoneDex itself is GPL-3.0-or-later (see `LICENSE`). It bundles no fonts, no photographs and no brand logos.

## Libraries (all free software, none proprietary, no Google Play Services / Firebase)

| Component | License |
|---|---|
| AndroidX (core, activity, lifecycle, navigation, Room) | Apache-2.0 |
| Jetpack Compose and Material 3 | Apache-2.0 |
| Material Icons (material-icons-extended) | Apache-2.0 |
| osmdroid | Apache-2.0 |
| Kotlin standard library and kotlinx.coroutines | Apache-2.0 |
| JUnit 4 (tests only, not in the APK) | EPL-1.0 |

Apache-2.0 text: `LICENSES/Apache-2.0.txt`.

## Icons, fonts, images

| Asset | Source | License |
|---|---|---|
| In-app icons | Material Icons library (Google) | Apache-2.0 |
| Launcher icon (`ic_launcher_foreground.xml`, store `icon.png`) | Drawn for this project | GPL-3.0-or-later |
| Fonts | None bundled (system font) | n/a |

## Map data

Map tiles and data © OpenStreetMap contributors, ODbL: https://www.openstreetmap.org/copyright
The attribution is displayed on the map.

## Adding assets later

For every file you add (image, icon, font), add a row here with: path, author, source URL, license.
Put new license texts in `LICENSES/`. Do **not** commit trademarked brand logos: F-Droid only accepts
content that is free to redistribute.
