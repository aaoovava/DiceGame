package controllers;

import exceptions.SomeGameFieldsMissing;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Game;
import model.observers.GameObserver;
import model.records.Turn;
import model.records.dice.Dice;
import model.records.dice.DiceDeck;
import model.records.dice.RoyalDice;
import model.records.npc.NPC;
import model.records.npc.Player;
import services.ScoreCalculatorService;
import view.applications.MainApplication;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Controller class for managing the game screen UI and interactions.
 * Implements GameObserver to receive game state updates.
 */
public class GameScreenController implements Controller, GameObserver {
    @FXML
    private Text turnScore;

    @FXML
    private Text betOfTheGame;
    @FXML
    private Text winText;
    @FXML
    private StackPane messageContainer;
    @FXML
    private Text bustedText;
    @FXML
    private GridPane selectedDiceGrid;
    @FXML
    private Pane diceContainer;
    @FXML
    private Text npcName;
    @FXML
    private Text playerName;
    @FXML
    private Text playerCounter;
    @FXML
    private Text npcCounter;
    @FXML
    private ImageView backButton;

    // Constants for UI configuration
    private static final int DICE_SIZE = 60;
    private static final int MAX_ROTATION = 45;
    private static final int CONTAINER_PADDING = 30;

    private int countOFDiceNpc = 6;

    // UI assets
    private Image backButtonNormalImage;
    private Image backButtonHoverImage;

    // Event handlers
    private EventHandler<MouseEvent> backButtonClickHandler;
    private EventHandler<MouseEvent> backButtonEnterHandler;
    private EventHandler<MouseEvent> backButtonExitHandler;

    // Dice management collections
    private List<ImageView> displayedDiceViews = new ArrayList<>();

    private List<Dice> npcSelectedDice = new CopyOnWriteArrayList<>();
    private List<ImageView> selectedDiceViews = new CopyOnWriteArrayList<>();
    private Map<Dice, ImageView> diceToImageViewMap = new ConcurrentHashMap<>();
    private Map<ImageView, Dice> imageViewToDiceMap = new ConcurrentHashMap<>();

    private List<Dice> listOfNpcPickedDiceForCounting = new ArrayList<>();

    private int previousScoreNpc = 0;
    private int currentScoreNpc = 0;

    private boolean isNpcScored = false;

    // Game state
    private Game game = Game.getInstance();
    private List<Dice> displayedDice;
    private List<Dice> pickedDice = new ArrayList<>();
    private Boolean needToRoll = false;
    private boolean[][] gridOccupied = new boolean[10][2];
    private NPC npc = game.getNpc();
    private Turn npcLastTurn;

    private Timeline npcAnimationTimeline;
    private int currentNPCMoveStep = 0;

    ScoreCalculatorService scoreCalculatorService = ScoreCalculatorService.getInstance();

    private int currentPlayerTurnScoreValue = 0;
    private int pickedScoreValue;

    private boolean isProcessingAction = false; //for key press spam defense

    private boolean isClickAllowed = true; //for dice click spam defense


