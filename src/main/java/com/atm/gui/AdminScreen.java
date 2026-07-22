package com.atm.gui;

import com.atm.core.Bank;
import com.atm.models.Account;
import com.atm.models.Transaction;
import com.atm.utils.InputValidator;
import com.atm.utils.MoneyFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Admin console — list, freeze, and create accounts.
 */
public class AdminScreen {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final ATMGuiApp app;
    private final StackPane root;

    public AdminScreen(ATMGuiApp app) {
        this.app = app;
        this.root = new StackPane();
        root.getStyleClass().addAll("root", "login-root");
        showLogin();
    }

    public Parent getRoot() {
        return root;
    }

    private void showLogin() {
        Label brand = new Label("SECUREBANK");
        brand.getStyleClass().add("brand");
        Label title = new Label("Admin Console");
        title.getStyleClass().add("title");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.getStyleClass().add("text-field");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.getStyleClass().add("password-field");

        Label error = new Label();
        error.getStyleClass().add("error-label");
        error.setWrapText(true);

        Button login = new Button("Sign In");
        login.getStyleClass().add("primary-button");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setDefaultButton(true);
        Runnable doLogin = () -> {
            String user = safe(userField.getText());
            String pass = safe(passField.getText());
            if (ADMIN_USER.equals(user) && ADMIN_PASS.equals(pass)) {
                showDashboard();
            } else {
                error.setText("Invalid admin credentials.");
                passField.clear();
            }
        };
        login.setOnAction(e -> doLogin.run());
        passField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                doLogin.run();
            }
        });

        Button back = new Button("Back to Customer Login");
        back.getStyleClass().add("link-button");
        back.setOnAction(e -> app.switchTo(new LoginScreen(app).getRoot()));

        Label hint = new Label("Demo admin · admin / admin123");
        hint.getStyleClass().add("hint");

        VBox card = new VBox(12, brand, title, userField, passField, login, back, hint, error);
        card.getStyleClass().add("card");
        card.setMaxWidth(380);
        card.setAlignment(Pos.CENTER);

        StackPane wrap = new StackPane(card);
        wrap.setPadding(new Insets(40));
        root.getChildren().setAll(wrap);
    }

    private void showDashboard() {
        Label heading = new Label("Admin Console");
        heading.getStyleClass().add("section-title");

        Label sub = new Label("Manage accounts · freeze / unfreeze · create new A/C");
        sub.getStyleClass().add("subtitle");

        TableView<Account> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        refreshAccounts(table);

        TableColumn<Account, String> idCol = col("ID", c -> c.getAccountId());
        TableColumn<Account, String> ownerCol = col("Owner", Account::getOwnerName);
        TableColumn<Account, String> balCol = col("Balance",
                c -> MoneyFormatter.format(c.getBalance()));
        TableColumn<Account, String> txCol = col("Txns",
                c -> String.valueOf(c.getTransactionHistory().size()));
        TableColumn<Account, String> statusCol = col("Status",
                c -> c.isFrozen() ? "Frozen" : "Active");

        table.getColumns().addAll(List.of(idCol, ownerCol, balCol, txCol, statusCol));

        Label detailTitle = new Label("Select an account");
        detailTitle.getStyleClass().add("section-title");

        TableView<Transaction> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        historyTable.setPrefHeight(220);
        historyTable.setPlaceholder(new Label("Select an account to view history."));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getType().getDisplayName()));
        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c ->
                new SimpleStringProperty(MoneyFormatter.format(c.getValue().getAmount())));
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTimestamp().format(DATE_FMT)));
        TableColumn<Transaction, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDetails()));
        historyTable.getColumns().addAll(List.of(typeCol, amountCol, dateCol, detailsCol));

        Button freezeBtn = new Button("Freeze Account");
        freezeBtn.getStyleClass().add("danger-button");
        freezeBtn.setDisable(true);

        Label banner = new Label();
        banner.setVisible(false);
        banner.setManaged(false);
        banner.setWrapText(true);

        freezeBtn.setOnAction(e -> {
            Account selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            String selectedId = selected.getAccountId();
            selected.setFrozen(!selected.isFrozen());
            refreshAccounts(table);
            for (Account account : table.getItems()) {
                if (account.getAccountId().equals(selectedId)) {
                    table.getSelectionModel().select(account);
                    break;
                }
            }
            Account refreshed = table.getSelectionModel().getSelectedItem();
            if (refreshed != null) {
                freezeBtn.setText(refreshed.isFrozen() ? "Unfreeze Account" : "Freeze Account");
                showAdminBanner(banner,
                        refreshed.isFrozen()
                                ? "Account " + refreshed.getAccountId() + " is now frozen."
                                : "Account " + refreshed.getAccountId() + " is active again.",
                        !refreshed.isFrozen());
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            if (selected == null) {
                detailTitle.setText("Select an account");
                historyTable.setItems(FXCollections.observableArrayList());
                freezeBtn.setDisable(true);
                return;
            }
            detailTitle.setText(selected.getOwnerName() + " · A/C " + selected.getAccountId()
                    + (selected.isFrozen() ? " (Frozen)" : ""));
            historyTable.setItems(FXCollections.observableArrayList(selected.getTransactionHistory()));
            freezeBtn.setDisable(false);
            freezeBtn.setText(selected.isFrozen() ? "Unfreeze Account" : "Freeze Account");
        });

        VBox createForm = buildCreateAccountForm(table, banner);

        Button logout = new Button("Logout");
        logout.getStyleClass().add("nav-button");
        logout.setOnAction(e -> app.switchTo(new LoginScreen(app).getRoot()));

        HBox top = new HBox(16, new VBox(4, heading, sub), logout);
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(top.getChildren().get(0), Priority.ALWAYS);

        VBox detailPanel = new VBox(12, detailTitle, historyTable, freezeBtn, createForm);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        detailPanel.setPadding(new Insets(0, 0, 0, 12));

        HBox center = new HBox(12, table, detailPanel);
        HBox.setHgrow(table, Priority.ALWAYS);
        HBox.setHgrow(detailPanel, Priority.ALWAYS);

        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-panel");
        layout.setTop(top);
        layout.setCenter(new VBox(12, banner, center));
        BorderPane.setMargin(layout.getTop(), new Insets(0, 0, 12, 0));

        root.getStyleClass().setAll("root");
        root.getChildren().setAll(layout);
    }

    private TableColumn<Account, String> col(String title,
                                             java.util.function.Function<Account, String> mapper) {
        TableColumn<Account, String> column = new TableColumn<>(title);
        column.setCellValueFactory(c -> new SimpleStringProperty(mapper.apply(c.getValue())));
        return column;
    }

    private VBox buildCreateAccountForm(TableView<Account> table, Label banner) {
        Label title = new Label("Create New Account");
        title.getStyleClass().add("section-title");

        TextField idField = field("Account ID (4 digits)");
        PasswordField pinField = new PasswordField();
        pinField.setPromptText("PIN (4–6 digits)");
        pinField.getStyleClass().add("password-field");
        TextField ownerField = field("Owner name");
        TextField balanceField = field("Opening balance (₹)");

        Button create = new Button("Create Account");
        create.getStyleClass().add("primary-button");
        create.setOnAction(e -> {
            String id = safe(idField.getText());
            String pin = safe(pinField.getText());
            String owner = safe(ownerField.getText());
            String balRaw = safe(balanceField.getText());

            if (!InputValidator.isValidAccountId(id)) {
                showAdminBanner(banner, "Account ID must be 4 digits.", false);
                return;
            }
            if (!InputValidator.isValidPin(pin)) {
                showAdminBanner(banner, "PIN must be 4–6 digits.", false);
                return;
            }
            if (!InputValidator.isValidOwnerName(owner)) {
                showAdminBanner(banner, "Enter a valid owner name (letters only).", false);
                return;
            }
            Double opening = InputValidator.tryParseAmount(balRaw.isEmpty() ? "0" : balRaw);
            if (opening == null || opening < 0) {
                showAdminBanner(banner, "Invalid opening balance.", false);
                return;
            }

            boolean created = Bank.getInstance().createAccount(id, pin, owner, opening);
            if (!created) {
                showAdminBanner(banner, "Account ID already exists.", false);
                return;
            }
            refreshAccounts(table);
            idField.clear();
            pinField.clear();
            ownerField.clear();
            balanceField.clear();
            showAdminBanner(banner, "Account " + id + " created for " + owner + ".", true);
        });

        VBox form = new VBox(10, title, idField, pinField, ownerField, balanceField, create);
        form.getStyleClass().add("card");
        return form;
    }

    private TextField field(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("text-field");
        return textField;
    }

    private void refreshAccounts(TableView<Account> table) {
        List<Account> accounts = new ArrayList<>(Bank.getInstance().getAllAccounts());
        accounts.sort(Comparator.comparing(Account::getAccountId));
        table.setItems(FXCollections.observableArrayList(accounts));
        table.refresh();
    }

    private void showAdminBanner(Label banner, String message, boolean success) {
        banner.setText(message);
        banner.getStyleClass().setAll(success ? "success-banner" : "error-banner");
        banner.setVisible(true);
        banner.setManaged(true);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
