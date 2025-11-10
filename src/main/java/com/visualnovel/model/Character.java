package com.visualnovel.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a character in the scenario with name, sprites, and stats.
 */
public class Character {
    private String name;
    private Map<String, String> sprites; // Map of sprite variant names to sprite IDs
    private List<String> randomSprites; // List of sprite IDs for random selection
    private Map<String, Number> stats; // Map of stat names to stat values
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, String> getSprites() {
        return sprites;
    }
    
    public void setSprites(Map<String, String> sprites) {
        this.sprites = sprites;
    }
    
    public List<String> getRandomSprites() {
        return randomSprites;
    }
    
    public void setRandomSprites(List<String> randomSprites) {
        this.randomSprites = randomSprites;
    }
    
    public Map<String, Number> getStats() {
        return stats;
    }
    
    public void setStats(Map<String, Number> stats) {
        this.stats = stats;
    }
}

