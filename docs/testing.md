# Lab 1 acceptance tests

Run the checks against the existing Firebase project on a Google Play emulator or physical device. Use a dedicated test account rather than another person's credentials. Unit tests use a fake repository and do not prove that the cloud settings are correct.

| Test | Steps | Expected result |
| --- | --- | --- |
| Registration | Open Register; enter a fresh valid email, password of at least six characters and matching confirmation | Home opens and displays the user's email |
| Local validation | Submit an invalid email, short password and mismatched confirmation | Field errors appear; the repository is not called |
| Duplicate email | Log out and try to register the same email | A readable account-exists message; no crash |
| Wrong password | Log in with a valid account email and the wrong password | A readable credentials error; stay on Login |
| Loading | Submit a valid form | Progress indicator appears; duplicate submissions and field edits are disabled |
| Login back stack | Sign in, then press Back | Login/Register do not reappear; the app may return to the launcher |
| Logout back stack | Log out, then press Back | Home does not reappear |
| Auto-login | Sign in, force-stop/close the app and relaunch without clearing data | Home appears directly with the session email |
| Rotation | Fill a form and rotate the device | The ViewModel retains entered form values |
| Firebase user | Owner opens Authentication > Users | The newly registered test user is present |

## Automated coverage

`AuthValidatorTest` checks email structure, the six-character boundary, and exact confirmation matching. `AuthViewModelTest` verifies validation prevents repository calls, duplicate submissions are ignored, email whitespace is trimmed, sessions follow the auth-state flow, saved sessions restore, logout follows the listener, private exception details do not reach the screen, and password fields clear when forms switch.

Record real device and cloud results separately from these unit tests. An APK build alone is not an end-to-end authentication test.

## Verified on 23 October 2026

The real project `officepulse-86e4d` passed registration through Compose, user reload from the server, duplicate-email handling, incorrect-password handling, logout, and correct login. A force-stop followed by launching MainActivity restored the signed-in Home screen. The Android Back tests passed both before and after logout; reopening after logout stayed on Login. Activity recreation retained the email and password fields.

The test account uses a generated `officepulse-lab1-...@example.com` address. Its password is random, held only in the test process, and is not printed or committed. The first Gradle run cleaned up the installed APK automatically, so a retained-APK run was used for the persistence checks; two dedicated test accounts were created in total. The project owner can identify them by this email prefix in Firebase Console. The final emulator session is logged out.

See [screenshots and actual result records](evidence/README.md). These records contain no password, ID token, refresh token or private key. Firebase Console billing/membership and the instructor's access still need the project owner to confirm.

## Repeat the opt-in real-cloud checks

The regular device tests do not create cloud accounts. To deliberately run the cloud flow and retain the APK/session for the following test:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest `
  -Pandroid.testInstrumentationRunnerArguments.class=com.officepulse.app.FirebaseAcceptanceTest `
  -Pandroid.testInstrumentationRunnerArguments.runCloudAuth=true `
  -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true
```

Each enabled acceptance-test run creates one additional test account. The following command checks Back and session behavior using that retained session, then logs out:

```powershell
adb shell am instrument -w -r `
  -e class com.officepulse.app.FirebaseSessionNavigationTest `
  -e runCloudSessionChecks true `
  com.officepulse.app.test/androidx.test.runner.AndroidJUnitRunner
```

While logged out, Activity recreation can be checked without another cloud account:

```powershell
adb shell am instrument -w -r `
  -e class com.officepulse.app.AuthRotationTest `
  -e runRotationCheck true `
  com.officepulse.app.test/androidx.test.runner.AndroidJUnitRunner
```

`adb` is in the Android SDK's platform-tools folder. Run these after building/installing the current test APK. The opt-in tests skip when their explicit flag or required session state is absent.
