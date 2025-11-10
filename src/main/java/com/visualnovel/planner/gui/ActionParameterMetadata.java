package com.visualnovel.planner.gui;

import com.visualnovel.model.ActionRegistry;
import com.visualnovel.model.ActionType;

import java.util.*;

/**
 * Metadata system for action parameters.
 * Uses reflection to discover available actions and provides parameter definitions.
 */
public class ActionParameterMetadata {
    
    /**
     * Represents a parameter definition for an action.
     */
    public static class ParameterDef {
        private final String name;
        private final Class<?> type;
        private final boolean required;
        private final String description;
        private final List<String> allowedValues; // For enum-like parameters
        
        public ParameterDef(String name, Class<?> type, boolean required, String description) {
            this(name, type, required, description, null);
        }
        
        public ParameterDef(String name, Class<?> type, boolean required, String description, List<String> allowedValues) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.description = description;
            this.allowedValues = allowedValues;
        }
        
        public String getName() {
            return name;
        }
        
        public Class<?> getType() {
            return type;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<String> getAllowedValues() {
            return allowedValues;
        }
    }
    
    private static final Map<String, List<ParameterDef>> PARAMETER_METADATA = new HashMap<>();
    
    static {
        initializeParameterMetadata();
    }
    
    /**
     * Initializes parameter metadata for all known action types.
     */
    private static void initializeParameterMetadata() {
        // ShowMessage
        PARAMETER_METADATA.put("ShowMessage", Arrays.asList(
            new ParameterDef("context", String.class, true, "Message context (e.g., 'Narrator', 'Player')"),
            new ParameterDef("message", String.class, true, "The message text to display")
        ));
        
        // ShowSprite
        List<String> positions = Arrays.asList("Left", "Right", "Back", "Center");
        PARAMETER_METADATA.put("ShowSprite", Arrays.asList(
            new ParameterDef("id", String.class, true, "Object ID from the ids dictionary"),
            new ParameterDef("position", String.class, true, "Sprite position", positions),
            new ParameterDef("sprite_key", String.class, false, "Sprite key (defaults to 'default')"),
            new ParameterDef("depth", Integer.class, false, "Depth for layering (defaults to 0)"),
            new ParameterDef("offset", Integer.class, false, "Horizontal offset in pixels (defaults to 0)")
        ));
        
        // HideSprite
        List<String> hidePositions = Arrays.asList("Left", "Right", "Back", "Center");
        PARAMETER_METADATA.put("HideSprite", Arrays.asList(
            new ParameterDef("id", String.class, false, "Object ID to hide"),
            new ParameterDef("position", String.class, false, "Sprite position to hide", hidePositions)
        ));
        
        // Battle
        PARAMETER_METADATA.put("Battle", Arrays.asList(
            new ParameterDef("team1", List.class, true, "List of character IDs for team 1"),
            new ParameterDef("team2", List.class, true, "List of character IDs for team 2"),
            new ParameterDef("playerTeam", String.class, true, "Player team ID ('team1' or 'team2')", Arrays.asList("team1", "team2")),
            new ParameterDef("OnWin", Object.class, false, "OnWin handler (file path String or action Map)"),
            new ParameterDef("OnLose", Object.class, false, "OnLose handler (file path String or action Map)")
        ));
        
        // ChooseAction
        PARAMETER_METADATA.put("ChooseAction", Arrays.asList(
            new ParameterDef("context", String.class, true, "Message context"),
            new ParameterDef("message", String.class, true, "The choice prompt message"),
            new ParameterDef("options", List.class, true, "List of choice options (each with 'text' and 'onPress')")
        ));
        
        // Delay
        PARAMETER_METADATA.put("Delay", Arrays.asList(
            new ParameterDef("delayMs", Integer.class, true, "Delay duration in milliseconds")
        ));
        
        // EndScenarioAction
        PARAMETER_METADATA.put("EndScenarioAction", Arrays.asList(
            new ParameterDef("restart", String.class, false, "Restart parameter ('allowed' to allow restart)")
        ));
        
        // ContinueAction
        PARAMETER_METADATA.put("ContinueAction", Collections.emptyList());
        
        // ClearAllSprites
        PARAMETER_METADATA.put("ClearAllSprites", Collections.emptyList());
        
        // AddInventoryItem
        PARAMETER_METADATA.put("AddInventoryItem", Arrays.asList(
            new ParameterDef("characterId", String.class, true, "Character ID to add item to"),
            new ParameterDef("itemName", String.class, true, "Name of the item to add"),
            new ParameterDef("quantity", Integer.class, false, "Quantity to add (defaults to 1)"),
            new ParameterDef("description", String.class, false, "Item description"),
            new ParameterDef("type", String.class, false, "Item type")
        ));
        
        // RemoveInventoryItem
        PARAMETER_METADATA.put("RemoveInventoryItem", Arrays.asList(
            new ParameterDef("characterId", String.class, true, "Character ID to remove item from"),
            new ParameterDef("itemName", String.class, true, "Name of the item to remove"),
            new ParameterDef("quantity", Integer.class, false, "Quantity to remove (defaults to 1)")
        ));
    }
    
    /**
     * Gets all available action types from the ActionRegistry.
     * 
     * @return List of action type names (JSON values)
     */
    public static List<String> getAvailableActions() {
        ActionRegistry registry = ActionRegistry.getInstance();
        List<String> actions = new ArrayList<>();
        
        // Use reflection to get all registered actions
        try {
            // Access the private actions map via reflection
            java.lang.reflect.Field actionsField = ActionRegistry.class.getDeclaredField("actions");
            actionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ActionType> actionsMap = (Map<String, ActionType>) actionsField.get(registry);
            
            actions.addAll(actionsMap.keySet());
            Collections.sort(actions);
        } catch (Exception e) {
            // Fallback: manually list known actions
            actions.addAll(Arrays.asList(
                "ShowMessage", "ShowSprite", "HideSprite", "ClearAllSprites", "Battle",
                "ChooseAction", "Delay", "EndScenarioAction", "ContinueAction",
                "AddInventoryItem", "RemoveInventoryItem"
            ));
        }
        
        return actions;
    }
    
    /**
     * Gets parameter definitions for a specific action type.
     * 
     * @param actionType The action type name (JSON value)
     * @return List of parameter definitions, or empty list if not found
     */
    public static List<ParameterDef> getParametersForAction(String actionType) {
        return PARAMETER_METADATA.getOrDefault(actionType, Collections.emptyList());
    }
    
    /**
     * Gets a specific parameter definition.
     * 
     * @param actionType The action type name
     * @param parameterName The parameter name
     * @return The parameter definition, or null if not found
     */
    public static ParameterDef getParameterDef(String actionType, String parameterName) {
        List<ParameterDef> params = getParametersForAction(actionType);
        return params.stream()
            .filter(p -> p.getName().equals(parameterName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Checks if an action type has parameter metadata defined.
     * 
     * @param actionType The action type name
     * @return true if metadata exists, false otherwise
     */
    public static boolean hasMetadata(String actionType) {
        return PARAMETER_METADATA.containsKey(actionType);
    }
}

