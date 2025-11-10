package com.visualnovel.model.battle;

/**
 * Represents a move that a character can use in battle.
 * Each move has a type, effects, energy cost, and cooldown.
 */
public class Move {
    private String type; // "physical", "defense", "healing", "status_effect"
    private Integer damage; // Optional: damage amount for physical moves
    private Integer defense; // Optional: defense boost for defense moves
    private Integer health; // Optional: healing amount for healing moves
    private String effect; // Optional: status effect type ("stun", "poison", "confuse", etc.)
    private Integer duration; // Optional: duration in turns for status effects
    private int cooldown; // Cooldown turns
    private int energyCost; // Energy cost to use
    private String description; // Move description
    private String message; // Optional: message to display when move is executed
    private String afflictedMessage; // Optional: message to display when afflicted character attempts to act
    private String spriteKey; // Optional: sprite key to display when move is executed
    
    public Move() {
        // Default constructor
    }
    
    public Move(String type, int energyCost, int cooldown, String description) {
        this.type = type;
        this.energyCost = energyCost;
        this.cooldown = cooldown;
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getDamage() {
        return damage;
    }
    
    public void setDamage(Integer damage) {
        this.damage = damage;
    }
    
    public Integer getDefense() {
        return defense;
    }
    
    public void setDefense(Integer defense) {
        this.defense = defense;
    }
    
    public Integer getHealth() {
        return health;
    }
    
    public void setHealth(Integer health) {
        this.health = health;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
    
    public int getEnergyCost() {
        return energyCost;
    }
    
    public void setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEffect() {
        return effect;
    }
    
    public void setEffect(String effect) {
        this.effect = effect;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getAfflictedMessage() {
        return afflictedMessage;
    }
    
    public void setAfflictedMessage(String afflictedMessage) {
        this.afflictedMessage = afflictedMessage;
    }
    
    public String getSpriteKey() {
        return spriteKey;
    }
    
    public void setSpriteKey(String spriteKey) {
        this.spriteKey = spriteKey;
    }
}

