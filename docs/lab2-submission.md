# Lab 2 – submission

OfficePulse for Akademia WSB · Kotlin/Compose, Firebase Auth and Firestore local emulators.

Implementation and automated checks are complete. Formal requirement acceptance remains pending peer review and merge to main.

## Published evidence

| Requirement | Evidence | Acceptance |
| --- | --- | --- |
| R1 Planning | [Sprint 2 board](https://github.com/users/Yazanleva/projects/2), #27, README DoD, completed Lab 1 retro #8 | Board maintenance continues |
| R2 Backend | #9, PR #22; README setup and seeded offices | Awaiting peer review |
| R3 Model | #10, PR #22; README field/type/setter table | Awaiting peer review |
| R4 Architecture | #11, PR #22; repository and screen ViewModels | Awaiting peer review |
| R5 Private live list | #12, PR #22; list screenshot and account-switch tests | Awaiting peer review |
| R6 Validated create/edit | #13, PR #22; validation screenshot and device tests | Awaiting peer review |
| R7 Delete/cancel | #14, PR #22; device tests | Awaiting peer review |
| R8 Rules | #15, PR #22; 11 passed scenarios including required 1–8 | Awaiting peer review |
| R9 Extensions | #16; E1 #17 / PR #23, E3 #18 / PR #24, E4 #19 / PR #25 | Awaiting peer review |
| R10 Team/demo | Retro #20; handoff checklist | Actual contributions, reviews, demo and retro pending |

- [x] Implementation, build, 26 unit tests, nine device tests and 11 rules tests recorded.
- [x] Five PRs published; Nasser requested as reviewer.
- [x] Issue owners, Sprint 2 board and Lab 2 milestone created.
- [ ] R1–R9 peer acceptance and reviewed merges completed.
- [ ] Every member authored at least one reviewed and merged Lab 2 PR.
- [ ] 3–4 minute live demo completed.
- [ ] Both members' retrospective comments and agreed actions in #20.
- [ ] Instructor assigned (GitHub username needed).
- [ ] Reviewed main revision tagged lab2.

## Screenshots and test results

Evidence is from the recovered build verified on 17 November 2026.

![R5 private visits](https://github.com/Yazanleva/1.-officepulse/blob/lab2/recovered/docs/evidence/lab2/list.png?raw=true)
![R6 validation](https://github.com/Yazanleva/1.-officepulse/blob/lab2/recovered/docs/evidence/lab2/validation.png?raw=true)
![R9 E1 search](https://github.com/Yazanleva/1.-officepulse/blob/lab2/recovered/docs/evidence/lab2/search.png?raw=true)
![R9 E4 picker](https://github.com/Yazanleva/1.-officepulse/blob/lab2/recovered/docs/evidence/lab2/picker.png?raw=true)

[Device tests](https://github.com/Yazanleva/1.-officepulse/blob/lab2/recovered/docs/evidence/lab2/device-tests.txt) · [Rules tests](https://github.com/Yazanleva/1.-officepulse/blob/lab2/recovered/docs/evidence/lab2/rules-tests.txt) · [Unit test totals](https://github.com/Yazanleva/1.-officepulse/blob/lab2/recovered/docs/evidence/lab2/unit-tests-summary.json)

## Demo order

1. Show three office records and numeric serviceMinutes.
2. Register and book; inspect owner and timestamps.
3. Edit; show unchanged createdAt and updated updatedAt.
4. Show validation, search, empty/no-results and native pickers.
5. Cancel deletion, then confirm and show backend deletion.
6. Switch accounts and demonstrate private lists.
7. Show rules results, reviewed PRs, board and retrospective.

[Full handoff checklist](https://github.com/Yazanleva/1.-officepulse/blob/lab2/recovered/docs/lab2-handoff.md). Debug APK requires local backend emulators and adb reverse setup; no production cloud deployment is claimed.
