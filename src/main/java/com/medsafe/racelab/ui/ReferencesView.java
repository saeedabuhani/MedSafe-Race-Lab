package com.medsafe.racelab.ui;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class ReferencesView extends ScrollPane {
    public ReferencesView(Consumer<String> openUrl) {
        setFitToWidth(true);
        getStyleClass().add("transparent-scroll");

        VBox course = UiFactory.card("Course material",
                UiFactory.body("BIO-MED SW COURSE 2025, Unit 1: Software as a Medical Device, "
                        + "slides 18-25 and course rules on slides 9-10."),
                UiFactory.body("FDA / IMDRF, Software as a Medical Device (SaMD): Clinical Evaluation, 2017."));

        VBox research = UiFactory.card("Research and official reports",
                reference(openUrl,
                        "Leveson, N. G. & Turner, C. S. - An Investigation of the Therac-25 Accidents",
                        "https://escholarship.org/uc/item/5dr206s3"),
                reference(openUrl,
                        "U.S. GAO - Patriot Missile Defense: Software Problem Led to System Failure",
                        "https://www.gao.gov/products/imtec-92-26"),
                reference(openUrl,
                        "U.S. House Committee - Boeing 737 MAX Investigation",
                        "https://democrats-transportation.house.gov/committee-activity/boeing-737-max-investigation"),
                reference(openUrl,
                        "NTSB - Collision Between Developmental Automated Driving System and Pedestrian",
                        "https://www.ntsb.gov/investigations/accidentreports/reports/har1903.pdf"));

        VBox engineering = UiFactory.card("Java APIs used",
                reference(openUrl, "Java synchronized methods and statements",
                        "https://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html"),
                reference(openUrl, "Java ReentrantLock API",
                        "https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html"),
                reference(openUrl, "Java Semaphore API",
                        "https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/Semaphore.html"),
                reference(openUrl, "Java CountDownLatch API",
                        "https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/CountDownLatch.html"));

        VBox disclaimer = UiFactory.card("Safety disclaimer",
                UiFactory.body("This project is an educational concurrency simulator. It does not calculate, "
                        + "recommend or deliver medical treatment and must never be used for clinical decisions."));
        disclaimer.getStyleClass().add("warning-card");

        VBox page = UiFactory.page(
                UiFactory.badge("REFERENCES", "badge-blue"),
                UiFactory.pageTitle("Traceable evidence and technical documentation"),
                UiFactory.pageSubtitle("Every factual case and Java mechanism used by the project has a visible source."),
                course, research, engineering, disclaimer);
        setContent(page);
    }

    private VBox reference(Consumer<String> openUrl, String title, String url) {
        Hyperlink link = new Hyperlink(title);
        link.getStyleClass().add("reference-link");
        link.setWrapText(true);
        link.setOnAction(event -> openUrl.accept(url));
        return new VBox(3, link, UiFactory.muted(url));
    }
}
