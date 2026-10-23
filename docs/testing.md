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
