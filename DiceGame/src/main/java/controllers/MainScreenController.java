package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.records.dice.Dice;
import model.records.dice.RegularDice;
import services.PlayerService;
import view.applications.MainApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for the main screen of the dice game application.
 * This screen includes elements like the player's name, balance, settings, and buttons to navigate to different scenes.
 */
public class MainScreenController implements Controller {

    @FXML private Text deckButton; // Button to navigate to the deck scene
    @FXML private Text balance; // Text displaying the player's balance
    @FXML private Text menuPlayerText; // Text displaying the player's name in the menu
    @FXML private Button saveNameButton; // Button to save the player's name
    @FXML private TextField nameTextField; // TextField for entering the player's name
    @FXML private VBox settingBox; // VBox containing the settings options
    @FXML private Text storyButton; // Button to navigate to the story scene
    @FXML private ImageView setName; // ImageView for the settings icon
    private EventHandler<MouseEvent> playClickHandler; // Event handler for clicking the play button
    private EventHandler<MouseEvent> settingHoverHandler; // Event handler for hover over the settings icon
    private EventHandler<MouseEvent> settingExitHandler; // Event handler for exit hover over the settings icon
    private EventHandler<MouseEvent> deckClickHandler; // Event handler for clicking the deck button
    private EventHandler<MouseEvent> settingsClickHandler; // Event handler for clicking the settings button

    private boolean isSettingVisible = false; // Flag to track visibility of the settings

    private PlayerService playerService = PlayerService.getInstance(); // Singleton instance of PlayerService

    static List<Dice> deck = new ArrayList<>(); // Deck of dice

    /**
     * Initializes the controller class, setting up the player's name, balance, and event handlers.
     */
    @FXML
    public void init() {
        menuPlayerText.setText(playerService.getPlayer().getName()); // Set player's name in menu
        nameTextField.setText(playerService.getPlayer().getName()); // Set player's name in the text field
        balance.setText(String.valueOf(playerService.getPlayer().getBalance())); // Set player's balance
        setupEventHandlers(); // Set up event handlers for user interactions
    }

    /**
     * Static block that initializes the deck of dice with six regular dice.
     */
    static {
        for (int i = 0; i < 6; i++) {
            deck.add(new RegularDice(1)); // Add regular dice to the deck
        }
    }

    /**
     * Sets up event handlers for user interactions, including button clicks and mouse events.
     */
    public void setupEventHandlers() {
        // Story button click handler
        playClickHandler = event -> {
            removeEventHandlers(); // Remove previous event handlers
            MainApplication.loadStoryScene(); // Load the story scene
        };

        // Deck button click handler
        deckClickHandler = event -> {
            removeEventHandlers(); // Remove previous event handlers
            MainApplication.loadDeckScene(); // Load the deck scene
        };

        deckButton.setOnMouseClicked(deckClickHandler); // Add click event handler to deck button
        storyButton.setOnMouseClicked(playClickHandler); // Add click event handler to story button

        // Settings icon rotation handlers
        settingHoverHandler = event -> {
            RotateTransition rotateIn = new RotateTransition(Duration.millis(200), setName);
            rotateIn.setToAngle(90); // Rotate the icon when hovered
            rotateIn.play(); // Play the rotation animation
        };

        settingExitHandler = event -> {
            RotateTransition rotateOut = new RotateTransition(Duration.millis(200), setName);
            rotateOut.setToAngle(0); // Reset the rotation when the mouse exits
            rotateOut.play(); // Play the reset rotation animation
        };

        // Initialize settings box as invisible
        settingBox.setVisible(false);
        settingBox.setManaged(false);

        setName.setOnMouseEntered(settingHoverHandler); // Add hover handler for settings icon
        setName.setOnMouseExited(settingExitHandler); // Add exit hover handler for settings icon
        setName.setOnMouseClicked(settingsClick()); // Add click handler for settings icon

        // Save button handler
        saveNameButton.setOnAction(event -> saveName()); // Save the player's name when the save button is clicked
        MainApplication.getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent); // Add key event handler
    }

    /**
     * Saves the player's name and updates the menu player text.
     */
    private void saveName() {
        String name = nameTextField.getText(); // Get the name from the text field
        playerService.setName(name); // Set the player's name in the player service
        menuPlayerText.setText(name); // Update the player's name in the menu
        animateVisibility(); // Toggle the visibility of the settings
    }

    /**
     * Handles key events, specifically the Enter key to save the name.
     *
     * @param event The KeyEvent representing the key press.
     */
    private void handleKeyEvent(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) { // If the Enter key is pressed
            saveName(); // Save the player's name
        }
    }

    /**
     * Returns an event handler for clicking the settings icon.
     * Toggles the visibility of the settings box when clicked.
     *
     * @return The event handler for the settings icon click.
     */
    private EventHandler<MouseEvent> settingsClick() {
        return event -> {
            animateVisibility(); // Toggle the visibility of the settings
        };
    }

    /**
     * Toggles the visibility of the settings box and applies animations for smooth transitions.
     */
    private void animateVisibility() {
        isSettingVisible = !isSettingVisible; // Toggle the visibility state
        settingBox.setVisible(isSettingVisible); // Set the visibility of the settings box
        settingBox.setManaged(isSettingVisible); // This affects layout management

        // Optional fade-in and fade-out animation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), settingBox);
        fadeTransition.setFromValue(isSettingVisible ? 0 : 1); // Set the starting opacity
        fadeTransition.setToValue(isSettingVisible ? 1 : 0); // Set the target opacity

        // Optional slide-in and slide-out animation
        TranslateTransition slideTransition = new TranslateTransition(Duration.millis(200), settingBox);
        slideTransition.setFromX(isSettingVisible ? -20 : 0); // Set the starting X position
        slideTransition.setToX(isSettingVisible ? 0 : -20); // Set the target X position

        // Play both animations sequentially
        SequentialTransition sequence = new SequentialTransition(fadeTransition, slideTransition);
        sequence.play(); // Play the animations
    }

    /**
     * Removes event handlers for buttons and other interactive elements.
     * This is called when transitioning between scenes or cleaning up event listeners.
     */
    public void removeEventHandlers() {
        storyButton.setOnMouseClicked(null); // Remove click handler from story button
        setName.setOnMouseEntered(null); // Remove hover handler from settings icon
        setName.setOnMouseExited(null); // Remove exit hover handler from settings icon
        deckButton.setOnMouseClicked(null); // Remove click handler from deck button
        MainApplication.getPrimaryStage().getScene().removeEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent); // Remove key event handler
    }
}