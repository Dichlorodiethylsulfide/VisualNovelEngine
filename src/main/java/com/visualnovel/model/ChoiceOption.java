package com.visualnovel.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a single choice option in a ChooseAction.
 * onPress can be a String (file path), a Map (single action object), or a List (array of actions).
 */
public class ChoiceOption {
    private String text;
    private Object onPress; // Can be String (file path), Map (single action), or List (array of actions)
    private String requires; // Optional requirement expression (e.g., "inventory.Player.Gold >= 10")
    
    public ChoiceOption() {
    }
    
    public ChoiceOption(String text, Object onPress) {
        this.text = text;
        this.onPress = onPress;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Gets the onPress value, which can be a String (file path), Map (single action), or List (array of actions).
     * 
     * @return The onPress value as Object
     */
    public Object getOnPress() {
        return onPress;
    }
    
    public void setOnPress(Object onPress) {
        this.onPress = onPress;
    }
    
    /**
     * Checks if onPress is a file path (String).
     * 
     * @return true if onPress is a String, false otherwise
     */
    public boolean isFilePath() {
        return onPress instanceof String;
    }
    
    /**
     * Gets onPress as a file path string.
     * 
     * @return The file path, or null if onPress is not a String
     */
    public String getOnPressAsFilePath() {
        return onPress instanceof String ? (String) onPress : null;
    }
    
    /**
     * Gets onPress as a single action map.
     * 
     * @return The action map, or null if onPress is not a Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOnPressAsAction() {
        return onPress instanceof Map ? (Map<String, Object>) onPress : null;
    }
    
    /**
     * Gets onPress as a list of actions.
     * 
     * @return The action list, or null if onPress is not a List
     */
    @SuppressWarnings("unchecked")
    public List<Object> getOnPressAsList() {
        return onPress instanceof List ? (List<Object>) onPress : null;
    }
    
    /**
     * Gets the requirement expression for this option.
     * 
     * @return The requirement expression string, or null if not set
     */
    public String getRequires() {
        return requires;
    }
    
    /**
     * Sets the requirement expression for this option.
     * 
     * @param requires The requirement expression string (e.g., "inventory.Player.Gold >= 10")
     */
    public void setRequires(String requires) {
        this.requires = requires;
    }
}

