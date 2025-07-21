package controllers;

import exceptions.SomeGameFieldsMissing;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Game;
import model.records.npc.NPC;
import services.NPCService;
import services.PlayerService;
import view.applications.MainApplication;

import java.util.Objects;

/**
 * Controller class for the story screen in the game.
 * Handles the interaction with the player, including character selection,
 * bet management, and transitioning to the game scene.
 */
public class StoryScreenController implements Controller {

    private static final int ANIMATION_DURATION = 250;

    @FXML private Text balance;
    @FXML private ImageView plus;
    @FXML private Text betText;
    @FXML private ImageView minus;
    @FXML private Text storyButton;
    @FXML private ImageView characterImageView;
    @FXML private Text samButton;
    @FXML private Text jebButton;
    @FXML private Text storyText1;
    @FXML private Text storyText2;
    @FXML private ImageView backButton;
    @FXML private HBox gambleButton;

    private int gameBet = 0;
    private int playerBalance = 0;

    // Handlers for mouse events
    private EventHandler<MouseEvent> SamClickHandler;
    private EventHandler<MouseEvent> JebClickHandler;
    private EventHandler<MouseEvent> SamEnterHandler;
    private EventHandler<MouseEvent> JebEnterHandler;
    private EventHandler<MouseEvent> backButtonClickHandler;
    private EventHandler<MouseEvent> backButtonEnterHandler;
    private EventHandler<MouseEvent> backButtonExitHandler;
    private EventHandler<MouseEvent> gambleButtonClickHandler;
    private EventHandler<MouseEvent> leftButtonClickHandler;
    private EventHandler<MouseEvent> leftButtonEnterHandler;
    private EventHandler<MouseEvent> leftButtonExitHandler;
    private EventHandler<MouseEvent> rightButtonClickHandler;
    private EventHandler<MouseEvent> rightButtonEnterHandler;
    private EventHandler<MouseEvent> rightButtonExitHandler;

    private boolean isSamSelected = false;
    private boolean isJebSelected = true;

    private Image backButtonNormalImage;
    private Image backButtonHoverImage;
    private Image leftButtonNormalImage;
    private Image leftButtonHoverImage;
    private Image rightButtonNormalImage;
    private Image rightButtonHoverImage;

    private NPCService npcService = NPCService.getInstance();
    private PlayerService playerService = PlayerService.getInstance();

