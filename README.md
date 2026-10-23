# SkipMate: Android-First Automatic YouTube Ad Skip Assistant

**Tagline**: *Watch videos. Skip the hassle.*  
**Application ID**: `com.skipmate.app`  
**Target Platform**: Android (Min SDK 29 / Android 10+, Target SDK 34)  
**Primary Language**: Kotlin  
**UI Framework**: Jetpack Compose with Material 3  

---

## 1. Executive Summary & Purpose

SkipMate is a native Android accessibility companion designed to assist users watching videos hands-free (e.g. while cooking, eating, working, or exercising). When a skippable advertisement plays in the official YouTube Android application and YouTube presents the standard accessible **Skip Ads** control, SkipMate detects the control and triggers a click action automatically.

### Key Tenets & Compliance
- **Zero Traffic Interception**: SkipMate does not modify YouTube's APK, inject code, block network traffic, or interfere with Google's advertising servers.
- **Privacy First (No INTERNET Permission)**: SkipMate does **not** request the `android.permission.INTERNET` permission. All event evaluation, state matching, and diagnostic logging occur 100% locally on the user's device.
- **Transparent Disclosures**: SkipMate never claims to skip all ads, unskippable bumper ads, or guarantee skips on every YouTube interface variation.
- **User Agency**: The user has full control to enable or disable the service at any time via the in-app master toggle or Android system settings.

---

## 2. Technical Feasibility & Android Accessibility Architecture

### Why Accessibility Service?
Android's `AccessibilityService` API is designed to assist users by reacting to user interface events (`AccessibilityEvent`) and querying the window hierarchy (`AccessibilityNodeInfo`). 

```
                                +---------------------------+
                                | Official YouTube App      |
                                +-------------+-------------+
                                              |
                                     (UI State Update)
                                              v
                                +---------------------------+
                                | Android OS Accessibility  |
                                | Framework                 |
                                +-------------+-------------+
                                              |
                          (TYPE_WINDOW_STATE_CHANGED / CONTENT_CHANGED)
                                              v
+-----------------------------------------------------------------------------------+
| SkipMate Accessibility Engine                                                     |
|                                                                                   |
|  1. Service State Check  ---> Enabled in App Settings?                           |
|  2. Target Package Check ---> Package matches com.google.android.youtube?        |
|  3. Root Node Validation ---> Active window hierarchy present?                    |
|  4. Multi-Tier Matcher   ---> 1. Known Resource ID                                |
|                               2. Localized Text Dictionary (14+ languages)        |
|                               3. Content Description Match                        |
|                               - Negative Exclusion Pattern Filter                 |
|  5. Actionability Guard  ---> isVisibleToUser && isEnabled && isClickable?        |
|  6. Debounce Protection  ---> > 1000ms elapsed since last action dispatch?        |
|  7. Click Dispatch       ---> performAction(ACTION_CLICK)                         |
|  8. Diagnostic Log       ---> Record dispatch result in bounded ring buffer       |
+-----------------------------------------------------------------------------------+
```

### Critical Distinction: Action Acceptance vs. Skip Confirmation
- Calling `AccessibilityNodeInfo.performAction(ACTION_CLICK)` returns `true` when the **Android framework accepts the click action**.
- A return value of `true` **does not** prove that YouTube actually skipped the advertisement (e.g. if an unskippable countdown is active or the view is mid-transition).
- SkipMate accurately records `CLICK_DISPATCH_SUCCESS`, and never fabricates claims that an advertisement was skipped.

---

## 3. Core Features

### Feature A: Skip Assistant (`SkipMateAccessibilityService`)
- Operates strictly when the user enables it in Android settings.
- Scoped to `com.google.android.youtube` via service configuration and runtime validation.
- Reactive event pipeline (no battery-draining polling or coordinate taps).
- Multi-tier matching algorithm supporting 14+ languages (English, Spanish, French, German, Portuguese, Italian, Russian, Japanese, Korean, Chinese, Hindi, Indonesian, Turkish, Arabic).
- Negative filters prevent false positives (e.g., "Skip forward 10s", "Skip chapter", "Skip trial").

### Feature B: Main Dashboard
- Modern Material 3 Jetpack Compose UI with dark/light theme support.
- Large interactive master switch for pausing/resuming automation.
- Real-time status cards:
  - YouTube Detection Status (Installed / Version / Not Found)
  - Accessibility Permission Status (Ready / Permission Required)
  - Last Activity Status (Live event reporting)
- Quick actions: Configure Permissions, Open YouTube, Diagnostics, Settings.

### Feature C: Permission Setup Guide
- Transparent, educational 4-step walkthrough explaining how to enable the service in Android Settings.
- Deep link to `Settings.ACTION_ACCESSIBILITY_SETTINGS`.
- Comprehensive explanation of data safety and local processing.

### Feature D: Diagnostic Center
- Live event feed distinguishing between:
  1. `NO_RELEVANT_EVENT`: Service paused or outside target app.
  2. `TARGET_APP_DETECTED`: YouTube active in foreground.
  3. `NO_CANDIDATE_FOUND`: Scanned hierarchy, no skip control present.
  4. `CANDIDATE_NOT_ACTIONABLE`: Candidate found but disabled or hidden.
  5. `CLICK_ATTEMPTED`: Initiating click action.
  6. `CLICK_DISPATCH_SUCCESS`: Framework accepted action.
  7. `CLICK_DISPATCH_FAILED`: Action rejected by framework or node is stale.
- One-tap button to clear local diagnostic logs.

### Feature E: Local Settings (DataStore Preferences)
- Preferences stored via Jetpack DataStore:
  - Master toggle state
  - Target application package
  - Diagnostic logging toggle
  - Theme mode (System, Light, Dark)
  - Onboarding state

