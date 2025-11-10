package com.visualnovel.model;

/**
 * Enumeration of sprite types (Background, Character, LoseScreen, WinScreen).
 */
public enum SpriteType {
    BACKGROUND("Background"),
    CHARACTER("Character"),
    LOSE_SCREEN("LoseScreen"),
    WIN_SCREEN("WinScreen");
    
    private final String jsonValue;
    
    SpriteType(String jsonValue) {
        this.jsonValue = jsonValue;
    }
    
    public String getJsonValue() {
        return jsonValue;
    }
    
    public static SpriteType fromString(String value) {
        for (SpriteType type : SpriteType.values()) {
            if (type.jsonValue.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown sprite type: " + value);
    }
}

