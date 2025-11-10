package com.visualnovel.model.battle;

import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.SpriteData;
import com.visualnovel.util.AssetManager;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

/**
 * Handles GUI updates for battle-related events.
 * Extracted from BattleManager to separate concerns.
 */
public class GuiNotifier {
    private final VisualNovelGUI gui;
    private final BattleState battleState;
    private List<SpriteData> storedSprites; // Sprites stored before showing attack sprite
    
    public GuiNotifier(VisualNovelGUI gui, BattleState battleState) {
        this.gui = gui;
        this.battleState = battleState;
        this.storedSprites = null;
    }
    
    /**
     * Updates health bars for all characters in the battle.
     * Updates health bars for all characters (alive and defeated) from all teams.
     * Defeated characters will show 0 HP.
     * 
     * @param turnOrder The current turn order
     * @param currentTurnIndex The current turn index
     * @param findTeamForCharacter Function to find the team for a character
     */
    public void updateAllTeamHealthBars(List<BattleCharacter> turnOrder, int currentTurnIndex,
                                       Function<BattleCharacter, BattleTeam> findTeamForCharacter) {
        if (gui == null || battleState.isBattleEnded()) {
            return;
        }
        
        // Update health bars for all characters (alive and defeated) in all teams
        for (BattleTeam team : battleState.getTeams()) {
            // Update health bar for each character in the team (both alive and defeated)
            for (BattleCharacter character : team.getCharacters()) {
                // Show 0 HP for defeated characters, actual HP for alive characters
                int currentHealth = character.isAlive() ? character.getCurrentHealth() : 0;
                gui.updateHealthBar(character.getCharacterId(), team.getTeamId(), 
                                  character.getName(), 
                                  currentHealth, character.getMaxHealth(),
                                  character.getCurrentEnergy(), character.getMaxEnergy());
            }
        }
    }
    
    /**
     * Displays the battle start message.
     */
    public void displayBattleStart() {
        if (gui != null) {
            gui.showMessage("Battle", "The battle begins!");
        }
    }
    
    /**
     * Displays the attack with optional image and text.
     * 
     * @param attacker The attacking character
     * @param target The target character
     * @param damage The damage dealt
     */
    public void displayAttack(BattleCharacter attacker, BattleCharacter target, int damage) {
        if (gui == null) {
            return;
        }
        
        // Build attack text
        String attackText = String.format("%s attacks %s for %d damage!", 
            attacker.getName(), target.getName(), damage);
        
        if (target.isAlive()) {
            attackText += String.format(" %s has %d HP remaining.", 
                target.getName(), target.getCurrentHealth());
        } else {
            attackText += String.format(" %s is defeated!", target.getName());
        }
        
        // Try to load attack image (optional)
        // Check if there's an attack image in the character's sprites or parameters
        BufferedImage attackImage = null;
        
        // Try to load attack image from object sprites (e.g., "attack" sprite)
        IdentifiableObject attackerObject = attacker.getIdentifiableObject();
        if (attackerObject != null && attackerObject.getSprites() != null) {
            String attackSpriteKey = attackerObject.getSprites().get("attack");
            if (attackSpriteKey != null) {
                // Get category and type from the IdentifiableObject
                String category = attackerObject.getCategory();
                String type = attackerObject.getType();
                
                if (category != null && type != null) {
                    AssetManager assetManager = AssetManager.getInstance();
                    attackImage = assetManager.loadImage(type, category, attackSpriteKey);
                }
            }
        }
        
        // Display attack image if available (center screen)
        if (attackImage != null) {
            gui.showAttackImage(attackImage);
        }
        
        // Display attack text
        gui.showMessage(attacker.getName(), attackText);
    }
    