---

## 4. Project Structure

```
SkipMate/
├── build.gradle.kts                  # Root build configuration
├── settings.gradle.kts               # Plugin & dependency repositories
├── gradle.properties                 # JVM arguments & AndroidX flags
├── local.properties                  # Local Android SDK pointer
└── app/
    ├── build.gradle.kts              # App module dependencies (Compose, DataStore)
    ├── proguard-rules.pro            # ProGuard optimization rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml   # Manifest (Service declaration, queries, NO internet)
        │   ├── res/
        │   │   ├── values/           # strings.xml, colors.xml, themes.xml
        │   │   ├── xml/              # accessibility_service_config.xml
        │   │   └── drawable/         # Vector icons (logo, launcher drawables)
        │   └── java/com/skipmate/app/
        │       ├── SkipMateApplication.kt
        │       ├── MainActivity.kt
        │       ├── accessibility/
        │       │   ├── AccessibilityNodeWrapper.kt        # Pure Kotlin interface
        │       │   ├── AndroidAccessibilityNodeWrapper.kt # Android OS implementation
        │       │   ├── ClickAttemptResult.kt              # Action result model
        │       │   ├── SkipControlMatcher.kt              # Multi-tier matching engine
        │       │   ├── AccessibilityEventProcessor.kt     # Reactive pipeline & safety
        │       │   └── SkipMateAccessibilityService.kt    # OS AccessibilityService
        │       ├── data/
        │       │   ├── diagnostics/                       # DiagnosticRepository & Event
        │       │   └── preferences/                       # SettingsRepository (DataStore)
        │       ├── platform/
        │       │   ├── InstalledAppChecker.kt             # Package verification
        │       │   └── AccessibilitySettingsLauncher.kt   # System Settings intent
        │       └── ui/
        │           ├── components/                        # StatusCard, PrimaryButton, etc.
        │           ├── navigation/                        # Screen routes & NavHost
        │           ├── screens/                           # Home, Permission, Diagnostics, Settings, About
        │           └── theme/                             # Material3 Color, Type, Theme
        └── test/java/com/skipmate/app/
            ├── accessibility/
            │   ├── TestAccessibilityNode.kt               # Headless test double
            │   ├── SkipControlMatcherTest.kt              # 12 required test scenarios
            │   └── AccessibilityEventProcessorTest.kt     # Debounce & lifecycle tests
            └── data/
                └── DiagnosticRepositoryTest.kt            # Ring-buffer queue tests
```

---

## 5. Build and Setup Instructions

### Prerequisites
- JDK 17 (e.g. Eclipse Temurin 17 or OpenJDK 17)
- Android SDK with Platform 34 and Build-Tools 34.0.0
- Gradle 8.4+

### Building and Testing from Command Line

1. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

2. **Assemble Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The resulting APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on Connected Device / Emulator**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Enabling the Accessibility Service on Device
1. Open **SkipMate**.
2. Complete the onboarding screen.
3. Tap **Configure Permissions** or **Open Accessibility Settings**.
4. In Android Settings, navigate to **Installed apps** / **Downloaded apps**.
5. Select **SkipMate Ad Skip Assistant** and toggle the switch to **ON**.
6. Return to **SkipMate**; the status indicator will update to **Ready**.
7. Open YouTube and play a video with skippable advertisements.

---

## 6. Test Report: Verified vs. Unverified Behavior

| Component / Scenario | Verification Status | Verification Methodology |
| :--- | :--- | :--- |
| **SkipControlMatcher: Exact label match** | **VERIFIED** | Unit tested (`SkipControlMatcherTest`) |
| **SkipControlMatcher: Localized labels (7+ langs)** | **VERIFIED** | Unit tested against multi-lingual test matrix |
| **SkipControlMatcher: Exclusion filters** | **VERIFIED** | Unit tested against false positives ("Skip intro", "Skip chapter", etc.) |
| **Ancestor resolution for non-clickable nodes** | **VERIFIED** | Unit tested (resolves clickable parent containers) |
| **Disabled / Invisible candidate rejection** | **VERIFIED** | Unit tested (validates `isVisibleToUser` and `isEnabled`) |
| **Debounce & duplicate event protection** | **VERIFIED** | Unit tested (`AccessibilityEventProcessorTest`) |
| **Stale node exception handling** | **VERIFIED** | Unit tested (simulated detached node failure) |
| **Target package scoping** | **VERIFIED** | Unit tested (ignores non-target packages) |
| **Diagnostic bounded ring buffer** | **VERIFIED** | Unit tested (`DiagnosticRepositoryTest`) |
| **DataStore local preferences** | **VERIFIED** | Validated via code and lifecycle observation |
| **Real YouTube Ad Skip on physical device** | **UNVERIFIED (Requires physical device / emulator playback)** | Requires active video stream with live ad server response |

---

## 7. Cross-Platform Analysis & Future Roadmap

### iOS Feasibility
- **iOS Sandbox Restrictions**: Apple iOS does **not** provide any public accessibility API comparable to Android's `AccessibilityService` that allows one application to inspect or automate user interactions within another third-party application (such as YouTube).
- **Feasible Alternatives on iOS**:
  - Safari Web Extension: Can automate skip button clicks for YouTube played inside the Safari browser.
  - Custom YouTube player client or Picture-in-Picture helper using YouTube IFrame Player API.

### Desktop & Web Extensions
- Browser extensions for Chrome, Firefox, and Edge can directly observe DOM mutations via `MutationObserver` and click the `.ytp-skip-ad-button` element reliably.
