package com.visualnovel.util;

import com.google.gson.JsonObject;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.util.SpriteDefinitionParser.SpriteParseResult;

import java.util.Map;

/**
 * Utility class for parsing IdentifiableObject instances from JSON.
 * Uses JsonObjectReader and SpriteDefinitionParser for common parsing patterns.
 */
public class IdentifiableObjectParser {
    
    /**
     * Parses a single IdentifiableObject from a JsonObject.
     * 
     * @param idObj The JsonObject containing the IdentifiableObject data
     * @return The parsed IdentifiableObject instance
     */
    public static IdentifiableObject parse(JsonObject idObj) {
        IdentifiableObject identifiableObject = new IdentifiableObject();
        
        // Parse category
        String category = JsonObjectReader.parseOptionalString(idObj, "category");
        if (category != null) {
            identifiableObject.setCategory(category);
        }
        
        // Parse type
        String type = JsonObjectReader.parseOptionalString(idObj, "type");
        if (type != null) {
            identifiableObject.setType(type);
        }
        
        // Parse display_name
        String displayName = JsonObjectReader.parseOptionalString(idObj, "display_name");
        if (displayName != null) {
            identifiableObject.setDisplayName(displayName);
        }
        
        // Parse sprites
        if (idObj.has("sprites") && idObj.get("sprites").isJsonObject()) {
            JsonObject spritesObj = idObj.getAsJsonObject("sprites");
            SpriteParseResult spriteResult = SpriteDefinitionParser.parseSprites(spritesObj);
            identifiableObject.setSprites(spriteResult.getSprites());
        }
        
        // Parse stats (optional)
        Map<String, Number> stats = JsonObjectReader.parseNumberMap(idObj, "stats");
        if (stats != null) {
            identifiableObject.setStats(stats);
        }
        
        // Parse moves (optional)
        Map<String, com.visualnovel.model.battle.Move> moves = JsonObjectReader.parseMovesMap(idObj, "moves");
        if (moves != null) {
            identifiableObject.setMoves(moves);
        }
        
        // Parse inventory (optional)
        Map<String, com.visualnovel.model.InventoryItem> inventory = JsonObjectReader.parseInventoryMap(idObj, "inventory");
        if (inventory != null) {
            identifiableObject.setInventory(inventory);
        }
        
        return identifiableObject;
    }
}

