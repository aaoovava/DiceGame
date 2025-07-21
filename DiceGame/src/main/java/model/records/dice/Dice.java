package model.records.dice;

import exceptions.WrongProbavilitiesSetUp;

import java.io.Serializable;
import java.util.*;

/**
 * Abstract base class for creating different types of dice with customizable probabilities.
 * The class handles dice rolling, setting and validating custom probabilities, and storing dice attributes.
 */
public abstract class Dice implements Serializable {

   private static final long serialVersionUID = 1L;

   private String name = "Dice"; // Name of the dice
   private int balance = 0; // Balance associated with the dice
   private String info = ""; // Additional information about the dice
   private long id = UUID.randomUUID().getLeastSignificantBits(); // Unique ID for the dice
   private final Random random = new Random(); // Random instance for dice rolling

   // Static map of standard probabilities for 6-sided dice
   private static final Map<Integer, Double> STANDARD_PROBABILITIES;

   static {
      // Initializes standard probabilities for a fair 6-sided dice
      Map<Integer, Double> probs = new HashMap<>();
      probs.put(1, 1.0 / 6);
      probs.put(2, 1.0 / 6);
      probs.put(3, 1.0 / 6);
      probs.put(4, 1.0 / 6);
      probs.put(5, 1.0 / 6);
      probs.put(6, 1.0 / 6);
      STANDARD_PROBABILITIES = Collections.unmodifiableMap(probs);
   }

   private int currentSide; // Current side showing on the dice
   private Map<Integer, Double> probabilities = new HashMap<>(STANDARD_PROBABILITIES); // Custom probabilities for sides
   private int price = 0; // Price associated with the dice
   private boolean cheatable; // Whether the dice is cheatable
   private Skin skin; // Visual appearance of the dice (skin)

   /**
    * Constructor to initialize the dice with a specific side.
    *
    * @param side The side of the dice to initialize with
    */
   public Dice(int side) {
      this.currentSide = side;
   }

   /**
    * Rolls the dice based on the defined probabilities.
    */
   public void roll() {
      double randomValue = random.nextDouble();
      double cumulativeProbability = 0.0;

      for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
         cumulativeProbability += entry.getValue();
         if (randomValue <= cumulativeProbability) {
            currentSide = entry.getKey();
            return;
         }
      }

      // Fallback for rounding errors, assigns the first side in the probabilities map
      currentSide = probabilities.keySet().iterator().next();
   }

   /**
    * Abstract method to return the image name for the dice's current side.
    * Implemented by subclasses to provide specific image names.
    *
    * @return A string representing the image file name for the current dice side
    */
   public abstract String returnImageName();

   /**
    * Sets custom probabilities for the dice after validating them.
    *
    * @param customProbabilities A map of side numbers to their probabilities
    * @throws WrongProbavilitiesSetUp if the probabilities are invalid
    */
   public void setCustomProbabilities(Map<Integer, Double> customProbabilities) {
      validateProbabilities(customProbabilities);
      this.probabilities = new HashMap<>(customProbabilities);
   }

   /**
    * Resets the dice probabilities to the standard (fair 6-sided) values.
    */
   public void resetToStandardProbabilities() {
      this.probabilities = new HashMap<>(STANDARD_PROBABILITIES);
   }

   /**
    * Validates that the given probabilities sum up to 1.0.
    *
    * @param probs A map of probabilities to validate
    * @throws WrongProbavilitiesSetUp if the probabilities do not sum to 1.0
    */
   private void validateProbabilities(Map<Integer, Double> probs) {
      if (probs == null || probs.isEmpty()) {
         throw new WrongProbavilitiesSetUp("Probabilities map cannot be null or empty");
      }

      double sum = probs.values().stream()
              .mapToDouble(Double::doubleValue)
              .sum();

      if (Math.abs(sum - 1.0) > 0.0001) {
         throw new WrongProbavilitiesSetUp("Probabilities must sum to 1.0");
      }
   }

   // Getters and setters
   public int getCurrentSide() {
      return currentSide;
   }

   public Map<Integer, Double> getProbabilities() {
      return Collections.unmodifiableMap(probabilities);
   }

   public int getPrice() {
      return price;
   }

   public void setPrice(int price) {
      this.price = price;
   }

   public boolean isCheatable() {
      return cheatable;
   }

   public void setCheatable(boolean cheatable) {
      this.cheatable = cheatable;
   }

   public void setCurrentSide(int currentSide) {
      this.currentSide = currentSide;
   }

   public Skin getSkin() {
      return skin;
   }

   /**
    * Provides a string representation of the dice object.
    *
    * @return A string representing the dice object with its attributes
    */
   @Override
   public String toString() {
      return "Dice{" +
              "currentSide=" + currentSide +
              ", probabilities=" + probabilities +
              ", valuable=" + price +
              ", cheatable=" + cheatable +
              ", skin=" + skin +
              '}';
   }

   public long getId() {
      return id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getInfo() {
      return info;
   }

   public void setInfo(String info) {
      this.info = info;
   }

   public int getBalance() {
      return balance;
   }

   public void setBalance(int balance) {
      this.balance = balance;
   }

   /**
    * Checks if two dice objects are equal by comparing their current side and ID.
    *
    * @param o the other object to compare with
    * @return true if the objects are equal, false otherwise
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Dice dice = (Dice) o;
      return currentSide == dice.currentSide &&
              Objects.equals(id, dice.id);
   }

   /**
    * Generates a hash code for the dice object based on current side and ID.
    *
    * @return a hash code for the dice object
    */
   @Override
   public int hashCode() {
      return Objects.hash(currentSide, id);
   }

   public void setSkin(Skin skin) {
      this.skin = skin;
   }
}