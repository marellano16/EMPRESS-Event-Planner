# Running log

Decisions made, things that broke, and what we changed. This is the raw
material for interview answers.

## 2026-10-10
- Created the repo with backend/ and frontend/ folders.
- Saved the project plan in docs/project-plan.md.
- Scaffolded the Java backend (Javalin, Maven): POST /plan returns a stub plan,
  Calculator does the math (per-person, fixed, pack rounding, 10% contingency),
  4 JUnit tests.
- Set up Java on Windows: installed JDK 21 and Maven 3.10.0, set JAVA_HOME and
  added Maven to PATH. `mvn -v` confirmed both.
- `mvn test` passes (4 tests) and the server starts on port 8080.
- Broke: extracted the scaffold zip into backend/ instead of the project root,
  so Maven found no pom.xml. Moved the files up one level and it worked.
- Decision: the stub /plan returns hard-coded items with real calculator math,
  so the frontend can build against the response shape before the Claude call exists.
- Next: write the API contract, add the Claude call to replace the stub, and get
  Ava set up on her Mac to start the Streamlit frontend.
