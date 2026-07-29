package com.medsafe.racelab.ui;

import com.medsafe.racelab.simulation.SimulationEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class DashboardView extends BorderPane {
    private final StackPane content = new StackPane();
    private final Map<Button, Node> pages = new LinkedHashMap<>();
    private Button activeButton;

    public DashboardView(
            String username,
            SimulationEngine engine,
            Runnable onLogout,
            Consumer<String> openUrl) {
        getStyleClass().add("dashboard-root");

        SimulatorView simulator = new SimulatorView(engine);
        TheoryView theory = new TheoryView();
        IncidentsView incidents = new IncidentsView(openUrl);
        ReferencesView references = new ReferencesView(openUrl);

        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(26, 18, 22, 18));
        sidebar.setPrefWidth(245);

        Label brand = new Label("MedSafe");
        brand.getStyleClass().add("sidebar-brand");
        Label brandSub = new Label("RACE LAB");
        brandSub.getStyleClass().add("sidebar-brand-sub");
        VBox brandBox = new VBox(1, brand, brandSub);
        brandBox.setPadding(new Insets(0, 10, 24, 10));

        Label navigation = new Label("NAVIGATION");
        navigation.getStyleClass().add("sidebar-section-label");
        navigation.setPadding(new Insets(0, 10, 4, 10));

        Button overviewButton = navButton("Overview");
        Button simulatorButton = navButton("Simulator");
        Button theoryButton = navButton("Race condition theory");
        Button incidentsButton = navButton("Fatal incidents");
        Button referencesButton = navButton("References");

        OverviewView overview = new OverviewView(
                () -> showPage(simulatorButton),
                () -> showPage(theoryButton));

        pages.put(overviewButton, overview);
        pages.put(simulatorButton, simulator);
        pages.put(theoryButton, theory);
        pages.put(incidentsButton, incidents);
        pages.put(referencesButton, references);

        pages.keySet().forEach(button -> button.setOnAction(event -> showPage(button)));

        VBox userCard = new VBox(3);
        userCard.getStyleClass().add("sidebar-user-card");
        Label signedIn = new Label("SIGNED IN AS");
        signedIn.getStyleClass().add("sidebar-section-label");
        Label user = new Label(username);
        user.getStyleClass().add("sidebar-user");
        userCard.getChildren().addAll(signedIn, user);

        Button logout = new Button("Log out");
        logout.getStyleClass().add("sidebar-logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(event -> onLogout.run());

        Label disclaimer = new Label("Educational use only\nNot for clinical use");
        disclaimer.getStyleClass().add("sidebar-disclaimer");
        disclaimer.setWrapText(true);

        sidebar.getChildren().addAll(
                brandBox, navigation,
                overviewButton, simulatorButton, theoryButton, incidentsButton, referencesButton,
                UiFactory.spacer(), userCard, logout, disclaimer);
        VBox.setVgrow(sidebar.getChildren().get(7), Priority.ALWAYS);

        content.getStyleClass().add("content-holder");
        setLeft(sidebar);
        setCenter(content);
        showPage(overviewButton);
    }

    private Button navButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private void showPage(Button button) {
        Node node = pages.get(button);
        if (node == null) {
            return;
        }
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
        }
        activeButton = button;
        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
        content.getChildren().setAll(node);
    }
}