    /**
     * Initializes the story screen, setting up player balance, bet values,
     * and event handlers for UI interactions.
     */
    public void init() {
        playerBalance = playerService.getPlayer().getBalance();
        gameBet = isJebSelected ? npcService.getJeb().getMinBet() : npcService.getSam().getMinBet();

        balance.setText(String.valueOf(playerBalance));
        betText.setText(String.valueOf(gameBet));

        backButtonNormalImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("back.png")));
        backButtonHoverImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("backHover.png")));

        leftButtonNormalImage = new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/img/arrows/left.png")));
        leftButtonHoverImage = new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/img/arrows/left_hover.png")));
        rightButtonNormalImage = new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/img/arrows/right.png")));
        rightButtonHoverImage = new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/img/arrows/right_hover.png")));

        backButton.setImage(backButtonNormalImage);

        createHandlers();
        setupEventHandlers();
        handleJebClick();
    }

    /**
     * Creates animations for button hover effects.
     *
     * @param button The ImageView button to animate.
     * @param scale The scale to which the button will be animated.
     * @return A ParallelTransition containing the animation.
     */
    private ParallelTransition animateButtonHover(ImageView button, double scale) {
        ScaleTransition scaleAnim = new ScaleTransition(Duration.millis(ANIMATION_DURATION), button);
        scaleAnim.setToX(scale);
        scaleAnim.setToY(scale);

        FadeTransition fadeAnim = new FadeTransition(Duration.millis(ANIMATION_DURATION), button);
        fadeAnim.setToValue(scale > 1.0 ? 0.9 : 1.0);

        ParallelTransition parallel = new ParallelTransition(scaleAnim, fadeAnim);
        parallel.play();
        return parallel;
    }

    /**
     * Creates all event handlers for mouse interactions with the UI elements.
     */
    private void createHandlers() {
        SamClickHandler = event -> handleSamClick();
        JebClickHandler = event -> handleJebClick();

        SamEnterHandler = event -> {
            if (!isSamSelected) {
                samButton.setCursor(Cursor.HAND);
            }
        };

        JebEnterHandler = event -> {
            if (!isJebSelected) {
                jebButton.setCursor(Cursor.HAND);
            }
        };

        backButtonClickHandler = event -> handleBackButtonClick();

        backButtonEnterHandler = event -> {
            backButton.setImage(backButtonHoverImage);
            backButton.setCursor(Cursor.HAND);
        };

        backButtonExitHandler = event -> {
            backButton.setImage(backButtonNormalImage);
            backButton.setCursor(Cursor.DEFAULT);
        };

        gambleButtonClickHandler = event -> {
            try {
                handleGambleButtonClick();
            } catch (SomeGameFieldsMissing e) {
                removeEventHandlers();
                MainApplication.loadStoryScene();
            }
        };
    }

    /**
     * Sets up all event handlers for UI elements such as buttons.
     */
    public void setupEventHandlers() {
        samButton.setOnMouseClicked(SamClickHandler);
        samButton.setOnMouseEntered(SamEnterHandler);
        jebButton.setOnMouseClicked(JebClickHandler);
        jebButton.setOnMouseEntered(JebEnterHandler);

        backButton.setOnMouseClicked(backButtonClickHandler);
        backButton.setOnMouseEntered(backButtonEnterHandler);
        backButton.setOnMouseExited(backButtonExitHandler);

        // Left/Right buttons event handlers
        leftButtonEnterHandler = event -> {
            minus.setImage(leftButtonHoverImage);
            minus.setCursor(Cursor.HAND);
            animateButtonHover(minus, 1.1);
        };

        leftButtonExitHandler = event -> {
            minus.setImage(leftButtonNormalImage);
            minus.setCursor(Cursor.DEFAULT);
            animateButtonHover(minus, 1.0);
        };

        rightButtonEnterHandler = event -> {
            plus.setImage(rightButtonHoverImage);
            plus.setCursor(Cursor.HAND);
            animateButtonHover(plus, 1.1);
        };

        rightButtonExitHandler = event -> {
            plus.setImage(rightButtonNormalImage);
            plus.setCursor(Cursor.DEFAULT);
            animateButtonHover(plus, 1.0);
        };

        rightButtonClickHandler = event -> {
            animateButtonClick(plus);
            NPC npc = isJebSelected ? npcService.getJeb() : npcService.getSam();
            if (npc.getMaxBet() >= gameBet + 5 && playerBalance >= gameBet + 5) {
                gameBet += 5;
                betText.setText(String.valueOf(gameBet));
            }
        };

        leftButtonClickHandler = event -> {
            animateButtonClick(minus);
            NPC npc = isJebSelected ? npcService.getJeb() : npcService.getSam();
            if (gameBet > 0 && npc.getMinBet() <= gameBet - 5) {
                gameBet -= 5;
                betText.setText(String.valueOf(gameBet));
            }
        };

        gambleButton.setOnMouseClicked(gambleButtonClickHandler);
        minus.setOnMouseEntered(leftButtonEnterHandler);
        minus.setOnMouseExited(leftButtonExitHandler);
        plus.setOnMouseEntered(rightButtonEnterHandler);
        plus.setOnMouseExited(rightButtonExitHandler);
        minus.setOnMouseClicked(leftButtonClickHandler);
        plus.setOnMouseClicked(rightButtonClickHandler);
    }

    /**
     * Animates the button click effect.
     *
     * @param button The button to animate.
     * @return A SequentialTransition containing the animation.
     */
    private SequentialTransition animateButtonClick(ImageView button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), button);
        scaleDown.setToX(0.9);
        scaleDown.setToY(0.9);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), button);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        SequentialTransition seq = new SequentialTransition(scaleDown, scaleUp);
        seq.play();
        return seq;
    }

    /**
     * Handles the gamble button click event.
     *
     * @throws SomeGameFieldsMissing If necessary fields for the game are missing.
     */
    private void handleGambleButtonClick() throws SomeGameFieldsMissing {
        removeEventHandlers();
        if (isSamSelected)
            gambleWithSam();
        else
            gambleWithJeb();
    }

    /**
     * Handles the back button click event.
     */
    private void handleBackButtonClick() {
        removeEventHandlers();
        MainApplication.loadMainScene();
    }

    /**
     * Handles the Sam button click event, selecting Sam as the character.
     */
    private void handleSamClick() {
        if (isSamSelected) return;

        gameBet = npcService.getSam().getMinBet();
        betText.setText(String.valueOf(gameBet));

        isSamSelected = true;
        isJebSelected = false;

        samButton.setFill(Color.web("#39596D"));
        jebButton.setFill(Color.web("#6C4A4A"));

        samButton.setCursor(Cursor.DEFAULT);

        characterImageView.setImage(
                new Image(Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("Sam.png")
                )));

        storyText1.setText("\"What? Me? Naw, I’d never… unless the opportunity arises.\"");
        storyText2.setText("A grinning rogue who palms dice, \"accidentally\" miscounts, and always has an excuse.");
    }

    /**
     * Handles the Jeb button click event, selecting Jeb as the character.
     */
    private void handleJebClick() {
        if (isJebSelected) return;

        gameBet = npcService.getJeb().getMinBet();
        betText.setText(String.valueOf(gameBet));

        isJebSelected = true;
        isSamSelected = false;

        jebButton.setFill(Color.web("#39596D"));
        samButton.setFill(Color.web("#6C4A4A"));

        jebButton.setCursor(Cursor.DEFAULT);

        characterImageView.setImage(
                new Image(Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("Jeb.png")
                )));

        storyText1.setText("\"Back in my day, we rolled bones, not fancy dice!\"");
        storyText2.setText("A wrinkled, tobacco-chewing farmer who’s been playing dice since before the others were born.");
    }

    /**
     * Removes all event handlers from UI elements.
     */
    public void removeEventHandlers() {
        samButton.setOnMouseClicked(null);
        samButton.setOnMouseEntered(null);
        jebButton.setOnMouseClicked(null);
        jebButton.setOnMouseEntered(null);

        backButton.setOnMouseClicked(null);
        backButton.setOnMouseEntered(null);
        backButton.setOnMouseExited(null);
    }

    /**
     * Initiates the game with Jeb.
     *
     * @throws SomeGameFieldsMissing If necessary fields for the game are missing.
     */
    private void gambleWithJeb() throws SomeGameFieldsMissing {
        Game.getInstance().setUpGame(NPCService.getInstance().getJeb(), gameBet);
        MainApplication.loadGameScene();
    }

    /**
     * Initiates the game with Sam.
     *
     * @throws SomeGameFieldsMissing If necessary fields for the game are missing.
     */
    private void gambleWithSam() throws SomeGameFieldsMissing {
        Game.getInstance().setUpGame(NPCService.getInstance().getSam(), gameBet);
        MainApplication.loadGameScene();
    }
}