package com.visualnovel.model.battle;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks battle state (teams, active characters, turn order).
 * Manages character health during battle and stores battle configuration.
 */
public class BattleState {
    private List<BattleTeam> teams;
    private BattleTeam playerTeam;
    private int currentTurn;
    private boolean battleEnded;
    private BattleTeam winner;
    
    public BattleState() {
        this.teams = new ArrayList<>();
        this.currentTurn = 0;
        this.battleEnded = false;
    }
    
    public List<BattleTeam> getTeams() {
        return teams;
    }
    
    public void addTeam(BattleTeam team) {
        teams.add(team);
    }
    
    public BattleTeam getPlayerTeam() {
        return playerTeam;
    }
    
    public void setPlayerTeam(BattleTeam playerTeam) {
        this.playerTeam = playerTeam;
    }
    
    public int getCurrentTurn() {
        return currentTurn;
    }
    
    public void incrementTurn() {
        currentTurn++;
    }
    
    public boolean isBattleEnded() {
        return battleEnded;
    }
    
    public void setBattleEnded(boolean battleEnded) {
        this.battleEnded = battleEnded;
    }
    
    public BattleTeam getWinner() {
        return winner;
    }
    
    public void setWinner(BattleTeam winner) {
        this.winner = winner;
    }
    
    /**
     * Gets all alive characters from all teams, sorted by speed (highest first).
     * 
     * @return List of alive characters sorted by speed
     */
    public List<BattleCharacter> getAllAliveCharactersSortedBySpeed() {
        List<BattleCharacter> allAlive = new ArrayList<>();
        for (BattleTeam team : teams) {
            allAlive.addAll(team.getAliveCharacters());
        }
        
        // Sort by speed (highest first), then by character ID for consistency
        allAlive.sort((a, b) -> {
            int speedCompare = Integer.compare(b.getSpeed(), a.getSpeed());
            if (speedCompare != 0) {
                return speedCompare;
            }
            return a.getCharacterId().compareTo(b.getCharacterId());
        });
        
        return allAlive;
    }
    
    /**
     * Checks if the battle has ended (one team has no alive characters).
     * 
     * @return true if battle has ended, false otherwise
     */
    public boolean checkBattleEnd() {
        int teamsWithAliveCharacters = 0;
        BattleTeam lastAliveTeam = null;
        
        for (BattleTeam team : teams) {
            if (team.hasAliveCharacters()) {
                teamsWithAliveCharacters++;
                lastAliveTeam = team;
            }
        }
        
        if (teamsWithAliveCharacters <= 1) {
            battleEnded = true;
            if (teamsWithAliveCharacters == 1) {
                winner = lastAliveTeam;
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets the opposing team for a given team.
     * 
     * @param team The team to find the opponent for
     * @return The opposing team, or null if not found
     */
    public BattleTeam getOpposingTeam(BattleTeam team) {
        for (BattleTeam otherTeam : teams) {
            if (otherTeam != team) {
                return otherTeam;
            }
        }
        return null;
    }
}

