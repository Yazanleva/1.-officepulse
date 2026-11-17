# OfficePulse sprint issues and proposed owners

The assignees below own the next acceptance check or follow-up. Each teammate records their own changes, device checks, comments and approvals. The existing Lab 1 stories for browsing offices, booking, visits and queue updates are #4–#7. The last Lab 1 retrospective is #8.

## Lab 2

| Issue | Assignee | Acceptance criteria |
| --- | --- | --- |
| R2 · Seed read-only offices and configure Firestore | Mohamad Yazan Alsaloumi | Start the emulators, inspect three offices with numeric service minutes and verify the stack table. |
| R3 · Verify the visit data model | Mohamad Yazan Alsaloumi | Compare every README field, type and setter with a stored booking. Confirm the owner comes from the session. |
| R4 · Review the visit repository and state holders | Nasser Almassry | Confirm injectable repository, one ViewModel per booking screen, pure validation, and zero backend calls from Compose UI. Identify and implement any review fixes. |
| R5 · Show my visits live | Mohamad Yazan Alsaloumi | Add a visit in the console while its owner is signed in. The row appears live. Verify that signing in as the other account hides it. |
| R6 · Verify booking and rescheduling | Mohamad Yazan Alsaloumi | Demonstrate office, topic, date and cross-field errors, then save an edit. Creation timestamp stays unchanged and updated timestamp advances. |
| R7 · Verify delete and cancel | Nasser Almassry | On a second device, cancel the named dialog and verify the record survives. Confirm deletion and verify the owner list and backend update. |
| R8 · Enforce booking ownership and reference access | Nasser Almassry | Reproduce scenarios 1–8, attach results to the PR, then run valid owner CRUD with the same rules. |
| R9 · Verify three booking extensions | Nasser Almassry | Link three separate extension PRs, test their acceptance criteria and attach screenshots. |
| E1 · Search visits by office and topic | Mohamad Yazan Alsaloumi | Query both fields, vary case and clear the query. |
| E3 · Distinguish empty history and no results | Nasser Almassry | Demonstrate both messages on a device and verify the empty history offers a first-booking action. |
| E4 · Choose appointment dates and times | Mohamad Yazan Alsaloumi | Use native pickers for both endpoints and check end-after-start validation. |
| Retro – Lab 2 | Nasser Almassry | Each member posts what went well and what did not. Agree on one or two specific actions. |
| Lab 2 – submission | Mohamad Yazan Alsaloumi | Link the Sprint 2 board, reviewed PRs for R2–R9, screenshots for R5/R6/R9 and the retro. Assign the instructor and tag the reviewed main revision after acceptance. |

