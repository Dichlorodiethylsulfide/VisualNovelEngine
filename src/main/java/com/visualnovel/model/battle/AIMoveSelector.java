package com.visualnovel.model.battle;

import java.util.List;
import java.util.Random;

/**
 * Simple AI move selector that chooses a random affordable move.
 * If no moves are affordable, returns null (skip turn).
 */
public class AIMoveSelector {
    private Random random;
    
    public AIMoveSelector() {
        this.random = new Random();
    }
    
    /**
     * Selects a move for an AI character.
     * Chooses randomly from affordable moves.
     * 
     * @param character The character to select a move for
     * @return The name of the selected move, or null if no affordable moves are available
     */
    public String selectMove(BattleCharacter character) {
        if (character == null) {
            return null;
        }
        
        // Get affordable moves
        List<String> affordableMoves = character.getAffordableMoves();
        
        if (affordableMoves == null || affordableMoves.isEmpty()) {
            // No affordable moves available - skip turn
            return null;
        }
        
        // Choose a random affordable move
        int randomIndex = random.nextInt(affordableMoves.size());
        return affordableMoves.get(randomIndex);
    }
}