    /**
     * Displays an attack sprite if the move has a sprite_key.
     * 
     * @param character The character using the move
     * @param move The move being executed
     */
    private void displayAttackSprite(BattleCharacter character, Move move) {
        if (gui == null || character == null || move == null) {
            return;
        }
        
        // Check if move has a sprite_key
        String spriteKey = move.getSpriteKey();
        if (spriteKey == null || spriteKey.isEmpty()) {
            return;
        }
        
        // Get the character's IdentifiableObject to access sprites
        IdentifiableObject characterObject = character.getIdentifiableObject();
        if (characterObject == null || characterObject.getSprites() == null) {
            return;
        }
        
        // Look up the sprite name using the sprite_key
        String spriteName = characterObject.getSprites().get(spriteKey);
        if (spriteName == null || spriteName.isEmpty()) {
            return;
        }
        
        // Get category and type from the IdentifiableObject
        String category = characterObject.getCategory();
        String type = characterObject.getType();
        
        if (category != null && type != null) {
            AssetManager assetManager = AssetManager.getInstance();
            BufferedImage attackImage = assetManager.loadImage(type, category, spriteName);
            
            // Display attack image if available (center screen)
            if (attackImage != null) {
                // Store current sprites before clearing them
                storedSprites = gui.getSpritesCopy();
                // Hide all other sprites before showing the move sprite
                gui.clearSprites();
                gui.showAttackImage(attackImage);
            }
        }
    }
    
    /**
     * Displays a move execution with move name, description, and effects.
     * 
     * @param character The character using the move
     * @param target The target character (null for self-targeting moves)
     * @param moveName The name of the move being executed
     * @param move The move being executed
     * @param effectValue The effect value (damage dealt, healing amount, etc.)
     */
    public void displayMove(BattleCharacter character, BattleCharacter target, String moveName, Move move, int effectValue) {
        if (gui == null || character == null || move == null || moveName == null) {
            return;
        }
        
        // Display attack sprite if sprite_key is present
        displayAttackSprite(character, move);
        
        String moveText = String.format("%s uses %s!", character.getName(), moveName);
        
        // Add effect information based on move type
        String moveType = move.getType();
        if (moveType == null) {
            moveType = "physical";
        }
        
        switch (moveType) {
            case "physical":
                if (target != null && effectValue > 0) {
                    moveText += String.format(" Deals %d damage to %s!", effectValue, target.getName());
                    if (target.isAlive()) {
                        moveText += String.format(" %s has %d HP remaining.", 
                            target.getName(), target.getCurrentHealth());
                    } else {
                        moveText += String.format(" %s is defeated!", target.getName());
                    }
                }
                break;
            case "healing":
                if (effectValue > 0) {
                    moveText += String.format(" Restores %d HP! %s now has %d/%d HP.", 
                        effectValue, character.getName(), character.getCurrentHealth(), character.getMaxHealth());
                }
                break;
            case "defense":
                if (move.getDefense() != null) {
                    moveText += String.format(" Increases defense by %d!", move.getDefense());
                }
                break;
            case "status_effect":
                // Status effect moves are handled by displayStatusEffectApplied
                // This case is here for completeness but shouldn't be called
                break;
        }
        
        // Add energy cost information
        moveText += String.format(" (Energy: %d/%d)", character.getCurrentEnergy(), character.getMaxEnergy());
        
        // Display move text
        gui.showMessage(character.getName(), moveText);
    }
    
    /**
     * Displays a status effect being applied to a character.
     * 
     * @param attacker The character using the move
     * @param target The target character receiving the effect
     * @param moveName The name of the move being used
     * @param move The move being used
     * @param effectType The type of status effect being applied
     * @param duration The duration of the effect in turns
     * @param damage The damage dealt (if any)
     */
    public void displayStatusEffectApplied(BattleCharacter attacker, BattleCharacter target, 
                                          String moveName, Move move, String effectType, Integer duration, int damage) {
        if (gui == null || attacker == null || target == null || moveName == null || effectType == null) {
            return;
        }
        
        // Display attack sprite if sprite_key is present
        displayAttackSprite(attacker, move);
        
        String message = String.format("%s uses %s!", attacker.getName(), moveName);
        
        // Add damage information if applicable
        if (damage > 0) {
            message += String.format(" Deals %d damage to %s!", damage, target.getName());
            if (target.isAlive()) {
                message += String.format(" %s has %d HP remaining.", 
                    target.getName(), target.getCurrentHealth());
            } else {
                message += String.format(" %s is defeated!", target.getName());
            }
        }
        
        // Add status effect information
        String effectName = capitalizeFirst(effectType);
        if (duration != null && duration > 0) {
            message += String.format(" %s is afflicted with %s for %d turn%s!", 
                target.getName(), effectName, duration, duration == 1 ? "" : "s");
        } else {
            message += String.format(" %s is afflicted with %s!", target.getName(), effectName);
        }
        
        // Add move message on a new line if present
        if (move != null && move.getMessage() != null && !move.getMessage().isEmpty()) {
            message += "\n" + move.getMessage();
        }
        
        gui.showMessage(attacker.getName(), message);
    }
    
