package com.visualnovel.model;

import com.visualnovel.model.actions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for action types. Allows dynamic registration of custom action types.
 */
public class ActionRegistry {
    private static ActionRegistry instance;
    private final Map<String, ActionType> actions;
    
    private ActionRegistry() {
        actions = new HashMap<>();
        registerDefaultActions();
    }
    
    /**
     * Gets the singleton instance of the ActionRegistry.
     * 
     * @return The ActionRegistry instance
     */
    public static synchronized ActionRegistry getInstance() {
        if (instance == null) {
            instance = new ActionRegistry();
        }
        return instance;
    }
    
    /**
     * Registers a new action type. If an action with the same JSON value already exists,
     * it will be replaced.
     * 
     * @param actionType The action type to register
     */
    public void register(ActionType actionType) {
        actions.put(actionType.getJsonValue(), actionType);
    }
    
    /**
     * Gets an action type by its JSON value.
     * 
     * @param jsonValue The JSON string identifier
     * @return The ActionType, or null if not found
     */
    public ActionType get(String jsonValue) {
        return actions.get(jsonValue);
    }
    
    /**
     * Checks if an action type is registered.
     * 
     * @param jsonValue The JSON string identifier
     * @return true if the action type is registered, false otherwise
     */
    public boolean isRegistered(String jsonValue) {
        return actions.containsKey(jsonValue);
    }
    
    /**
     * Registers the default action types that come with the game.
     */
    private void registerDefaultActions() {
        register(new ShowSpriteAction());
        register(new HideSpriteAction());
        register(new ClearAllSpritesAction());
        register(new ShowMessageAction());
        register(new DelayAction());
        register(new BattleAction());
        register(new EndScenarioAction());
        register(new ChooseAction());
        register(new ContinueAction());
        register(new AddInventoryItemAction());
        register(new RemoveInventoryItemAction());
    }
}

