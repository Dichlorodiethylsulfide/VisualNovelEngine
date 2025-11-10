package com.visualnovel.planner;

import com.visualnovel.model.Timing;

/**
 * Builder for creating Timing objects.
 */
public class TimingBuilder {
    private Timing timing;
    
    public TimingBuilder() {
        this.timing = new Timing();
    }
    
    /**
     * Sets the timing type to Immediate.
     * 
     * @return This builder for method chaining
     */
    public TimingBuilder immediate() {
        timing.setType(TimingType.IMMEDIATE.getJsonValue());
        return this;
    }
    
    /**
     * Sets the timing type to Interaction.
     * 
     * @return This builder for method chaining
     */
    public TimingBuilder interaction() {
        timing.setType(TimingType.INTERACTION.getJsonValue());
        return this;
    }
    
    /**
     * Sets the timing type to Animated with the specified duration and animation.
     * 
     * @param durationMs The duration of the animation in milliseconds
     * @param animation The animation type (e.g., "fadeIn", "flash")
     * @return This builder for method chaining
     */
    public TimingBuilder animated(int durationMs, String animation) {
        timing.setType(TimingType.ANIMATED.getJsonValue());
        timing.setDurationMs(durationMs);
        timing.setAnimation(animation);
        return this;
    }
    
    /**
     * Sets the timing type using a TimingType enum.
     * 
     * @param type The timing type
     * @return This builder for method chaining
     */
    public TimingBuilder type(TimingType type) {
        timing.setType(type.getJsonValue());
        return this;
    }
    
    /**
     * Sets the duration for animated timing.
     * 
     * @param durationMs The duration in milliseconds
     * @return This builder for method chaining
     */
    public TimingBuilder durationMs(int durationMs) {
        timing.setDurationMs(durationMs);
        return this;
    }
    
    /**
     * Sets the animation type.
     * 
     * @param animation The animation type (e.g., "fadeIn", "flash")
     * @return This builder for method chaining
     */
    public TimingBuilder animation(String animation) {
        timing.setAnimation(animation);
        return this;
    }
    
    /**
     * Builds and returns the Timing object.
     * 
     * @return The constructed Timing object
     */
    public Timing build() {
        return timing;
    }
}

