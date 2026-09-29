# Time Since

Time Since is a private, native Android app for keeping any number of live **time-since** and **countdown** counters. It is designed to remain accurate after the app closes, the phone sleeps, or the device reboots: persisted timestamps are used instead of a background second counter.

## Features

- Unlimited independently running count-up and countdown timers
- Pause/resume, restart with history, or choose an exact date and time
- Six accessible counter colors and six independently saved display formats
- Live, battery-conscious one-second updates while the app is visible
- Move-up/move-down ordering persisted with Room
- Per-counter milestone/countdown notifications scheduled with WorkManager
- System share sheet, restart history, and confirmation before destructive actions
- System, light, and dark themes plus configurable defaults
- Fully offline: no account, ads, analytics, tracking, cloud service, or internet permission

## Technology

Kotlin, Jetpack Compose with Material 3, MVVM, Room, DataStore, Coroutines/Flow, WorkManager, and Gradle Kotlin DSL. The app supports Android 6.0 (API 23) and later and targets API 35.

## Build locally

Install JDK 17, Gradle 8.9, and an Android SDK containing API 35, then run:

```sh
gradle testDebugUnitTest assembleDebug
```

The APK is created at `app/build/outputs/apk/debug/app-debug.apk`. Android Studio is not required.

## Download an APK using only a phone

1. Push or merge this project to the repository's `main` branch, or open the **Actions** tab, choose **Build Android APK**, and tap **Run workflow**.
2. Open the completed green workflow run in GitHub.
3. Scroll to **Artifacts** and download **Time-Since-debug-apk**.
4. Extract the downloaded ZIP and open `app-debug.apk`. Android may ask you to allow installation from your browser or file manager.

The workflow checks out a clean copy, installs JDK 17 and the pinned Gradle 8.9 distribution, runs unit tests, builds the debug APK, and uploads it. No generated Gradle wrapper binaries are stored in the repository. Debug APKs are intended for personal installation and testing; a store release would require a private signing key.

## Privacy

All counters and preferences are stored locally on the device. Notification permission is requested only when notification features are used on Android 13 or later. Device backups may include the local database through Android's standard encrypted backup/transfer facilities.
