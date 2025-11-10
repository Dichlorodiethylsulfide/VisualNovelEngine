package com.visualnovel.model.battle;

import java.util.List;

/**
 * Default targeting strategy that selects the first alive character from the opposing team.
 */
public class FirstAliveTargetingStrategy implements TargetingStrategy {
    
    @Override
    public BattleCharacter selectTarget(BattleCharacter attacker, BattleState state) {
        // Find the team containing the attacker
        BattleTeam attackerTeam = null;
        for (BattleTeam team : state.getTeams()) {
            if (team.getCharacters().contains(attacker)) {
                attackerTeam = team;
                break;
            }
        }
        
        if (attackerTeam == null) {
            return null;
        }
        
        // Get opposing team
        BattleTeam opposingTeam = state.getOpposingTeam(attackerTeam);
        if (opposingTeam == null) {
            return null;
        }
        
        // Select first alive character from opposing team
        List<BattleCharacter> aliveOpponents = opposingTeam.getAliveCharacters();
        if (aliveOpponents.isEmpty()) {
            return null;
        }
        
        return aliveOpponents.get(0);
    }
}

