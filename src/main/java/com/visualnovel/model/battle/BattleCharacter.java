package com.visualnovel.model.battle;

import com.visualnovel.model.IdentifiableObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for IdentifiableObject with battle-specific state (current health, battle status).
 * Tracks if character is alive/defeated during battle.
 */
public class BattleCharacter {
    private IdentifiableObject identifiableObject;
    private String characterId;
    private int currentHealth;
    private int maxHealth;
    private boolean isAlive;
    private int currentEnergy;
    private int maxEnergy;
    private Map<String, StatusEffect> statusEffects; // Active status effects keyed by effect type
    
    public BattleCharacter(IdentifiableObject identifiableObject, String characterId) {
        this.identifiableObject = identifiableObject;
        this.characterId = characterId;
        
        // Initialize health from object stats
        if (identifiableObject != null && identifiableObject.getStats() != null && 
            identifiableObject.getStats().containsKey("health")) {
            Number healthStat = identifiableObject.getStats().get("health");
            this.maxHealth = healthStat.intValue();
            this.currentHealth = maxHealth;
        } else {
            this.maxHealth = 100; // Default health
            this.currentHealth = maxHealth;
        }
        
        // Initialize energy from object stats
        if (identifiableObject != null && identifiableObject.getStats() != null && 
            identifiableObject.getStats().containsKey("energy")) {
            Number energyStat = identifiableObject.getStats().get("energy");
            this.maxEnergy = energyStat.intValue();
            this.currentEnergy = maxEnergy;
        } else {
            this.maxEnergy = 100; // Default energy
            this.currentEnergy = maxEnergy;
        }
        
        this.isAlive = true;
        this.statusEffects = new HashMap<>();
    }
    
    /**
     * Constructor that accepts initial health and energy values.
     * Used when battle_strategy is "persist_stats" to restore character state.
     * 
     * @param identifiableObject The IdentifiableObject for this character
     * @param characterId The character ID
     * @param currentHealth Initial current health (null to use max health)
     * @param currentEnergy Initial current energy (null to use max energy)
     */
    public BattleCharacter(IdentifiableObject identifiableObject, String characterId, 
                          Integer currentHealth, Integer currentEnergy) {
        this.identifiableObject = identifiableObject;
        this.characterId = characterId;
        
        // Initialize health from object stats
        if (identifiableObject != null && identifiableObject.getStats() != null && 
            identifiableObject.getStats().containsKey("health")) {
            Number healthStat = identifiableObject.getStats().get("health");
            this.maxHealth = healthStat.intValue();
        } else {
            this.maxHealth = 100; // Default health
        }
        
        // Use provided currentHealth or default to maxHealth
        if (currentHealth != null) {
            this.currentHealth = Math.max(0, Math.min(currentHealth, maxHealth));
        } else {
            this.currentHealth = maxHealth;
        }
        
        // Initialize energy from object stats
        if (identifiableObject != null && identifiableObject.getStats() != null && 
            identifiableObject.getStats().containsKey("energy")) {
            Number energyStat = identifiableObject.getStats().get("energy");
            this.maxEnergy = energyStat.intValue();
        } else {
            this.maxEnergy = 100; // Default energy
        }
        
        // Use provided currentEnergy or default to maxEnergy
        if (currentEnergy != null) {
            this.currentEnergy = Math.max(0, Math.min(currentEnergy, maxEnergy));
        } else {
            this.currentEnergy = maxEnergy;
        }
        
        this.isAlive = this.currentHealth > 0;
        this.statusEffects = new HashMap<>();
    }
    
    public IdentifiableObject getIdentifiableObject() {
        return identifiableObject;
    }
    
    public String getCharacterId() {
        return characterId;
    }
    
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.max(0, currentHealth);
        this.isAlive = this.currentHealth > 0;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    /**
     * Applies damage to this character.
     * 
     * @param damage The amount of damage to apply
     * @return The actual damage dealt
     */
    public int takeDamage(int damage) {
        int oldHealth = currentHealth;
        setCurrentHealth(currentHealth - damage);
        return oldHealth - currentHealth;
    }
    
    /**
     * Gets a stat value from the object's stats.
     * 
     * @param statName The name of the stat
     * @return The stat value, or 0 if not found
     */
    public int getStat(String statName) {
        if (identifiableObject != null && identifiableObject.getStats() != null && 
            identifiableObject.getStats().containsKey(statName)) {
            Number stat = identifiableObject.getStats().get(statName);
            return stat.intValue();
        }
        return 0;
    }
    
    /**
     * Gets the character's speed stat.
     * 
     * @return The speed value
     */
    public int getSpeed() {
        return getStat("speed");
    }
    
    /**
     * Gets the character's attack stat.
     * 
     * @return The attack value
     */
    public int getAttack() {
        return getStat("attack");
    }
    
    /**
     * Gets the character's defense stat.
     * 
     * @return The defense value
     */
    public int getDefense() {
        return getStat("defense");
    }
    
