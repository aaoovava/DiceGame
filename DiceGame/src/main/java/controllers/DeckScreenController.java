package controllers;

import db.GameDatabase;
import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.records.dice.*;
import model.records.npc.HumanPlayer;
import services.PlayerService;
import view.applications.MainApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The controller class for managing the deck screen in the dice game application.
 * It handles the interactions between the market, the player's deck, and the UI elements on the screen.
 */
public class DeckScreenController implements Controller {
    private static final int DICE_SIZE = 60;
    private static final int MARKET_DICE_COUNT = 4;
    private static final long DOUBLE_CLICK_DELAY = 300; // milliseconds
    private static final int ANIMATION_DURATION = 250;
    @FXML
    private Text deckBalance;
    @FXML
    Text balance;

    @FXML
    private Text diceName;
    @FXML
    private Text dicePrice;
    @FXML
    private Text diceInfo;
    @FXML
    private ImageView backButton;
    @FXML
    private HBox marketRow;
    @FXML
    private HBox playerDeckRow;

    private Image backButtonNormalImage;
    private Image backButtonHoverImage;
    private long lastClickTime = 0;

    private Map<Dice, ImageView> diceToImageViewMap = new ConcurrentHashMap<>();
    private Map<ImageView, Dice> imageViewToDiceMap = new ConcurrentHashMap<>();

    private EventHandler<MouseEvent> backButtonClickHandler;
    private EventHandler<MouseEvent> backButtonEnterHandler;
    private EventHandler<MouseEvent> backButtonExitHandler;

    private List<Dice> playerDeck = new ArrayList<>();
    private List<Dice> market = new ArrayList<>();
    private static final PlayerService playerService = PlayerService.getInstance();

    private HumanPlayer player = playerService.getPlayer();

    /**
     * Sets up event handlers for UI interactions such as back button hover, exit, and click events.
     */
    @Override
    public void setupEventHandlers() {
        backButtonEnterHandler = event -> {
            backButton.setImage(backButtonHoverImage);
            backButton.setCursor(Cursor.HAND);
            animateButtonHover(backButton, 1.1);
        };

        backButtonExitHandler = event -> {
            backButton.setImage(backButtonNormalImage);
            backButton.setCursor(Cursor.DEFAULT);
            animateButtonHover(backButton, 1.0);
        };

        backButtonClickHandler = event -> {
            animateButtonClick(backButton).setOnFinished(e -> {

                if (playerDeck.size() == 6 && player.getBalance() >= 0) {
                    MainApplication.loadMainScene();
                    GameDatabase.getInstance().save(playerService.getPlayer());
                    removeEventHandlers();
                }
            });
        };

        backButton.setOnMouseEntered(backButtonEnterHandler);
        backButton.setOnMouseExited(backButtonExitHandler);
        backButton.setOnMouseClicked(backButtonClickHandler);
    }

    /**
     * Removes all event handlers attached to the back button and dice images.
     */
    @Override
    public void removeEventHandlers() {
        backButton.setOnMouseClicked(null);
        backButton.setOnMouseEntered(null);
        backButton.setOnMouseExited(null);

        for (ImageView diceView : diceToImageViewMap.values()) {
            diceView.setOnMouseClicked(null);
        }
    }

    /**
     * Sets up the market and the player's deck on the screen.
     * Initializes the dice in the market and populates the player's deck.
     */
    private void setUpMarketAndPlayerDeck() {
        marketRow.getChildren().clear();
        playerDeckRow.getChildren().clear();
        diceToImageViewMap.clear();
        imageViewToDiceMap.clear();

        initializeMarket();
        playerDeck.clear();
        playerDeck.addAll(playerService.getPlayer().getDiceDeck().getDeck());

        for (Dice dice : market) {
            ImageView diceView = createDiceImageView(dice, true);
            marketRow.getChildren().add(diceView);
        }

        for (Dice dice : playerDeck) {
            ImageView diceView = createDiceImageView(dice, false);
            playerDeckRow.getChildren().add(diceView);
        }
    }

    /**
     * Initializes the dice in the market with predefined types of dice.
     */
    private void initializeMarket() {
        market.clear();
        market.add(new RegularDice(1));
        market.add(new CursedDice(1));
        market.add(new LuckyDice(1));
        market.add(new RoyalDice(1));
    }