    /**
     * Initializes the controller, setting up UI elements and event handlers.
     *
     * @throws SomeGameFieldsMissing if required game fields are not set
     */
    @FXML
    public void init() throws SomeGameFieldsMissing {
        npc = game.getNpc();
        setupEventHandlers();

        backButtonNormalImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("back.png")));
        backButtonHoverImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("backHover.png")));

        npcName.setText(game.getNpc().getName());
        playerName.setText(game.getPlayer().getName());

        npcCounter.setText(String.valueOf(game.getNpcScore()));
        playerCounter.setText(String.valueOf(game.getPlayerScore()));
        betOfTheGame.setText(String.valueOf(game.getGameBet()));

        game.setGameObserver(this);
        showWhoseTurn();
        turnScore.setText(game.getPlayer().getName() + " : " + scoreCalculatorService.calculateScore(pickedDice));
        messageContainer.toFront();
    }

    /**
     * Sets up all event handlers for UI interactions.
     */
    public void setupEventHandlers() {
        backButtonClickHandler = (MouseEvent event) -> handleBackButtonClick();

        backButtonEnterHandler = event -> {
            backButton.setImage(backButtonHoverImage);
            backButton.setCursor(Cursor.HAND);
        };

        backButtonExitHandler = event -> {
            backButton.setImage(backButtonNormalImage);
            backButton.setCursor(Cursor.DEFAULT);
        };

        backButton.setOnMouseClicked(backButtonClickHandler);
        backButton.setOnMouseEntered(backButtonEnterHandler);
        backButton.setOnMouseExited(backButtonExitHandler);

        MainApplication.getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
    }

    /**
     * Shows busted animation with callback
     *
     * @param afterAnimation Runnable to execute after animation completes
     */
    public void showBustedAnimation(Runnable afterAnimation) {
        messageContainer.toFront();
        if (bustedText == null) {
            bustedText = new Text("BUSTED!");
            bustedText.getStyleClass().add("busted-text");
            messageContainer.getChildren().add(bustedText);
        }

        bustedText.setText("BUSTED!");

        resetAnimations(bustedText);

        messageContainer.setVisible(true);
        bustedText.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), bustedText);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), bustedText);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(1));

        // Создаем SequentialTransition и добавляем анимации
        SequentialTransition sequence = new SequentialTransition(
                fadeIn,
                fadeOut
        );

        sequence.setOnFinished(e -> {
            messageContainer.setVisible(false);
            bustedText.setVisible(false);
            if (afterAnimation != null) {
                afterAnimation.run();
            }
        });

        sequence.play();
    }

    /**
     * Resets the busted text animation properties to initial values.
     */
    private void resetAnimations( Text text) {
        text.setOpacity(0);
        text.setScaleX(1);
        text.setScaleY(1);
        text.setTranslateX(0);
    }

    /**
     * Handles key press events for player actions.
     *
     * @param t the event to handle
     */
    private <T extends Event> void handleKeyEvent(T t) {
        if (game.getCurrentTurn() == 0 && t instanceof KeyEvent && !isProcessingAction) {
            KeyEvent event = (KeyEvent) t;

            if ((event.getCode() == KeyCode.Q || event.getCode() == KeyCode.E)
                    && scoreCalculatorService.calculateScore(pickedDice) > 0) {

                isProcessingAction = true;

                if (event.getCode() == KeyCode.Q) {
                    playerPass();
                } else {
                    playerScoreAndContinue();
                }

                // Re-enable after animation/action completes
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> isProcessingAction = false);
                    }
                }, 500); // Match this with your action duration
            }
        }
    }


    private void busted() {
        previousScoreNpc = 0;
        listOfNpcPickedDiceForCounting.clear();
        showBustedAnimation(() -> {
            Platform.runLater(() -> {
                resetNPCSelectionState();
                game.endTurn();
            });
        });
    }

    /**
     * Scores the current selection and continues the game by rolling remaining dice.
     */
    private void playerScoreAndContinue() {
        for (Dice dice : pickedDice) {
            displayedDice.remove(dice);
        }

        if (displayedDice.isEmpty()) {
            displayedDice = new ArrayList<>(game.getRolledDice());
        }

        DiceDeck partOfDeck = new DiceDeck(displayedDice);
        partOfDeck.roll();

        diceToImageViewMap.clear();
        imageViewToDiceMap.clear();
        pickedDice.clear();
        currentPlayerTurnScoreValue = pickedScoreValue;
        turnScore.setText(game.getPlayer().getName() + ": " + currentPlayerTurnScoreValue);

        throwDice(displayedDice);
    }

    /**
     * Passes the turn, adds the current score to the player, and ends the turn.
     */
    private void playerPass() {
        game.setPlayerScore(game.getPlayerScore() + pickedScoreValue);
        resetNPCSelectionState();
        game.endTurn();
    }

    /**
     * Handles the back button click event by returning to the story scene.
     */
    private void handleBackButtonClick() {
        removeEventHandlers();
        game.cancelGame();
        MainApplication.loadStoryScene();
    }

    /**
     * Initiates a dice roll and displays the results.
     */
    private void pRollDice() {
        displayedDice = new ArrayList<>(game.getRolledDice());
        resetNPCSelectionState();
        throwDice(displayedDice);
    }


    /**
     * Animates throwing dice onto the table and displays them.
     *
     * @param diceList the list of dice to display
     */
    private void throwDice(List<Dice> diceList) {
        // Reset grid occupancy
        for (int i = 0; i < gridOccupied.length; i++) {
            Arrays.fill(gridOccupied[i], false);
        }

        // Clear previous dice
        diceContainer.getChildren().clear();
        selectedDiceGrid.getChildren().clear();
        displayedDiceViews.clear();
        selectedDiceViews.clear();
        pickedDice.clear();

        // Ensure container layout is updated
        diceContainer.layout();

        Platform.runLater(() -> {
            double containerWidth = diceContainer.getWidth() - 2 * CONTAINER_PADDING;
            double containerHeight = diceContainer.getHeight() - 2 * CONTAINER_PADDING;

            for (Dice dice : diceList) {
                ImageView diceImage = createDiceImageView(dice);
                positionDice(diceImage, containerWidth, containerHeight);
                addDiceAnimation(diceImage);

                diceContainer.getChildren().add(diceImage);
                displayedDiceViews.add(diceImage);
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!scoreCalculatorService.hasAnyScoringCombination(displayedDice)) {
                    busted();
                }
            }
        }, 800);

    }

    /**
     * Animates the way NPC throws dice onto the table and displays them.
     * @param diceList
     */
    private void throwDiceNPC(List<Dice> diceList) {

        diceToImageViewMap.clear();
        imageViewToDiceMap.clear();
        // Reset grid occupancy
        for (int i = 0; i < gridOccupied.length; i++) {
            Arrays.fill(gridOccupied[i], false);
        }

        // Clear previous dice
        diceContainer.getChildren().clear();
        selectedDiceGrid.getChildren().clear();
        displayedDiceViews.clear();
        selectedDiceViews.clear();
        pickedDice.clear();

        // Ensure container layout is updated
        diceContainer.layout();

        Platform.runLater(() -> {
            double containerWidth = diceContainer.getWidth() - 2 * CONTAINER_PADDING;
            double containerHeight = diceContainer.getHeight() - 2 * CONTAINER_PADDING;

            for (Dice dice : diceList) {
                ImageView diceImage = createDiceImageView(dice);
                positionDice(diceImage, containerWidth, containerHeight);
                addDiceAnimation(diceImage);

                diceContainer.getChildren().add(diceImage);
                displayedDiceViews.add(diceImage);
            }
        });
    }

    /**
     * Creates an ImageView for a dice with appropriate settings and event handlers.
     *
     * @param dice the dice to create an ImageView for
     * @return the configured ImageView
     */
    private ImageView createDiceImageView(Dice dice) {
        Image image = new Image(Objects.requireNonNull(
                MainApplication.class.getResourceAsStream(dice.returnImageName())
        ));

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(DICE_SIZE);
        imageView.setFitHeight(DICE_SIZE);
        imageView.setPreserveRatio(true);
        imageView.setRotate(ThreadLocalRandom.current().nextInt(-MAX_ROTATION, MAX_ROTATION));

        if (game.getCurrentTurn() == 0) {
            imageView.getStyleClass().add("clickable");
        }

        // Maintain mappings between dice and their views
        diceToImageViewMap.put(dice, imageView);
        imageViewToDiceMap.put(imageView, dice);

        // Set up click handler with animation

        if (game.getCurrentTurn() == 0) {
            imageView.setOnMouseClicked(event -> {
                ScaleTransition clickAnim = new ScaleTransition(Duration.millis(100), imageView);
                clickAnim.setToX(1.1);
                clickAnim.setToY(1.1);
                clickAnim.setAutoReverse(true);
                clickAnim.setCycleCount(2);
                clickAnim.setOnFinished(e -> handleDiceClick(dice, imageView));
                clickAnim.play();
            });
        }

        return imageView;
    }

    /**
     * Positions a dice ImageView randomly within the container while avoiding overlaps.
     *
     * @param dice the ImageView to position
     * @param maxX the maximum x-coordinate
     * @param maxY the maximum y-coordinate
     */
    private void positionDice(ImageView dice, double maxX, double maxY) {
        boolean positionFound = false;
        int attempts = 0;

        // Center dice if container is too small
        if (maxX <= DICE_SIZE || maxY <= DICE_SIZE) {
            dice.setLayoutX(maxX / 2 - DICE_SIZE / 2);
            dice.setLayoutY(maxY / 2 - DICE_SIZE / 2);
            return;
        }

        while (!positionFound && attempts < 100) {
            double minX = CONTAINER_PADDING;
            double minY = CONTAINER_PADDING;
            double availableWidth = maxX - DICE_SIZE - CONTAINER_PADDING;
            double availableHeight = maxY - DICE_SIZE - CONTAINER_PADDING;

            if (availableWidth <= 0 || availableHeight <= 0) {
                dice.setLayoutX(minX);
                dice.setLayoutY(minY);
                return;
            }

            double x = ThreadLocalRandom.current().nextDouble(minX, availableWidth);
            double y = ThreadLocalRandom.current().nextDouble(minY, availableHeight);

            Bounds newBounds = new BoundingBox(x, y, DICE_SIZE, DICE_SIZE);

            if (displayedDiceViews.stream().noneMatch(view -> {
                Bounds existingBounds = view.getBoundsInParent();
                return existingBounds.intersects(newBounds);
            })) {
                dice.setLayoutX(x);
                dice.setLayoutY(y);
                positionFound = true;
            }
            attempts++;
        }

        // Fallback positioning
        if (!positionFound) {
            double x = ThreadLocalRandom.current().nextDouble(CONTAINER_PADDING, maxX - DICE_SIZE);
            double y = ThreadLocalRandom.current().nextDouble(CONTAINER_PADDING, maxY - DICE_SIZE);
            dice.setLayoutX(Math.max(CONTAINER_PADDING, x));
            dice.setLayoutY(Math.max(CONTAINER_PADDING, y));
        }
    }

    /**
     * Adds animation effects to a dice when it first appears.
     *
     * @param dice the ImageView to animate
     */
    private void addDiceAnimation(ImageView dice) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(800), dice);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(850), dice);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(true);

        ParallelTransition parallelTransition = new ParallelTransition(
                fadeTransition,
                rotateTransition
        );

        TranslateTransition bounceTransition = new TranslateTransition(Duration.millis(300), dice);
        bounceTransition.setFromY(-20);
        bounceTransition.setToY(0);

        SequentialTransition sequentialTransition = new SequentialTransition(
                parallelTransition,
                bounceTransition
        );

        sequentialTransition.play();
    }

    /**
     * Handles click events on dice, toggling them between table and selection grid.
     *
     * @param dice     the clicked dice
     * @param diceView the ImageView representing the dice
     */
    private void handleDiceClick(Dice dice, ImageView diceView) {
        if (!isClickAllowed) return;

        // Immediately disable further clicks
        isClickAllowed = false;

        // Re-enable clicks after 500ms
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> isClickAllowed = true);
            }
        }, 300);

        // Original click handling logic
        if (diceContainer.getChildren().contains(diceView)) {
            moveDiceToGrid(dice, diceView);
        } else {
            if (dice instanceof RoyalDice) {
                if (((RoyalDice) dice).riskNumberDropped()) {
                    List<Dice> tempListOfDices = new ArrayList<>(pickedDice);
                    tempListOfDices.remove(dice);
                    int temp = scoreCalculatorService.calculateScore(tempListOfDices);
                    pickedScoreValue = temp;
                }
            }
            moveDiceToTable(dice, diceView);
        }

        System.out.println("Picked dice: ");
        for (Dice d : pickedDice) {
            System.out.println(d.getCurrentSide() + " ");
        }
        pickedScoreValue = currentPlayerTurnScoreValue + scoreCalculatorService.calculateScore(pickedDice);

        if (dice instanceof RoyalDice && pickedDice.contains(dice)) {
            if (((RoyalDice) dice).riskNumberDropped()) {
                pickedScoreValue += scoreCalculatorService.calculateScore(pickedDice) * 2 - scoreCalculatorService.calculateScore(pickedDice);
            }
        }

        turnScore.setText(game.getPlayer().getName() + ": " + pickedScoreValue);
    }

    /**
     * Moves a dice from the table to the selection grid with animation.
     *
     * @param dice     the dice to move
     * @param diceView the ImageView representing the dice
     */
    private void moveDiceToGrid(Dice dice, ImageView diceView) {
        RotateTransition resetRotation = new RotateTransition(Duration.millis(200), diceView);
        resetRotation.setToAngle(0);

        int[] freeCell = findFreeGridCell();
        int row = freeCell[0];
        int col = freeCell[1];

        if (row == -1) {
            System.out.println("No free cells in grid!");
            return;
        }

        gridOccupied[row][col] = true;

        double targetX = col * (DICE_SIZE + 5) + (DICE_SIZE / 2);
        double targetY = row * (DICE_SIZE + 10) + (DICE_SIZE / 2);

        Bounds containerBounds = diceView.localToScene(diceView.getBoundsInLocal());
        Bounds gridLocalBounds = selectedDiceGrid.sceneToLocal(containerBounds);

        TranslateTransition moveTransition = new TranslateTransition(Duration.millis(300), diceView);
        moveTransition.setByX(targetX - gridLocalBounds.getMinX() - DICE_SIZE / 2);
        moveTransition.setByY(targetY - gridLocalBounds.getMinY() - DICE_SIZE / 2);

        diceView.setUserData(new int[]{row, col});

        SequentialTransition sequence = new SequentialTransition(resetRotation, moveTransition);

        sequence.setOnFinished(e -> {
            diceContainer.getChildren().remove(diceView);
            Platform.runLater(() -> {
                selectedDiceGrid.add(diceView, col, row);
                List<Node> children = new ArrayList<>(selectedDiceGrid.getChildren());
                for (Node node : children) {
                    node.toBack();
                }
            });

            selectedDiceViews.add(diceView);
            diceView.setTranslateX(0);
            diceView.setTranslateY(0);
            diceView.setRotate(0);
        });

        sequence.play();
        pickedDice.add(dice);
    }

    /**
     * Moves a dice from the selection grid back to the table with animation.
     *
     * @param dice     the dice to move
     * @param diceView the ImageView representing the dice
     */
    private void moveDiceToTable(Dice dice, ImageView diceView) {
        if (diceView.getUserData() != null) {
            int[] pos = (int[]) diceView.getUserData();
            gridOccupied[pos[0]][pos[1]] = false;
        }


        selectedDiceGrid.getChildren().remove(diceView);
        selectedDiceViews.remove(diceView);
        pickedDice.remove(dice);

        diceContainer.getChildren().add(diceView);

        Bounds diceSceneBounds = diceView.localToScene(diceView.getBoundsInLocal());
        Bounds containerSceneBounds = diceContainer.localToScene(diceContainer.getBoundsInLocal());

        double initialX = diceSceneBounds.getMinX() - containerSceneBounds.getMinX();
        double initialY = diceSceneBounds.getMinY() - containerSceneBounds.getMinY();

        diceView.setLayoutX(initialX);
        diceView.setLayoutY(initialY);
        diceView.setTranslateX(0);
        diceView.setTranslateY(0);

        Position targetPosition = findValidTablePosition();

        TranslateTransition moveTransition = new TranslateTransition(Duration.millis(300), diceView);
        moveTransition.setToX(targetPosition.x() - initialX);
        moveTransition.setToY(targetPosition.y() - initialY);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), diceView);
        rotateTransition.setToAngle(ThreadLocalRandom.current().nextInt(-MAX_ROTATION, MAX_ROTATION));

        ParallelTransition parallelTransition = new ParallelTransition(
                moveTransition,
                rotateTransition
        );

        parallelTransition.setOnFinished(e -> {
            diceView.setLayoutX(targetPosition.x());
            diceView.setLayoutY(targetPosition.y());
            diceView.setTranslateX(0);
            diceView.setTranslateY(0);
        });

        parallelTransition.play();
    }

    /**
     * Finds a valid position on the table for a dice that avoids overlaps.
     *
     * @return a valid Position object with x and y coordinates
     */
    private Position findValidTablePosition() {
        double containerWidth = diceContainer.getWidth() - 2 * CONTAINER_PADDING;
        double containerHeight = diceContainer.getHeight() - 2 * CONTAINER_PADDING;

        for (int attempt = 0; attempt < 100; attempt++) {
            double x = ThreadLocalRandom.current().nextDouble(
                    CONTAINER_PADDING,
                    containerWidth - DICE_SIZE
            );
            double y = ThreadLocalRandom.current().nextDouble(
                    CONTAINER_PADDING,
                    containerHeight - DICE_SIZE
            );

            Bounds newBounds = new BoundingBox(x, y, DICE_SIZE, DICE_SIZE);

            boolean positionValid = displayedDiceViews.stream()
                    .noneMatch(view -> view.getBoundsInParent().intersects(newBounds));

            if (positionValid) {
                return new Position(x, y);
            }
        }

        // Fallback position
        return new Position(
                ThreadLocalRandom.current().nextDouble(CONTAINER_PADDING, containerWidth - DICE_SIZE),
                ThreadLocalRandom.current().nextDouble(CONTAINER_PADDING, containerHeight - DICE_SIZE)
        );
    }

    /**
     * Finds the first available cell in the selection grid.
     *
     * @return an array with row and column indices, or [-1, -1] if no cells are available
     */
    private int[] findFreeGridCell() {
        for (int row = 0; row < gridOccupied.length; row++) {
            for (int col = 0; col < gridOccupied[row].length; col++) {
                if (!gridOccupied[row][col]) {
                    return new int[]{row, col};
                }
            }
        }
        return new int[]{-1, -1};
    }

    /**
     * Removes all event handlers to prevent memory leaks.
     */
    public void removeEventHandlers() {



        backButton.setOnMouseClicked(null);
        backButton.setOnMouseEntered(null);
        backButton.setOnMouseExited(null);
    }

    /**
     * Called when the game state changes, triggering a dice roll.
     */
    @Override
    public void playerRollDice() {
        showWhoseTurn();
        pRollDice();
        currentPlayerTurnScoreValue = 0;
    }

    /**
     * Handles NPC dice roll with animations
     */
    public void npcRollDice() {

        showWhoseTurn();

        if (!isNpcScored) {
            previousScoreNpc = 0;
        }
        npcLastTurn = npc.getLastTurn();

        resetNPCSelectionState();
        listOfNpcPickedDiceForCounting.clear();

        throwDiceNPC(npcLastTurn.getDisplayedDice());

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    npcSelectedDice = new CopyOnWriteArrayList<>(npcLastTurn.getSelectedDice());
                    if (!npcSelectedDice.isEmpty()) {
                        animateNPCSelection();
                    } else {
                        processNPCResultImmediately();
                    }
                });
            }
        }, 1500);
    }

    /**
     * Displays whose turn it is with color highlighting and updates score displays
     */
    private void showWhoseTurn() {
        int turn = game.getCurrentTurn();

        npcCounter.setText(String.valueOf(game.getNpcScore()));
        playerCounter.setText(String.valueOf(game.getPlayerScore()));

        String selectedHex = "#FFC31D";  // Gold (selected)
        String unselectedHex = "#F9F9F9"; // White (unselected)

        // Create separate animations for each property change
        Timeline npcNameAnimation = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(npcName.fillProperty(),
                                turn == 1 ? Color.web(selectedHex) : Color.web(unselectedHex)))
        );

        Timeline playerNameAnimation = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(playerName.fillProperty(),
                                turn == 1 ? Color.web(unselectedHex) : Color.web(selectedHex)))
        );


        if (turn != 1 || !isNpcScored) {
            turnScore.setText((turn == 0) ?
                    game.getPlayer().getName() + " : 0" :
                    game.getNpc().getName() + " : 0");
        }
        // Play animations in parallel
        ParallelTransition parallelTransition = new ParallelTransition(
                npcNameAnimation,
                playerNameAnimation
        );

        parallelTransition.play();
    }

    /**
     * Initiates NPC dice selection animation sequence
     */
    private void animateNPCSelection() {
        npcAnimationTimeline = new Timeline();
        currentNPCMoveStep = 0;

        // Create animation steps for each selected dice
        for (Dice dice : npcSelectedDice) {
            ImageView diceView = diceToImageViewMap.get(dice);
            if (diceView != null) {
                npcAnimationTimeline.getKeyFrames().add(
                        new KeyFrame(
                                Duration.millis(600 * currentNPCMoveStep++),
                                e -> moveNPCdiceToGrid(dice, diceView)

                        )
                );
            }

        }

        // Final cleanup frame
        npcAnimationTimeline.getKeyFrames().add(
                new KeyFrame(
                        Duration.millis(500 * currentNPCMoveStep),
                        e -> handleNPCAfterAnimation()
                )
        );

        npcAnimationTimeline.play();
    }

    /**
     * Immediately processes NPC turn result without animations
     */
    private void processNPCResultImmediately() {
        // Handle turn outcome
        switch (npcLastTurn.getEndingOfTurn()) {
            case SCORED -> handleNPCScoredOutcome();
            case PASS -> npcPass();
            case BUSTED -> busted();
        }

        resetNPCSelectionState();
    }

    /**
     * Animates moving NPC's selected dice to the selection grid
     *
     * @param dice     The dice to move
     * @param diceView The ImageView representing the dice
     */
    private void moveNPCdiceToGrid(Dice dice, ImageView diceView) {
        Bounds originalBounds = diceView.localToScene(diceView.getBoundsInLocal());

        diceContainer.getChildren().remove(diceView);

        diceView.setTranslateX(0);
        diceView.setTranslateY(0);
        diceView.setRotate(0);

        int[] freeCell = findFreeGridCell();
        int row = freeCell[0];
        int col = freeCell[1];
        if (row == -1) return;

        selectedDiceGrid.add(diceView, col, row);
        selectedDiceGrid.layout(); // Форсируем расчет позиции

        Bounds targetBounds = diceView.localToScene(diceView.getBoundsInLocal());

        selectedDiceGrid.getChildren().remove(diceView);
        diceContainer.getChildren().add(diceView);

        diceView.setLayoutX(originalBounds.getMinX() - diceContainer.localToScene(diceContainer.getBoundsInLocal()).getMinX());
        diceView.setLayoutY(originalBounds.getMinY() - diceContainer.localToScene(diceContainer.getBoundsInLocal()).getMinY());

        TranslateTransition moveTransition = new TranslateTransition(Duration.millis(300), diceView);
        double targetX = targetBounds.getMinX() - originalBounds.getMinX();
        double targetY = targetBounds.getMinY() - originalBounds.getMinY();
        moveTransition.setByX(targetX);
        moveTransition.setByY(targetY);

        synchronized (this) {
            gridOccupied[row][col] = true;
            selectedDiceViews.add(diceView);
            npcSelectedDice.add(dice);
            listOfNpcPickedDiceForCounting.add(dice);

            currentScoreNpc = scoreCalculatorService.calculateScore(listOfNpcPickedDiceForCounting) + previousScoreNpc;
            turnScore.setText(game.getNpc().getName() + ": " + currentScoreNpc);
        }



        moveTransition.play();
    }


    /**
     * Handles cleanup and next steps after NPC's animation completes
     */
    private void handleNPCAfterAnimation() {
        // 11. Обработка исхода хода
        switch (npcLastTurn.getEndingOfTurn()) {
            case SCORED -> handleNPCScoredOutcome();
            case PASS -> npcPass();
            case BUSTED -> busted();
        }
        // 12. Полный сброс состояния
        resetNPCSelectionState();
    }

    /**
     * Processes NPC's pass action, updating scores and ending turn
     */
    private void npcPass() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    listOfNpcPickedDiceForCounting.clear();
                    isNpcScored = false;
                    game.setNpcScore(game.getNpcScore() + currentScoreNpc);
                    game.endTurn();
                });
            }
        }, 500);
    }

    /**
     * Resets the NPC's dice selection state and clears the grid
     */
    private void resetNPCSelectionState() {
        Platform.runLater(() -> {
            selectedDiceGrid.getChildren().clear();
            Arrays.stream(gridOccupied).forEach(row -> Arrays.fill(row, false));
        });

        synchronized (this) {
            npcSelectedDice.clear();
            selectedDiceViews.clear();
        }
    }

    /**
     * Handles successful NPC scoring outcome and prepares for next roll
     */
    private void handleNPCScoredOutcome() {
        List<Dice> remaining = new ArrayList<>(game.getRolledDice());
        remaining.removeAll(npcSelectedDice);

        if (remaining.isEmpty()) {
            game.setRolledDice(npc.getDiceDeck().getDeck());
        } else {
            game.setRolledDice(remaining);
        }
        new Timer().schedule(new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    listOfNpcPickedDiceForCounting.clear();
                    isNpcScored = true;
                    previousScoreNpc = currentScoreNpc;
                    DiceDeck diceDeck = new DiceDeck(game.getRolledDice());
                    diceDeck.roll();
                    game.setRolledDice(diceDeck.getDeck());
                    npc.rollDice(game.getRolledDice());
                    npcLastTurn = npc.getLastTurn();
                    npcRollDice();
                });
            }
        }, 400);
    }

    /**
     * Represents a position with x and y coordinates
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    private record Position(double x, double y) {
    }

    @Override
    public void showLuckyMesage() {

    }

    @Override
    public void displayWin(String name, int bet) {
        System.out.println("DisplayWin - winText exists: " + (winText != null));
        System.out.println("DisplayWin - messageContainer exists: " + (messageContainer != null));

        npcCounter.setText(Integer.toString(game.getNpcScore()));
        playerCounter.setText(Integer.toString(game.getPlayerScore()));

        messageContainer.toFront();
        winText.getStyleClass().setAll("win-text");
        winText.setText(name + " WINS " + bet + " CROWNS!");

        if (!playerName.getText().equals(name)) {
           winText.getStyleClass().clear();
           winText.getStyleClass().setAll("busted-text");
        }

        resetAnimations(winText);

        winText.setOpacity(0);
        messageContainer.setVisible(true);
        winText.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), winText);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> System.out.println("FadeIn completed"));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), winText);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(2));
        fadeOut.setOnFinished(e -> System.out.println("FadeOut completed"));

        SequentialTransition sequence = new SequentialTransition(fadeIn, fadeOut);
        sequence.setOnFinished(e -> {
            System.out.println("Sequence completed");
            messageContainer.setVisible(false);
            winText.setVisible(false);
            removeEventHandlers();
            MainApplication.loadStoryScene();
        });

        sequence.play();
    }

}