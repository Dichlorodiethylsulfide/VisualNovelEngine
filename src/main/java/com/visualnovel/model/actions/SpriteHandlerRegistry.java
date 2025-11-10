package com.visualnovel.model.actions;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for sprite handlers by sprite type.
 * Maps sprite type strings ("background", "character", "screen") to their handlers.
 */
public class SpriteHandlerRegistry {
    private static final Map<String, SpriteHandler> handlers = new HashMap<>();
    
    static {
        // Initialize default handlers
        handlers.put("background", new BackgroundSpriteHandler());
        handlers.put("character", new CharacterSpriteHandler());
        handlers.put("screen", new ScreenSpriteHandler());
    }
    
    /**
     * Gets the handler for a given sprite type.
     * 
     * @param spriteType The sprite type ("background", "character", "screen")
     * @return The handler for the sprite type, or null if not found
     */
    public static SpriteHandler getHandler(String spriteType) {
        return handlers.get(spriteType);
    }
    
    /**
     * Registers a custom handler for a sprite type.
     * 
     * @param spriteType The sprite type to register
     * @param handler The handler to use for this sprite type
     */
    public static void registerHandler(String spriteType, SpriteHandler handler) {
        handlers.put(spriteType, handler);
    }
}