    /**
     * Displays the information of a selected dice, including its name, price, and description.
     *
     * @param dice The dice whose information needs to be displayed.
     */
    private void showDiceInfo(Dice dice) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), diceInfo);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), diceInfo);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(e -> {
            diceName.setText(dice.getName());
            dicePrice.setText("Price: " + dice.getPrice());
            diceInfo.setText(dice.getInfo());
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Creates an ImageView for a dice, setting its image, size, and interactions.
     *
     * @param dice       The dice to create the image for.
     * @param isMarketDice Indicates if the dice is part of the market.
     * @return The ImageView for the dice.
     */
    private ImageView createDiceImageView(Dice dice, boolean isMarketDice) {
        Image image = new Image(Objects.requireNonNull(
                MainApplication.class.getResourceAsStream(dice.returnImageName()))
        );

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(DICE_SIZE);
        imageView.setFitWidth(DICE_SIZE);
        imageView.setPreserveRatio(true);

        // Initialize with default shadow
        DropShadow shadow = new DropShadow(5, Color.color(0, 0, 0, 0.3));
        shadow.setSpread(0.1);
        imageView.setEffect(shadow);

        diceToImageViewMap.put(dice, imageView);
        imageViewToDiceMap.put(imageView, dice);

        setupDiceInteractions(imageView, dice, isMarketDice);

        return imageView;
    }

    /**
     * Sets up interactions for dice images, including hover, exit, and click events.
     *
     * @param imageView   The ImageView representing the dice.
     * @param dice        The dice associated with the ImageView.
     * @param isMarketDice Indicates if the dice is part of the market.
     */
    private void setupDiceInteractions(ImageView imageView, Dice dice, boolean isMarketDice) {
        imageView.setOnMouseEntered(event -> {
            if ((isMarketDice && playerDeck.size() < 6) || !isMarketDice) {
                imageView.setCursor(Cursor.HAND);
                animateDiceHover(imageView, true);
            }
        });

        imageView.setOnMouseExited(event -> {
            animateDiceHover(imageView, false);
        });

        imageView.setOnMouseClicked(event -> {
            long clickTime = System.currentTimeMillis();

            if (clickTime - lastClickTime < DOUBLE_CLICK_DELAY) {
                // Double click detected - move dice
                if ((isMarketDice && playerDeck.size() < 6) || !isMarketDice) {
                    animateDiceClick(imageView).setOnFinished(e ->
                            handleDiceMovement(dice, isMarketDice)
                    );
                }
            } else {
                // Single click - show info with animation
                animateDiceInfoClick(imageView);
                showDiceInfo(dice);
            }

            lastClickTime = clickTime;
        });
    }

    /**
     * Animates the hover effect on a dice image, including scaling and shadow changes.
     *
     * @param imageView The ImageView representing the dice.
     * @param hover     Indicates whether to apply the hover effect or not.
     * @return The ParallelTransition that animates the hover effect.
     */
    private ParallelTransition animateDiceHover(ImageView imageView, boolean hover) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(ANIMATION_DURATION), imageView);
        scale.setToX(hover ? 1.1 : 1.0);
        scale.setToY(hover ? 1.1 : 1.0);

        DropShadow currentShadow = (DropShadow) imageView.getEffect();
        DropShadow newShadow = new DropShadow(
                hover ? 10 : 5,
                Color.color(0, 0, 0, hover ? 0.4 : 0.3)
        );
        newShadow.setSpread(hover ? 0.2 : 0.1);

        ShadowTransition shadowAnim = new ShadowTransition(
                Duration.millis(ANIMATION_DURATION),
                currentShadow,
                newShadow,
                imageView  // Now properly passing the ImageView
        );

        ParallelTransition parallel = new ParallelTransition(scale, shadowAnim);
        parallel.play();
        return parallel;
    }


    /**
     * Animates the dice click effect.
     * @param imageView
     * @return sequential transition
     */
    private SequentialTransition animateDiceClick(ImageView imageView) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), imageView);
        scaleDown.setToX(0.9);
        scaleDown.setToY(0.9);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), imageView);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        SequentialTransition seq = new SequentialTransition(scaleDown, scaleUp);
        seq.play();
        return seq;
    }

    /**
     * Animates the dice info click effect.
     * @param imageView
     * @return rotate transition
     */
    private RotateTransition animateDiceInfoClick(ImageView imageView) {
        RotateTransition rotate = new RotateTransition(Duration.millis(100), imageView);
        rotate.setByAngle(5);
        rotate.setCycleCount(4);
        rotate.setAutoReverse(true);
        rotate.play();
        return rotate;
    }


    /**
     * Animates the button hover effect.
     * @param button
     * @param scale
     * @return parallel transition
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
     * Animates the button click effect.
     * @param button
     * @return sequential transition
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
     * Handles the movement of a dice between the market and the player's deck, updating balances accordingly.
     *
     * @param dice        The dice to be moved.
     * @param isMarketDice Indicates if the dice is currently in the market.
     */
    private void handleDiceMovement(Dice dice, boolean isMarketDice) {
        if (isMarketDice && player.getBalance() > dice.getPrice() && player.getDiceDeck().getBalance() + dice.getBalance() >= 0) {
            player.setBalance(player.getBalance() - dice.getPrice());
            market.remove(dice);
            playerDeck.add(dice);
            addReplacementDie(dice.getClass());
        } else if(!isMarketDice){
            player.setBalance(player.getBalance() + dice.getPrice());
            playerDeck.remove(dice);
            market.add(dice);
        }

        else {
            playerDeck.remove(dice);
            market.add(dice);
        }
        updatePlayerDeckInService();
        refreshViews();
    }
    /**
     * Adds a replacement dice to the market after a dice is bought by the player.
     *
     * @param diceClass The class type of the dice to add to the market.
     */
    private void addReplacementDie(Class<? extends Dice> diceClass) {
        try {
            Dice newDie = diceClass.getConstructor(int.class).newInstance(1);
            market.add(newDie);
        } catch (Exception e) {
            market.add(new RegularDice(1));
        }
    }
    /**
     * Refreshes the views for the market, player's deck, and balance.
     */
    private void refreshViews() {
        setUpMarketAndPlayerDeck();
        balance.setText(String.valueOf(player.getBalance()));
        deckBalance.setText("Deck balance: " + player.getDiceDeck().getBalance());
    }

    /**
     * Updates the player's deck in the player service.
     */
    private void updatePlayerDeckInService() {
        playerService.getPlayer().getDiceDeck().setDeck(new ArrayList<>(playerDeck));
    }

    /**
     * Initializes the deck screen, setting initial balance and deck balance, and loading resources.
     */
    public void init() {
        balance.setText(String.valueOf(player.getBalance()));
        deckBalance.setText("Deck balance: " + player.getDiceDeck().getBalance());

        backButtonNormalImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("back.png")));
        backButtonHoverImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("backHover.png")));

        setupEventHandlers();
        setUpMarketAndPlayerDeck();
    }

    /**
     * Custom transition to animate the shadow effect of dice images.
     */
    private class ShadowTransition extends Transition {
        private final DropShadow start;
        private final DropShadow end;
        private final ImageView node;

        /**
         * Creates a shadow transition for animating the shadow effect of an ImageView.
         *
         * @param duration The duration of the transition.
         * @param start    The starting shadow.
         * @param end      The ending shadow.
         * @param node     The node (ImageView) to apply the shadow transition to.
         */
        public ShadowTransition(Duration duration, DropShadow start, DropShadow end, ImageView node) {
            this.start = start;
            this.end = end;
            this.node = node;  // Now properly accepting the ImageView
            setCycleDuration(duration);
        }

        @Override
        protected void interpolate(double frac) {
            DropShadow current = new DropShadow(
                    start.getRadius() + (end.getRadius() - start.getRadius()) * frac,
                    Color.color(
                            end.getColor().getRed(),
                            end.getColor().getGreen(),
                            end.getColor().getBlue(),
                            start.getColor().getOpacity() + (end.getColor().getOpacity() - start.getColor().getOpacity()) * frac
                    )
            );
            current.setSpread(start.getSpread() + (end.getSpread() - start.getSpread()) * frac);
            node.setEffect(current);
        }
    }
}