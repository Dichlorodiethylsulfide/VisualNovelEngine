package com.visualnovel.model.battle;

/**
 * Interface for selecting targets during battle.
 * Different strategies can be implemented (e.g., first alive, weakest, random).
 */
public interface TargetingStrategy {
    /**
     * Selects a target for the attacker from the opposing team.
     * 
     * @param attacker The attacking character
     * @param state The battle state
     * @return The selected target character, or null if no valid target
     */
    BattleCharacter selectTarget(BattleCharacter attacker, BattleState state);
}

