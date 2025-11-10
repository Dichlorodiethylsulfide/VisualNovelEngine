package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.battle.BattleManager;
import com.visualnovel.util.AssetManager;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Action that triggers a battle and loads the appropriate scenario file based on the outcome.
 */
public class BattleAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "Battle"
     */
    public static String getName() {
        return "Battle";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        // Parse battle parameters
        Map<String, Object> parameters = action.getParameters();
        if (parameters == null) {
            System.err.println("Error: Battle action has no parameters.");
            return;
        }
        
        // Get player team ID (default to "team1" if not specified)
        String playerTeamId = (String) parameters.get("playerTeam");
        if (playerTeamId == null || playerTeamId.isEmpty()) {
            playerTeamId = "team1"; // Default to team1
        }
        
        // Count how many of each character ID are in the battle
        Map<String, Integer> battleCharacterCounts = new HashMap<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Skip OnWin, OnLose, and playerTeam parameters
            if (key.equals("OnWin") || key.equals("OnLose") || key.equals("playerTeam")) {
                continue;
            }
            
            // Check if this is a team definition (array of character IDs)
            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> characterIds = (List<String>) value;
                for (String characterId : characterIds) {
                    battleCharacterCounts.put(characterId, 
                        battleCharacterCounts.getOrDefault(characterId, 0) + 1);
                }
            }
        }
        
        // Hide sprites based on battle character counts and ensure battle characters are visible
        VisualNovelGUI gui = controller.getGUI();
        if (gui != null) {
            // Get all objects from the main scenario's ids dictionary
            com.visualnovel.model.Scenario mainScenario = controller.getMainScenario();
            if (mainScenario != null && mainScenario.getIds() != null) {
                for (String objectId : mainScenario.getIds().keySet()) {
                    // Only process sprites for characters (not backgrounds or screens)
                    com.visualnovel.model.IdentifiableObject obj = mainScenario.getIds().get(objectId);
                    if (obj != null && "character".equals(obj.getType())) {
                        int battleCount = battleCharacterCounts.getOrDefault(objectId, 0);
                        // Hide excess sprites: keep only as many as are in the battle
                        gui.hideExcessSpritesByCharacterId(objectId, battleCount);
                    }
                }
            }
            
            // Ensure all characters in the battle have their sprites visible
            AssetManager assetManager = AssetManager.getInstance();
            for (Map.Entry<String, Integer> entry : battleCharacterCounts.entrySet()) {
                String characterId = entry.getKey();
                int requiredCount = entry.getValue();
                
                // Count how many sprites are currently visible for this character
                int visibleCount = gui.countVisibleSpritesByCharacterId(characterId);
                
                // If we need more sprites than are visible, show them
                if (requiredCount > visibleCount) {
                    int spritesToShow = requiredCount - visibleCount;
                    
                    // Get the character's IdentifiableObject to load their sprite
                    IdentifiableObject characterObj = controller.getIdentifiableObject(characterId);
                    if (characterObj != null && characterObj.getSprites() != null) {
                        // Get the default sprite key
                        String spriteKey = characterObj.getSprites().get("default");
                        if (spriteKey == null || spriteKey.isEmpty()) {
                            // If no "default" key, try to get the first sprite
                            Map<String, String> sprites = characterObj.getSprites();
                            if (!sprites.isEmpty()) {
                                spriteKey = sprites.values().iterator().next();
                            }
                        }
                        
                        if (spriteKey != null && !spriteKey.isEmpty()) {
                            String category = characterObj.getCategory();
                            String type = characterObj.getType();
                            
                            if (category != null && type != null) {
                                // Load and show the sprite for each instance needed
                                for (int i = 0; i < spritesToShow; i++) {
                                    BufferedImage image = assetManager.loadImage(type, category, spriteKey);
                                    if (image != null) {
                                        // Use default position "Left" if no position is specified
                                        // Depth and offset default to 0
                                        gui.addSprite(image, "Left", 0, 0, characterId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Create and initialize battle manager
        BattleManager battleManager = new BattleManager(controller);
        battleManager.initializeBattle(parameters, playerTeamId);
        
        // Set battle manager in controller
        controller.setBattleManager(battleManager);
        
        // Only start first turn if battle was initialized successfully
        if (!battleManager.isBattleEnded()) {
            // Start first turn
            battleManager.processNextTurn();
        }
        
        // Set waiting for user input so user can click to advance turns
        controller.setWaitingForUserInput(true);
    }
    
    @Override
    public boolean requiresUserInput() {
        return true; // Battle requires user clicks to advance turns
    }
    
    @Override
    public boolean shouldContinueImmediately() {
        return false; // Battle will load a new scenario when it ends
    }
}

