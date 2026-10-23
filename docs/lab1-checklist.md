# Lab 1 completion checklist

Use this list for the live demo. A checkbox is complete only when supported by the actual repository, device or console.

## Product and repository

- [x] App name, problem statement, two team members and responsibilities documented
- [x] Six MVP features marked Sprint 1/later
- [x] At most five planned screens and three typed Firestore collections with ownership fields
- [x] Mermaid architecture and run instructions
- [x] PR and user-story templates
- [x] Board: Backlog, Sprint 1, In progress, In review, Done
- [x] Sprint 1 stories have acceptance criteria, labels and assignees
- [x] Board workflows move closed issues and merged PRs to Done
- [x] Protect-main ruleset verified: PR and one approval, no force pushes, no deletions
- [x] Board made public so the teammate and instructor can read it (Naser manages the board)
- [ ] At least two PRs merged after a review by the other member

## App and Firebase

- [x] Kotlin/Compose app, minimum API 26, existing Firebase client configuration
- [x] Repository, pure validator, ViewModel/StateFlow and UI layers
- [x] Listener-driven session state and navigation
- [x] Updated source builds; 9 authentication unit tests plus the starter test pass; lint has 0 errors
- [x] Three authentication UI tests plus the starter device test pass on the API 36 emulator (fake repository)
- [ ] Device acceptance tests in testing.md passed
- [ ] Owner confirms Spark plan, Email/Password enabled, team and instructor access
- [ ] Owner confirms new test user is visible in Firebase Authentication

## Demo and retrospective

- [ ] Demo: problem/MVP, data model/architecture, register/logout/login/restart, board/PRs/rules
- [x] Retro - Lab 1 issue #8 contains two action items; Naser's comment is posted
- [ ] Each member comments: went well / did not go well / we will change

Firestore CRUD, queue logic and Cloud Functions are planned for Labs 2 and 3, not prerequisites for completing the Lab 1 login skeleton.

Verification date: 23 October 2026. Build/lint: 0 errors, 13 warnings (dependency update notices and starter-resource warnings). These automated results do not mark real Firebase acceptance tests complete.
