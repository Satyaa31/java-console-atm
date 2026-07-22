package com.atm.gui;

import com.atm.core.Bank;
import com.atm.models.Account;
import com.atm.models.FixedDeposit;
import com.atm.models.Transaction;
import com.atm.utils.InputValidator;
import com.atm.utils.MoneyFormatter;
import com.atm.utils.StatementExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer dashboard for SecureBank India.
 */
public class DashboardScreen {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final ATMGuiApp app;
    private final Account account;
    private final BorderPane root;
    private final Label balanceValue;
    private final StackPane contentArea;
    private final Label banner;
    private final List<Button> navButtons = new ArrayList<>();
    private Transaction lastCompleted;

    public DashboardScreen(ATMGuiApp app, Account account) {
        this.app = app;
        this.account = account;

        Label bankLabel = new Label("SECUREBANK INDIA");
        bankLabel.getStyleClass().add("brand-small");

        balanceValue = new Label(formatMoney(account.getBalance()));
        balanceValue.getStyleClass().add("balance-value");

        Label balanceCaption = new Label("Available Balance");
        balanceCaption.getStyleClass().add("subtitle");

        Label ownerLabel = new Label(account.getOwnerName() + "  ·  A/C " + account.getAccountId());
        ownerLabel.getStyleClass().add("subtitle");

        Label status = new Label(account.isFrozen() ? "FROZEN" : "ACTIVE");
        status.getStyleClass().add(account.isFrozen() ? "status-frozen" : "status-active");

        VBox balanceCard = new VBox(6, bankLabel, balanceCaption, balanceValue, ownerLabel, status);
        balanceCard.getStyleClass().add("balance-card");

        Button historyBtn = navButton("Passbook", this::showHistory);
        Button withdrawBtn = navButton("Withdraw", this::showWithdraw);
        Button depositBtn = navButton("Deposit", this::showDeposit);
        Button transferBtn = navButton("NEFT Transfer", this::showTransfer);
        Button billBtn = navButton("Bill Pay (BBPS)", this::showBillPay);
        Button fdBtn = navButton("Fixed Deposit", this::showFixedDeposits);
        Button pinBtn = navButton("Change PIN", this::showChangePin);
        Button logoutBtn = navButton("Logout", () -> app.switchTo(new LoginScreen(app).getRoot()));
        logoutBtn.getStyleClass().add("danger-nav");

        VBox sidebar = new VBox(8,
                balanceCard,
                historyBtn, withdrawBtn, depositBtn, transferBtn,
                billBtn, fdBtn, pinBtn, logoutBtn
        );
        sidebar.getStyleClass().add("sidebar");

        banner = new Label();
        banner.setVisible(false);
        banner.setManaged(false);
        banner.setWrapText(true);
        banner.setMaxWidth(Double.MAX_VALUE);

        contentArea = new StackPane();
        contentArea.getStyleClass().add("main-panel");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        VBox mainColumn = new VBox(12, banner, contentArea);
        mainColumn.getStyleClass().add("main-panel");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        root = new BorderPane();
        root.getStyleClass().add("root");
        root.setLeft(sidebar);
        root.setCenter(mainColumn);

        showHistory();
        setActiveNav(historyBtn);
    }

