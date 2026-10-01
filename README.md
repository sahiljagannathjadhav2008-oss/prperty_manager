# Property Manager (native Android)

Kotlin + Jetpack Compose + Room (SQLite). Fully offline, no network permission.

Toolchain: JDK 17, Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21, KSP 2.0.21-1.0.28, Room 2.6.1,
compileSdk/targetSdk 35, minSdk 24.

Build: push to GitHub, open Actions -> "Build Android APK" -> download the `PropertyManager-debug-apk` artifact.
Local build: `gradle assembleDebug` (Gradle 8.9) or open the folder in Android Studio.
