# EMPRESS Event Planner

An AI event planner: enter an event and a member count, get back a materials
list and a budget breakdown. Claude drafts the plan; a Java engine does the
math; a Streamlit page lets you review and edit it.

Full plan: [docs/project-plan.md](docs/project-plan.md) ·
Running log: [docs/log.md](docs/log.md)

## Layout

```
backend/    Java engine: POST /plan, calls Claude, calculates the budget
frontend/   Python Streamlit page: form, editable line items, totals
docs/       Project plan and running log
```

The API key lives on the backend only. The frontend never talks to Claude
directly. Only member counts are sent to Claude, never names or personal
information from Empress records.

## Setup (once)

1. Create an Anthropic Console account, an API key, and a monthly spending limit.
2. Copy the example env file and add your key:
   ```
   cp .env.example .env
   ```
   `.env` is git-ignored. Never commit it.

## Run the backend (Java)

Requires JDK 17+ and Maven. Built with Javalin, Jackson and JUnit 5.

```
cd backend
mvn test                  # run the calculator tests
mvn compile exec:java     # start the server on http://localhost:8080
```

Check it is up: open http://localhost:8080/health (should say `ok`).

`POST /plan` currently returns a **stub** plan (hard-coded items, real
calculator math) so the frontend has something to build against. Try it:

```
curl -X POST http://localhost:8080/plan -H "Content-Type: application/json" \
  -d '{"eventType":"pizza night","memberCount":40,"budgetCap":200}'
```

On Windows PowerShell, use `curl.exe` and escape the quotes, or use the
frontend instead.

Next step: replace `App.buildStubPlan` with the Claude call. The Anthropic Java
SDK dependency is not in `pom.xml` yet. The backend should read
`ANTHROPIC_API_KEY` from the environment.

## Run the frontend (Python)

```
cd frontend
python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements.txt
export BACKEND_URL=http://localhost:8080
streamlit run app.py
```

## Working together

- Branch per task, open a pull request, and have the other person review it.
- Add decisions and things that broke to [docs/log.md](docs/log.md).
