package services;

import model.records.dice.*;
import model.records.npc.NPC;

import java.util.ArrayList;
import java.util.List;

public class NPCService {

    private NPC Sem;
    private NPC Jeb;

    private static NPCService instance;

    private static List<Dice> samDeck = new ArrayList<>() {};
    private static List<Dice> jebDeck = new ArrayList<>() {};


    static {
        for (int i = 0; i < 4; i++) {
            jebDeck.add(new RegularDice(1));
        }
        jebDeck.add(new LuckyDice(1));
        jebDeck.add(new CursedDice(1));


        for (int i = 0; i < 4; i++) {
            samDeck.add(new LuckyDice(1));
        }

        for (int i = 0; i < 2; i++) {
            samDeck.add(new CursedDice(1));
        }

    }

    private NPCService() {
        Sem = new NPC("Sam", new DiceDeck(samDeck), Integer.MAX_VALUE, 0, 3, false, 700, 30, "A grinning rogue who palms dice, \"accidentally\" miscounts, and always has an excuse.","\"What? Me? Naw, I’d never… unless the opportunity arises.\"");
        Jeb = new NPC("Jeb", new DiceDeck(jebDeck), Integer.MAX_VALUE, 0, 2, true, 400, 10, "A wrinkled, tobacco-chewing farmer who’s been playing dice since before the others were born.","\"Back in my day, we rolled bones, not fancy dice!\"");
    }

    public static NPCService getInstance() {
        if (instance == null) {
            instance = new NPCService();
        }
        return instance;
    }


    public NPC getSam() {
        return Sem;
    }

    public NPC getJeb() {
        return Jeb;
    }
}
