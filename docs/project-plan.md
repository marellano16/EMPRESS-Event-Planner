Empress Event Planner — AI Project Plan
Oct 10, 2026 · @Someone
Overview and the Empress story
We are building an AI event planner: enter an event and a member count, get back a materials list and a budget breakdown. The goal is a project we can explain in depth, not necessarily a finished product.
The story we tell (draft, fill in with real details):
• Problem: As Empress membership chairs, Ava and Malia planned events by guessing quantities and costs, then fixing mistakes after the fact. [Add one real example: an event where we over- or under-bought.]
• What we did: Interviewed other club leaders, collected real past-event costs, and built a tool where Claude drafts the plan and a Java engine does the math.
• Result: [Fill in after testing, e.g. "estimates landed within X% of actual cost on our past events."]
• What we learned: Splitting the work (Java backend, Python frontend), designing prompts for reliable JSON, and keeping a human in the loop on AI estimates.
Where this lives: This plan goes into the Empress documentation folder in Google Drive, next to the event records it draws on.
Ground rules: Progress over perfection. A well-documented half-built project beats an undocumented finished one. No stress.
Part 1: Groundwork
Define the problem (Ava leads)
[ ] Interview 3–5 event planners (club presidents, treasurers, event chairs). Start with Empress board members.
[ ] Ask how they plan now, what goes wrong, and what they'd want to control themselves
[ ] Write user stories from the interviews (e.g., "As a treasurer, I want cost per member…")
[ ] Save interview notes in this Drive folder — they become quotes for the story
Collect test data (Both)
[ ] Pull 3–5 past Empress events with actual purchases and costs from the Drive records
[ ] Organize them in a spreadsheet to use as the accuracy test set
Scope the MVP (Both)
[ ] Agree on the core feature: event + member count → materials list + budget breakdown
[ ] Write a "later" list for everything else
Learn the tools
Who
Topics
Malia
Javalin or Spring Boot basics; JSON parsing with Jackson; JUnit testing; Anthropic Java SDK
Ava
Python fundamentals and Streamlit; pandas for tables and charts; requests for calling the backend; SQLite basics
Both
Git and GitHub (branches, pull requests); how REST APIs and JSON work
Set up accounts and workspace (Both)
[ ] Create an Anthropic Console account and API key
[ ] Set a monthly spending limit
[ ] Create a GitHub repo with backend/ and frontend/ folders
[ ] Add a .gitignore that excludes the API key file
[ ] Write a README explaining how to run each half
Part 2: Demo build (what we actually build)
We build one working path end to end, not a full product: type in an event, get a priced materials list back, adjust it, see the budget. Enough to demo live and screen-record; everything else is designed on paper and explained in interviews.
Phase 1: Design (Both, one sitting)
[ ] Agree on the JSON contract between backend and frontend (one request shape, one response shape)
[ ] Sketch the one screen (Ava's drawing goes here)
[ ] Write the data model: Event, Material, BudgetLineItem
Phase 2: The slice we build
Malia — Java backend, one endpoint (POST /plan)
[ ] Takes event type, member count, and budget cap
[ ] Calls Claude (Haiku 5.5) with the system prompt and one example event; parses the JSON; retries once on bad JSON
[ ] Calculator: per-person vs. fixed items, pack rounding, 10% contingency, cost per member
[ ] 3 JUnit tests: zero members, pack rounding, a large event
Ava — Streamlit frontend, one page
[ ] Input form → calls the backend
[ ] Editable line-item table (st.data_editor) with Claude's assumptions shown above it
[ ] Totals by category, cost per member, and an over-budget warning
[ ] A saved sample response to fall back on if the API fails mid-demo
Phase 3: Prove it (Both)
[ ] Run 3 past Empress events through the tool and fill in the table below — this is the headline result
Event
Members
Actual cost ($)
Tool estimate ($)
Difference















Phase 4: Package it for interviews
[ ] README with setup steps and a screenshot
[ ] 2-minute screen recording of the demo (link it here)
[ ] Each of us rehearses the walkthrough once
Shortcuts that are fine: runs locally only, no login, no database, no deployment.
If time gets really tight: Ava's Streamlit app can call Claude directly in Python and skip the Java layer. It still demos, but Malia's half of the story gets thinner, so treat it as a last resort.
Designed, not built
These stay in the plan so we can describe them as next steps: saving events to SQLite, CSV/PDF export, cheaper-alternative suggestions when over budget, response caching, preferring real past prices over estimates, event templates, prep timeline, shopping list by store, and an estimated-vs-actual post-event report.
Part 3: Design checklist
This is the full design we can talk through. Only what's listed in Part 2's slice gets built; the rest is our "here's what we'd do next" answer.
Architecture
• Three separate layers: interface (Python/Streamlit), engine (Java), Claude as advisor
• The API key lives on the backend only
Placeholder sketch until Ava's drawing is ready: the interface never talks to Claude directly, so the key and the math stay in the engine.
Data model
• Per-person items (pizza, cups)
• Fixed items (speaker, room rental)
• Pack-based items with rounding (plates in packs of 50)
Prompt design (Malia)
• System prompt defining Claude's role and the JSON format
• Rules: US retail prices, skip items already owned
• 1–2 example events with ideal answers
• Validate responses and retry once on bad JSON; show a clear error if the retry fails
Price accuracy
• Every line item is editable
• Store actual costs after events
• Prefer real past prices over Claude's estimates when available
Human in the loop
• Display Claude's assumptions (e.g., "2 slices per person")
• Let users add, remove, and adjust items
Budget intelligence
• 10% contingency buffer
• Costs grouped by category (food, supplies, venue, prizes)
• Cost per member
• Warning when over the budget cap
• Ask Claude for cheaper alternatives when over budget
Testing
• JUnit tests for the calculator (zero members, pack rounding, large events)
• Sanity checks on Claude's output (no negative prices, sensible quantities)
• Compare estimates to real past Empress events
Cost and privacy
• Use Claude Haiku 5.5 for drafts; switch to Claude Sonnet 5.5 only if quality falls short (updated from Haiku 4.5 / Sonnet 5 — check current model names when you start)
• Cache responses for repeated events
• Send only member counts, never member names or personal information from Empress records
Stretch goals (later list)
• Prep timeline with reminders
• Templates for recurring Empress events
• Shopping list grouped by store
• Post-event report: estimated vs. actual spending
Interview and resume talking points
Each of us should be able to explain the whole system, then go deep on our own half.
Draft resume bullets (swap in real numbers):
• Co-built an AI event-planning tool for Empress that turns an event description and member count into a materials list and budget, tested against [N] past events
• Malia: Designed a Java backend that calls the Claude API, validates structured JSON output, and computes budgets with pack rounding and contingency, covered by JUnit tests
• Ava: Led user research with [N] club leaders and built a Streamlit interface with editable line items, cost-per-member views, and saved event history
Questions to rehearse:
• Why split Java and Python? (Division of skills; clean separation between interface and logic.)
• How do you handle bad AI output? (Validation, one retry, clear error, every item editable.)
• How did you know it was accurate? (Real Empress cost data as a test set.)
• How did you protect member privacy? (Only counts leave our system, never names.)
• What would you do next? (Point to the later list.)
Keep a running log in this folder: decisions made, things that broke, and what we changed. It's the raw material for every interview answer.