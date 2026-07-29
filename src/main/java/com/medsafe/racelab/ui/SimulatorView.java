package com.medsafe.racelab.ui;

import com.medsafe.racelab.model.BatchResult;
import com.medsafe.racelab.model.SimulationEvent;
import com.medsafe.racelab.model.SimulationResult;
import com.medsafe.racelab.model.TreatmentSnapshot;
import com.medsafe.racelab.simulation.SafetyMode;
import com.medsafe.racelab.simulation.SimulationEngine;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

public final class SimulatorView extends BorderPane {
    private final SimulationEngine engine;
    private final ComboBox<SafetyMode> modeSelector = new ComboBox<>();
    private final Label modeDescription = UiFactory.muted("");
    private final Button runButton = new Button("Run selected mode");
    private final Button compareButton = new Button("Compare all modes x25");
    private final ProgressIndicator progress = new ProgressIndicator();

    private final VBox statusBanner = new VBox(5);
    private final Label statusTitle = new Label("Ready to run");
    private final Label statusDetails = new Label("Select a mode and run the controlled scenario.");
    private final Label observedMode = valueLabel("-");
    private final Label observedEnergy = valueLabel("-");
    private final Label observedTarget = valueLabel("-");
    private final Label observedVerified = valueLabel("-");
    private final Label observedRevision = valueLabel("-");
    private final Label duration = valueLabel("-");

    private final ObservableList<SimulationEvent> events = FXCollections.observableArrayList();
    private final ObservableList<BatchResult> comparisons = FXCollections.observableArrayList();
    private final TableView<SimulationEvent> eventTable = new TableView<>(events);
    private final TableView<BatchResult> comparisonTable = new TableView<>(comparisons);
    private final TextArea codeArea = new TextArea();
    private final TabPane detailTabs = new TabPane();
    private final Tab timelineTab = new Tab("Event timeline");
    private final Tab codeTab = new Tab("Java code");
    private final Tab comparisonTab = new Tab("Comparison");

    public SimulatorView(SimulationEngine engine) {
        this.engine = engine;
        getStyleClass().add("simulator-view");

        VBox header = new VBox(8,
                UiFactory.badge("LIVE DEMONSTRATION", "badge-red"),
                UiFactory.pageTitle("Medical treatment race simulator"),
                UiFactory.pageSubtitle(
                        "The same operator edit and treatment-start attempt run under four protection modes."));
        header.setPadding(new Insets(28, 30, 14, 30));
        setTop(header);

        VBox controls = buildControls();
        VBox results = buildResults();
        VBox left = new VBox(18, controls, results);
        left.setPadding(new Insets(0, 0, 28, 30));
        left.setPrefWidth(390);
        left.setMinWidth(350);

        configureEventTable();
        configureCodeArea();
        configureComparisonTable();
        timelineTab.setContent(eventTable);
        codeTab.setContent(codeArea);
        comparisonTab.setContent(comparisonTable);
        for (Tab tab : new Tab[]{timelineTab, codeTab, comparisonTab}) {
            tab.setClosable(false);
        }
        detailTabs.getTabs().addAll(timelineTab, codeTab, comparisonTab);
        detailTabs.getStyleClass().add("detail-tabs");

        VBox right = new VBox(detailTabs);
        right.setPadding(new Insets(0, 30, 28, 4));
        VBox.setVgrow(detailTabs, Priority.ALWAYS);

        SplitPane split = new SplitPane(left, right);
        split.setOrientation(Orientation.HORIZONTAL);
        split.setDividerPositions(0.34);
        split.getStyleClass().add("clean-split");
        setCenter(split);

        modeSelector.getItems().setAll(SafetyMode.values());
        modeSelector.getSelectionModel().select(SafetyMode.UNSAFE);
        updateModeInformation(SafetyMode.UNSAFE);
        modeSelector.valueProperty().addListener((observable, oldMode, newMode) -> {
            if (newMode != null) {
                updateModeInformation(newMode);
            }
        });

        runButton.setOnAction(event -> runSelectedMode());
        compareButton.setOnAction(event -> compareAllModes());
    }

