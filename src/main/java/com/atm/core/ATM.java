package com.atm.core;

import com.atm.models.Account;
import com.atm.models.Transaction;
import com.atm.utils.InputValidator;
import com.atm.utils.MoneyFormatter;
import com.atm.utils.SecurityManager;

import java.util.List;
import java.util.Scanner;

/**
 * Console ATM for SecureBank India.
 */
public class ATM {

    private final Scanner scanner;
    private final SecurityManager securityManager;
    private Account currentAccount;
    private boolean sessionActive;

    public ATM(Scanner scanner) {
        this.scanner = scanner;
        this.securityManager = new SecurityManager();
        this.currentAccount = null;
        this.sessionActive = false;
    }

    public void start() {
        printBanner();
        if (login()) {
            mainMenu();
        } else {
            System.out.println("Session ended.");
        }
    }

    public boolean login() {
        while (!securityManager.isSessionLocked()) {
            System.out.print("Account ID : ");
            String id = scanner.nextLine().trim();
            System.out.print("PIN        : ");
            String pin = scanner.nextLine().trim();

            Account account = securityManager.authenticate(id, pin);
            if (account != null) {
                currentAccount = account;
                sessionActive = true;
                System.out.println();
                System.out.println("Namaste, " + account.getOwnerName() + "!");
                if (account.isFrozen()) {
                    System.out.println("Note: this account is FROZEN. Debits are blocked.");
                }
                System.out.println();
                return true;
            }

            System.out.println(securityManager.getLastMessage());
            System.out.println();
            if (securityManager.isSessionLocked()) {
                return false;
            }
        }
        return false;
    }

    public void mainMenu() {
        while (sessionActive) {
            printMenu();
            System.out.print("Select option (1-5): ");
            String choice = scanner.nextLine().trim();
            System.out.println();

            switch (choice) {
                case "1" -> viewTransactionHistory();
                case "2" -> withdraw();
                case "3" -> deposit();
                case "4" -> transfer();
                case "5" -> logout();
                default -> System.out.println("Invalid option. Choose 1–5.");
            }
            System.out.println();
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("       SecureBank India — ATM");
        System.out.println("       Amounts in Indian Rupees (₹)");
        System.out.println("========================================");
        System.out.println("Demo A/C: 1001–1008  |  Admin GUI: admin");
        System.out.println();
    }

    private void printMenu() {
        String status = currentAccount.isFrozen() ? "FROZEN" : "ACTIVE";
        System.out.println("----------------------------------------");
        System.out.println("  " + currentAccount.getOwnerName()
                + "  |  A/C " + currentAccount.getAccountId()
                + "  |  " + status);
        System.out.println("  Balance: " + MoneyFormatter.format(currentAccount.getBalance()));
        System.out.println("----------------------------------------");
        System.out.println("  1. Passbook / History");
        System.out.println("  2. Withdraw Cash");
        System.out.println("  3. Deposit Cash");
        System.out.println("  4. NEFT Transfer");
        System.out.println("  5. Logout");
        System.out.println("----------------------------------------");
    }

    public void viewTransactionHistory() {
        List<Transaction> history = currentAccount.getTransactionHistory();
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }

        System.out.println("Passbook");
        System.out.println("| Type           | Amount       | Timestamp         | Balance After  | Details                |");
        System.out.println("|----------------|--------------|-------------------|----------------|------------------------|");
        for (Transaction transaction : history) {
            System.out.println(transaction);
        }
    }

    public void withdraw() {
        if (currentAccount.isFrozen()) {
            System.out.println("Account is frozen. Withdrawals are blocked.");
            return;
        }

        System.out.println("Available: " + MoneyFormatter.format(currentAccount.getBalance()));
        System.out.print("Withdraw amount (multiples of ₹100): ");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidAmount(input)) {
            System.out.println("Enter a valid amount up to ₹10,00,000.");
            return;
        }

        double amount = InputValidator.parseAmount(input);
        if (!InputValidator.isAtmCashAmount(amount)) {
            System.out.println("ATM cash must be a whole multiple of ₹100.");
            return;
        }
        if (amount > currentAccount.getBalance()) {
            System.out.println("Insufficient funds. Available: "
                    + MoneyFormatter.format(currentAccount.getBalance()));
            return;
        }

        if (currentAccount.withdraw(amount)) {
            System.out.println("Please collect your cash.");
            System.out.println("New balance: " + MoneyFormatter.format(currentAccount.getBalance()));
        } else {
            System.out.println("Withdrawal failed.");
        }
    }

    public void deposit() {
        System.out.print("Deposit amount (₹): ");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidAmount(input)) {
            System.out.println("Enter a valid positive amount.");
            return;
        }

        double amount = InputValidator.parseAmount(input);
        currentAccount.deposit(amount);
        System.out.println("Deposit successful.");
        System.out.println("New balance: " + MoneyFormatter.format(currentAccount.getBalance()));
    }

    public void transfer() {
        if (currentAccount.isFrozen()) {
            System.out.println("Account is frozen. Transfers are blocked.");
            return;
        }

        System.out.print("Recipient Account ID: ");
        String recipientId = scanner.nextLine().trim();

        if (!InputValidator.isValidAccountId(recipientId)) {
            System.out.println("Account ID must be 4 digits.");
            return;
        }

        Bank bank = Bank.getInstance();
        if (!bank.accountExists(recipientId)) {
            System.out.println("Recipient account not found.");
            return;
        }
        if (recipientId.equals(currentAccount.getAccountId())) {
            System.out.println("Cannot transfer to your own account.");
            return;
        }

        System.out.print("Transfer amount (₹): ");
        String input = scanner.nextLine().trim();
        if (!InputValidator.isValidAmount(input)) {
            System.out.println("Enter a valid positive amount.");
            return;
        }

        double amount = InputValidator.parseAmount(input);
        if (amount > currentAccount.getBalance()) {
            System.out.println("Insufficient funds. Available: "
                    + MoneyFormatter.format(currentAccount.getBalance()));
            return;
        }

        if (bank.processTransfer(currentAccount.getAccountId(), recipientId, amount)) {
            System.out.println("NEFT transfer successful.");
            System.out.println("Sent " + MoneyFormatter.format(amount) + " to A/C " + recipientId);
            System.out.println("New balance: " + MoneyFormatter.format(currentAccount.getBalance()));
        } else {
            System.out.println("Transfer failed.");
        }
    }

    public void logout() {
        System.out.println("Dhanyavaad, " + currentAccount.getOwnerName() + "!");
        System.out.println("Final balance: " + MoneyFormatter.format(currentAccount.getBalance()));
        System.out.println("Please remove your card. Session closed.");
        sessionActive = false;
        currentAccount = null;
    }
}
