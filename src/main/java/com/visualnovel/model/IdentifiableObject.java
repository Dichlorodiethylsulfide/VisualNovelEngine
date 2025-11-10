package com.visualnovel.model;

import com.visualnovel.model.battle.Move;
import java.util.Map;

/**
 * Represents an identifiable object in the scenario's ids dictionary.
 * Can be a background, character, screen, or other identifiable entity.
 */
public class IdentifiableObject {
    private String category; // Background, Character, LoseScreen, WinScreen
    private String type; // Forest, Player, Enemy, Lose, Win, etc.
    private String displayName; // Display name for the object
    private Map<String, String> sprites; // Map of sprite keys to sprite names
    private Map<String, Number> stats; // Optional stats (for characters)
    private Map<String, Move> moves; // Optional moves (for characters)
    private Map<String, InventoryItem> inventory; // Optional inventory (for characters)
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public Map<String, String> getSprites() {
        return sprites;
    }
    
    public void setSprites(Map<String, String> sprites) {
        this.sprites = sprites;
    }
    
    public Map<String, Number> getStats() {
        return stats;
    }
    
    public void setStats(Map<String, Number> stats) {
        this.stats = stats;
    }
    
    public Map<String, Move> getMoves() {
        return moves;
    }
    
    public void setMoves(Map<String, Move> moves) {
        this.moves = moves;
    }
    
    public Map<String, InventoryItem> getInventory() {
        return inventory;
    }
    
    public void setInventory(Map<String, InventoryItem> inventory) {
        this.inventory = inventory;
    }
}

