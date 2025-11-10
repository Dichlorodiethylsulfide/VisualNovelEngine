package com.visualnovel.model;

/**
 * Represents an item in a character's inventory.
 */
public class InventoryItem {
    private String name;
    private String description;
    private String type;
    private int quantity;
    
    public InventoryItem() {
        // Default constructor
    }
    
    public InventoryItem(String name, String description, String type, int quantity) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.quantity = quantity;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    /**
     * Adds to the quantity of this item.
     * 
     * @param amount The amount to add
     */
    public void addQuantity(int amount) {
        this.quantity += amount;
    }
    
    /**
     * Removes from the quantity of this item.
     * 
     * @param amount The amount to remove
     * @return The new quantity after removal
     */
    public int removeQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
        return this.quantity;
    }
}

