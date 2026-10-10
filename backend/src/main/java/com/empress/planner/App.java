package com.empress.planner;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;

import java.util.List;

public class App {

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        createApp().start(port);
    }

    /** Split out from main so tests can start the app later if we want them to. */
    public static Javalin createApp() {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("ok"));

        app.post("/plan", ctx -> {
            PlanRequest req = ctx.bodyAsClass(PlanRequest.class);
            if (req.memberCount() < 0) {
                throw new BadRequestResponse("memberCount cannot be negative");
            }
            ctx.json(buildStubPlan(req));
        });

        return app;
    }

    /**
     * STUB: returns a hard-coded sample plan so the frontend has something to
     * build against. Replace this with the Claude call (system prompt, one
     * example event, parse JSON, retry once on bad JSON). The Calculator
     * stays exactly as it is.
     */
    static PlanResponse buildStubPlan(PlanRequest req) {
        List<LineItem> items = List.of(
                new LineItem("Pizza slices", "food", LineItem.Kind.PER_PERSON, 1.50, 2, 0, 0),
                new LineItem("Paper plates (pack of 50)", "supplies", LineItem.Kind.PACK, 8.00, 1, 0, 50),
                new LineItem("Speaker rental", "venue", LineItem.Kind.FIXED, 40.00, 0, 1, 0));
        List<String> assumptions = List.of(
                "STUB DATA: not from Claude yet",
                "2 slices of pizza per person",
                "1 plate per person");
        Budget budget = Calculator.compute(items, req.memberCount(), req.budgetCap());
        return new PlanResponse(req.eventType(), req.memberCount(), assumptions, items, budget);
    }
}
