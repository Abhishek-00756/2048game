# Hex Merge

A native Android portrait hexagonal number-merging puzzle game inspired by 2048-style merging, with an original visual identity.

## Current implementation

- Native Android project using Kotlin and Gradle.
- True axial hex-grid coordinates with six-neighbor lookup.
- Hexagonal board rendering with responsive sizing for portrait phones.
- Number tile queue with weighted generation.
- Tap/drop placement onto empty hex cells.
- Deterministic adjacent-equal merge resolution with combo-aware scoring.
- Level targets: 32, 64, 128, 256, ...
- Score, best score, coins, stars, unlocked level and current board persistence.
- Pause, restart and settings dialogs.
- Shuffle queue and coin-based hammer utility.
- Corrupted/invalid save values are clamped during restore.
- Portrait orientation and release configuration.

## Open in Android Studio

1. Clone this repository.
2. Open the repository root in Android Studio.
3. Let Gradle sync.
4. Use JDK 17 for the Gradle/Android toolchain.
5. Run the `app` configuration on an Android emulator or physical device.

## Build

Debug APK:

```bash
./gradlew assembleDebug
```

Release APK:

```bash
./gradlew assembleRelease
```

The generated APKs are under `app/build/outputs/apk/`.

## Important configuration

- Package ID: `com.abhishek.hexmerge`
- App module: `app/`
- Game engine: `app/src/main/java/com/abhishek/hexmerge/GameEngine.kt`
- Hex math: `app/src/main/java/com/abhishek/hexmerge/HexGrid.kt`
- UI/input: `app/src/main/java/com/abhishek/hexmerge/MainActivity.kt`
- Persistence: `app/src/main/java/com/abhishek/hexmerge/SaveManager.kt`
- Android manifest: `app/src/main/AndroidManifest.xml`

## Testing notes

Test edge/corner hexes, repeated merges, rapid taps, rotation/orientation changes, restart/resume persistence, game-over behavior, and reset-progress handling on at least one emulator and one physical Android device before release.

## Not yet production-complete

This repository currently contains a playable core game rather than every item from the supplied master specification. Advanced items such as polished merge VFX/audio/haptics, rewarded ads, analytics backend, daily challenge/streak, full level-select/home/shop flows, undo history, wild/bomb boosters, generated app icon/splash artwork, automated tests and signed AAB release configuration still require a dedicated implementation pass.
