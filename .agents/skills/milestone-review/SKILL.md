---
name: milestone-review
description: Review milestone work and progress against the MILESTONES.md tracker; handle task-scoped commit and push only when the user authorizes Git delivery.
---

# Milestone review

1. Read `AGENTS.md` and the `MILESTONES.md` tracker. Inspect Git status and the relevant diff.
2. Identify the current card and its acceptance criteria. Review correctness, Spring wiring, coupling, mutable state, configuration, error handling, resource ownership, and scope compliance where applicable.
3. Verify Maven uses Java 25 and the selected stable Spring Boot version is compatible. Run relevant existing checks; do not write tests unless requested.
4. Classify findings by severity, citing files and lines where useful. Record actual verification evidence and unresolved issues in the tracker when documentation updates are authorized. Report the next unfinished card and its learning topics.
5. A review-only request stays read-only unless tracker or documentation updates are explicitly authorized. Commit and push only when the user's request authorizes Git delivery. Stage only task-related changes, inspect the staged diff, commit, and push to the intended configured remote and branch.
6. Report the commit hash, branch, push result, and remaining blockers. Never force-push, rewrite history, modify remotes, or include unrelated changes. If the destination is ambiguous or access is unavailable, complete the local review and report the specific blocker.
