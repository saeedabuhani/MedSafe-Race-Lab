package com.medsafe.racelab.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class TheoryView extends ScrollPane {
    public TheoryView() {
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");

        VBox definition = UiFactory.card("Exact definition",
                UiFactory.badge("CORE CONCEPT", "badge-red"),
                largeQuote("A race condition occurs when the result of a program depends on the "
                        + "unpredictable timing or interleaving of concurrent tasks that access shared "
                        + "mutable state, with at least one write, without adequate synchronization."),
                UiFactory.body("The problem is not merely that two threads exist. The problem is that a "
                        + "multi-step safety decision can observe an incomplete or stale state."));

        VBox condition1 = condition("1", "Shared mutable data",
                "Both tasks access the same TreatmentState object.");
        VBox condition2 = condition("2", "At least one write",
                "The operator changes mode, energy, target position and verification.");
        VBox condition3 = condition("3", "No atomic boundary",
                "The treatment task can enter between individual field updates.");
        HBox conditions = new HBox(18, condition1, condition2, condition3);
        for (VBox card : new VBox[]{condition1, condition2, condition3}) {
            HBox.setHgrow(card, Priority.ALWAYS);
            card.setMaxWidth(Double.MAX_VALUE);
        }

        VBox invariant = UiFactory.card("Medical safety invariant used by this simulator",
                UiFactory.body("X_RAY mode is safe only when the beam-spreading target is in place and "
                        + "the configured energy is within the allowed range."),
                UiFactory.body("ELECTRON mode is safe only when the X-ray target is outside the beam path."),
                UiFactory.body("Treatment may start only after the complete configuration has been verified."));
        invariant.getStyleClass().add("accent-card");

        VBox therac = UiFactory.card("Therac-25 context - important accuracy note",
                UiFactory.body("The course material presents Therac-25 as a classic race-condition disaster. "
                        + "The historical investigation documents six known massive-overdose accidents and "
                        + "shows that concurrent tasks accessed shared memory without real synchronization."),
                UiFactory.body("The accidents must not be reduced to one line of code. Missing independent "
                        + "hardware interlocks, inadequate safety analysis, weak testing, poor error messages, "
                        + "and delayed response to incident reports also mattered."),
                UiFactory.muted("This application is a conceptual teaching model inspired by those lessons; "
                        + "it is not a literal reproduction of Therac-25 software."));

        VBox controlled = UiFactory.card("Why the demonstration uses controlled scheduling",
                UiFactory.body("A real race may disappear during a classroom presentation because thread "
                        + "timing changes from run to run. CountDownLatch is used only by the demonstration "
                        + "harness to place the threads at the dangerous boundary."),
                UiFactory.body("The unsafe shared state is still unprotected. In the three corrected modes, "
                        + "the treatment task reaches the same boundary but must wait for the lock or permit."));

        VBox page = UiFactory.page(
                UiFactory.badge("THEORY", "badge-blue"),
                UiFactory.pageTitle("Race condition, without the confusion"),
                UiFactory.pageSubtitle("A precise definition, the three necessary ingredients, and the safety invariant."),
                definition,
                UiFactory.sectionTitle("The three ingredients"),
                conditions,
                invariant,
                therac,
                controlled);
        setContent(page);
    }

    private Label largeQuote(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("definition-quote");
        label.setWrapText(true);
        return label;
    }

    private VBox condition(String number, String title, String text) {
        Label marker = new Label(number);
        marker.getStyleClass().add("step-number");
        HBox header = new HBox(12, marker, UiFactory.sectionTitle(title));
        header.setAlignment(Pos.CENTER_LEFT);
        return UiFactory.card(null, header, UiFactory.body(text));
    }
}
