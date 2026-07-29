package com.medsafe.racelab.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public final class IncidentsView extends ScrollPane {
    private static final List<Incident> INCIDENTS = List.of(
            new Incident(
                    "Therac-25", "1985-1987", "3 deaths in course material",
                    "Concurrent shared state, missing independent interlocks, weak testing and incident response.",
                    "UC eScholarship investigation",
                    "https://escholarship.org/uc/item/5dr206s3"),
            new Incident(
                    "Patriot missile failure", "1991", "28 deaths",
                    "A timing calculation error accumulated after prolonged continuous operation.",
                    "U.S. GAO IMTEC-92-26",
                    "https://www.gao.gov/products/imtec-92-26"),
            new Incident(
                    "Boeing 737 MAX", "2018-2019", "346 deaths",
                    "Systemic design, software, certification, training and organizational failures; not one simple bug.",
                    "U.S. House investigation",
                    "https://democrats-transportation.house.gov/committee-activity/boeing-737-max-investigation"),
            new Incident(
                    "Uber developmental ADS", "2018", "1 death",
                    "Automation design, disabled emergency response and inadequate safety oversight combined.",
                    "NTSB HAR-19/03",
                    "https://www.ntsb.gov/investigations/accidentreports/reports/har1903.pdf"));

    public IncidentsView(Consumer<String> openUrl) {
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");

        TableView<Incident> table = buildTable();
        table.getItems().setAll(INCIDENTS);
        table.setPrefHeight(300);

        VBox dataNote = UiFactory.card("How the statistics are presented",
                UiFactory.body("There is no unsupported global total in this project. Each number is attached "
                        + "to a documented incident and its own source."),
                UiFactory.body("The cases are not added together as if they had identical definitions of "
                        + "'software bug'. Several were complex system accidents in which software was one "
                        + "important part of a wider failure."));
        dataNote.getStyleClass().add("accent-card");

        HBox sourceButtons = new HBox(10);
        for (Incident incident : INCIDENTS) {
            Button button = new Button(incident.name());
            button.getStyleClass().add("link-button");
            button.setOnAction(event -> openUrl.accept(incident.url()));
            sourceButtons.getChildren().add(button);
        }

        VBox common = UiFactory.card("Common circumstances across large software-related disasters",
                lesson("01", "No independent safety barrier",
                        "One software path was allowed to become a single point of catastrophic failure."),
                lesson("02", "Incomplete verification and validation",
                        "Testing did not represent timing boundaries, prolonged operation or realistic use."),
                lesson("03", "Human factors were treated as an afterthought",
                        "Warnings, controls and recovery paths were confusing or relied on perfect human response."),
                lesson("04", "Organizational signals were missed",
                        "Production pressure, fragmented responsibility or ignored reports delayed correction."),
                lesson("05", "The accident was systemic",
                        "Code, hardware, procedures, management and regulation interacted."));

        VBox page = UiFactory.page(
                UiFactory.badge("DOCUMENTED CASES", "badge-blue"),
                UiFactory.pageTitle("Software-related fatalities: evidence, not sensational numbers"),
                UiFactory.pageSubtitle(
                        "A careful case table supports the second bonus question and avoids false precision."),
                table,
                sourceButtons,
                dataNote,
                common);
        setContent(page);
    }

    private TableView<Incident> buildTable() {
        TableView<Incident> table = new TableView<>();
        table.getStyleClass().add("incident-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Incident, String> incident = column("Incident", 150, Incident::name);
        TableColumn<Incident, String> period = column("Period", 100, Incident::period);
        TableColumn<Incident, String> fatalities = column("Reported fatalities", 170, Incident::fatalities);
        TableColumn<Incident, String> lesson = column("Software and system lesson", 440, Incident::lesson);
        TableColumn<Incident, String> source = column("Primary / official source", 190, Incident::source);

        table.getColumns().add(incident);
        table.getColumns().add(period);
        table.getColumns().add(fatalities);
        table.getColumns().add(lesson);
        table.getColumns().add(source);
        return table;
    }

    private TableColumn<Incident, String> column(
            String title, double minWidth, java.util.function.Function<Incident, String> getter) {
        TableColumn<Incident, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
        column.setMinWidth(minWidth);
        return column;
    }

    private HBox lesson(String number, String title, String description) {
        Label marker = UiFactory.badge(number, "badge-blue");
        VBox text = new VBox(3, UiFactory.sectionTitle(title), UiFactory.body(description));
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox row = new HBox(14, marker, text);
        row.setPadding(new Insets(4, 0, 4, 0));
        return row;
    }

    private record Incident(
            String name,
            String period,
            String fatalities,
            String lesson,
            String source,
            String url) {
    }
}
