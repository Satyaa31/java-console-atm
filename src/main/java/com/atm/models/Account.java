package com.atm.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bank account holding identity, credentials, balance, and history.
 */
public class Account {

    private final String accountId;
    private String pin;
    private final String ownerName;
    private double balance;
    private boolean frozen;
    private final ArrayList<Transaction> history;
    private final ArrayList<FixedDeposit> fixedDeposits;

    /**
     * Creates a new account with the given identity and opening balance.
     *
     * @param accountId account identifier
     * @param pin       PIN credential
     * @param ownerName account holder name
     * @param balance   opening balance
     */
    public Account(String accountId, String pin, String ownerName, double balance) {
        this.accountId = accountId;
        this.pin = pin;
        this.ownerName = ownerName;
        this.balance = balance;
        this.frozen = false;
        this.history = new ArrayList<>();
        this.fixedDeposits = new ArrayList<>();
    }

    /**
     * Returns the account identifier.
     *
     * @return account id
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Returns the account holder name.
     *
     * @return owner name
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Returns the current balance.
     *
     * @return balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Indicates whether the account is frozen.
     *
     * @return true if frozen
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Sets the frozen status of this account.
     *
     * @param frozen true to freeze; false to unfreeze
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * Validates the supplied PIN against this account.
     *
     * @param pin candidate PIN
     * @return true if the PIN matches
     */
    public boolean validatePin(String pin) {
        return this.pin.equals(pin);
    }

    /**
     * Changes the PIN after validating the current one.
     *
     * @param oldPin current PIN
     * @param newPin replacement PIN
     * @return true if the PIN was updated
     */
    public boolean changePin(String oldPin, String newPin) {
        if (!validatePin(oldPin)) {
            return false;
        }
        this.pin = newPin;
        return true;
    }

    /**
     * Deposits funds and records a deposit transaction.
     *
     * @param amount amount to deposit
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            return;
        }
        balance += amount;
        history.add(new Transaction(
                Transaction.Type.DEPOSIT,
                amount,
                balance,
                "Cash deposit"
        ));
    }

    /**
     * Withdraws funds if the account is active and has sufficient balance.
     *
     * @param amount amount to withdraw
     * @return true if withdrawal succeeded; false if frozen or insufficient funds
     */
    public boolean withdraw(double amount) {
        if (frozen || amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        history.add(new Transaction(
                Transaction.Type.WITHDRAW,
                amount,
                balance,
                "Cash withdrawal"
        ));
        return true;
    }

    /**
     * Debits this account for an outgoing transfer if active and funded.
     *
     * @param amount      transfer amount
     * @param recipientId destination account id
     * @return true if the transfer was logged
     */
    public boolean transfer(double amount, String recipientId) {
        if (frozen || amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        history.add(new Transaction(
                Transaction.Type.TRANSFER,
                amount,
                balance,
                "To A/C " + recipientId
        ));
        return true;
    }

    /**
     * Records an incoming transfer credit on this account.
     *
     * @param amount   transfer amount
     * @param senderId source account id
     */
    public void receiveTransfer(double amount, String senderId) {
        if (amount <= 0) {
            return;
        }
        balance += amount;
        history.add(new Transaction(
                Transaction.Type.TRANSFER,
                amount,
                balance,
                "From A/C " + senderId
        ));
    }

    /**
     * Pays a bill by deducting the amount and logging a BILL_PAY transaction.
     *
     * @param amount     bill amount
     * @param billType   bill category label
     * @param providerRef provider reference
     * @return true if payment succeeded; false if frozen or insufficient funds
     */
    public boolean payBill(double amount, String billType, String providerRef) {
        if (frozen || amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        history.add(new Transaction(
                Transaction.Type.BILL_PAY,
                amount,
                balance,
                billType + " / " + providerRef
        ));
        return true;
    }

    /**
     * Opens a fixed deposit by locking principal from the available balance.
     *
     * @param principal amount to lock
     * @param months    tenure in months
     * @return the opened FD, or null if frozen or insufficient funds
     */
    public FixedDeposit openFixedDeposit(double principal, int months) {
        if (frozen || principal <= 0 || months <= 0 || principal > balance) {
            return null;
        }
        balance -= principal;
        FixedDeposit fd = new FixedDeposit(principal, months);
        fixedDeposits.add(fd);
        history.add(new Transaction(
                Transaction.Type.FIXED_DEPOSIT,
                principal,
                balance,
                "FD opened · " + months + " months @ " + fd.getAnnualRatePercent() + "%"
        ));
        return fd;
    }

    /**
     * Returns an unmodifiable view of the transaction history.
     *
     * @return unmodifiable history list
     */
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Returns an unmodifiable view of open fixed deposits.
     *
     * @return unmodifiable FD list
     */
    public List<FixedDeposit> getFixedDeposits() {
        return Collections.unmodifiableList(fixedDeposits);
    }

    /**
     * Returns the most recent transaction, if any.
     *
     * @return last transaction or null
     */
    public Transaction getLastTransaction() {
        if (history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }
}
