package com.visualnovel.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for parsing common JSON patterns.
 * Provides reusable methods for extracting data from JsonObject instances.
 */
public class JsonObjectReader {
    
    /**
     * Parses a string-to-string map from a JsonObject.
     * 
     * @param obj The JsonObject containing the map
     * @param key The key of the map in the JsonObject
     * @return A Map of string keys to string values, or null if the key doesn't exist or is not an object
     */
    public static Map<String, String> parseStringMap(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonObject()) {
            return null;
        }
        
        JsonObject mapObj = obj.getAsJsonObject(key);
        Map<String, String> result = new HashMap<>();
        
        for (Map.Entry<String, JsonElement> entry : mapObj.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                result.put(entry.getKey(), value.getAsString());
            }
        }
        
        return result;
    }
    
    /**
     * Parses a string-to-number map from a JsonObject.
     * 
     * @param obj The JsonObject containing the map
     * @param key The key of the map in the JsonObject
     * @return A Map of string keys to Number values, or null if the key doesn't exist or is not an object
     */
    public static Map<String, Number> parseNumberMap(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonObject()) {
            return null;
        }
        
        JsonObject mapObj = obj.getAsJsonObject(key);
        Map<String, Number> result = new HashMap<>();
        
        for (Map.Entry<String, JsonElement> entry : mapObj.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                result.put(entry.getKey(), value.getAsNumber());
            }
        }
        
        return result;
    }
    
    /**
     * Parses a string array from a JsonObject.
     * 
     * @param obj The JsonObject containing the array
     * @param key The key of the array in the JsonObject
     * @return A List of strings, or null if the key doesn't exist or is not an array
     */
    public static List<String> parseStringList(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonArray()) {
            return null;
        }
        
        List<String> result = new ArrayList<>();
        for (JsonElement element : obj.getAsJsonArray(key)) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                result.add(element.getAsString());
            }
        }
        
        return result;
    }
    
    /**
     * Parses an optional string from a JsonObject.
     * 
     * @param obj The JsonObject containing the string
     * @param key The key of the string in the JsonObject
     * @return The string value, or null if the key doesn't exist or is not a string
     */
    public static String parseOptionalString(JsonObject obj, String key) {
        if (!obj.has(key)) {
            return null;
        }
        
        JsonElement element = obj.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        
        return null;
    }
    
    /**
     * Parses an optional integer from a JsonObject.
     * 
     * @param obj The JsonObject containing the integer
     * @param key The key of the integer in the JsonObject
     * @return The integer value, or null if the key doesn't exist or is not a number
     */
    public static Integer parseOptionalInt(JsonObject obj, String key) {
        if (!obj.has(key)) {
            return null;
        }
        
        JsonElement element = obj.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        
        return null;
    }
    
    /**
     * Parses an optional boolean from a JsonObject.
     * 
     * @param obj The JsonObject containing the boolean
     * @param key The key of the boolean in the JsonObject
     * @return The boolean value, or null if the key doesn't exist or is not a boolean
     */
    public static Boolean parseOptionalBoolean(JsonObject obj, String key) {
        if (!obj.has(key)) {
            return null;
        }
        
        JsonElement element = obj.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }
        
        return null;
    }
    
    /**
     * Parses a moves map from a JsonObject.
     * Each move is parsed as a Move object with type, damage, defense, health, cooldown, energy_cost, and description.
     * 
     * @param obj The JsonObject containing the moves
     * @param key The key of the moves in the JsonObject
     * @return A Map of string keys (move names) to Move values, or null if the key doesn't exist or is not an object
     */
    public static java.util.Map<String, com.visualnovel.model.battle.Move> parseMovesMap(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonObject()) {
            return null;
        }
        
        JsonObject movesObj = obj.getAsJsonObject(key);
        java.util.Map<String, com.visualnovel.model.battle.Move> result = new HashMap<>();
        
        for (Map.Entry<String, JsonElement> entry : movesObj.entrySet()) {
            String moveName = entry.getKey();
            JsonElement moveElement = entry.getValue();
            
            if (moveElement.isJsonObject()) {
                JsonObject moveObj = moveElement.getAsJsonObject();
                com.visualnovel.model.battle.Move move = new com.visualnovel.model.battle.Move();
                
                // Parse type
                String type = parseOptionalString(moveObj, "type");
                if (type != null) {
                    move.setType(type);
                }
                
                // Parse damage (optional)
                Integer damage = parseOptionalInt(moveObj, "damage");
                if (damage != null) {
                    move.setDamage(damage);
                }
                
                // Parse defense (optional)
                Integer defense = parseOptionalInt(moveObj, "defense");
                if (defense != null) {
                    move.setDefense(defense);
                }
                
                // Parse health (optional)
                Integer health = parseOptionalInt(moveObj, "health");
                if (health != null) {
                    move.setHealth(health);
                }
                
                // Parse cooldown
                Integer cooldown = parseOptionalInt(moveObj, "cooldown");
                if (cooldown != null) {
                    move.setCooldown(cooldown);
                } else {
                    move.setCooldown(0); // Default cooldown
                }
                
                // Parse energy_cost
                Integer energyCost = parseOptionalInt(moveObj, "energy_cost");
                if (energyCost != null) {
                    move.setEnergyCost(energyCost);
                } else {
                    move.setEnergyCost(0); // Default energy cost
                }
                
                // Parse description
                String description = parseOptionalString(moveObj, "description");
                if (description != null) {
                    move.setDescription(description);
                }
                
                // Parse effect (optional) - for status effect moves
                String effect = parseOptionalString(moveObj, "effect");
                if (effect != null) {
                    move.setEffect(effect);
                }
                
                // Parse duration (optional) - for status effect moves
                Integer duration = parseOptionalInt(moveObj, "duration");
                if (duration != null) {
                    move.setDuration(duration);
                }
                
                // Parse message (optional) - for status effect moves
                String message = parseOptionalString(moveObj, "message");
                if (message != null) {
                    move.setMessage(message);
                }
                
                // Parse afflicted_message (optional) - for status effect moves
                String afflictedMessage = parseOptionalString(moveObj, "afflicted_message");
                if (afflictedMessage != null) {
                    move.setAfflictedMessage(afflictedMessage);
                }
                
                // Parse sprite_key (optional) - determines which attack sprite to display
                String spriteKey = parseOptionalString(moveObj, "sprite_key");
                if (spriteKey != null) {
                    move.setSpriteKey(spriteKey);
                }
                
                result.put(moveName, move);
            }
        }
        
        return result;
    }
    
    /**
     * Parses an inventory map from a JsonObject.
     * The inventory structure is: { "items": [ { "name": "...", "description": "...", "type": "...", "quantity": ... } ] }
     * Items are keyed by their name in the returned map.
     * 
     * @param obj The JsonObject containing the inventory
     * @param key The key of the inventory in the JsonObject
     * @return A Map of string keys (item names) to InventoryItem values, or null if the key doesn't exist or is not an object
     */
    public static java.util.Map<String, com.visualnovel.model.InventoryItem> parseInventoryMap(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonObject()) {
            return null;
        }
        
        JsonObject inventoryObj = obj.getAsJsonObject(key);
        if (!inventoryObj.has("items") || !inventoryObj.get("items").isJsonArray()) {
            return null;
        }
        
        java.util.Map<String, com.visualnovel.model.InventoryItem> result = new HashMap<>();
        
        for (JsonElement itemElement : inventoryObj.getAsJsonArray("items")) {
            if (itemElement.isJsonObject()) {
                JsonObject itemObj = itemElement.getAsJsonObject();
                com.visualnovel.model.InventoryItem item = new com.visualnovel.model.InventoryItem();
                
                // Parse name (required)
                String name = parseOptionalString(itemObj, "name");
                if (name == null || name.isEmpty()) {
                    continue; // Skip items without names
                }
                item.setName(name);
                
                // Parse description (optional)
                String description = parseOptionalString(itemObj, "description");
                if (description != null) {
                    item.setDescription(description);
                }
                
                // Parse type (optional)
                String type = parseOptionalString(itemObj, "type");
                if (type != null) {
                    item.setType(type);
                }
                
                // Parse quantity (required, default to 1)
                Integer quantity = parseOptionalInt(itemObj, "quantity");
                if (quantity != null) {
                    item.setQuantity(quantity);
                } else {
                    item.setQuantity(1); // Default quantity
                }
                
                result.put(name, item);
            }
        }
        
        return result.isEmpty() ? null : result;
    }
}

