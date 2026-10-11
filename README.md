# EMPRESS Event Planner

An AI event planner: enter an event and a member count, get back a materials
list and a budget breakdown. An AI model (Google Gemini) drafts the plan; a Java
engine does the math; a Streamlit page lets you review and edit it. The model
sits behind a small `PlanGenerator` interface, so the provider can be swapped.

Full plan: [docs/project-plan.md](docs/project-plan.md) ·
Running log: [docs/log.md](docs/log.md)

## Layout

```
backend/    Java engine: POST /plan, calls Gemini, calculates the budget
frontend/   Python Streamlit page: form, editable line items, totals
docs/       Project plan and running log
```

The API key lives on the backend only. The frontend never talks to the model
directly. Only the event type, member count and budget cap are sent to the
model, never names or personal information from Empress records.

## Setup (once)

The backend works without a key (it returns a sample plan). To use Gemini:

1. Create an API key in Google AI Studio (aistudio.google.com). Check the
   current free-tier limits and terms there, including how your data is used.
2. Keep the key in a `.env` file you create from the example:
   ```
   cp .env.example .env
   ```
   `.env` is git-ignored. Never commit it.
3. Java does not read `.env` by itself. Set the key in the terminal you start
   the backend from, every time you open a new terminal:
   ```
   # Windows PowerShell
   $env:GEMINI_API_KEY="your-key-here"
   # Mac / Linux
   export GEMINI_API_KEY="your-key-here"
   ```
   Optional: set `GEMINI_MODEL` the same way. Model names change over time, so
   if you get an error about the model, pick a current one in AI Studio.

## Run the backend (Java)

Requires JDK 17+ and Maven. Built with Javalin, Jackson and JUnit 5.

```
cd backend
mvn test                  # run the calculator and parser tests
mvn compile exec:java     # start the server on http://localhost:8080
```

Check it is up: open http://localhost:8080/health (should say `ok`). On
startup it prints whether it is using Gemini or the stub generator.

`POST /plan` asks Gemini for a materials list (validated, with one retry if
the JSON is bad), then the Java calculator adds up the costs. With no
`GEMINI_API_KEY` set it returns a **stub** plan (hard-coded items, real
calculator math). If Gemini fails or answers badly twice, the endpoint returns
HTTP 502 with an `error` message. Try it:

```
curl -X POST http://localhost:8080/plan -H "Content-Type: application/json" \
  -d '{"eventType":"pizza night","memberCount":40,"budgetCap":200}'
```

On Windows PowerShell, use `curl.exe` and escape the quotes, or use the
frontend instead.

Next step: ground the model in real past Empress events (exact match, then
similar events) so estimates come from our own data.

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