    private VBox buildControls() {
        modeSelector.setMaxWidth(Double.MAX_VALUE);
        modeSelector.getStyleClass().add("mode-selector");

        runButton.getStyleClass().add("primary-button");
        runButton.setMaxWidth(Double.MAX_VALUE);
        compareButton.getStyleClass().add("secondary-button");
        compareButton.setMaxWidth(Double.MAX_VALUE);

        progress.setPrefSize(24, 24);
        progress.setMaxSize(24, 24);
        progress.setVisible(false);
        progress.setManaged(false);

        HBox busyRow = new HBox(10, progress, UiFactory.muted("Runs execute on a background task."));
        busyRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = UiFactory.card("Execution mode",
                UiFactory.muted("Protection mechanism"),
                modeSelector,
                modeDescription,
                runButton,
                compareButton,
                busyRow);
        card.getStyleClass().add("control-card");
        return card;
    }

    private VBox buildResults() {
        statusTitle.getStyleClass().add("status-title");
        statusDetails.getStyleClass().add("status-details");
        statusDetails.setWrapText(true);
        statusDetails.setPrefWidth(285);
        statusDetails.setMaxWidth(285);
        statusDetails.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        statusBanner.setMaxWidth(Double.MAX_VALUE);
        statusBanner.getStyleClass().addAll("status-banner", "status-neutral");
        statusBanner.getChildren().addAll(statusTitle, statusDetails);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(11);
        addResultRow(grid, 0, "Observed mode", observedMode);
        addResultRow(grid, 1, "Energy", observedEnergy);
        addResultRow(grid, 2, "Target in place", observedTarget);
        addResultRow(grid, 3, "Verified flag", observedVerified);
        addResultRow(grid, 4, "State revision", observedRevision);
        addResultRow(grid, 5, "Duration", duration);

        Label footnote = UiFactory.muted("Fictional patient DEMO-001; no clinical data is processed.");
        footnote.setPrefWidth(285);
        footnote.setMaxWidth(285);
        footnote.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        VBox card = UiFactory.card("Observed at treatment start", statusBanner, grid, footnote);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private void addResultRow(GridPane grid, int row, String name, Label value) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("result-key");
        grid.add(nameLabel, 0, row);
        grid.add(value, 1, row);
    }

