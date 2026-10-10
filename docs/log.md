# Running log

Decisions made, things that broke, and what we changed. This is the raw
material for interview answers.

## 2026-10-10
- Created the repo with backend/ and frontend/ folders.
- Saved the project plan in docs/project-plan.md.
- Scaffolded the Java backend (Javalin, Maven): POST /plan returns a stub plan,
  Calculator does the math (per-person, fixed, pack rounding, 10% contingency),
  4 JUnit tests.  mvn test passes (4 tests) and the server starts on port 8080 (JDK 21, Maven 3.10.0).
