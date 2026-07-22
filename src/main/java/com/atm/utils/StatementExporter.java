package com.atm.utils;

import com.atm.models.Account;
import com.atm.models.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes transaction receipts under statements/.
 */
public final class StatementExporter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    private StatementExporter() {
    }

    public static File exportReceipt(Account account, Transaction transaction) throws IOException {
        Path dir = Path.of("statements");
        Files.createDirectories(dir);

        String fileName = "receipt_" + account.getAccountId() + "_"
                + System.currentTimeMillis() + ".txt";
        File file = dir.resolve(fileName).toFile();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("========================================\n");
            writer.write("          SecureBank India\n");
            writer.write("             ATM Receipt\n");
            writer.write("========================================\n");
            writer.write("Account ID : " + account.getAccountId() + "\n");
            writer.write("Customer   : " + account.getOwnerName() + "\n");
            writer.write("----------------------------------------\n");
            writer.write("Type       : " + transaction.getType().getDisplayName() + "\n");
            writer.write("Amount     : " + MoneyFormatter.format(transaction.getAmount()) + "\n");
            writer.write("When       : " + transaction.getTimestamp().format(FORMATTER) + "\n");
            writer.write("Balance    : " + MoneyFormatter.format(transaction.getBalanceAfter()) + "\n");
            writer.write("Details    : " + transaction.getDetails() + "\n");
            writer.write("----------------------------------------\n");
            writer.write("Printed    : " + LocalDateTime.now().format(FORMATTER) + "\n");
            writer.write("Thank you for banking with us.\n");
            writer.write("========================================\n");
        }

        return file;
    }
}
