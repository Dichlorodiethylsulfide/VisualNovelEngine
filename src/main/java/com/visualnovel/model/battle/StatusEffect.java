package com.visualnovel.model.battle;

/**
 * Represents a status effect applied to a character in battle.
 * Status effects have a type (e.g., "stun", "poison", "confuse") and a duration in turns.
 */
public class StatusEffect {
    private String effectType; // Type of effect: "stun", "poison", "confuse", etc.
    private int duration; // Duration in turns
    private String source; // Optional: source of the effect (e.g., move name)
    private String afflictedMessage; // Optional: message to display when afflicted character attempts to act
    
    /**
     * Creates a new status effect.
     * 
     * @param effectType The type of effect ("stun", "poison", "confuse", etc.)
     * @param duration The duration in turns
     */
    public StatusEffect(String effectType, int duration) {
        this.effectType = effectType;
        this.duration = duration;
        this.source = null;
    }
    
    /**
     * Creates a new status effect with a source.
     * 
     * @param effectType The type of effect ("stun", "poison", "confuse", etc.)
     * @param duration The duration in turns
     * @param source The source of the effect (e.g., move name)
     */
    public StatusEffect(String effectType, int duration, String source) {
        this.effectType = effectType;
        this.duration = duration;
        this.source = source;
        this.afflictedMessage = null;
    }
    
    /**
     * Creates a new status effect with a source and afflicted message.
     * 
     * @param effectType The type of effect ("stun", "poison", "confuse", etc.)
     * @param duration The duration in turns
     * @param source The source of the effect (e.g., move name)
     * @param afflictedMessage The message to display when afflicted character attempts to act
     */
    public StatusEffect(String effectType, int duration, String source, String afflictedMessage) {
        this.effectType = effectType;
        this.duration = duration;
        this.source = source;
        this.afflictedMessage = afflictedMessage;
    }
    
    /**
     * Gets the effect type.
     * 
     * @return The effect type
     */
    public String getEffectType() {
        return effectType;
    }
    
    /**
     * Sets the effect type.
     * 
     * @param effectType The effect type
     */
    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }
    
    /**
     * Gets the duration in turns.
     * 
     * @return The duration in turns
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Sets the duration in turns.
     * 
     * @param duration The duration in turns
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    /**
     * Gets the source of the effect.
     * 
     * @return The source of the effect, or null if not set
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Sets the source of the effect.
     * 
     * @param source The source of the effect
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /**
     * Gets the afflicted message.
     * 
     * @return The afflicted message, or null if not set
     */
    public String getAfflictedMessage() {
        return afflictedMessage;
    }
    
    /**
     * Sets the afflicted message.
     * 
     * @param afflictedMessage The afflicted message
     */
    public void setAfflictedMessage(String afflictedMessage) {
        this.afflictedMessage = afflictedMessage;
    }
    
    /**
     * Decrements the duration by 1.
     */
    public void decrementDuration() {
        if (duration > 0) {
            duration--;
        }
    }
    
    /**
     * Checks if the effect has expired (duration <= 0).
     * 
     * @return true if the effect has expired, false otherwise
     */
    public boolean isExpired() {
        return duration <= 0;
    }
}

