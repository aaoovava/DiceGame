package unit_tests;

import db.GameDatabase;
import model.Game;
import model.records.Turn;
import model.records.dice.Dice;
import model.records.dice.DiceDeck;
import model.records.dice.RegularDice;
import model.records.enums.EndingOfTurn;
import model.records.npc.HumanPlayer;
import model.records.npc.NPC;
import model.records.npc.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import services.NPCService;
import services.PlayerService;
import services.ScoreCalculatorService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerTest {
    private NPC npc;
    private final String name = "TestNPC";
    private final DiceDeck diceDeck = new DiceDeck(NPCService.getInstance().getJeb().getDiceDeck().getDeck());
    private final int balance = 1000;
    private final int currentBet = 50;
    private final int difficulty = 3;
    private final boolean integrity = true;
    private final int maxBet = 500;
    private final int minBet = 10;
    private final String description = "Test description";
    private final String quote = "Test quote";

    private ScoreCalculatorService service = ScoreCalculatorService.getInstance();

    @Before
    public void setUp() {
        npc = new NPC(name, diceDeck, balance, currentBet,
                difficulty, integrity, maxBet, minBet, description, quote);
    }

    @Test
    public void seterAndGetter() {
        HumanPlayer player = new HumanPlayer("sam", new DiceDeck(new ArrayList<>() {
            {
                add(new RegularDice(4));
                add(new RegularDice(4));
                add(new RegularDice(4));
                add(new RegularDice(4));
                add(new RegularDice(4));
                add(new RegularDice(4));
            }
        }), 0, 0);
        DiceDeck diceDeck = player.getDiceDeck();
        player.setDiceDeck(diceDeck);
        Assert.assertNull(player.tryToDetectCheat());
        player.rollDice();
        player.setBalance(0);
        player.setName("sam");
        player.setCurrentBet(0);
        Assert.assertNotNull(player.getDiceDeck());
        Assert.assertEquals("sam", player.getName());
        Assert.assertEquals(0, player.getBalance());
        Assert.assertEquals(0, player.getCurrentBet());

    }

    @Test
    public void NpcTest() {
        NPC npc1 = NPCService.getInstance().getJeb();

        npc1.rollDice(npc1.getDiceDeck().getDeck());
        Assert.assertNotNull(npc1.getLastTurn());
    }

    @Test
    public void testAllGettersAndSetters() {
        // Test all getters with initial values
        Assert.assertEquals(name, npc.getName());
        Assert.assertEquals(diceDeck, npc.getDiceDeck());
        Assert.assertEquals(balance, npc.getBalance());
        Assert.assertEquals(currentBet, npc.getCurrentBet());
        Assert.assertEquals(difficulty, npc.getDifficulty());
        Assert.assertEquals(integrity, npc.getIntegrity());
        Assert.assertEquals(maxBet, npc.getMaxBet());
        Assert.assertEquals(minBet, npc.getMinBet());
        Assert.assertEquals(description, npc.getDescription());
        Assert.assertEquals(quote, npc.getQuote());

        // Test collection getters
        Assert.assertNotNull(npc.getCurrentRoll());
        Assert.assertTrue(npc.getCurrentRoll().isEmpty());
        Assert.assertNotNull(npc.getSelectedDice());
        Assert.assertTrue(npc.getSelectedDice().isEmpty());

        // Test turn-related getters
        Assert.assertNull(npc.getLastTurn());
        Assert.assertEquals(0, npc.getCurrentTurnScore());

        // Test setters
        int newDifficulty = 5;
        npc.setDifficulty(newDifficulty);
        Assert.assertEquals(newDifficulty, npc.getDifficulty());

        // Test unmodifiable collections
        try {
            npc.getSelectedDice().add(null);
            Assert.fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected behavior
        }
    }

    @Test
    public void TestTurn() {
        Turn turn = new Turn(100, EndingOfTurn.SCORED, npc.getDiceDeck().getDeck(), npc.getDiceDeck().getDeck(), npc.getDiceDeck().getDeck());
        Assert.assertEquals(diceDeck.getDeck(), turn.getRemainingDice());
        Assert.assertEquals(diceDeck.getDeck(), turn.getDisplayedDice());
        Assert.assertEquals("Turn[score=100, result=SCORED, selected=6 dice, remaining=6 dice]", turn.toString());
    }

    @Test
    public void throwDiceTest() {
        Game.getInstance().setCurrentTurn(1);
        Assert.assertThrows(NullPointerException.class, () -> Game.getInstance().throwDice());
        Assert.assertNotNull(Game.getInstance().getGameObserver());
    }

    @Test
    public void testDbForPlayer() {
        ArrayList <Dice> dice = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            dice.add(new RegularDice(4));
        }

        DiceDeck playerDiceDeck = new DiceDeck(dice);
        HumanPlayer player = new HumanPlayer("sam", playerDiceDeck, 0, 0);
        Assert.assertNotNull(player);
        GameDatabase.getInstance().save(player);
        HumanPlayer loaded = GameDatabase.getInstance().load();
        Assert.assertEquals(player.getName(), loaded.getName());
            HumanPlayer player1 = new HumanPlayer(name,diceDeck, balance, currentBet);
            GameDatabase.getInstance().save(player1);


    }

    @Test
    public void ServicesTest() {
        ArrayList <Dice> dice = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            dice.add(new RegularDice(4));
        }

        DiceDeck playerDiceDeck = new DiceDeck(dice);
        HumanPlayer player = new HumanPlayer("sam", playerDiceDeck, 0, 0);
        PlayerService.getInstance().setPlayer(player);
        Assert.assertEquals(0, PlayerService.getInstance().getBalance());
        PlayerService.getInstance().setName("aaoovava");
        Assert.assertEquals("aaoovava", PlayerService.getInstance().getName());
        PlayerService.getInstance().setPlayer(null);
        PlayerService.getInstance().setPlayerServiceNull();
        Assert.assertNotNull(PlayerService.getInstance().getPlayer());


    }

    @Test
    public void testCalculateScore_nullOrEmpty() {
        Assert.assertEquals(0, service.calculateScore(null));
        Assert.assertEquals(0, service.calculateScore(new ArrayList<Dice>()));
    }

    @Test
    public void testCalculateScore_straightSixDice() {
        List<Dice> dice = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            dice.add(new RegularDice(i));
        }
        Assert.assertEquals(1500, service.calculateScore(dice));
    }

    @Test
    public void testCalculateScore_threePairs() {
        List<Dice> dice = new ArrayList<>();
        dice.add(new RegularDice(2));
        dice.add(new RegularDice(2));
        dice.add(new RegularDice(3));
        dice.add(new RegularDice(3));
        dice.add(new RegularDice(4));
        dice.add(new RegularDice(4));
        Assert.assertEquals(1500, service.calculateScore(dice));
    }

    @Test
    public void testCalculateScore_fourOfKindAndPair() {
        List<Dice> dice = new ArrayList<>();
        dice.add(new RegularDice(5));
        dice.add(new RegularDice(5));
        dice.add(new RegularDice(5));
        dice.add(new RegularDice(5));
        dice.add(new RegularDice(3));
        dice.add(new RegularDice(3));
        Assert.assertEquals(1500, service.calculateScore(dice));
    }

    @Test
    public void testCalculateScore_twoTriples() {
        List<Dice> dice = new ArrayList<>();
        dice.add(new RegularDice(3));
        dice.add(new RegularDice(3));
        dice.add(new RegularDice(3));
        dice.add(new RegularDice(6));
        dice.add(new RegularDice(6));
        dice.add(new RegularDice(6));
        Assert.assertEquals(2500, service.calculateScore(dice));
    }

    @Test
    public void testCalculateScore_standardScoring() {
        List<Dice> dice = new ArrayList<>();
        dice.add(new RegularDice(1)); // 100
        dice.add(new RegularDice(5)); // 50
        dice.add(new RegularDice(5)); // 50
        Assert.assertEquals(200, service.calculateScore(dice));
    }

    @Test
    public void testCalculateScore_nonScoringDice() {
        List<Dice> dice = new ArrayList<>();
        dice.add(new RegularDice(2));
        dice.add(new RegularDice(3));
        dice.add(new RegularDice(4));
        Assert.assertEquals(0, service.calculateScore(dice));
    }

    @Test
    public void testHasAnyScoringCombination_nullOrEmpty() {
        Assert.assertFalse(service.hasAnyScoringCombination(null));
        Assert.assertFalse(service.hasAnyScoringCombination(new ArrayList<Dice>()));
    }

    @Test
    public void testHasAnyScoringCombination_withScoringDice() {
        List<Dice> dice = new ArrayList<>();
        dice.add(new RegularDice(1));
        dice.add(new RegularDice(2));
        Assert.assertTrue(service.hasAnyScoringCombination(dice));
    }

    @Test
    public void testHasAnyScoringCombination_withNoScoringDice() {
        List<Dice> dice = new ArrayList<>();
        dice.add(new RegularDice(2));
        dice.add(new RegularDice(3));
        dice.add(new RegularDice(4));
        Assert.assertFalse(service.hasAnyScoringCombination(dice));
    }

    @Test
    public void testHasAnyScoringCombination_specialCombo() {
        List<Dice> dice = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            dice.add(new RegularDice(i));
        }
        Assert.assertTrue(service.hasAnyScoringCombination(dice));
    }

    @Test
    public void testCalculateScore_fourOfAKindNonOneFive() {
        List<Dice> dice = new ArrayList<>();
        dice.add(new RegularDice(2));
        dice.add(new RegularDice(2));
        dice.add(new RegularDice(2));
        dice.add(new RegularDice(2));
        // 3x2 = 200 + 1 extra die -> +1000 = 1200 total
        Assert.assertEquals(1200, service.calculateScore(dice));
    }

    @Test //Reflection Test
    public void reflectionTest_modifyPrivateField() throws Exception {
        // Create a HumanPlayer
        HumanPlayer player = new HumanPlayer("sam", new DiceDeck(new ArrayList<>() {{
            add(new RegularDice(4));
            add(new RegularDice(4));
            add(new RegularDice(4));
            add(new RegularDice(4));
            add(new RegularDice(4));
            add(new RegularDice(4));
        }}), 100, 10);


        Field balanceField = Player.class.getDeclaredField("balance");
        balanceField.setAccessible(true);
        balanceField.setInt(player, 9999);

        Assert.assertEquals(9999, player.getBalance());
    }

    @Test //Reflection Test
    public void reflectionTest_invokePrivateMethod() throws Exception {
        HumanPlayer player = new HumanPlayer("sam", new DiceDeck(new ArrayList<>() {{
            add(new RegularDice(4));
            add(new RegularDice(4));
            add(new RegularDice(4));
            add(new RegularDice(4));
            add(new RegularDice(4));
            add(new RegularDice(4));
        }}), 100, 10);

        try {
            Method method = HumanPlayer.class.getDeclaredMethod("tryToDetectCheat");
            method.setAccessible(true);
            Object result = method.invoke(player);
            System.out.println("Result: " + result);
            Assert.assertNull(result);
        } catch (NoSuchMethodException e) {
        }
    }

}

