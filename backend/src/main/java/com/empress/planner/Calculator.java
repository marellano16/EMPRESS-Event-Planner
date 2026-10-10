package com.empress.planner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The math engine. Claude suggests items and quantities; this class does the
 * arithmetic so the numbers are always consistent.
 */
public final class Calculator {

    public static final double CONTINGENCY_RATE = 0.10;

    private Calculator() { }

    /** Cost of one line item for the given number of members. */
    public static double lineCost(LineItem item, int members) {
        return switch (item.kind()) {
            case PER_PERSON -> item.unitPrice() * item.perPerson() * members;
            case FIXED -> item.unitPrice() * item.quantity();
            case PACK -> {
                if (item.packSize() <= 0) {
                    throw new IllegalArgumentException("packSize must be positive for " + item.name());
                }
                int needed = (int) Math.ceil(item.perPerson() * members);
                int packs = (needed + item.packSize() - 1) / item.packSize();
                yield packs * item.unitPrice();
            }
        };
    }

    /**
     * @param budgetCap maximum the club wants to spend; 0 or less means "no cap"
     */
    public static Budget compute(List<LineItem> items, int members, double budgetCap) {
        if (members < 0) {
            throw new IllegalArgumentException("members cannot be negative");
        }

        Map<String, Double> byCategory = new LinkedHashMap<>();
        double subtotal = 0;
        for (LineItem item : items) {
            double cost = lineCost(item, members);
            subtotal += cost;
            byCategory.merge(item.category(), round2(cost), Double::sum);
        }

        double contingency = subtotal * CONTINGENCY_RATE;
        double total = subtotal + contingency;
        double costPerMember = members == 0 ? 0 : total / members;
        boolean overBudget = budgetCap > 0 && total > budgetCap;

        byCategory.replaceAll((k, v) -> round2(v));
        return new Budget(round2(subtotal), round2(contingency), round2(total),
                round2(costPerMember), overBudget, byCategory);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
