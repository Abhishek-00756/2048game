# Hex Merge

Hex Merge is a native Android portrait puzzle game built around a true hexagonal grid. It is inspired by number-merging games but uses an original name, UI and implementation.

## Included in this repository

- Kotlin + Gradle Android Studio project
- Portrait responsive UI for modern Android phones
- True axial hex coordinates and six-neighbor detection
- Honeycomb-shaped 37-cell board
- Weighted 2 / 4 / 8 / 16 tile generation that changes with level
- Tile queue and tap/drop placement
- Adjacent equal-number merging and chain resolution
- Combo-aware scoring and best score
- Level progression with targets 32 → 64 → 128 → ...
- Star ratings and coin rewards
- Persistent board, queue, score, level, coins, stars and unlocked levels
- Undo history (20 moves)
- Shuffle, Hammer and Bomb boosters
- Game-over and retry flow
- Level-select screen
- Home screen
- Daily Challenge screen with deterministic daily best storage
- Shop screen using the offline coin economy
- Settings and reset-progress confirmation
- First-launch gameplay tutorial
- Pause / restart / home navigation
- Android back-button handling
- Audio abstraction, analytics abstraction and ad abstraction ready for SDK integration
- Invalid save-data validation
- Release build configuration

## Open in Android Studio

1. Clone the repository.
2. Open the repository root (`2048game`) in Android Studio.
3. Allow Gradle to sync.
4. Use JDK 17.
5. Run the `app` configuration on an emulator or physical Android phone.

The launcher is `HomeActivity`; selecting Play opens `GameActivity`.

## Build

If Android Studio generated the local Gradle wrapper for the project, use:

```bash
./gradlew assembleDebug
```

or use **Build → Make Project** / **Build → Generate App Bundles or APKs** in Android Studio.

## Architecture

- `GameEngine.kt` — gameplay state, merges, scoring, levels, boosters and undo
- `HexGrid.kt` — axial hex mathematics and neighbor lookup
- `GameModels.kt` — coordinates and save snapshot models
- `GameActivity.kt` — playable board and touch UI
- `HomeActivity.kt` — home, level select, daily, shop and settings
- `SaveManager.kt` — local persistence and corrupted-save recovery
- `ServiceManagers.kt` — audio, analytics, ads and daily-support abstractions

## Package

`com.abhishek.hexmerge`

## Before Google Play release

The game is a complete playable offline project, but Play Store production work still needs real store assets and service configuration: final icon/splash artwork, privacy-policy URL, production ad IDs/SDK integration if monetization is enabled, analytics provider, signing keystore, Play Console configuration, automated device testing and final balance/playtesting. The repository deliberately does not contain fake production ad IDs or pretend that those external services are already configured.

## Recommended test pass

Test first-launch tutorial, placement on every edge/corner, all six neighbor directions, repeated merges, undo, boosters, target completion, level unlocks, game over, app restart restoration, reset progress, different portrait aspect ratios and Android back navigation on an emulator and a physical device.
