package model.observers;

/**
 * Interface for game observers
 */
public interface GameObserver {
    void playerRollDice();
    void npcRollDice();

    void showLuckyMesage();

    void displayWin(String name, int bet);

}
