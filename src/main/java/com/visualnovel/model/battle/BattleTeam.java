package com.visualnovel.model.battle;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team with multiple characters.
 * Tracks team health status and identifies if team contains player.
 */
public class BattleTeam {
    private String teamId;
    private List<BattleCharacter> characters;
    private boolean containsPlayer;
    
    public BattleTeam(String teamId) {
        this.teamId = teamId;
        this.characters = new ArrayList<>();
        this.containsPlayer = false;
    }
    
    public String getTeamId() {
        return teamId;
    }
    
    public List<BattleCharacter> getCharacters() {
        return characters;
    }
    
    public void addCharacter(BattleCharacter character) {
        characters.add(character);
    }
    
    public boolean containsPlayer() {
        return containsPlayer;
    }
    
    public void setContainsPlayer(boolean containsPlayer) {
        this.containsPlayer = containsPlayer;
    }
    
    /**
     * Gets all alive characters in the team.
     * 
     * @return List of alive characters
     */
    public List<BattleCharacter> getAliveCharacters() {
        List<BattleCharacter> alive = new ArrayList<>();
        for (BattleCharacter character : characters) {
            if (character.isAlive()) {
                alive.add(character);
            }
        }
        return alive;
    }
    
    /**
     * Checks if the team has any alive characters.
     * 
     * @return true if at least one character is alive, false otherwise
     */
    public boolean hasAliveCharacters() {
        return !getAliveCharacters().isEmpty();
    }
    
    /**
     * Gets the character with the highest speed among alive characters.
     * 
     * @return The fastest alive character, or null if no alive characters
     */
    public BattleCharacter getFastestAliveCharacter() {
        List<BattleCharacter> alive = getAliveCharacters();
        if (alive.isEmpty()) {
            return null;
        }
        
        BattleCharacter fastest = alive.get(0);
        for (BattleCharacter character : alive) {
            if (character.getSpeed() > fastest.getSpeed()) {
                fastest = character;
            }
        }
        return fastest;
    }
}

