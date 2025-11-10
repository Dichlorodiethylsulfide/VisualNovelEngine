package com.visualnovel.model;

import com.visualnovel.GameController;

/**
 * Base class for all action types in the scenario.
 * This allows the game to be extended with custom actions by inheriting from this class.
 */
public abstract class ActionType {
    /**
     * Creates a new ActionType. The name of the action type is used to identify the action type in the JSON file.
     */
    protected ActionType() {
    }
    
    /**
     * Gets the JSON string value for this action type.
     * 
     * @return The JSON identifier string
     */
    public String getJsonValue() {
        throw new UnsupportedOperationException("getJsonValue() must be overridden by subclasses");
    }    
    /**
     * Executes this action using the provided scenario action data and game controller.
     * 
     * @param action The scenario action containing parameters and metadata
     * @param controller The game controller that manages game state
     */
    public abstract void execute(ScenarioAction action, GameController controller);
    
    /**
     * Determines whether this action requires user input before continuing.
     * 
     * @return true if the action should wait for user input, false otherwise
     */
    public abstract boolean requiresUserInput();
    
    /**
     * Determines whether the game should continue to the next action immediately after this one.
     * 
     * @return true if the game should continue immediately, false if it should wait
     */
    public abstract boolean shouldContinueImmediately();

    /**
     * Gets the name of the action type. This method must be overridden by subclasses.
     * 
     * @return The name of the action type
     */
    public static String getName() {
        throw new UnsupportedOperationException("getName() must be overridden by subclasses");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ActionType that = (ActionType) obj;
        return this.getJsonValue().equals(that.getJsonValue());
    }
    
    @Override
    public int hashCode() {
        return this.getJsonValue().hashCode();
    }
    
    @Override
    public String toString() {
        return this.getJsonValue();
    }
}
