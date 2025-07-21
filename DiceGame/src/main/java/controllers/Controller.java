package controllers;

import exceptions.SomeGameFieldsMissing;

/**
 * Interface for controllers
 */
public interface Controller {

    void init() throws SomeGameFieldsMissing;

    /**
     * Sets up event handlers
     */
     void setupEventHandlers();

    /**
     * Removes event handlers
     */
     void removeEventHandlers();
}
