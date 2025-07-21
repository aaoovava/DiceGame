package unit_tests;

import controllers.GameScreenController;
import exceptions.SomeGameFieldsMissing;
import model.Game;
import model.observers.GameObserver;
import model.records.Turn;
import model.records.dice.Dice;
import model.records.enums.EndingOfTurn;
import model.records.npc.HumanPlayer;
import model.records.npc.Player;
import org.junit.Assert;
import org.junit.Test;
import services.NPCService;
import services.PlayerService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameTest {

    @Test
    public void setUpWithMissingPlayer() {
        Game game = Game.getInstance();
        Player player = null;
        PlayerService.getInstance().setPlayer((HumanPlayer) player);
        Assert.assertThrows(SomeGameFieldsMissing.class, () -> {
                game.setUpGame(NPCService.getInstance().getJeb(), 10);
                game.setGameObserver(new GameScreenController());
            Game.getInstance().setGameNull();
            Assert.assertNotNull(Game.getInstance());
            Game.getInstance().setGameNull();
        });
    }

    @Test
    public void setUpWithMissingNpc() {
        Game game = Game.getInstance();
        Assert.assertThrows(SomeGameFieldsMissing.class, () -> {
            game.setUpGame(null, 10);
            game.setGameObserver(new GameScreenController());
            Game.getInstance().setGameNull();
            Assert.assertNotNull(Game.getInstance());
            Game.getInstance().setGameNull();
        });
    }

    @Test
    public void setUpWithMissingRightMaxBet() {
        Game game = Game.getInstance();
        Assert.assertThrows(SomeGameFieldsMissing.class, () ->{
            game.setUpGame(NPCService.getInstance().getJeb(),0);
            game.setGameObserver(new GameScreenController());
            Game.getInstance().setGameNull();
            Assert.assertNotNull(Game.getInstance());
            Game.getInstance().setGameNull();
        });
    }
    @Test
    public void afterSetUpFirstTurnByPlayer() throws SomeGameFieldsMissing {
        Game game = Game.getInstance();
        game.setPlayer(PlayerService.getInstance().getPlayer());
        game.setUpGame(NPCService.getInstance().getSam(),0);
        Assert.assertEquals(0, game.getCurrentTurn());
        Game.getInstance().setGameNull();
        Assert.assertNotNull(Game.getInstance());
        Game.getInstance().setGameNull();
    }

    @Test
    public void secondTurnByNpc_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            Game game = Game.getInstance();
            GameScreenController gameScreenController = new GameScreenController();
            game.setPlayer(PlayerService.getInstance().getPlayer());
            game.setUpGame(NPCService.getInstance().getSam(),10);
            game.setGameObserver(gameScreenController);
            game.endTurn();
            Game.getInstance().setGameNull();
            Assert.assertNotNull(Game.getInstance());
            Game.getInstance().setGameNull();
        });
    }

    @Test
    public void setNpc() {
        Game.getInstance().setNpc(NPCService.getInstance().getJeb());
        Assert.assertEquals(NPCService.getInstance().getJeb(), Game.getInstance().getNpc());
        Game.getInstance().setGameNull();
        Assert.assertNotNull(Game.getInstance());
        Game.getInstance().setGameNull();
    }

    @Test
    public void setPlayer() {
        Game.getInstance().setPlayer(PlayerService.getInstance().getPlayer());
        Assert.assertEquals(PlayerService.getInstance().getPlayer(), Game.getInstance().getPlayer());
        Game.getInstance().setGameNull();
        Assert.assertNotNull(Game.getInstance());
        Game.getInstance().setGameNull();

    }

    @Test
    public void EndTurnConf() throws SomeGameFieldsMissing {

        Game game = Game.getInstance();
        game.setPlayer(PlayerService.getInstance().getPlayer());
        game.setUpGame(NPCService.getInstance().getSam(),0);


        game.setCurrentTurn(1);
        Assert.assertEquals(1, game.getCurrentTurn());
        game.endTurn();
        Game.getInstance().setGameNull();
        Assert.assertNotNull(Game.getInstance());
        Game.getInstance().setGameNull();

    }

    @Test
    public void checkSetersAndGeters() {
        Game.getInstance().setPlayerScore(10);
        Assert.assertEquals(10, Game.getInstance().getPlayerScore());
        Game.getInstance().setScoreToWin(20);
        Assert.assertEquals(20, Game.getInstance().getScoreToWin());
        Game.getInstance().setNpcScore(30);
        Assert.assertEquals(30, Game.getInstance().getNpcScore());
        ArrayList<Dice> dice = new ArrayList<>();
        Game.getInstance().setRolledDice(dice);
        Assert.assertEquals(dice, Game.getInstance().getRolledDice());
        Game.getInstance().setTurnScore(40);
        Assert.assertEquals(40, Game.getInstance().getTurnScore());
        Assert.assertEquals(10, Game.getInstance().getGameBet());
        Game.getInstance().cancelGame();
        Assert.assertNull(Game.getInstance().getNpc());
        Game.getInstance().setCurrentTurn(1);
        Game.getInstance().showLuckyMesage();
        Game.getInstance().showRoyalDiceMessage();
        Assert.assertThrows(NullPointerException.class, () -> {
            Game.getInstance().throwDice();
        });
    }
}
