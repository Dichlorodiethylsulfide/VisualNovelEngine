package com.visualnovel.util;

import com.visualnovel.GameController;
import com.visualnovel.model.InventoryItem;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for evaluating requirement expressions in ChooseAction options.
 * Supports expressions like "inventory.Player.Gold >= 10"
 */
public class RequirementEvaluator {
    
    // Pattern to match: inventory.CharacterId.ItemName operator number
    // Example: "inventory.Player.Gold >= 10"
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
        "inventory\\.([^.]+)\\.([^.]+)\\s*(>=|<=|==|!=|>|<)\\s*(\\d+)"
    );
    
    /**
     * Evaluates a requirement expression.
     * 
     * @param expression The expression to evaluate (e.g., "inventory.Player.Gold >= 10")
     * @param controller The GameController to access character inventories
     * @return true if the requirement is met, false otherwise (or if expression is invalid)
     */
    public static boolean evaluate(String expression, GameController controller) {
        if (expression == null || expression.trim().isEmpty()) {
            // No requirement means always enabled
            return true;
        }
        
        if (controller == null) {
            // No controller means we can't evaluate - default to disabled (safe)
            return false;
        }
        
        expression = expression.trim();
        
        // Match the expression pattern
        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
        if (!matcher.matches()) {
            // Invalid expression format - default to disabled (safe)
            System.err.println("Warning: Invalid requirement expression format: " + expression);
            return false;
        }
        
        // Extract components
        String characterId = matcher.group(1).trim();
        String itemName = matcher.group(2).trim();
        String operator = matcher.group(3).trim();
        String valueStr = matcher.group(4).trim();
        
        // Parse the numeric value
        int requiredValue;
        try {
            requiredValue = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            System.err.println("Warning: Invalid number in requirement expression: " + valueStr);
            return false;
        }
        
        // Get character inventory
        Map<String, InventoryItem> inventory = controller.getCharacterInventory(characterId);
        
        // Get item quantity (0 if item doesn't exist)
        int itemQuantity = 0;
        if (inventory != null) {
            InventoryItem item = inventory.get(itemName);
            if (item != null) {
                itemQuantity = item.getQuantity();
            }
        }
        
        // Evaluate the comparison
        return evaluateComparison(itemQuantity, operator, requiredValue);
    }
    
    /**
     * Evaluates a comparison between two values.
     * 
     * @param leftValue The left side value
     * @param operator The comparison operator (>=, <=, ==, !=, >, <)
     * @param rightValue The right side value
     * @return true if the comparison is true, false otherwise
     */
    private static boolean evaluateComparison(int leftValue, String operator, int rightValue) {
        switch (operator) {
            case ">=":
                return leftValue >= rightValue;
            case "<=":
                return leftValue <= rightValue;
            case "==":
                return leftValue == rightValue;
            case "!=":
                return leftValue != rightValue;
            case ">":
                return leftValue > rightValue;
            case "<":
                return leftValue < rightValue;
            default:
                System.err.println("Warning: Unknown comparison operator: " + operator);
                return false;
        }
    }
}

