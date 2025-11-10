package com.visualnovel.model.battle;

/**
 * Stores persistent character stats (health and energy) between battles.
 * Used when battle_strategy is "persist_stats" to maintain state across battles.
 */
public class CharacterStats {
    private int currentHealth;
    private int maxHealth;
    private int currentEnergy;
    private int maxEnergy;
    
    public CharacterStats(int currentHealth, int maxHealth, int currentEnergy, int maxEnergy) {
        this.currentHealth = currentHealth;
        this.maxHealth = maxHealth;
        this.currentEnergy = currentEnergy;
        this.maxEnergy = maxEnergy;
    }
    
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.max(0, Math.min(currentHealth, maxHealth));
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        // Ensure current health doesn't exceed new max
        if (this.currentHealth > maxHealth) {
            this.currentHealth = maxHealth;
        }
    }
    
    public int getCurrentEnergy() {
        return currentEnergy;
    }
    
    public void setCurrentEnergy(int currentEnergy) {
        this.currentEnergy = Math.max(0, Math.min(currentEnergy, maxEnergy));
    }
    
    public int getMaxEnergy() {
        return maxEnergy;
    }
    
    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
        // Ensure current energy doesn't exceed new max
        if (this.currentEnergy > maxEnergy) {
            this.currentEnergy = maxEnergy;
        }
    }
}

