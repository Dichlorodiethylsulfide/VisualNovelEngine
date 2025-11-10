package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.InventoryItem;
import com.visualnovel.model.ScenarioAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that adds an item to a character's inventory.
 * If the item already exists, increases its quantity.
 */
public class AddInventoryItemAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "AddInventoryItem"
     */
    public static String getName() {
        return "AddInventoryItem";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        Map<String, Object> params = action.getParameters();
        if (params == null) {
            System.err.println("Warning: AddInventoryItem action has no parameters.");
            controller.processNextAction();
            return;
        }
        
        // Get characterId parameter
        String characterId = null;
        Object characterIdObj = params.get("characterId");
        if (characterIdObj instanceof String) {
            characterId = (String) characterIdObj;
        }
        if (characterId == null || characterId.isEmpty()) {
            System.err.println("Warning: AddInventoryItem action missing or invalid characterId parameter.");
            controller.processNextAction();
            return;
        }
        
        // Get itemName parameter
        String itemName = null;
        Object itemNameObj = params.get("itemName");
        if (itemNameObj instanceof String) {
            itemName = (String) itemNameObj;
        }
        if (itemName == null || itemName.isEmpty()) {
            System.err.println("Warning: AddInventoryItem action missing or invalid itemName parameter.");
            controller.processNextAction();
            return;
        }
        
        // Get quantity parameter (default to 1 if not specified)
        int quantity = 1;
        Object quantityObj = params.get("quantity");
        if (quantityObj instanceof Number) {
            quantity = ((Number) quantityObj).intValue();
        } else if (quantityObj != null) {
            System.err.println("Warning: AddInventoryItem action has invalid quantity parameter, defaulting to 1.");
        }
        
        // Get or create character inventory
        Map<String, InventoryItem> inventory = controller.getCharacterInventory(characterId);
        if (inventory == null) {
            inventory = new HashMap<>();
            controller.setCharacterInventory(characterId, inventory);
        }
        
        // Add or update item
        InventoryItem item = inventory.get(itemName);
        if (item != null) {
            // Item exists, increase quantity
            item.addQuantity(quantity);
        } else {
            // Item doesn't exist, create new item
            // Try to get description and type from parameters, or use defaults
            String description = null;
            Object descObj = params.get("description");
            if (descObj instanceof String) {
                description = (String) descObj;
            }
            
            String type = null;
            Object typeObj = params.get("type");
            if (typeObj instanceof String) {
                type = (String) typeObj;
            }
            
            item = new InventoryItem(itemName, description, type, quantity);
            inventory.put(itemName, item);
        }
        
        controller.processNextAction();
    }
    
    @Override
    public boolean requiresUserInput() {
        return false;
    }
    
    @Override
    public boolean shouldContinueImmediately() {
        return false; // execute() calls processNextAction() directly
    }
}