    public Parent getRoot() {
        return root;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {
            setActiveNav(button);
            action.run();
        });
        navButtons.add(button);
        return button;
    }

    private void setActiveNav(Button active) {
        for (Button button : navButtons) {
            button.getStyleClass().remove("nav-button-active");
        }
        if (!active.getStyleClass().contains("nav-button-active")) {
            active.getStyleClass().add("nav-button-active");
        }
    }

    private void refreshBalance() {
        balanceValue.setText(formatMoney(account.getBalance()));
    }

    private void showBanner(String message, boolean success) {
        banner.setText(message);
        banner.getStyleClass().setAll(success ? "success-banner" : "error-banner");
        banner.setVisible(true);
        banner.setManaged(true);
    }

    private void clearBanner() {
        banner.setVisible(false);
        banner.setManaged(false);
    }

    private void setContent(Parent node) {
        contentArea.getChildren().setAll(node);
    }

    private void showHistory() {
        clearBanner();
        Label title = new Label("Passbook");
        title.getStyleClass().add("section-title");

        TableView<Transaction> table = buildTransactionTable(account.getTransactionHistory());
        if (account.getTransactionHistory().isEmpty()) {
            table.setPlaceholder(new Label("No transactions yet — make a deposit to get started."));
        }

        Button exportBtn = new Button("Export Receipt");
        exportBtn.getStyleClass().add("primary-button");
        exportBtn.setOnAction(e -> exportReceipt(table.getSelectionModel().getSelectedItem()));

        HBox actions = new HBox(10, exportBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(14, title, table, actions);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(panel);
    }

    private void exportReceipt(Transaction selected) {
        Transaction target = selected != null ? selected : lastCompleted;
        if (target == null && !account.getTransactionHistory().isEmpty()) {
            target = account.getLastTransaction();
        }
        if (target == null) {
            showBanner("No transaction available to export.", false);
            return;
        }
        try {
            File file = StatementExporter.exportReceipt(account, target);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Receipt");
            alert.setHeaderText("Receipt saved");
            alert.setContentText(file.getAbsolutePath());
            alert.showAndWait();
            showBanner("Receipt exported: " + file.getName(), true);
        } catch (Exception ex) {
            showBanner("Failed to export receipt: " + ex.getMessage(), false);
        }
    }

    private TableView<Transaction> buildTransactionTable(List<Transaction> history) {
        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setItems(FXCollections.observableArrayList(history));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getType().getDisplayName()));

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c ->
                new SimpleStringProperty(formatMoney(c.getValue().getAmount())));

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTimestamp().format(DATE_FMT)));

        TableColumn<Transaction, String> balCol = new TableColumn<>("Balance");
        balCol.setCellValueFactory(c ->
                new SimpleStringProperty(formatMoney(c.getValue().getBalanceAfter())));

        TableColumn<Transaction, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDetails()));

        table.getColumns().addAll(List.of(typeCol, amountCol, dateCol, balCol, detailsCol));
        return table;
    }

    private void showWithdraw() {
        clearBanner();
        Label title = new Label("Withdraw Cash");
        title.getStyleClass().add("section-title");
        Label hint = new Label("ATM cash is dispensed in multiples of ₹100.");
        hint.getStyleClass().add("hint");

        TextField amountField = amountField("Amount (₹)");
        FlowPane quick = quickCashRow(amountField, 500, 1000, 2000, 5000, 10000);

        Button confirm = primaryButton("Confirm Withdrawal", () -> {
            if (account.isFrozen()) {
                showBanner("Account is frozen. Withdrawals are blocked.", false);
                return;
            }
            Double amount = parsePositiveAmount(amountField.getText());
            if (amount == null) {
                showBanner("Enter a valid amount.", false);
                return;
            }
            if (!InputValidator.isAtmCashAmount(amount)) {
                showBanner("Amount must be a whole multiple of ₹100.", false);
                return;
            }
            if (amount > account.getBalance()) {
                showBanner("Insufficient funds. Available: " + formatMoney(account.getBalance()), false);
                return;
            }
            if (account.withdraw(amount)) {
                lastCompleted = account.getLastTransaction();
                refreshBalance();
                showBanner("Please collect your cash. New balance: "
                        + formatMoney(account.getBalance()), true);
                amountField.clear();
            } else {
                showBanner("Withdrawal failed.", false);
            }
        });

        setContent(formPanel(title, hint, labeled("Amount", amountField), quick, confirm));
    }

    private void showDeposit() {
        clearBanner();
        Label title = new Label("Deposit Cash");
        title.getStyleClass().add("section-title");

        TextField amountField = amountField("Amount (₹)");
        Button confirm = primaryButton("Confirm Deposit", () -> {
            Double amount = parsePositiveAmount(amountField.getText());
            if (amount == null) {
                showBanner("Enter a valid positive amount.", false);
                return;
            }
            account.deposit(amount);
            lastCompleted = account.getLastTransaction();
            refreshBalance();
            showBanner("Deposited " + formatMoney(amount)
                    + ". New balance: " + formatMoney(account.getBalance()), true);
            amountField.clear();
        });

        setContent(formPanel(title, labeled("Amount", amountField), confirm));
    }

    private void showTransfer() {
        clearBanner();
        Label title = new Label("NEFT Fund Transfer");
        title.getStyleClass().add("section-title");

        TextField recipientField = new TextField();
        recipientField.setPromptText("4-digit Account ID");
        recipientField.getStyleClass().add("text-field");

        TextField amountField = amountField("Amount (₹)");
        Button confirm = primaryButton("Send Money", () -> {
            if (account.isFrozen()) {
                showBanner("Account is frozen. Transfers are blocked.", false);
                return;
            }
            String recipientId = safe(recipientField.getText());
            if (!InputValidator.isValidAccountId(recipientId)) {
                showBanner("Recipient Account ID must be 4 digits.", false);
                return;
            }
            Bank bank = Bank.getInstance();
            if (!bank.accountExists(recipientId)) {
                showBanner("Recipient account does not exist.", false);
                return;
            }
            if (recipientId.equals(account.getAccountId())) {
                showBanner("Cannot transfer to your own account.", false);
                return;
            }
            Double amount = parsePositiveAmount(amountField.getText());
            if (amount == null) {
                showBanner("Enter a valid positive amount.", false);
                return;
            }
            if (amount > account.getBalance()) {
                showBanner("Insufficient funds. Available: " + formatMoney(account.getBalance()), false);
                return;
            }
            if (bank.processTransfer(account.getAccountId(), recipientId, amount)) {
                lastCompleted = account.getLastTransaction();
                refreshBalance();
                showBanner("NEFT of " + formatMoney(amount) + " sent to A/C " + recipientId
                        + ". New balance: " + formatMoney(account.getBalance()), true);
                amountField.clear();
            } else {
                showBanner("Transfer failed.", false);
            }
        });

        setContent(formPanel(title,
                labeled("Recipient Account ID", recipientField),
                labeled("Amount", amountField),
                confirm));
    }

    private void showBillPay() {
        clearBanner();
        Label title = new Label("Bill Pay (BBPS)");
        title.getStyleClass().add("section-title");

        ComboBox<String> billType = new ComboBox<>(FXCollections.observableArrayList(
                "Electricity (BESCOM)", "Water Board", "Mobile Recharge", "LPG Gas", "Broadband"
        ));
        billType.getSelectionModel().selectFirst();
        billType.getStyleClass().add("combo-box");
        billType.setMaxWidth(Double.MAX_VALUE);

        TextField amountField = amountField("Amount (₹)");
        TextField providerRef = new TextField();
        providerRef.setPromptText("Consumer / reference no.");
        providerRef.getStyleClass().add("text-field");

        Button confirm = primaryButton("Pay Bill", () -> {
            if (account.isFrozen()) {
                showBanner("Account is frozen. Bill payments are blocked.", false);
                return;
            }
            Double amount = parsePositiveAmount(amountField.getText());
            if (amount == null) {
                showBanner("Enter a valid positive amount.", false);
                return;
            }
            String ref = safe(providerRef.getText());
            if (ref.isEmpty()) {
                showBanner("Provider reference is required.", false);
                return;
            }
            if (account.payBill(amount, billType.getValue(), ref)) {
                lastCompleted = account.getLastTransaction();
                refreshBalance();
                showBanner("Paid " + formatMoney(amount) + " for " + billType.getValue()
                        + ". New balance: " + formatMoney(account.getBalance()), true);
                amountField.clear();
                providerRef.clear();
            } else {
                showBanner("Bill payment failed. Check balance.", false);
            }
        });

        setContent(formPanel(title,
                labeled("Bill Type", billType),
                labeled("Amount", amountField),
                labeled("Provider Reference", providerRef),
                confirm));
    }

    private void showFixedDeposits() {
        clearBanner();
        Label title = new Label("Fixed Deposits");
        title.getStyleClass().add("section-title");
        Label rateNote = new Label("Interest: 6.5% p.a. simple · tenure in months");
        rateNote.getStyleClass().add("hint");

        TableView<FixedDeposit> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setItems(FXCollections.observableArrayList(account.getFixedDeposits()));
        table.setPlaceholder(new Label("No FDs yet."));

        TableColumn<FixedDeposit, String> principalCol = new TableColumn<>("Principal");
        principalCol.setCellValueFactory(c ->
                new SimpleStringProperty(formatMoney(c.getValue().getPrincipal())));

        TableColumn<FixedDeposit, String> rateCol = new TableColumn<>("Rate %");
        rateCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("%.1f", c.getValue().getAnnualRatePercent())));

        TableColumn<FixedDeposit, String> tenureCol = new TableColumn<>("Tenure");
        tenureCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTenureMonths() + " months"));

        TableColumn<FixedDeposit, String> maturityDateCol = new TableColumn<>("Matures On");
        maturityDateCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().maturityDate().format(DATE_FMT)));

        TableColumn<FixedDeposit, String> maturityAmtCol = new TableColumn<>("Maturity Amount");
        maturityAmtCol.setCellValueFactory(c ->
                new SimpleStringProperty(formatMoney(c.getValue().maturityAmount())));

        table.getColumns().addAll(List.of(
                principalCol, rateCol, tenureCol, maturityDateCol, maturityAmtCol));

        TextField amountField = amountField("Principal (₹)");
        TextField monthsField = new TextField();
        monthsField.setPromptText("e.g. 12");
        monthsField.getStyleClass().add("text-field");

        Button openBtn = primaryButton("Open FD", () -> {
            if (account.isFrozen()) {
                showBanner("Account is frozen. Opening FDs is blocked.", false);
                return;
            }
            Double amount = parsePositiveAmount(amountField.getText());
            if (amount == null) {
                showBanner("Enter a valid principal amount.", false);
                return;
            }
            int months;
            try {
                months = Integer.parseInt(safe(monthsField.getText()));
                if (months < 1 || months > 120) {
                    showBanner("Tenure must be between 1 and 120 months.", false);
                    return;
                }
            } catch (Exception ex) {
                showBanner("Enter tenure in months (numbers only).", false);
                return;
            }
            FixedDeposit fd = account.openFixedDeposit(amount, months);
            if (fd == null) {
                showBanner("Unable to open FD. Check available balance.", false);
                return;
            }
            lastCompleted = account.getLastTransaction();
            refreshBalance();
            table.setItems(FXCollections.observableArrayList(account.getFixedDeposits()));
            showBanner("FD opened. Maturity value: " + formatMoney(fd.maturityAmount()), true);
            amountField.clear();
            monthsField.clear();
        });

        VBox openForm = new VBox(10,
                new Label("Open New FD"),
                labeled("Principal", amountField),
                labeled("Tenure (months)", monthsField),
                openBtn
        );
        openForm.getStyleClass().add("card");

        VBox panel = new VBox(16, title, rateNote, table, openForm);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(panel);
    }

    private void showChangePin() {
        clearBanner();
        Label title = new Label("Change PIN");
        title.getStyleClass().add("section-title");

        PasswordField currentPin = passwordField("Current PIN");
        PasswordField newPin = passwordField("New PIN (4–6 digits)");
        PasswordField confirmPin = passwordField("Confirm New PIN");

        Button save = primaryButton("Update PIN", () -> {
            String oldVal = safe(currentPin.getText());
            String newVal = safe(newPin.getText());
            String confirmVal = safe(confirmPin.getText());

            if (!InputValidator.isValidPin(newVal)) {
                showBanner("New PIN must be 4–6 digits.", false);
                return;
            }
            if (newVal.equals(oldVal)) {
                showBanner("New PIN must be different from the current PIN.", false);
                return;
            }
            if (!newVal.equals(confirmVal)) {
                showBanner("New PIN confirmation does not match.", false);
                return;
            }
            if (!account.validatePin(oldVal)) {
                showBanner("Current PIN is incorrect.", false);
                return;
            }
            if (account.changePin(oldVal, newVal)) {
                showBanner("PIN updated successfully.", true);
                currentPin.clear();
                newPin.clear();
                confirmPin.clear();
            } else {
                showBanner("Unable to change PIN.", false);
            }
        });

        setContent(formPanel(title,
                labeled("Current PIN", currentPin),
                labeled("New PIN", newPin),
                labeled("Confirm New PIN", confirmPin),
                save));
    }

    private FlowPane quickCashRow(TextField target, int... amounts) {
        FlowPane row = new FlowPane(8, 8);
        for (int amount : amounts) {
            Button chip = new Button(MoneyFormatter.format(amount));
            chip.getStyleClass().add("chip-button");
            chip.setOnAction(e -> target.setText(String.valueOf(amount)));
            row.getChildren().add(chip);
        }
        return row;
    }

    private VBox formPanel(javafx.scene.Node... nodes) {
        VBox form = new VBox(12);
        form.getChildren().addAll(nodes);
        form.setMaxWidth(460);
        form.setAlignment(Pos.TOP_LEFT);
        return form;
    }

    private VBox labeled(String text, javafx.scene.Node control) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return new VBox(6, label, control);
    }

    private TextField amountField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("text-field");
        return field;
    }

    private PasswordField passwordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.getStyleClass().add("password-field");
        return field;
    }

    private Button primaryButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        button.setDefaultButton(true);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Double parsePositiveAmount(String raw) {
        Double value = InputValidator.tryParseAmount(raw);
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    private String formatMoney(double amount) {
        return MoneyFormatter.format(amount);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
