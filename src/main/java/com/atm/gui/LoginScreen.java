package com.atm.gui;

import com.atm.models.Account;
import com.atm.utils.SecurityManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Customer login for SecureBank India.
 */
public class LoginScreen {

    private final ATMGuiApp app;
    private final SecurityManager securityManager;
    private final StackPane root;
    private final TextField accountIdField;
    private final PasswordField pinField;
    private final Button loginButton;
    private final Label errorLabel;

    public LoginScreen(ATMGuiApp app) {
        this.app = app;
        this.securityManager = new SecurityManager();

        Label brand = new Label("SECUREBANK");
        brand.getStyleClass().add("brand");

        Label title = new Label("India");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Namaste — sign in to continue");
        subtitle.getStyleClass().add("subtitle");

        accountIdField = new TextField();
        accountIdField.setPromptText("Account ID  (e.g. 1001)");
        accountIdField.getStyleClass().add("text-field");

        pinField = new PasswordField();
        pinField.setPromptText("PIN");
        pinField.getStyleClass().add("password-field");

        loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> handleLogin());

        Button adminLink = new Button("Admin Console");
        adminLink.getStyleClass().add("link-button");
        adminLink.setOnAction(e -> app.switchTo(new AdminScreen(app).getRoot()));

        Label tip = new Label("Demo · Satya 1001/1234 · Hitesh 1004/2468 · Ram 1008/1122");
        tip.getStyleClass().add("hint");

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMinHeight(36);

        HBox footer = new HBox(adminLink);
        footer.setAlignment(Pos.CENTER);

        VBox card = new VBox(12,
                brand, title, subtitle,
                labeled("Account ID", accountIdField),
                labeled("PIN", pinField),
                loginButton, footer, tip, errorLabel
        );
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);

        root = new StackPane(card);
        root.getStyleClass().addAll("root", "login-root");
        root.setPadding(new Insets(40));
        StackPane.setAlignment(card, Pos.CENTER);

        pinField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        accountIdField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                pinField.requestFocus();
            }
        });
    }

    public Parent getRoot() {
        return root;
    }

    private VBox labeled(String text, javafx.scene.Node control) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return new VBox(6, label, control);
    }

    private void handleLogin() {
        if (securityManager.isSessionLocked()) {
            lockUi();
            errorLabel.setText(securityManager.getLastMessage());
            return;
        }

        String id = safe(accountIdField.getText());
        String pin = safe(pinField.getText());

        Account account = securityManager.authenticate(id, pin);
        if (account != null) {
            app.switchTo(new DashboardScreen(app, account).getRoot());
            return;
        }

        errorLabel.setText(securityManager.getLastMessage());
        pinField.clear();
        if (securityManager.isSessionLocked()) {
            lockUi();
        }
    }

    private void lockUi() {
        accountIdField.setDisable(true);
        pinField.setDisable(true);
        loginButton.setDisable(true);
        errorLabel.setText("Too many failed attempts. Restart the app to try again.");
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
