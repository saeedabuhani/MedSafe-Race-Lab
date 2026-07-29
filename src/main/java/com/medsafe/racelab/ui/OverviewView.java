package com.medsafe.racelab.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class OverviewView extends ScrollPane {
    public OverviewView(Runnable openSimulator, Runnable openTheory) {
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");

        Label eyebrow = UiFactory.badge("PROJECT OVERVIEW", "badge-blue");
        Label title = UiFactory.pageTitle("Safety depends on timing - until we make it atomic.");
        Label subtitle = UiFactory.pageSubtitle(
                "This project demonstrates one medical treatment update under four execution modes. "
                        + "The business scenario stays identical; only the synchronization mechanism changes.");

        Button runDemo = new Button("Open simulator");
        runDemo.getStyleClass().add("primary-button");
        runDemo.setOnAction(event -> openSimulator.run());
        Button readTheory = new Button("Review race condition theory");
        readTheory.getStyleClass().add("secondary-button");
        readTheory.setOnAction(event -> openTheory.run());
        HBox actions = new HBox(12, runDemo, readTheory);

        VBox hero = new VBox(14, eyebrow, title, subtitle, actions);
        hero.getStyleClass().add("hero-card");

        VBox problem = metricCard(
                "01", "Unsafe shared state",
                "Two tasks read and write one mutable treatment configuration without mutual exclusion.",
                "RACE WINDOW", "badge-red");
        VBox solutions = metricCard(
                "03", "Safe mechanisms",
                "synchronized, ReentrantLock and Semaphore(1) protect the full critical transaction.",
                "ZERO VIOLATIONS", "badge-green");
        VBox evidence = metricCard(
                "04", "Verified modes",
                "Every mode is tested repeatedly and exposes an ordered event timeline for review.",
                "JUnit + UI", "badge-blue");
        HBox metrics = new HBox(18, problem, solutions, evidence);
        HBox.setHgrow(problem, Priority.ALWAYS);
        HBox.setHgrow(solutions, Priority.ALWAYS);
        HBox.setHgrow(evidence, Priority.ALWAYS);
        problem.setMaxWidth(Double.MAX_VALUE);
        solutions.setMaxWidth(Double.MAX_VALUE);
        evidence.setMaxWidth(Double.MAX_VALUE);

        VBox step1 = stepCard("1", "Operator edit",
                "The operator begins changing a verified electron plan to X-ray mode.");
        VBox step2 = stepCard("2", "Concurrent start",
                "The treatment task attempts to start while the configuration is between states.");
        VBox step3 = stepCard("3", "Safety decision",
                "The simulator records whether the observed snapshot satisfies the invariant.");
        HBox flow = new HBox(18, step1, step2, step3);
        HBox.setHgrow(step1, Priority.ALWAYS);
        HBox.setHgrow(step2, Priority.ALWAYS);
        HBox.setHgrow(step3, Priority.ALWAYS);
        step1.setMaxWidth(Double.MAX_VALUE);
        step2.setMaxWidth(Double.MAX_VALUE);
        step3.setMaxWidth(Double.MAX_VALUE);

        VBox assignment = UiFactory.card("Assignment coverage",
                checklist("A", "Define exactly what a race condition is."),
                checklist("B", "Demonstrate the bug with executable Java code."),
                checklist("C", "Compare three correct synchronization solutions."),
                checklist("D", "Connect the technical fault to safety-critical engineering lessons."));

        VBox page = UiFactory.page(hero, metrics, UiFactory.sectionTitle("How one run works"), flow, assignment);
        setContent(page);
    }

    private VBox metricCard(
            String number, String title, String description, String badge, String badgeStyle) {
        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("metric-number");
        VBox box = UiFactory.card(null,
                new HBox(10, numberLabel, UiFactory.badge(badge, badgeStyle)),
                UiFactory.sectionTitle(title), UiFactory.body(description));
        box.getStyleClass().add("metric-card");
        return box;
    }

    private VBox stepCard(String number, String title, String description) {
        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("step-number");
        VBox content = new VBox(6, UiFactory.sectionTitle(title), UiFactory.body(description));
        HBox row = new HBox(14, numberLabel, content);
        row.setAlignment(Pos.TOP_LEFT);
        VBox card = UiFactory.card(null, row);
        return card;
    }

    private HBox checklist(String letter, String text) {
        Label marker = UiFactory.badge(letter, "badge-blue");
        Label label = UiFactory.body(text);
        HBox row = new HBox(12, marker, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
