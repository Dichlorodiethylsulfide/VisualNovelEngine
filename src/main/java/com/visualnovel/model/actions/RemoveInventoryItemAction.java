package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.InventoryItem;
import com.visualnovel.model.ScenarioAction;

import java.util.Map;

/**
 * Action that removes an item from a character's inventory.
 * If quantity reaches zero, the item is removed completely.
 */
public class RemoveInventoryItemAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "RemoveInventoryItem"
     */
    public static String getName() {
        return "RemoveInventoryItem";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        Map<String, Object> params = action.getParameters();
        if (params == null) {
            System.err.println("Warning: RemoveInventoryItem action has no parameters.");
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
            System.err.println("Warning: RemoveInventoryItem action missing or invalid characterId parameter.");
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
            System.err.println("Warning: RemoveInventoryItem action missing or invalid itemName parameter.");
            controller.processNextAction();
            return;
        }
        
        // Get quantity parameter (default to 1 if not specified)
        int quantity = 1;
        Object quantityObj = params.get("quantity");
        if (quantityObj instanceof Number) {
            quantity = ((Number) quantityObj).intValue();
        } else if (quantityObj != null) {
            System.err.println("Warning: RemoveInventoryItem action has invalid quantity parameter, defaulting to 1.");
        }
        
        // Get character inventory
        Map<String, InventoryItem> inventory = controller.getCharacterInventory(characterId);
        if (inventory == null || inventory.isEmpty()) {
            // No inventory or empty inventory, nothing to remove
            controller.processNextAction();
            return;
        }
        
        // Find and remove item
        InventoryItem item = inventory.get(itemName);
        if (item != null) {
            // Item exists, remove quantity
            int newQuantity = item.removeQuantity(quantity);
            
            // If quantity reaches zero, remove item completely
            if (newQuantity <= 0) {
                inventory.remove(itemName);
            }
        }
        // If item doesn't exist, silently do nothing (item already not in inventory)
        
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

