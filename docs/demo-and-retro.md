# Lab 1 demo and retrospective

## Three-minute demo

1. Naser: explain the problem, Sprint 1 MVP and the five-screen/three-collection limit.
2. Yazan: show the typed data model and Mermaid architecture in README.
3. Register a fresh test account. Show Home and its signed-in email, log out, log in again, then restart the app to demonstrate auto-login. Test one invalid form and one incorrect password.
4. Show the sprint board, protected main ruleset, reviewed PRs and merged history.

The Firebase project owner verifies the test user in Authentication > Users. Both members need to be able to explain why ownership must be enforced by Firestore Security Rules in Lab 2.

## One-presenter rehearsal

Naser can present the whole demonstration if Yazan is unavailable; this does not replace Yazan's own review or retrospective comment.

- **0:00-0:30:** "OfficePulse helps university students avoid office queues by booking visits and tracking their turn. Lab 1 delivers registration, login, logout and automatic session restoration. Office browsing and visits are planned for Lab 2; queue updates and expiry are planned for Lab 3."
- **0:30-1:00:** Open README. Show the five planned screens, the three collections and their field types. Explain that `visits.ownerId` identifies the owner. Show the architecture: Compose calls the ViewModel, which validates input and calls the repository; the repository communicates with Firebase Authentication.
- **1:00-2:15:** Run the app. Show one invalid form, register a fresh dedicated test account, show Home and its email, log out, log in, then close and reopen the app to show automatic login. After logout, Back must not restore Home. Use a password you can enter again; keep it out of screenshots and the repository.
- **2:15-3:00:** Show the public sprint board, the actual review comments and merged PR history, then the active protect-main ruleset. Open Retro - Lab 1 and show the action items. If the reviews or merges are still pending, say so rather than describing them as completed.

## Likely technical questions

- **Where is ownership enforced?** In server-side Firestore Security Rules in Lab 2, using the authenticated UID and the document owner field. UI checks alone cannot protect cloud data.
- **Why a repository?** It isolates Firebase calls and allows the ViewModel and navigation to be tested with a fake implementation.
- **What controls navigation?** The Firebase auth-state listener feeds `isLoggedIn`. A session change resets the navigation graph so Back cannot reveal the previous session's screens.
- **Does Lab 1 need Firestore CRUD?** The handout requires its initial data model now. The temporary Home becomes the list screen in Lab 2; Lab 1's working cloud feature is Firebase Authentication.

## Final sign-off order

1. Yazan reviews and approves PR #3, then merges it into `feature/auth`.
2. Naser checks the updated PR #2, approves it and merges it into `main` without bypassing the ruleset.
3. Verify authentication issue #1 is closed and the merged PRs/issue move to Done.
4. Confirm repository and Firebase team/instructor access, the Firebase Spark plan and the dedicated `officepulse-lab1-...@example.com` test users in Authentication > Users.
5. Each member posts their own retrospective comment; present the live demo.

## Retrospective action items

- Run build, unit tests and emulator checks before requesting PR review; record cloud test results separately.
- Keep Sprint 1 issues assigned and update the board when a PR enters review or merges.

Each member adds their own honest comment to `Retro - Lab 1`: what went well, what did not go well, and what to change. Do not add a comment on behalf of the other member.
