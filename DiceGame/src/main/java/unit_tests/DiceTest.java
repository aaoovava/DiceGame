package unit_tests;

import exceptions.WrongProbavilitiesSetUp;
import model.records.dice.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DiceTest {
    RegularDice dice;
    RegularDice dice2;

    @Before
    public void setUp() {
        dice = new RegularDice(4);
        dice2 = new RegularDice(4);
    }

    @Test
    public void roll() {
        dice.roll();
        Assert.assertNotNull(dice);
    }

    @Test
    public void checkEqualsAndHashCode() {
        Assert.assertTrue(dice.equals(dice));
        Assert.assertEquals(dice.hashCode(), dice.hashCode());
        Assert.assertNotEquals(dice.hashCode(), dice2.hashCode());
        Assert.assertFalse(dice.equals(dice2));
        Assert.assertEquals("Dice{currentSide=4, probabilities={1=0.16666666666666666, 2=0.16666666666666666, 3=0.16666666666666666, 4=0.16666666666666666, 5=0.16666666666666666, 6=0.16666666666666666}, valuable=0, cheatable=false, skin=null}", dice.toString());
    }

    @Test
    public void checkForWrongPropobabilities() {
        Assert.assertThrows(WrongProbavilitiesSetUp.class, () -> {
            Map<Integer, Double> probs = new HashMap<>();
            probs.put(1, 1.1);
            probs.put(2, 1.0 / 6);
            probs.put(3, 1.0 / 6);
            probs.put(4, 1.0 / 6);
            probs.put(5, 1.0 / 6);
            probs.put(6, 1.0 / 6);

            dice.setCustomProbabilities(probs);
        });
        Assert.assertThrows(WrongProbavilitiesSetUp.class, () -> {
            Map<Integer, Double> probs = null;

            dice.setCustomProbabilities(probs);
        });

        dice.resetToStandardProbabilities();
        Assert.assertNotNull(dice.getProbabilities());
    }

    @Test
    public void checkGeterAndSetter() {
        dice.setCheatable(true);
        Assert.assertTrue(dice.isCheatable());
        dice.setBalance(2);
        Assert.assertEquals(2, dice.getBalance());
        dice.setName("test");
        Assert.assertEquals("test", dice.getName());
        dice.setCurrentSide(1);
        Assert.assertEquals(1, dice.getCurrentSide());
        dice.setPrice(1);
        Assert.assertEquals(1, dice.getPrice());
        dice.setSkin(Skin.GOLDEN);
        Assert.assertNotNull(dice.getSkin());
        dice.setInfo("test");
        Assert.assertEquals("test", dice.getInfo());
        Assert.assertNotNull(dice.getId());
    }

    @Test
    public void diceDeckTest() {
        DiceDeck diceDeck = new DiceDeck(new ArrayList<>() {
            {
                add(dice);
                add(dice2);
            }
        });

        Assert.assertEquals(0, diceDeck.getBalance());
        diceDeck.removeDice(dice);
        diceDeck.addDice(dice);
        diceDeck.setDeck(new ArrayList<>());
        Assert.assertNotNull(diceDeck.getDeck());
    }

    @Test
    public void checkDiceImageName() {
        CursedDice cd = new CursedDice(6);
        Assert.assertEquals("/img/cursed_dice/cd6.png", cd.returnImageName());

        LuckyDice ld = new LuckyDice(6);
        Assert.assertEquals("/img/lucky_dice/ld6.png", ld.returnImageName());

        RegularDice rd = new RegularDice(6);
        Assert.assertEquals("/img/dice_normal/n6.png", rd.returnImageName());

        RoyalDice rd2 = new RoyalDice(6);
        Assert.assertEquals("/img/royal_dice/rd6.png", rd2.returnImageName());

        RiskDice rd3 = new RiskDice(6);
        Assert.assertEquals("/img/royal_dice/rd6.png", rd3.returnImageName());

        rd3.setRiskNumber(5);
        Assert.assertFalse(rd3.riskNumberDropped());
        rd3.setCurrentSide(5);
        Assert.assertTrue(rd3.riskNumberDropped());

        rd2.setRiskNumber(1);
        Assert.assertFalse(rd2.riskNumberDropped());
        rd2.setCurrentSide(1);
        Assert.assertTrue(rd2.riskNumberDropped());

    }

}
