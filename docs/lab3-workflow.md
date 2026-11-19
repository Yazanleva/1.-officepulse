# Lab 3: local booking decisions

## Fresh clone

Install Node 22/npm, JDK 25 and Android SDK 37. Set JAVA_HOME and ANDROID_HOME. Clone this repository and check out `lab3/server-workflow` while its PR is under review; after merge use main.

```sh
npm ci --prefix functions
npm run build --prefix functions
npm ci --prefix backend
npm --prefix backend start
```

Leave that terminal running. The local demo project does not require a paid Firebase plan or cloud deployment. Auth uses 9099, Firestore 8080, Functions 5001 and the UI 4000. Stop another local emulator suite before starting this one; preserve its data first if needed. In another terminal:

```sh
npm --prefix backend run seed
adb reverse tcp:9099 tcp:9099
adb reverse tcp:8080 tcp:8080
./gradlew :app:installDebug
```

The seed prints two disposable local account credentials and creates three demo offices. Open OfficePulse and sign in. Debug builds select the demo emulators via USE_EMULATORS; release builds select the existing cloud configuration. The release backend needs separately deployed rules, seed references and functions; local verification does not claim that deployment.

## Decision rule

A new visit or a key-field edit is pending. The server checks other visits in the same office. Confirmed bookings retain their slots. Pending bookings are ordered by server creation timestamp, then document ID. Overlap rejects the later candidate with a readable reason; an end time equal to the next start time is allowed. Different offices do not conflict.

The worker reads an office lock and its visits in a Firestore transaction, then writes the decisions, lock and audit records atomically. A concurrent worker retries against the current data. Updates already marked confirmed/rejected are ignored by the trigger. A cancelled/deleted confirmed booking releases its slot for new or edited requests; previously rejected bookings require an edit/resubmission.

This is a small-coursework workload: one transaction reads an office's visits. Production scale would require bounded scheduling windows and capacity design.

## Tests

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
npm test --prefix functions
npm test --prefix backend
npm --prefix backend run test:workflow
```

The backend test scripts use firebase.test.json on port 8180, separate from the app's Firestore port. Run the two backend commands sequentially. Server tests build TypeScript first. Transaction tests must use the same Firebase Admin dependency instance as the implementation.

Saved evidence is in docs/evidence/lab3. GitHub Actions runs app tests/build/lint and server/security/transaction tests for PRs and pushes to main. Actual CI outcomes and the deliberate failure demonstration remain tracked in LAB3-PROGRESS.md.

## Device acceptance

1. Sign in as account A and create a visit. Observe pending, then confirmed.
2. Sign in as account B and request the same office and overlapping time. Observe rejected and its reason.
3. Edit B's visit to start when A's ends. Observe pending, then confirmed.
4. Verify each account only sees its own visits; test cancel/delete and a key edit again.
5. Record screenshots and actual results. Do not mark these complete solely from server tests.

Cross-team usability, personal reviews/retrospectives and the classroom demo require the actual participants. Record findings and links in the submission issue.
