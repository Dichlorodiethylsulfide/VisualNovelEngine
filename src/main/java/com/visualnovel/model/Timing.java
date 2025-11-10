package com.visualnovel.model;

import java.util.Map;

/**
 * Represents the timing configuration for a scenario action.
 * Can be Immediate, Interaction, or Animated.
 */
public class Timing {
    private String type; // "Immediate", "Interaction", or "Animated"
    private Integer durationMs; // Optional: duration for animated timing
    private String animation; // Optional: animation type (e.g., "fadeIn")
    private Map<String, Object> additionalProperties; // For any other timing properties
    
    public Timing() {
    }
    
    public Timing(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }
    
    public String getAnimation() {
        return animation;
    }
    
    public void setAnimation(String animation) {
        this.animation = animation;
    }
    
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
    
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
    
    /**
     * Checks if this timing requires user interaction.
     * 
     * @return true if type is "Interaction", false otherwise
     */
    public boolean requiresInteraction() {
        return "Interaction".equals(type);
    }
    
    /**
     * Checks if this timing is animated.5
     * 
     * @return true if type is "Animated", false otherwise
     */
    public boolean isAnimated() {
        return "Animated".equals(type);
    }
    
    /**
     * Checks if this timing is immediate.
     * 
     * @return true if type is "Immediate", false otherwise
     */
    public boolean isImmediate() {
        return "Immediate".equals(type);
    }
}

