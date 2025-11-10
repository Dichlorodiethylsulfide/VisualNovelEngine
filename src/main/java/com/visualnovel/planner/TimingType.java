package com.visualnovel.planner;

/**
 * Enum representing the timing types for scenario actions.
 */
public enum TimingType {
    IMMEDIATE("Immediate"),
    INTERACTION("Interaction"),
    ANIMATED("Animated");
    
    private final String jsonValue;
    
    TimingType(String jsonValue) {
        this.jsonValue = jsonValue;
    }
    
    /**
     * Gets the JSON string value for this timing type.
     * 
     * @return The JSON identifier string
     */
    public String getJsonValue() {
        return jsonValue;
    }
}

