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

Not scaffolded yet. The plan calls for Javalin or Spring Boot, one endpoint
(`POST /plan`), Jackson for JSON, and JUnit tests. Once the build tool is
chosen, the run command goes here, for example `mvn exec:java` for Maven.
The backend should read `ANTHROPIC_API_KEY` from the environment and listen
on port 8080.

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
