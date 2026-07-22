package com.atm.utils;

/**
 * Validates and parses account IDs, PINs, and money amounts.
 */
public final class InputValidator {

    private static final double MAX_AMOUNT = 10_00_000.00;

    private InputValidator() {
    }

    /**
     * Accepts plain numbers, optional ₹, and Indian/Western commas.
     */
    public static boolean isValidAmount(String input) {
        Double value = tryParseAmount(input);
        return value != null && value > 0 && value <= MAX_AMOUNT;
    }

    public static double parseAmount(String input) {
        Double value = tryParseAmount(input);
        return value == null ? -1 : value;
    }

    public static Double tryParseAmount(String input) {
        if (input == null) {
            return null;
        }
        String cleaned = input.trim()
                .replace("₹", "")
                .replace(",", "")
                .replace(" ", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(cleaned);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            // Keep money to 2 decimal places (paise).
            value = Math.round(value * 100.0) / 100.0;
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Indian ATM cash usually comes in ₹100 notes. */
    public static boolean isAtmCashAmount(double amount) {
        return amount > 0
                && amount <= MAX_AMOUNT
                && Math.abs(amount - Math.rint(amount)) < 0.001
                && ((long) Math.rint(amount)) % 100 == 0;
    }

    public static boolean isValidAccountId(String input) {
        return input != null && input.matches("\\d{4}");
    }

    public static boolean isValidPin(String input) {
        return input != null && input.matches("\\d{4,6}");
    }

    public static boolean isValidOwnerName(String input) {
        return input != null && input.trim().matches("[A-Za-z .']{2,40}");
    }
}
