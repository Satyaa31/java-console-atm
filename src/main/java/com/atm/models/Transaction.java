package com.atm.models;

import com.atm.utils.MoneyFormatter;

import java.time.LocalDateTime;

/**
 * Immutable record of a single banking transaction.
 */
public final class Transaction {

    /**
     * Supported transaction categories with human-readable labels.
     */
    public enum Type {
        WITHDRAW("Withdraw"),
        DEPOSIT("Deposit"),
        TRANSFER("NEFT Transfer"),
        BILL_PAY("Bill Payment"),
        FIXED_DEPOSIT("Fixed Deposit");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Type type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;
    private final String details;

    /**
     * Creates an immutable transaction stamped with the current time.
     *
     * @param type         transaction category
     * @param amount       monetary amount
     * @param balanceAfter account balance after the operation
     * @param details      free-text description
     */
    public Transaction(Type type, double amount, double balanceAfter, String details) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    /**
     * Returns the transaction type.
     *
     * @return type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the transaction amount.
     *
     * @return amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Returns the balance after this transaction.
     *
     * @return balance after
     */
    public double getBalanceAfter() {
        return balanceAfter;
    }

    /**
     * Returns when the transaction occurred.
     *
     * @return timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the transaction details text.
     *
     * @return details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Formats this transaction as a fixed-width history row.
     *
     * @return formatted row
     */
    @Override
    public String toString() {
        return String.format(
                "| %-14s | %12s | %s | %14s | %-22s |",
                type.displayName,
                MoneyFormatter.format(amount),
                timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")),
                MoneyFormatter.format(balanceAfter),
                details.length() > 22 ? details.substring(0, 19) + "..." : details
        );
    }
}
