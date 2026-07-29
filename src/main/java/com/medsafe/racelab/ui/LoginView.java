package com.medsafe.racelab.ui;

import com.medsafe.racelab.auth.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class LoginView extends BorderPane {
    public LoginView(AuthService authService, Consumer<String> onLogin) {
        getStyleClass().add("login-root");

        VBox identity = new VBox(22);
        identity.getStyleClass().add("login-identity");
        identity.setPadding(new Insets(70));
        identity.setAlignment(Pos.CENTER_LEFT);

        Label eyebrow = new Label("SAFETY-CRITICAL SOFTWARE LAB");
        eyebrow.getStyleClass().add("login-eyebrow");
        Label title = new Label("MedSafe\nRace Lab");
        title.getStyleClass().add("login-title");
        Label description = new Label(
                "An educational simulator that exposes a medical race condition, "
                        + "then prevents it with three Java synchronization mechanisms.");
        description.getStyleClass().add("login-description");
        description.setWrapText(true);
        description.setMaxWidth(500);

        VBox promise = new VBox(12,
                feature("01", "Observe the unsafe interleaving"),
                feature("02", "Compare synchronized, Lock and Semaphore"),
                feature("03", "Inspect a precise thread-event timeline"));
        promise.getStyleClass().add("feature-list");

        Label disclaimer = new Label(
                "EDUCATIONAL SIMULATION ONLY - NOT FOR CLINICAL USE");
        disclaimer.getStyleClass().add("login-disclaimer");
        identity.getChildren().addAll(eyebrow, title, description, promise, UiFactory.spacer(), disclaimer);

        StackPane formArea = new StackPane();
        formArea.getStyleClass().add("login-form-area");
        formArea.setPadding(new Insets(60));

        VBox form = new VBox(16);
        form.getStyleClass().add("login-card");
        form.setMaxWidth(430);

        Label formTitle = new Label("Welcome back");
        formTitle.getStyleClass().add("login-form-title");
        Label formSubtitle = UiFactory.muted("Sign in to open the safety demonstration dashboard.");

        Label usernameLabel = fieldLabel("Username");
        TextField username = new TextField();
        username.setPromptText("Enter username");
        username.setText(AuthService.DEMO_USERNAME);

        Label passwordLabel = fieldLabel("Password");
        PasswordField password = new PasswordField();
        password.setPromptText("Enter password");
        password.setText(AuthService.DEMO_PASSWORD);

        Label error = new Label();
        error.getStyleClass().add("form-error");
        error.setWrapText(true);
        error.setManaged(false);
        error.setVisible(false);

        Button signIn = new Button("Sign in to MedSafe");
        signIn.getStyleClass().addAll("primary-button", "wide-button");
        signIn.setMaxWidth(Double.MAX_VALUE);

        Runnable submit = () -> {
            boolean valid = authService.authenticate(
                    username.getText(), password.getText().toCharArray());
            if (valid) {
                error.setVisible(false);
                error.setManaged(false);
                onLogin.accept(username.getText().trim());
            } else {
                error.setText("Incorrect username or password. Use the demo credentials below.");
                error.setManaged(true);
                error.setVisible(true);
                password.requestFocus();
                password.selectAll();
            }
        };
        signIn.setOnAction(event -> submit.run());
        password.setOnAction(event -> submit.run());

        VBox demoBox = new VBox(5,
                new Label("DEMO ACCESS"),
                UiFactory.muted("Username: student"),
                UiFactory.muted("Password: MedSafe2026!"));
        demoBox.getStyleClass().add("demo-credentials");

        form.getChildren().addAll(
                formTitle, formSubtitle,
                usernameLabel, username,
                passwordLabel, password,
                error, signIn, demoBox);
        formArea.getChildren().add(form);

        HBox layout = new HBox(identity, formArea);
        HBox.setHgrow(identity, Priority.ALWAYS);
        HBox.setHgrow(formArea, Priority.ALWAYS);
        identity.prefWidthProperty().bind(layout.widthProperty().multiply(0.56));
        formArea.prefWidthProperty().bind(layout.widthProperty().multiply(0.44));
        setCenter(layout);
    }

    private HBox feature(String number, String text) {
        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("feature-number");
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("feature-text");
        return new HBox(14, numberLabel, textLabel);
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }
}
