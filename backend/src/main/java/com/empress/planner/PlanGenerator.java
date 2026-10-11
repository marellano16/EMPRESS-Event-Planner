package com.empress.planner;

/**
 * Anything that can draft a materials list for an event. Swap providers by
 * writing another implementation; the Calculator, tests and frontend do not change.
 */
public interface PlanGenerator {

    PlanDraft generate(PlanRequest request);
}
