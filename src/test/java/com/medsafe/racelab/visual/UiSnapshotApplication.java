package com.medsafe.racelab.visual;

import com.medsafe.racelab.MedSafeApplication;
import com.medsafe.racelab.auth.AuthService;
import com.medsafe.racelab.simulation.SimulationEngine;
import com.medsafe.racelab.ui.DashboardView;
import com.medsafe.racelab.ui.LoginView;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Visual QA helper. It is not used by the submitted application at runtime.
 */
public final class UiSnapshotApplication extends Application {
    @Override
    public void start(Stage stage) {
        Parameters parameters = getParameters();
        String screen = parameters.getRaw().isEmpty() ? "login" : parameters.getRaw().get(0);
        Path output = Path.of(parameters.getRaw().size() < 2
                ? "docs/screenshots/visual-check.png"
                : parameters.getRaw().get(1));

        boolean loginScreen = "login".equalsIgnoreCase(screen);
        Parent root;
        if (loginScreen) {
            root = new LoginView(new AuthService(), ignored -> { });
        } else {
            root = new DashboardView(
                    AuthService.DEMO_USERNAME,
                    new SimulationEngine(),
                    () -> { },
                    ignored -> { });
        }

        Scene scene = new Scene(root, 1360, 840);
        URL stylesheet = MedSafeApplication.class.getResource("/com/medsafe/racelab/app.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        stage.setScene(scene);
        stage.show();

        if (!loginScreen) {
            Platform.runLater(() -> {
                String navigation = switch (screen.toLowerCase()) {
                    case "simulator" -> "Simulator";
                    case "theory" -> "Race condition theory";
                    case "incidents" -> "Fatal incidents";
                    case "references" -> "References";
                    default -> "Overview";
                };
                fireButton(root, navigation);
                if ("simulator".equalsIgnoreCase(screen)) {
                    Platform.runLater(() -> fireButton(root, "Run selected mode"));
                }
            });
        }

        PauseTransition waitForLayoutAndTask = new PauseTransition(Duration.millis(
                "simulator".equalsIgnoreCase(screen) ? 1_500 : 500));
        waitForLayoutAndTask.setOnFinished(event -> {
            try {
                snapshot(root, output.toFile());
            } catch (IOException exception) {
                throw new IllegalStateException("Could not save UI snapshot", exception);
            } finally {
                Platform.exit();
            }
        });
        waitForLayoutAndTask.play();
    }

    private void fireButton(Node node, String text) {
        if (node instanceof Button button && text.equals(button.getText())) {
            button.fire();
            return;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                fireButton(child, text);
            }
        }
    }

    private void snapshot(Node node, File output) throws IOException {
        output.getParentFile().mkdirs();
        WritableImage image = node.snapshot(new SnapshotParameters(), null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(
                0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(bufferedImage, "png", output);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
