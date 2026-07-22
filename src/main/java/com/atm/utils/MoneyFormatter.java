package com.atm.utils;

/**
 * Formats amounts in Indian Rupees with lakh/crore digit grouping.
 */
public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    /**
     * Formats an amount as INR, e.g. {@code ₹1,25,000.00}.
     */
    public static String format(double amount) {
        boolean negative = amount < 0;
        double abs = Math.abs(amount);
        long rupees = (long) abs;
        int paise = (int) Math.round((abs - rupees) * 100);
        if (paise == 100) {
            rupees++;
            paise = 0;
        }

        String grouped = groupIndian(rupees);
        String result = "₹" + grouped + "." + String.format("%02d", paise);
        return negative ? "-" + result : result;
    }

    private static String groupIndian(long value) {
        String digits = Long.toString(value);
        int len = digits.length();
        if (len <= 3) {
            return digits;
        }

        StringBuilder out = new StringBuilder();
        out.append(digits.substring(len - 3));
        int i = len - 3;
        while (i > 0) {
            int start = Math.max(0, i - 2);
            out.insert(0, digits.substring(start, i) + ",");
            i = start;
        }
        return out.toString();
    }
}
