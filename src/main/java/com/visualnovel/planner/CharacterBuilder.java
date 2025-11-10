package com.visualnovel.planner;

import com.visualnovel.model.Character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating Character instances.
 */
public class CharacterBuilder {
    private Character character;
    
    public CharacterBuilder() {
        this.character = new Character();
    }
    
    /**
     * Sets the character name.
     * 
     * @param name The character name
     * @return This builder for method chaining
     */
    public CharacterBuilder name(String name) {
        character.setName(name);
        return this;
    }
    
    /**
     * Adds a sprite variant.
     * 
     * @param key The sprite key (e.g., "default", "extra")
     * @param spriteId The sprite ID
     * @return This builder for method chaining
     */
    public CharacterBuilder sprite(String key, String spriteId) {
        if (character.getSprites() == null) {
            character.setSprites(new HashMap<>());
        }
        character.getSprites().put(key, spriteId);
        return this;
    }
    
    /**
     * Sets multiple sprites at once.
     * 
     * @param sprites Map of sprite keys to sprite IDs
     * @return This builder for method chaining
     */
    public CharacterBuilder sprites(Map<String, String> sprites) {
        character.setSprites(sprites);
        return this;
    }
    
    /**
     * Adds a random sprite (for random selection).
     * 
     * @param spriteId The sprite ID
     * @return This builder for method chaining
     */
    public CharacterBuilder randomSprite(String spriteId) {
        if (character.getRandomSprites() == null) {
            character.setRandomSprites(new ArrayList<>());
        }
        character.getRandomSprites().add(spriteId);
        return this;
    }
    
    /**
     * Sets the random sprites list.
     * 
     * @param randomSprites List of sprite IDs for random selection
     * @return This builder for method chaining
     */
    public CharacterBuilder randomSprites(List<String> randomSprites) {
        character.setRandomSprites(randomSprites);
        return this;
    }
    
    /**
     * Sets a stat value.
     * 
     * @param statName The stat name (e.g., "health", "attack", "defense")
     * @param value The stat value
     * @return This builder for method chaining
     */
    public CharacterBuilder stat(String statName, Number value) {
        if (character.getStats() == null) {
            character.setStats(new HashMap<>());
        }
        character.getStats().put(statName, value);
        return this;
    }
    
    /**
     * Sets multiple stats at once.
     * 
     * @param stats Map of stat names to stat values
     * @return This builder for method chaining
     */
    public CharacterBuilder stats(Map<String, Number> stats) {
        character.setStats(stats);
        return this;
    }
    
    /**
     * Sets common stats using convenience methods.
     * 
     * @param health Health stat
     * @param attack Attack stat
     * @param defense Defense stat
     * @param speed Speed stat
     * @param luck Luck stat
     * @param energy Energy stat
     * @return This builder for method chaining
     */
    public CharacterBuilder commonStats(int health, int attack, int defense, int speed, int luck, int energy) {
        stat("health", health);
        stat("attack", attack);
        stat("defense", defense);
        stat("speed", speed);
        stat("luck", luck);
        stat("energy", energy);
        return this;
    }
    
    /**
     * Builds and returns the Character object.
     * 
     * @return The constructed Character object
     */
    public Character build() {
        return character;
    }
}