    /**
     * Displays a status effect expiring on a character.
     * 
     * @param character The character whose effect expired
     * @param effectType The type of effect that expired
     */
    public void displayStatusEffectExpired(BattleCharacter character, String effectType) {
        if (gui == null || character == null || effectType == null) {
            return;
        }
        
        String effectName = capitalizeFirst(effectType);
        String message = String.format("%s is no longer affected by %s!", 
            character.getName(), effectName);
        
        gui.showMessage("Battle", message);
    }
    
    /**
     * Displays a character being stunned and skipping their turn.
     * 
     * @param character The stunned character
     */
    public void displayStunned(BattleCharacter character) {
        if (gui == null || character == null) {
            return;
        }
        
        String message;
        
        // Check if the status effect has an afflicted message
        StatusEffect stunEffect = character.getStatusEffect("stun");
        if (stunEffect != null && stunEffect.getAfflictedMessage() != null && !stunEffect.getAfflictedMessage().isEmpty()) {
            message = stunEffect.getAfflictedMessage();
        } else {
            message = String.format("%s is stunned and cannot act!", character.getName());
        }
        
        gui.showMessage("Battle", message);
    }
    
    /**
     * Displays poison damage being applied to a character.
     * 
     * @param character The character taking poison damage
     * @param damage The amount of poison damage dealt
     */
    public void displayPoisonDamage(BattleCharacter character, int damage) {
        if (gui == null || character == null) {
            return;
        }
        
        String message = String.format("%s takes %d poison damage!", character.getName(), damage);
        
        if (character.isAlive()) {
            message += String.format(" %s has %d HP remaining.", 
                character.getName(), character.getCurrentHealth());
        } else {
            message += String.format(" %s is defeated!", character.getName());
        }
        
        gui.showMessage("Battle", message);
    }
    
    /**
     * Displays a confused character failing an attack.
     * 
     * @param character The confused character
     * @param moveName The name of the move that failed
     */
    public void displayConfusedAttack(BattleCharacter character, String moveName) {
        if (gui == null || character == null || moveName == null) {
            return;
        }
        
        String message = String.format("%s is confused! %s fails to use %s!", 
            character.getName(), character.getName(), moveName);
        
        gui.showMessage("Battle", message);
    }
    
    /**
     * Capitalizes the first letter of a string.
     * 
     * @param str The string to capitalize
     * @return The string with the first letter capitalized
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Displays a general message.
     * 
     * @param message The message to display
     */
    public void displayMessage(String message) {
        if (gui != null) {
            gui.showMessage("Battle", message);
        }
    }
    
    /**
     * Displays the battle end message.
     * 
     * @param playerWon Whether the player won the battle
     */
    public void displayBattleEnd(boolean playerWon) {
        if (gui != null) {
            // Clear all health bars when battle ends
            gui.clearHealthBars();
            
            if (playerWon) {
                gui.showMessage("Battle", "Victory! You have won the battle!");
            } else {
                gui.showMessage("Battle", "Defeat! You have lost the battle.");
            }
        }
    }
    
    /**
     * Restores sprites that were hidden when showing an attack sprite.
     * Should be called when the attack sprite is cleared (e.g., when user clicks to continue).
     */
    public void restoreSprites() {
        if (gui != null) {
            // Clear the attack image first
            gui.clearScreen();
        }
        if (storedSprites != null) {
            // Restore the sprites
            gui.restoreSprites(storedSprites);
            storedSprites = null; // Clear stored sprites after restoring
        }
        if (gui != null) {
            gui.refresh();
        }
    }
}

