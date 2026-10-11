package com.empress.planner;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;

import java.util.Map;

public class App {

    private static final int MAX_EVENT_TYPE_LENGTH = 200;
    private static final int MAX_MEMBERS = 10_000;

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        createApp(chooseGenerator()).start(port);
    }

    /**
     * Uses Gemini when GEMINI_API_KEY is set in the environment, otherwise the
     * stub generator so everything still runs without a key.
     */
    static PlanGenerator chooseGenerator() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            System.out.println("No GEMINI_API_KEY set: using the stub plan generator.");
            return new StubGenerator();
        }
        // Model names change over time; if Gemini answers HTTP 404, set GEMINI_MODEL
        // to a current name from Google AI Studio.
        String model = System.getenv().getOrDefault("GEMINI_MODEL", "gemini-3.8-flash");
        System.out.println("Using Gemini model: " + model);
        return new GeminiGenerator(key, model);
    }

    /** Split out from main so tests can start the app with a fake generator. */
    public static Javalin createApp(PlanGenerator generator) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("ok"));

        app.post("/plan", ctx -> {
            PlanRequest req = ctx.bodyAsClass(PlanRequest.class);
            validate(req);
            PlanDraft draft = generator.generate(req);
            Budget budget = Calculator.compute(draft.items(), req.memberCount(), req.budgetCap());
            ctx.json(new PlanResponse(req.eventType(), req.memberCount(),
                    draft.assumptions(), draft.items(), budget));
        });

        // A bad model answer or a failed call is a gateway problem, not the user's fault.
        app.exception(GenerationException.class, (e, ctx) ->
                ctx.status(502).json(Map.of("error", e.getMessage())));

        return app;
    }

    private static void validate(PlanRequest req) {
        if (req.eventType() == null || req.eventType().isBlank()) {
            throw new BadRequestResponse("eventType is required");
        }
        if (req.eventType().length() > MAX_EVENT_TYPE_LENGTH) {
            throw new BadRequestResponse("eventType is too long");
        }
        if (req.memberCount() < 0 || req.memberCount() > MAX_MEMBERS) {
            throw new BadRequestResponse("memberCount must be between 0 and " + MAX_MEMBERS);
        }
        if (req.budgetCap() < 0) {
            throw new BadRequestResponse("budgetCap cannot be negative");
        }
    }
}
