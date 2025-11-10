package com.visualnovel.planner;

import com.visualnovel.model.battle.Move;

/**
 * Builder for creating Move instances.
 */
public class MoveBuilder {
    private Move move;
    
    public MoveBuilder() {
        this.move = new Move();
    }
    
    /**
     * Creates a physical move with damage.
     * 
     * @param damage The damage amount
     * @param energyCost The energy cost
     * @param cooldown The cooldown in turns
     * @param description The move description
     * @return This builder for method chaining
     */
    public MoveBuilder physical(int damage, int energyCost, int cooldown, String description) {
        move.setType("physical");
        move.setDamage(damage);
        move.setEnergyCost(energyCost);
        move.setCooldown(cooldown);
        move.setDescription(description);
        return this;
    }
    
    /**
     * Creates a defense move.
     * 
     * @param defense The defense boost amount
     * @param energyCost The energy cost
     * @param cooldown The cooldown in turns
     * @param description The move description
     * @return This builder for method chaining
     */
    public MoveBuilder defense(int defense, int energyCost, int cooldown, String description) {
        move.setType("defense");
        move.setDefense(defense);
        move.setEnergyCost(energyCost);
        move.setCooldown(cooldown);
        move.setDescription(description);
        return this;
    }
    
    /**
     * Creates a healing move.
     * 
     * @param health The healing amount
     * @param energyCost The energy cost
     * @param cooldown The cooldown in turns
     * @param description The move description
     * @return This builder for method chaining
     */
    public MoveBuilder healing(int health, int energyCost, int cooldown, String description) {
        move.setType("healing");
        move.setHealth(health);
        move.setEnergyCost(energyCost);
        move.setCooldown(cooldown);
        move.setDescription(description);
        return this;
    }
    
    /**
     * Creates a status effect move.
     * 
     * @param effect The status effect type (e.g., "stun", "poison")
     * @param damage The damage amount
     * @param duration The duration in turns
     * @param energyCost The energy cost
     * @param cooldown The cooldown in turns
     * @param description The move description
     * @return This builder for method chaining
     */
    public MoveBuilder statusEffect(String effect, int damage, int duration, int energyCost, int cooldown, String description) {
        move.setType("status_effect");
        move.setEffect(effect);
        move.setDamage(damage);
        move.setDuration(duration);
        move.setEnergyCost(energyCost);
        move.setCooldown(cooldown);
        move.setDescription(description);
        return this;
    }
    
    /**
     * Sets the move type directly.
     * 
     * @param type The move type ("physical", "defense", "healing", "status_effect")
     * @return This builder for method chaining
     */
    public MoveBuilder type(String type) {
        move.setType(type);
        return this;
    }
    
    /**
     * Sets the damage amount.
     * 
     * @param damage The damage amount
     * @return This builder for method chaining
     */
    public MoveBuilder damage(int damage) {
        move.setDamage(damage);
        return this;
    }
    
    /**
     * Sets the defense boost amount.
     * 
     * @param defense The defense boost
     * @return This builder for method chaining
     */
    public MoveBuilder defense(int defense) {
        move.setDefense(defense);
        return this;
    }
    
    /**
     * Sets the healing amount.
     * 
     * @param health The healing amount
     * @return This builder for method chaining
     */
    public MoveBuilder health(int health) {
        move.setHealth(health);
        return this;
    }
    
    /**
     * Sets the status effect type.
     * 
     * @param effect The effect type
     * @return This builder for method chaining
     */
    public MoveBuilder effect(String effect) {
        move.setEffect(effect);
        return this;
    }
    
    /**
     * Sets the duration for status effects.
     * 
     * @param duration The duration in turns
     * @return This builder for method chaining
     */
    public MoveBuilder duration(int duration) {
        move.setDuration(duration);
        return this;
    }
    
    /**
     * Sets the energy cost.
     * 
     * @param energyCost The energy cost
     * @return This builder for method chaining
     */
    public MoveBuilder energyCost(int energyCost) {
        move.setEnergyCost(energyCost);
        return this;
    }
    
    /**
     * Sets the cooldown.
     * 
     * @param cooldown The cooldown in turns
     * @return This builder for method chaining
     */
    public MoveBuilder cooldown(int cooldown) {
        move.setCooldown(cooldown);
        return this;
    }
    
    /**
     * Sets the move description.
     * 
     * @param description The description
     * @return This builder for method chaining
     */
    public MoveBuilder description(String description) {
        move.setDescription(description);
        return this;
    }
    
    /**
     * Sets the message displayed when the move is executed.
     * 
     * @param message The message
     * @return This builder for method chaining
     */
    public MoveBuilder message(String message) {
        move.setMessage(message);
        return this;
    }
    
    /**
     * Sets the message displayed when an afflicted character attempts to act.
     * 
     * @param afflictedMessage The afflicted message
     * @return This builder for method chaining
     */
    public MoveBuilder afflictedMessage(String afflictedMessage) {
        move.setAfflictedMessage(afflictedMessage);
        return this;
    }
    
    /**
     * Sets the sprite key to display when the move is executed.
     * 
     * @param spriteKey The sprite key
     * @return This builder for method chaining
     */
    public MoveBuilder spriteKey(String spriteKey) {
        move.setSpriteKey(spriteKey);
        return this;
    }
    
    /**
     * Builds and returns the Move object.
     * 
     * @return The constructed Move object
     */
    public Move build() {
        return move;
    }
}

