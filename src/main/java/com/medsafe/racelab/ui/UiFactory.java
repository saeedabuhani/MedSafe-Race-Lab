package com.medsafe.racelab.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

final class UiFactory {
    private UiFactory() {
    }

    static Label pageTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    static Label pageSubtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-subtitle");
        label.setWrapText(true);
        return label;
    }

    static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    static Label body(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("body-text");
        label.setWrapText(true);
        return label;
    }

    static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        label.setWrapText(true);
        return label;
    }

    static Label badge(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("badge", styleClass);
        return label;
    }

    static VBox card(String title, Node... content) {
        VBox box = new VBox(12);
        box.getStyleClass().add("card");
        if (title != null && !title.isBlank()) {
            box.getChildren().add(sectionTitle(title));
        }
        box.getChildren().addAll(content);
        return box;
    }

    static Region spacer() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    static VBox page(Node... children) {
        VBox page = new VBox(20, children);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(30));
        return page;
    }
}