    private static Label valueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("result-value");
        return label;
    }

    private void configureEventTable() {
        eventTable.setPlaceholder(UiFactory.muted("Run a simulation to populate the thread timeline."));
        eventTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SimulationEvent, Number> sequence = new TableColumn<>("#");
        sequence.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().sequence()));
        sequence.setMaxWidth(55);
        sequence.setMinWidth(45);

        TableColumn<SimulationEvent, String> time = new TableColumn<>("Time (ms)");
        time.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format(
                Locale.US, "%.3f", data.getValue().elapsedMicros() / 1000.0)));
        time.setMaxWidth(100);
        time.setMinWidth(90);

        TableColumn<SimulationEvent, String> thread = new TableColumn<>("Thread");
        thread.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().threadName()));
        thread.setMinWidth(125);

        TableColumn<SimulationEvent, String> level = new TableColumn<>("Level");
        level.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().level().name()));
        level.setMaxWidth(90);
        level.setMinWidth(80);

        TableColumn<SimulationEvent, String> message = new TableColumn<>("Event");
        message.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().message()));
        message.setMinWidth(420);

        eventTable.getColumns().add(sequence);
        eventTable.getColumns().add(time);
        eventTable.getColumns().add(thread);
        eventTable.getColumns().add(level);
        eventTable.getColumns().add(message);
    }

    private void configureCodeArea() {
        codeArea.setEditable(false);
        codeArea.setWrapText(false);
        codeArea.getStyleClass().add("code-area");
        codeArea.setText(CodeSamples.forMode(SafetyMode.UNSAFE));
    }

    private void configureComparisonTable() {
        comparisonTable.setPlaceholder(UiFactory.muted("Choose 'Compare all modes' to run the test matrix."));
        comparisonTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BatchResult, String> mode = new TableColumn<>("Mode");
        mode.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().safetyMode().displayName()));
        mode.setMinWidth(190);

        TableColumn<BatchResult, Number> runs = new TableColumn<>("Runs");
        runs.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().runs()));

        TableColumn<BatchResult, Number> started = new TableColumn<>("Started");
        started.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(
                data.getValue().treatmentsStarted()));

        TableColumn<BatchResult, Number> failures = new TableColumn<>("Safety violations");
        failures.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(
                data.getValue().safetyViolations()));

        TableColumn<BatchResult, String> rate = new TableColumn<>("Violation rate");
        rate.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format(
                Locale.US, "%.1f%%", data.getValue().failureRatePercent())));

        TableColumn<BatchResult, String> average = new TableColumn<>("Average ms");
        average.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format(
                Locale.US, "%.3f", data.getValue().averageDurationMillis())));

        comparisonTable.getColumns().add(mode);
        comparisonTable.getColumns().add(runs);
        comparisonTable.getColumns().add(started);
        comparisonTable.getColumns().add(failures);
        comparisonTable.getColumns().add(rate);
        comparisonTable.getColumns().add(average);
    }

    private void updateModeInformation(SafetyMode mode) {
        modeDescription.setText(mode.description());
        codeArea.setText(CodeSamples.forMode(mode));
    }

    private void runSelectedMode() {
        SafetyMode mode = modeSelector.getValue();
        if (mode == null) {
            return;
        }
        setBusy(true);
        setNeutralStatus("Running " + mode.displayName(), "Coordinating the operator and treatment tasks...");

        Task<SimulationResult> task = new Task<>() {
            @Override
            protected SimulationResult call() {
                return engine.run(mode);
            }
        };
        task.setOnSucceeded(event -> {
            setBusy(false);
            displayResult(task.getValue());
        });
        task.setOnFailed(event -> {
            setBusy(false);
            displayFailure(task.getException());
        });
        startBackground(task, "SelectedSimulationTask");
    }

    private void compareAllModes() {
        setBusy(true);
        setNeutralStatus("Running comparison", "Executing 25 controlled runs for each mode...");

        Task<List<BatchResult>> task = new Task<>() {
            @Override
            protected List<BatchResult> call() {
                return engine.compareAll(25);
            }
        };
        task.setOnSucceeded(event -> {
            setBusy(false);
            comparisons.setAll(task.getValue());
            detailTabs.getSelectionModel().select(comparisonTab);
            setSafeStatus("Comparison complete",
                    "Unsafe mode produced violations; all three protected modes completed safely.");
        });
        task.setOnFailed(event -> {
            setBusy(false);
            displayFailure(task.getException());
        });
        startBackground(task, "ComparisonTask");
    }

    private void startBackground(Task<?> task, String name) {
        Thread worker = new Thread(task, name);
        worker.setDaemon(true);
        worker.start();
    }

    private void displayResult(SimulationResult result) {
        events.setAll(result.events());
        TreatmentSnapshot snapshot = result.observedAtStart();
        if (snapshot != null) {
            observedMode.setText(snapshot.mode().displayName());
            observedEnergy.setText(snapshot.energyMeV() + " MeV");
            observedTarget.setText(snapshot.targetInPlace() ? "YES" : "NO");
            observedVerified.setText(snapshot.verified() ? "TRUE" : "FALSE");
            observedRevision.setText(Long.toString(snapshot.revision()));
        }
        duration.setText(String.format(Locale.US, "%.3f ms", result.durationNanos() / 1_000_000.0));

        if (result.safetyViolation()) {
            setDangerStatus("SAFETY VIOLATION DETECTED", result.observedAtStart().safetyExplanation());
        } else {
            setSafeStatus("SAFE EXECUTION", result.summary());
        }
        detailTabs.getSelectionModel().select(timelineTab);
    }

    private void displayFailure(Throwable throwable) {
        String message = throwable == null ? "Unknown error" : throwable.getMessage();
        setDangerStatus("SIMULATION ERROR", message);
    }

    private void setBusy(boolean busy) {
        runButton.setDisable(busy);
        compareButton.setDisable(busy);
        modeSelector.setDisable(busy);
        progress.setManaged(busy);
        progress.setVisible(busy);
    }

    private void setNeutralStatus(String title, String details) {
        setStatus("status-neutral", title, details);
    }

    private void setSafeStatus(String title, String details) {
        setStatus("status-safe", title, details);
    }

    private void setDangerStatus(String title, String details) {
        setStatus("status-danger", title, details);
    }

    private void setStatus(String statusClass, String title, String details) {
        statusBanner.getStyleClass().removeAll("status-neutral", "status-safe", "status-danger");
        statusBanner.getStyleClass().add(statusClass);
        statusTitle.setText(title);
        statusDetails.setText(details);
    }
}
