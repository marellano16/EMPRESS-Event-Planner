package com.empress.planner;

/**
 * One row of the materials list.
 *
 * kind decides how the cost is calculated:
 *   PER_PERSON - unitPrice * perPerson * members            (pizza, cups)
 *   FIXED      - unitPrice * quantity                       (speaker, room rental)
 *   PACK       - packs needed (rounded up) * unitPrice      (plates in packs of 50)
 *                unitPrice is the price of one whole pack.
 */
public record LineItem(
        String name,
        String category,
        Kind kind,
        double unitPrice,
        double perPerson,
        int quantity,
        int packSize) {

    public enum Kind { PER_PERSON, FIXED, PACK }
}
