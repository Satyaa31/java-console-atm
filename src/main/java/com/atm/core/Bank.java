package com.atm.core;

import com.atm.models.Account;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Thread-safe singleton registry of bank accounts.
 */
public final class Bank {

    private static Bank instance;
    private final HashMap<String, Account> accounts;

    private Bank() {
        accounts = new HashMap<>();
        initializeAccounts();
    }

    /**
     * Returns the singleton bank instance, creating it on first use.
     *
     * @return bank instance
     */
    public static synchronized Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }

    private void initializeAccounts() {
        accounts.put("1001", new Account("1001", "1234", "Satya Kumar", 52_500.00));
        accounts.put("1002", new Account("1002", "5678", "Priya Sharma", 38_750.00));
        accounts.put("1003", new Account("1003", "9012", "Arjun Reddy", 125_000.00));
        accounts.put("1004", new Account("1004", "2468", "Hitesh Patel", 41_200.00));
        accounts.put("1005", new Account("1005", "1357", "Aman Verma", 28_900.00));
        accounts.put("1006", new Account("1006", "8642", "Omkar Joshi", 67_350.00));
        accounts.put("1007", new Account("1007", "9753", "Ritikesh Singh", 93_000.00));
        accounts.put("1008", new Account("1008", "1122", "Ram Yadav", 15_500.00));
    }

    /**
     * Looks up an account by id.
     *
     * @param id account identifier
     * @return matching account, or null if not found
     */
    public Account findAccount(String id) {
        return accounts.get(id);
    }

    /**
     * Checks whether an account id exists in the registry.
     *
     * @param id account identifier
     * @return true if the account exists
     */
    public boolean accountExists(String id) {
        return accounts.containsKey(id);
    }

    /**
     * Creates a new account when the id is unused.
     *
     * @param id             account identifier
     * @param pin            PIN credential
     * @param owner          account holder name
     * @param openingBalance opening balance
     * @return true if created; false if id already exists
     */
    public synchronized boolean createAccount(String id, String pin, String owner, double openingBalance) {
        if (accounts.containsKey(id)) {
            return false;
        }
        accounts.put(id, new Account(id, pin, owner, openingBalance));
        return true;
    }

    /**
     * Returns an unmodifiable view of all registered accounts.
     *
     * @return all accounts
     */
    public Collection<Account> getAllAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    /**
     * Atomically transfers funds from one account to another.
     * Each side logs its own transfer transaction.
     *
     * @param fromId source account id
     * @param toId   destination account id
     * @param amount transfer amount
     * @return true if the transfer completed
     */
    public synchronized boolean processTransfer(String fromId, String toId, double amount) {
        Account from = findAccount(fromId);
        Account to = findAccount(toId);

        if (from == null || to == null) {
            return false;
        }
        if (from.isFrozen()) {
            return false;
        }
        if (amount <= 0 || amount > from.getBalance()) {
            return false;
        }

        boolean debited = from.transfer(amount, toId);
        if (!debited) {
            return false;
        }
        to.receiveTransfer(amount, fromId);
        return true;
    }
}
