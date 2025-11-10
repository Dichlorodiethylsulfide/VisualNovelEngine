package com.visualnovel.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for parsing sprite definitions from JSON.
 * Handles both regular sprite mappings and special "random" array case for characters.
 */
public class SpriteDefinitionParser {
    
    /**
     * Parses sprite definitions from a JsonObject.
     * Extracts sprite key-value pairs, handling the special "random" array case.
     * 
     * @param spritesObj The JsonObject containing sprite definitions
     * @return A Map of sprite keys to sprite values, and optionally a list of random sprites
     */
    public static SpriteParseResult parseSprites(JsonObject spritesObj) {
        if (spritesObj == null) {
            return new SpriteParseResult(new HashMap<>(), null);
        }
        
        Map<String, String> sprites = new HashMap<>();
        List<String> randomSprites = null;
        
        for (Map.Entry<String, JsonElement> spriteEntry : spritesObj.entrySet()) {
            String spriteKey = spriteEntry.getKey();
            JsonElement spriteValue = spriteEntry.getValue();
            
            // Handle "random" as a special case - it's an array
            if (spriteKey.equals("random") && spriteValue.isJsonArray()) {
                randomSprites = new ArrayList<>();
                for (JsonElement randomElement : spriteValue.getAsJsonArray()) {
                    if (randomElement.isJsonPrimitive() && randomElement.getAsJsonPrimitive().isString()) {
                        randomSprites.add(randomElement.getAsString());
                    }
                }
            } else if (spriteValue.isJsonPrimitive() && spriteValue.getAsJsonPrimitive().isString()) {
                // Regular sprite mapping (string to string)
                sprites.put(spriteKey, spriteValue.getAsString());
            }
        }
        
        return new SpriteParseResult(sprites, randomSprites);
    }
    
    /**
     * Result class for sprite parsing that includes both regular sprites and random sprites.
     */
    public static class SpriteParseResult {
        private final Map<String, String> sprites;
        private final List<String> randomSprites;
        
        public SpriteParseResult(Map<String, String> sprites, List<String> randomSprites) {
            this.sprites = sprites;
            this.randomSprites = randomSprites;
        }
        
        public Map<String, String> getSprites() {
            return sprites;
        }
        
        public List<String> getRandomSprites() {
            return randomSprites;
        }
    }
}

