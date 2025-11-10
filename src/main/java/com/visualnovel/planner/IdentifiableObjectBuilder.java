package com.visualnovel.planner;

import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.battle.Move;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for creating IdentifiableObject instances (backgrounds, characters, screens).
 */
public class IdentifiableObjectBuilder {
    private IdentifiableObject object;
    
    public IdentifiableObjectBuilder() {
        this.object = new IdentifiableObject();
    }
    
    /**
     * Sets the object type (e.g., "background", "character", "screen").
     * 
     * @param type The object type
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder type(String type) {
        object.setType(type);
        return this;
    }
    
    /**
     * Sets the object category (e.g., "forest", "player", "enemy", "lose", "win").
     * 
     * @param category The category
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder category(String category) {
        object.setCategory(category);
        return this;
    }
    
    /**
     * Sets the display name.
     * 
     * @param displayName The display name
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder displayName(String displayName) {
        object.setDisplayName(displayName);
        return this;
    }
    
    /**
     * Adds a sprite mapping.
     * 
     * @param key The sprite key (e.g., "default")
     * @param spriteName The sprite name
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder sprite(String key, String spriteName) {
        if (object.getSprites() == null) {
            object.setSprites(new HashMap<>());
        }
        object.getSprites().put(key, spriteName);
        return this;
    }
    
    /**
     * Sets multiple sprites at once.
     * 
     * @param sprites Map of sprite keys to sprite names
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder sprites(Map<String, String> sprites) {
        object.setSprites(sprites);
        return this;
    }
    
    /**
     * Sets a stat value.
     * 
     * @param statName The stat name (e.g., "health", "attack", "defense")
     * @param value The stat value
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder stat(String statName, Number value) {
        if (object.getStats() == null) {
            object.setStats(new HashMap<>());
        }
        object.getStats().put(statName, value);
        return this;
    }
    
    /**
     * Sets multiple stats at once.
     * 
     * @param stats Map of stat names to stat values
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder stats(Map<String, Number> stats) {
        object.setStats(stats);
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
    public IdentifiableObjectBuilder commonStats(int health, int attack, int defense, int speed, int luck, int energy) {
        stat("health", health);
        stat("attack", attack);
        stat("defense", defense);
        stat("speed", speed);
        stat("luck", luck);
        stat("energy", energy);
        return this;
    }
    
    /**
     * Adds a move.
     * 
     * @param moveName The move name
     * @param move The Move object
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder move(String moveName, Move move) {
        if (object.getMoves() == null) {
            object.setMoves(new HashMap<>());
        }
        object.getMoves().put(moveName, move);
        return this;
    }
    
    /**
     * Adds a move using a MoveBuilder.
     * 
     * @param moveName The move name
     * @param builderFunction Function that builds the move
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder move(String moveName, java.util.function.Function<MoveBuilder, Move> builderFunction) {
        MoveBuilder moveBuilder = new MoveBuilder();
        Move move = builderFunction.apply(moveBuilder);
        return move(moveName, move);
    }
    
    /**
     * Sets multiple moves at once.
     * 
     * @param moves Map of move names to Move objects
     * @return This builder for method chaining
     */
    public IdentifiableObjectBuilder moves(Map<String, Move> moves) {
        object.setMoves(moves);
        return this;
    }
    
    /**
     * Builds and returns the IdentifiableObject.
     * 
     * @return The constructed IdentifiableObject
     */
    public IdentifiableObject build() {
        return object;
    }
}

