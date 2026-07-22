package com.atm.utils;

import com.atm.core.Bank;
import com.atm.models.Account;

/**
 * Login attempts and lockout after too many failures.
 */
public class SecurityManager {

    private static final int MAX_ATTEMPTS = 3;

    private int attemptCount;
    private boolean isLocked;
    private String lastMessage;

    public SecurityManager() {
        this.attemptCount = 0;
        this.isLocked = false;
        this.lastMessage = "";
    }

    public Account authenticate(String id, String pin) {
        if (isLocked) {
            lastMessage = "Security lockout active. Please restart the app or contact support.";
            return null;
        }

        if (!InputValidator.isValidAccountId(id)) {
            attemptCount++;
            lastMessage = "Invalid Account ID (" + attemptCount + "/" + MAX_ATTEMPTS + ")";
            checkLockout();
            return null;
        }

        if (!InputValidator.isValidPin(pin)) {
            attemptCount++;
            lastMessage = "Invalid PIN format (" + attemptCount + "/" + MAX_ATTEMPTS + ")";
            checkLockout();
            return null;
        }

        Account account = Bank.getInstance().findAccount(id);
        if (account == null || !account.validatePin(pin)) {
            attemptCount++;
            lastMessage = "Invalid Account ID or PIN (" + attemptCount + "/" + MAX_ATTEMPTS + ")";
            checkLockout();
            return null;
        }

        attemptCount = 0;
        lastMessage = "Login successful";
        return account;
    }

    public boolean isSessionLocked() {
        return isLocked;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    private void checkLockout() {
        if (attemptCount >= MAX_ATTEMPTS) {
            isLocked = true;
            lastMessage = "Too many failed attempts. Session locked for security.";
        }
    }
}
