package com.atm.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/** JavaFX entry point for SecureBank India. */
public class ATMGuiApp extends Application {

    private Stage primaryStage;
    private Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) ->
                Platform.runLater(() -> showFatal("Unexpected error", error.getMessage())));

        this.primaryStage = stage;
        LoginScreen loginScreen = new LoginScreen(this);
        scene = new Scene(loginScreen.getRoot(), 980, 640);
        applyStylesheet();

        primaryStage.setTitle("SecureBank India — ATM");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public void switchTo(Parent root) {
        scene.setRoot(root);
        applyStylesheet();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void applyStylesheet() {
        var resource = getClass().getResource("/style.css");
        if (resource != null) {
            scene.getStylesheets().setAll(resource.toExternalForm());
            return;
        }
        for (String path : new String[] {
                "src/main/resources/style.css",
                "resources/style.css",
                "bin/style.css"
        }) {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                scene.getStylesheets().setAll(file.toURI().toString());
                return;
            }
        }
    }

    private void showFatal(String header, String detail) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("SecureBank India");
        alert.setHeaderText(header);
        alert.setContentText(detail == null ? "Unknown error" : detail);
        alert.showAndWait();
    }
}
