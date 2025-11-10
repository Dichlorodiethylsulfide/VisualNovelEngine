package com.visualnovel.model;

/**
 * Enumeration of sprite positions on screen.
 */
public enum SpritePosition {
    BACK("Back"),
    LEFT("Left"),
    RIGHT("Right"),
    CENTER("Center");
    
    private final String jsonValue;
    
    SpritePosition(String jsonValue) {
        this.jsonValue = jsonValue;
    }
    
    public String getJsonValue() {
        return jsonValue;
    }
    
    public static SpritePosition fromString(String value) {
        for (SpritePosition pos : SpritePosition.values()) {
            if (pos.jsonValue.equals(value)) {
                return pos;
            }
        }
        throw new IllegalArgumentException("Unknown sprite position: " + value);
    }
}

