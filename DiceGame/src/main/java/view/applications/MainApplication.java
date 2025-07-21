package view.applications;

import controllers.*;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The main entry point for the Dice Game application.
 * This class handles the initial setup, scene management, and transitions between different views.
 */
public class MainApplication extends Application {
    private static Stage primaryStage;
    private static Scene mainScene;
    private static final int FADE_DURATION = 200; // Duration of the fade transition in milliseconds
    private static StackPane rootContainer;

    /**
     * Starts the JavaFX application by setting up the primary stage and loading the first screen.
     *
     * @param stage The primary stage provided by the JavaFX runtime
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Dice Game");

        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        rootContainer = new StackPane();
        mainScene = new Scene(rootContainer);
        mainScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(mainScene);

        loadFirstScene();
        primaryStage.show();
    }

    /**
     * Applies a fade-in transition when switching scenes.
     *
     * @param sceneLoader A supplier that provides the new scene (Parent) to be loaded
     */
    private static void applyTransition(java.util.function.Supplier<Parent> sceneLoader) {
        try {
            Parent newRoot = sceneLoader.get();
            newRoot.setOpacity(0.5); // Start opacity at 50%

            rootContainer.getChildren().add(newRoot);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), newRoot);
            fadeIn.setFromValue(0.5);
            fadeIn.setToValue(1.0);

            fadeIn.setOnFinished(e -> {
                // Remove all previous scenes except the top one
                if (rootContainer.getChildren().size() > 1) {
                    rootContainer.getChildren().remove(0, rootContainer.getChildren().size() - 1);
                }
            });

            fadeIn.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the first screen of the application (FirstScreen).
     */
    public static void loadFirstScene() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/FirstScreen.fxml"));
            Parent root = loader.load();
            FirstScreenController controller = loader.getController();
            controller.init();

            rootContainer.getChildren().clear();
            rootContainer.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the Game screen (GameScreen) with a fade-in transition.
     */
    public static void loadGameScene() {
        applyTransition(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/GameScreen.fxml"));
                Parent root = loader.load();
                GameScreenController controller = loader.getController();
                controller.init();
                return root;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Loads the Main menu screen (MainScreen) with a fade-in transition.
     */
    public static void loadMainScene() {
        applyTransition(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/MainScreen.fxml"));
                Parent root = loader.load();
                MainScreenController controller = loader.getController();
                controller.init();
                return root;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Loads the Deck selection screen (DeckScreen) with a fade-in transition.
     */
    public static void loadDeckScene() {
        applyTransition(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/DeckScreen.fxml"));
                Parent root = loader.load();
                DeckScreenController controller = loader.getController();
                controller.init();
                return root;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Loads the Story mode screen (StoryScreen) with a fade-in transition.
     */
    public static void loadStoryScene() {
        applyTransition(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/StoryScreen.fxml"));
                Parent root = loader.load();
                StoryScreenController controller = loader.getController();
                controller.init();
                return root;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Returns the primary stage of the application.
     *
     * @return The primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * The main method, launching the JavaFX application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}