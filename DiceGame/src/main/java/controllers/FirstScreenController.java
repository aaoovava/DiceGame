package controllers;

import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;
import services.PlayerService;
import view.applications.MainApplication;

/**
 * Controller class for the first screen of the dice game application.
 * This screen shows a blinking text that prompts the user to press any key or mouse button to start the game.
 * It handles the transition from the first screen to the main game screen upon user interaction.
 */
public class FirstScreenController implements Controller {

    @FXML private Text pressText; // Text element that displays the prompt
    private FadeTransition fadeTransition; // Animation for blinking text
    private EventHandler<KeyEvent> keyEventHandler; // Event handler for key press events
    private EventHandler<MouseEvent> mouseEventHandler; // Event handler for mouse click events

    private PlayerService playerService = PlayerService.getInstance(); // Singleton instance of PlayerService

    /**
     * Initializes the controller class by starting the blinking animation
     * and setting up the event handlers for key and mouse events.
     */
    @FXML
    public void init() {
        startBlinkingAnimation(); // Start the blinking text animation
        setupEventHandlers(); // Set up the event handlers for user input
    }

    /**
     * Starts the blinking animation for the text "Press any key to start".
     * The text fades in and out in a loop to attract the user's attention.
     */
    private void startBlinkingAnimation() {
        fadeTransition = new FadeTransition(Duration.seconds(1.3), pressText);
        fadeTransition.setFromValue(1.0); // Full opacity
        fadeTransition.setToValue(0.2); // Low opacity
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE); // Repeat indefinitely
        fadeTransition.setAutoReverse(true); // Reverse the animation (fade in/out)
        fadeTransition.play(); // Start the animation
    }

    /**
     * Sets up event handlers for key and mouse input.
     * The event handlers transition to the main scene when a key or mouse click is detected.
     */
    public void setupEventHandlers() {
        keyEventHandler = this::handleKeyEvent; // Handle key press event
        mouseEventHandler = this::handleMouseEvent; // Handle mouse press event

        // Add event listeners for key and mouse events to the primary scene
        MainApplication.getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        MainApplication.getPrimaryStage().getScene().addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
    }

    /**
     * Handles the key press event. It triggers the transition to the main scene.
     *
     * @param event The KeyEvent representing the key press.
     */
    private void handleKeyEvent(KeyEvent event) {
        transitionToMain(); // Transition to the main scene
    }

    /**
     * Handles the mouse press event. It triggers the transition to the main scene.
     *
     * @param event The MouseEvent representing the mouse click.
     */
    private void handleMouseEvent(MouseEvent event) {
        transitionToMain(); // Transition to the main scene
    }

    /**
     * Transitions to the main scene of the application.
     * It stops the blinking animation and removes event handlers before loading the main scene.
     */
    private void transitionToMain() {
        removeEventHandlers(); // Remove event handlers to prevent further interactions
        fadeTransition.stop(); // Stop the blinking animation
        MainApplication.loadMainScene(); // Load the main game scene
    }

    /**
     * Removes the event handlers for key and mouse input.
     * This prevents further input handling after the user has interacted with the first screen.
     */
    public void removeEventHandlers() {
        MainApplication.getPrimaryStage().getScene().removeEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        MainApplication.getPrimaryStage().getScene().removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
    }
}