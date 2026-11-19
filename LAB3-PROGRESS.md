# Lab 3 progress and restart instructions

Last checkpoint: 19 November 2026. Work branch: `lab3/server-workflow`.
Project: `/Users/yazan/Desktop/OfficePulse-Lab3`.
Lab 2 is preserved separately; do not replace its submitted package.

## Completed implementation
- Separate Lab 3 Git worktree and branch created from integrated Lab 2 main.
- Pending/confirmed/rejected labels and rejection reasons added to visit cards.
- Explicit Edit visit button added.
- Debug/release backend choice made explicit through USE_EMULATORS.
- TypeScript Firestore trigger and transactional per-office booking arbitration implemented.
- Back-to-back visits allowed; conflicts rejected; pending requests sorted by creation timestamp and ID.
- Transactional audit records added; own-result updates ignored.
- Eight pure server tests pass.

## Written, still under verification
- Backend transaction integration tests (concurrency, retries, edits, independent offices).
- Security test suite extended from 11 to 20 cases.
- GitHub Actions app and server jobs including rule tests.

## Still to do
1. Finish local backend/emulator tests and fresh-clone instructions.
2. Verify Android tests/status UI against the running Functions emulator.
3. Create Sprint 3 issues, board tracking and submission/retro issues.
4. Push reviewed PR workflow; observe actual CI and fix any failures.
5. Demonstrate deliberately failing test then fix in PR history; require CI on main.
6. Save evidence, APK and backup bundle; prepare a contribution report at final handoff.
7. Human coursework: Nasser's own Sprint 3 PR/review; actual cross-team usability session with >=3 findings; fix critical findings; personal retrospectives; 4-minute demo and instructor assignment.
8. Tag lab3 only after acceptance, not while required work remains.

## Resume commands
Use Node 22 and npm; JDK from Android Studio (25 for Android build, JDK21+ for Firebase).
Local Node 22 is in `/Users/yazan/Documents/Codex/2026-11-16/ey/tmp/lab3-tools/node22/bin`.
```
npm ci --prefix functions
npm test --prefix functions
npm ci --prefix backend
./gradlew testDebugUnitTest assembleDebug lintDebug
npm test --prefix backend
npm --prefix backend run test:workflow
```
Emulator tests need their configured ports free. Existing Lab 2 services may occupy 8080/9099; do not delete emulator data to resolve this.

## Evidence
Build log: `/Users/yazan/Documents/Codex/2026-11-16/ey/work/lab3-android.log`.
No cross-team test, demo or student-authored contribution has been invented.