    /**
     * Gets the character's name.
     * 
     * @return The character name (display_name from IdentifiableObject, or characterId as fallback)
     */
    public String getName() {
        if (identifiableObject != null && identifiableObject.getDisplayName() != null && 
            !identifiableObject.getDisplayName().isEmpty()) {
            return identifiableObject.getDisplayName();
        }
        return characterId;
    }
    
    /**
     * Gets the character's current energy.
     * 
     * @return The current energy value
     */
    public int getCurrentEnergy() {
        return currentEnergy;
    }
    
    /**
     * Gets the character's maximum energy.
     * 
     * @return The maximum energy value
     */
    public int getMaxEnergy() {
        return maxEnergy;
    }
    
    /**
     * Sets the character's current energy.
     * 
     * @param energy The energy value to set (clamped between 0 and maxEnergy)
     */
    public void setCurrentEnergy(int energy) {
        this.currentEnergy = Math.max(0, Math.min(energy, maxEnergy));
    }
    
    /**
     * Consumes energy from the character.
     * 
     * @param cost The energy cost to consume
     * @return true if energy was successfully consumed, false if not enough energy
     */
    public boolean consumeEnergy(int cost) {
        if (currentEnergy >= cost) {
            setCurrentEnergy(currentEnergy - cost);
            return true;
        }
        return false;
    }
    
    /**
     * Gets the character's moves from the IdentifiableObject.
     * 
     * @return A map of move names to Move objects, or null if no moves are available
     */
    public Map<String, Move> getMoves() {
        if (identifiableObject != null) {
            return identifiableObject.getMoves();
        }
        return null;
    }
    
    /**
     * Gets a list of moves that the character can afford with their current energy.
     * 
     * @return A list of move names that can be used
     */
    public List<String> getAffordableMoves() {
        List<String> affordableMoves = new ArrayList<>();
        Map<String, Move> moves = getMoves();
        
        if (moves != null) {
            for (Map.Entry<String, Move> entry : moves.entrySet()) {
                String moveName = entry.getKey();
                Move move = entry.getValue();
                
                if (move != null && currentEnergy >= move.getEnergyCost()) {
                    affordableMoves.add(moveName);
                }
            }
        }
        
        return affordableMoves;
    }
    
    /**
     * Adds a status effect to this character.
     * If an effect of the same type already exists, it is replaced.
     * 
     * @param effect The status effect to add
     */
    public void addStatusEffect(StatusEffect effect) {
        if (effect != null && effect.getEffectType() != null) {
            statusEffects.put(effect.getEffectType(), effect);
        }
    }
    
    /**
     * Removes a status effect of the specified type.
     * 
     * @param effectType The type of effect to remove
     */
    public void removeStatusEffect(String effectType) {
        if (effectType != null) {
            statusEffects.remove(effectType);
        }
    }
    
    /**
     * Checks if this character has a status effect of the specified type.
     * 
     * @param effectType The type of effect to check
     * @return true if the character has the effect, false otherwise
     */
    public boolean hasStatusEffect(String effectType) {
        if (effectType == null) {
            return false;
        }
        StatusEffect effect = statusEffects.get(effectType);
        return effect != null && !effect.isExpired();
    }
    
    /**
     * Gets the status effect of the specified type.
     * 
     * @param effectType The type of effect to get
     * @return The status effect, or null if not found or expired
     */
    public StatusEffect getStatusEffect(String effectType) {
        if (effectType == null) {
            return null;
        }
        StatusEffect effect = statusEffects.get(effectType);
        if (effect != null && !effect.isExpired()) {
            return effect;
        }
        return null;
    }
    
    /**
     * Processes all status effects, decrementing their durations and removing expired ones.
     * 
     * @return A list of effect types that expired this turn
     */
    public List<String> processStatusEffects() {
        List<String> expiredEffects = new ArrayList<>();
        List<String> effectsToRemove = new ArrayList<>();
        
        for (Map.Entry<String, StatusEffect> entry : statusEffects.entrySet()) {
            StatusEffect effect = entry.getValue();
            if (effect != null) {
                effect.decrementDuration();
                if (effect.isExpired()) {
                    expiredEffects.add(entry.getKey());
                    effectsToRemove.add(entry.getKey());
                }
            }
        }
        
        // Remove expired effects
        for (String effectType : effectsToRemove) {
            statusEffects.remove(effectType);
        }
        
        return expiredEffects;
    }
    
    /**
     * Checks if this character is stunned.
     * 
     * @return true if the character has a stun effect, false otherwise
     */
    public boolean isStunned() {
        return hasStatusEffect("stun");
    }
    
    /**
     * Gets all active status effects.
     * 
     * @return A map of effect types to status effects
     */
    public Map<String, StatusEffect> getStatusEffects() {
        return new HashMap<>(statusEffects);
    }
}

