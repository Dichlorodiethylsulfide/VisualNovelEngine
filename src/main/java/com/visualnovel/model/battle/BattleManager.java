package com.visualnovel.model.battle;

import com.visualnovel.GameController;
import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.gui.MoveSelectionDialog;
import com.visualnovel.gui.TargetSelectionDialog;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.Scenario;

import java.util.*;

/**
 * Manages battle state and turn order.
 * Handles battle loop execution, processes attacks and damage calculation,
 * determines win/lose conditions, and integrates with GameController and GUI.
 */
public class BattleManager {
    private GameController controller;
    private BattleState battleState;
    private List<BattleCharacter> turnOrder;
    private int currentTurnIndex;
    private boolean waitingForUserInput;
    private GuiNotifier guiNotifier;
    private TargetingStrategy targetingStrategy;
    private AIMoveSelector aiMoveSelector;
    
    public BattleManager(GameController controller) {
        this.controller = controller;
        this.battleState = new BattleState();
        this.turnOrder = new ArrayList<>();
        this.currentTurnIndex = 0;
        this.waitingForUserInput = false;
        this.targetingStrategy = new FirstAliveTargetingStrategy();
        this.aiMoveSelector = new AIMoveSelector();
        // GuiNotifier will be initialized after GUI is available
    }
    
    /**
     * Initializes the battle with teams from parameters.
     * 
     * @param parameters Battle parameters containing team definitions
     * @param playerTeamId The ID of the team containing the player
     */
    public void initializeBattle(Map<String, Object> parameters, String playerTeamId) {
        // Clear previous battle state
        battleState = new BattleState();
        turnOrder = new ArrayList<>();
        currentTurnIndex = 0;
        
        // Parse teams from parameters
        // Expected format: "team1": ["Player"], "team2": ["Enemy"]
        Map<String, List<String>> teamDefinitions = new HashMap<>();
        
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
                teamDefinitions.put(key, characterIds);
            }
        }
        
        // Create teams and add characters
        for (Map.Entry<String, List<String>> teamEntry : teamDefinitions.entrySet()) {
            String teamId = teamEntry.getKey();
            List<String> characterIds = teamEntry.getValue();
            
            BattleTeam team = new BattleTeam(teamId);
            
            for (String characterId : characterIds) {
                IdentifiableObject identifiableObject = controller.getIdentifiableObject(characterId);
                if (identifiableObject == null) {
                    System.err.println("Warning: Object with id '" + characterId + "' not found in ids dictionary, skipping.");
                    continue;
                }
                
                // Only add characters (not backgrounds or screens)
                if (!"character".equals(identifiableObject.getType())) {
                    System.err.println("Warning: Object with id '" + characterId + "' is not a Character (type: " + 
                                     identifiableObject.getType() + "), skipping.");
                    continue;
                }
                
                // Check battle_strategy to determine if we should use persistent stats
                BattleCharacter battleCharacter;
                Scenario mainScenario = controller.getMainScenario();
                String battleStrategy = (mainScenario != null) ? mainScenario.getBattleStrategy() : null;
                
                if ("persist_stats".equals(battleStrategy)) {
                    // Load persistent stats if available
                    CharacterStats persistentStats = controller.getPersistentStats(characterId);
                    if (persistentStats != null) {
                        // Use persistent stats
                        battleCharacter = new BattleCharacter(identifiableObject, characterId, 
                            persistentStats.getCurrentHealth(), persistentStats.getCurrentEnergy());
                    } else {
                        // No persistent stats yet, use max values (first battle)
                        battleCharacter = new BattleCharacter(identifiableObject, characterId);
                    }
                } else {
                    // reset_stats or null - use max values (default behavior)
                    battleCharacter = new BattleCharacter(identifiableObject, characterId);
                }
                
                team.addCharacter(battleCharacter);
            }
            
            // Only add team if it has characters
            if (!team.getCharacters().isEmpty()) {
                // Mark if this is the player team
                if (teamId.equals(playerTeamId)) {
                    team.setContainsPlayer(true);
                    battleState.setPlayerTeam(team);
                }
                
                battleState.addTeam(team);
            }
        }
        
        // Validate that we have at least 2 teams with characters
        List<BattleTeam> validTeams = new ArrayList<>();
        for (BattleTeam team : battleState.getTeams()) {
            if (team.hasAliveCharacters()) {
                validTeams.add(team);
            }
        }
        
        if (validTeams.size() < 2) {
            System.err.println("Error: Battle requires at least 2 teams with characters. Found " + validTeams.size() + " team(s).");
            VisualNovelGUI gui = controller.getGUI();
            if (gui != null) {
                gui.showMessage("Error", "Battle setup failed: Need at least 2 teams with characters.");
            }
            battleState.setBattleEnded(true);
            waitingForUserInput = true;
            return;
        }
        
        // Build initial turn order (all characters sorted by speed)
        buildTurnOrder();
        
        // Validate turn order is not empty
        if (turnOrder.isEmpty()) {
            System.err.println("Error: No characters available for battle.");
            VisualNovelGUI gui = controller.getGUI();
            if (gui != null) {
                gui.showMessage("Error", "Battle setup failed: No characters available.");
            }
            battleState.setBattleEnded(true);
            waitingForUserInput = true;
            return;
        }
        
        // Initialize GuiNotifier now that we have battle state
        VisualNovelGUI gui = controller.getGUI();
        if (gui != null) {
            this.guiNotifier = new GuiNotifier(gui, battleState);
        }
        
        // Start battle
        waitingForUserInput = true;
        if (guiNotifier != null) {
            guiNotifier.displayBattleStart();
            guiNotifier.updateAllTeamHealthBars(turnOrder, currentTurnIndex, this::findTeamForCharacter);
        }
    }
    
    
    /**
     * Builds the turn order based on speed, ensuring teams alternate.
     * 
     * @param resetIndex If true, resets currentTurnIndex to 0. If false, preserves it.
     */
    private void buildTurnOrder(boolean resetIndex) {
        turnOrder.clear();
        
        // Get all alive characters sorted by speed
        List<BattleCharacter> allAlive = battleState.getAllAliveCharactersSortedBySpeed();
        
        // Group by team
        Map<BattleTeam, List<BattleCharacter>> charactersByTeam = new HashMap<>();
        for (BattleCharacter character : allAlive) {
            BattleTeam team = findTeamForCharacter(character);
            if (team != null) {
                charactersByTeam.computeIfAbsent(team, k -> new ArrayList<>()).add(character);
            }
        }
        
        // Build alternating turn order
        // Start with the fastest character overall, then alternate teams
        if (!allAlive.isEmpty()) {
            BattleCharacter first = allAlive.get(0);
            BattleTeam firstTeam = findTeamForCharacter(first);
            
            // Create alternating order
            List<BattleTeam> teams = new ArrayList<>(battleState.getTeams());
            int startIndex = teams.indexOf(firstTeam);
            
            // Rotate teams so first team is first
            Collections.rotate(teams, -startIndex);
            
            // Build turn order by alternating teams
            boolean done = false;
            int[] teamIndices = new int[teams.size()];
            Arrays.fill(teamIndices, 0);
            
            while (!done) {
                done = true;
                for (int i = 0; i < teams.size(); i++) {
                    BattleTeam team = teams.get(i);
                    List<BattleCharacter> teamChars = charactersByTeam.get(team);
                    if (teamChars != null && teamIndices[i] < teamChars.size()) {
                        turnOrder.add(teamChars.get(teamIndices[i]));
                        teamIndices[i]++;
                        done = false;
                    }
                }
            }
        }
        
        // Only reset index if requested (initial build) or if turn order is empty
        if (resetIndex || turnOrder.isEmpty()) {
            currentTurnIndex = 0;
        } else {
            // Adjust currentTurnIndex to stay within bounds
            currentTurnIndex = currentTurnIndex % turnOrder.size();
        }
    }
    
    /**
     * Builds the turn order based on speed, ensuring teams alternate.
     * Resets the turn index (for initial battle setup).
     */
    private void buildTurnOrder() {
        buildTurnOrder(true);
    }
    
    /**
     * Finds the team that contains the given character.
     */
    private BattleTeam findTeamForCharacter(BattleCharacter character) {
        for (BattleTeam team : battleState.getTeams()) {
            if (team.getCharacters().contains(character)) {
                return team;
            }
        }
        return null;
    }
    
    /**
     * Gets valid targets for the attacker (alive characters from opposing team).
     * 
     * @param attacker The attacking character
     * @return List of valid target characters, or empty list if no valid targets
     */
    public List<BattleCharacter> getValidTargets(BattleCharacter attacker) {
        List<BattleCharacter> validTargets = new ArrayList<>();
        
        if (attacker == null) {
            return validTargets;
        }
        
        // Find attacker's team
        BattleTeam attackerTeam = findTeamForCharacter(attacker);
        if (attackerTeam == null) {
            return validTargets;
        }
        
        // Get opposing team
        BattleTeam opposingTeam = battleState.getOpposingTeam(attackerTeam);
        if (opposingTeam == null) {
            return validTargets;
        }
        
        // Return alive characters from opposing team
        validTargets.addAll(opposingTeam.getAliveCharacters());
        
        return validTargets;
    }
    
    /**
     * Processes the next turn in the battle.
     * Called when user clicks to advance the battle.
     * Uses a loop instead of recursion to handle turn processing.
     */
    public void processNextTurn() {
        // Restore sprites that were hidden during attack animation
        if (guiNotifier != null) {
            guiNotifier.restoreSprites();
        }
        
        // Loop until we find a valid turn or battle ends
        while (true) {
            if (battleState.isBattleEnded()) {
                handleBattleEnd();
                return;
            }
            
            // Rebuild turn order to account for defeated characters (don't reset index)
            buildTurnOrder(false);
            
            if (turnOrder.isEmpty()) {
                handleBattleEnd();
                return;
            }
            
            // Get current attacker (use modulo to wrap around)
            BattleCharacter attacker = turnOrder.get(currentTurnIndex % turnOrder.size());
            BattleTeam attackerTeam = findTeamForCharacter(attacker);
            
            if (attackerTeam == null || !attacker.isAlive()) {
                // Skip defeated characters - move to next character
                currentTurnIndex++;
                // Wrap around if needed
                if (currentTurnIndex >= turnOrder.size()) {
                    currentTurnIndex = 0;
                }
                // Continue loop to find next valid attacker
                continue;
            }
            
            // Update health bars for all teams' current attackers
            if (guiNotifier != null) {
                guiNotifier.updateAllTeamHealthBars(turnOrder, currentTurnIndex, this::findTeamForCharacter);
            }
            
            // Check and apply turn-based effects (poison, etc.) at the start of the turn
            // Note: We check effects here but decrement duration at the END of the turn
            if (attacker.hasStatusEffect("poison")) {
                // Apply poison damage (e.g., 5 damage per turn)
                int poisonDamage = 5;
                boolean wasAlive = attacker.isAlive();
                int actualDamage = attacker.takeDamage(poisonDamage);
                
                if (guiNotifier != null) {
                    guiNotifier.displayPoisonDamage(attacker, actualDamage);
                }
                
                // If character was defeated, hide their sprites
                if (wasAlive && !attacker.isAlive()) {
                    VisualNovelGUI gui = controller.getGUI();
                    if (gui != null) {
                        gui.hideSpritesByCharacterId(attacker.getCharacterId());
                    }
                }
                
                // Update health bars after poison damage
                if (guiNotifier != null) {
                    guiNotifier.updateAllTeamHealthBars(turnOrder, currentTurnIndex, this::findTeamForCharacter);
                }
                
                // Check if battle ended after poison damage
                if (battleState.checkBattleEnd()) {
                    handleBattleEnd();
                    waitingForUserInput = true;
                    return;
                }
            }
            
            // Check for stun - if stunned, skip move selection
            if (attacker.isStunned()) {
                // Check if the stun effect will expire this turn (duration == 1)
                // If it has an afflicted message and will expire, we'll show the afflicted message
                // but skip the expired message to avoid replacing it
                StatusEffect stunEffect = attacker.getStatusEffect("stun");
                boolean willExpireThisTurn = (stunEffect != null && stunEffect.getDuration() == 1);
                boolean hasAfflictedMessage = (stunEffect != null && stunEffect.getAfflictedMessage() != null && !stunEffect.getAfflictedMessage().isEmpty());
                String effectTypeWithAfflictedMessage = null;
                if (willExpireThisTurn && hasAfflictedMessage) {
                    effectTypeWithAfflictedMessage = "stun";
                }
                
                if (guiNotifier != null) {
                    guiNotifier.displayStunned(attacker);
                }
                
                // Process status effects at the END of the turn (decrement durations)
                // This ensures a single-turn effect lasts for the full turn
                List<String> expiredEffects = attacker.processStatusEffects();
                
                // Display expired effects, but skip if we just showed an afflicted message for a 1-turn effect
                if (guiNotifier != null && !expiredEffects.isEmpty()) {
                    for (String effectType : expiredEffects) {
                        // Skip expired message if this effect just expired and had an afflicted message
                        if (effectTypeWithAfflictedMessage != null && effectType.equals(effectTypeWithAfflictedMessage)) {
                            continue; // Don't show expired message, already showed afflicted message
                        }
                        guiNotifier.displayStatusEffectExpired(attacker, effectType);
                    }
                }
                
                // Skip turn - move to next character
                currentTurnIndex++;
                if (currentTurnIndex >= turnOrder.size()) {
                    currentTurnIndex = 0;
                }
                waitingForUserInput = true;
                return;
            }
            
            // Check if this is a player character
            boolean isPlayerCharacter = isPlayerCharacter(attacker);
            
            // Select move
            String moveName = null;
            if (isPlayerCharacter) {
                // Player character - show move selection dialog
                VisualNovelGUI gui = controller.getGUI();
                if (gui != null) {
                    MoveSelectionDialog dialog = new MoveSelectionDialog(gui, attacker);
                    moveName = dialog.showDialog(); // Dialog is modal, blocks until selection
                }
                // If no move selected, skip turn
                if (moveName == null) {
                    // Skip turn - move to next character
                    currentTurnIndex++;
                    if (currentTurnIndex >= turnOrder.size()) {
                        currentTurnIndex = 0;
                    }
                    waitingForUserInput = true;
                    return;
                }
            } else {
                // AI character - use AI move selector
                moveName = aiMoveSelector.selectMove(attacker);
                // If no move selected, skip turn
                if (moveName == null) {
                    // Skip turn - move to next character
                    currentTurnIndex++;
                    if (currentTurnIndex >= turnOrder.size()) {
                        currentTurnIndex = 0;
                    }
                    waitingForUserInput = true;
                    return;
                }
            }
            
            // Get move to determine if target selection is needed
            Map<String, Move> moves = attacker.getMoves();
            Move selectedMove = null;
            if (moves != null && moveName != null) {
                selectedMove = moves.get(moveName);
            }
            
            // Select target
            BattleCharacter target = null;
            
            if (isPlayerCharacter && selectedMove != null) {
                // Player character - check move type
                String moveType = selectedMove.getType();
                if (moveType == null) {
                    moveType = "physical"; // Default to physical if type is missing
                }
                
                if ("physical".equals(moveType) || "status_effect".equals(moveType)) {
                    // Physical or status effect move - show target selection dialog
                    List<BattleCharacter> validTargets = getValidTargets(attacker);
                    if (validTargets.isEmpty()) {
                        // No valid targets - battle should end
                        handleBattleEnd();
                        return;
                    }
                    
                    VisualNovelGUI gui = controller.getGUI();
                    if (gui != null) {
                        TargetSelectionDialog targetDialog = new TargetSelectionDialog(gui, validTargets);
                        target = targetDialog.showDialog(); // Dialog is modal, blocks until selection
                    }
                    
                    // If no target selected, skip turn
                    if (target == null) {
                        // Skip turn - move to next character
                        currentTurnIndex++;
                        if (currentTurnIndex >= turnOrder.size()) {
                            currentTurnIndex = 0;
                        }
                        waitingForUserInput = true;
                        return;
                    }
                } else {
                    // Defense or healing move - self-targeting, skip target selection
                    // Target will be set to attacker in executeMove for self-targeting moves
                    target = attacker;
                }
            } else {
                // AI character - use automatic targeting strategy
                // Check move type to determine if target selection is needed
                if (selectedMove != null) {
                    String moveType = selectedMove.getType();
                    if (moveType == null) {
                        moveType = "physical"; // Default to physical if type is missing
                    }
                    
                    if ("physical".equals(moveType) || "status_effect".equals(moveType)) {
                        // Physical or status effect move - need target
                        target = targetingStrategy.selectTarget(attacker, battleState);
                        if (target == null) {
                            handleBattleEnd();
                            return;
                        }
                    } else {
                        // Defense or healing move - self-targeting
                        target = attacker;
                    }
                } else {
                    // No move selected - use default targeting
                    target = targetingStrategy.selectTarget(attacker, battleState);
                    if (target == null) {
                        handleBattleEnd();
                        return;
                    }
                }
            }
            
            // Execute move (moveName is passed to executeMove and then to displayMove)
            executeMove(attacker, target, moveName);
            
            // Update health bars after move (in case health changed)
            if (guiNotifier != null) {
                guiNotifier.updateAllTeamHealthBars(turnOrder, currentTurnIndex, this::findTeamForCharacter);
            }
            
            // Process status effects at the END of the turn (decrement durations)
            // This ensures a single-turn effect lasts for the full turn
            List<String> expiredEffects = attacker.processStatusEffects();
            
            // Display expired effects
            if (guiNotifier != null && !expiredEffects.isEmpty()) {
                for (String effectType : expiredEffects) {
                    guiNotifier.displayStatusEffectExpired(attacker, effectType);
                }
            }
            
            // Check if battle ended
            if (battleState.checkBattleEnd()) {
                handleBattleEnd();
                waitingForUserInput = true;
                return;
            }
            
            // Move to next turn - increment index and wrap around
            currentTurnIndex++;
            if (currentTurnIndex >= turnOrder.size()) {
                currentTurnIndex = 0;
            }
            waitingForUserInput = true;
            return;
        }
    }
    
    /**
     * Checks if a character is on the player team.
     * 
     * @param character The character to check
     * @return true if the character is on the player team, false otherwise
     */
    private boolean isPlayerCharacter(BattleCharacter character) {
        BattleTeam team = findTeamForCharacter(character);
        return team != null && team.containsPlayer();
    }
    
    /**
     * Executes a move from attacker to target (or on self for healing/defense moves).
     * 
     * @param attacker The character using the move
     * @param target The target character (may be attacker for self-targeting moves)
     * @param moveName The name of the move to execute
     */
    private void executeMove(BattleCharacter attacker, BattleCharacter target, String moveName) {
        if (attacker == null || moveName == null) {
            return;
        }
        
        // Get move from attacker's moveset
        Map<String, Move> moves = attacker.getMoves();
        if (moves == null || !moves.containsKey(moveName)) {
            // No move found - skip turn
            if (guiNotifier != null) {
                guiNotifier.displayMessage(attacker.getName() + " has no move: " + moveName);
            }
            return;
        }
        
        Move move = moves.get(moveName);
        if (move == null) {
            return;
        }
        
        // Check if move is affordable
        if (attacker.getCurrentEnergy() < move.getEnergyCost()) {
            // Not enough energy - skip turn
            if (guiNotifier != null) {
                guiNotifier.displayMessage(attacker.getName() + " doesn't have enough energy for " + moveName);
            }
            return;
        }
        
        // Consume energy
        attacker.consumeEnergy(move.getEnergyCost());
        
        // Check for confuse effect - randomly fail physical attacks
        String moveType = move.getType();
        if (moveType == null) {
            moveType = "physical"; // Default to physical if type is missing
        }
        
        if ("physical".equals(moveType) && attacker.hasStatusEffect("confuse")) {
            // 50% chance to fail attack when confused
            if (Math.random() < 0.5) {
                if (guiNotifier != null) {
                    guiNotifier.displayConfusedAttack(attacker, moveName);
                }
                return;
            }
        }
        
        // Execute move based on type
        switch (moveType) {
            case "physical":
                executePhysicalMove(attacker, target, moveName, move);
                break;
            case "defense":
                executeDefenseMove(attacker, moveName, move);
                break;
            case "healing":
                executeHealingMove(attacker, moveName, move);
                break;
            case "status_effect":
                executeStatusEffectMove(attacker, target, moveName, move);
                break;
            default:
                // Unknown move type - treat as physical
                executePhysicalMove(attacker, target, moveName, move);
                break;
        }
    }
    
    /**
     * Executes a physical move that deals damage to the target.
     */
    private void executePhysicalMove(BattleCharacter attacker, BattleCharacter target, String moveName, Move move) {
        // Calculate damage
        int damage = 0;
        if (move.getDamage() != null) {
            damage = move.getDamage();
        } else {
            // Fallback to attack stat if damage not specified
            damage = attacker.getAttack();
        }
        
        // Apply defense reduction
        int targetDefense = target.getDefense();
        damage = Math.max(1, damage - targetDefense);
        
        // Check if target was alive before attack
        boolean wasAlive = target.isAlive();
        
        // Apply damage
        int actualDamage = target.takeDamage(damage);
        
        // If character was defeated, hide their sprites
        if (wasAlive && !target.isAlive()) {
            VisualNovelGUI gui = controller.getGUI();
            if (gui != null) {
                gui.hideSpritesByCharacterId(target.getCharacterId());
            }
        }
        
        // Display move execution using GuiNotifier
        if (guiNotifier != null) {
            guiNotifier.displayMove(attacker, target, moveName, move, actualDamage);
        }
    }
    
    /**
     * Executes a defense move that boosts the character's defense.
     */
    private void executeDefenseMove(BattleCharacter character, String moveName, Move move) {
        if (move.getDefense() == null) {
            return;
        }
        
        // TODO: For now, we'll just display the move - defense boost could be temporary
        // In a more complex system, we'd track temporary stat boosts
        if (guiNotifier != null) {
            guiNotifier.displayMove(character, null, moveName, move, 0);
        }
    }
    
    /**
     * Executes a healing move that restores health to the character.
     */
    private void executeHealingMove(BattleCharacter character, String moveName, Move move) {
        if (move.getHealth() == null) {
            return;
        }
        
        int healingAmount = move.getHealth();
        int oldHealth = character.getCurrentHealth();
        int newHealth = Math.min(character.getMaxHealth(), oldHealth + healingAmount);
        character.setCurrentHealth(newHealth);
        int actualHealing = newHealth - oldHealth;
        
        // Display move execution using GuiNotifier
        if (guiNotifier != null) {
            guiNotifier.displayMove(character, null, moveName, move, actualHealing);
        }
    }
    
    /**
     * Executes a status effect move that applies a status effect to the target.
     * 
     * @param attacker The character using the move
     * @param target The target character
     * @param moveName The name of the move being executed
     * @param move The move being executed
     */
    private void executeStatusEffectMove(BattleCharacter attacker, BattleCharacter target, String moveName, Move move) {
        if (target == null) {
            return;
        }
        
        // Apply damage if move has damage value
        int damage = 0;
        if (move.getDamage() != null) {
            damage = move.getDamage();
            if (damage > 0) {
                // Apply defense reduction
                int targetDefense = target.getDefense();
                damage = Math.max(1, damage - targetDefense);
                
                // Check if target was alive before attack
                boolean wasAlive = target.isAlive();
                
                // Apply damage
                target.takeDamage(damage);
                
                // If character was defeated, hide their sprites
                if (wasAlive && !target.isAlive()) {
                    VisualNovelGUI gui = controller.getGUI();
                    if (gui != null) {
                        gui.hideSpritesByCharacterId(target.getCharacterId());
                    }
                }
            }
        }
        
        // Create and apply status effect if move has effect and duration
        if (move.getEffect() != null && move.getDuration() != null && move.getDuration() > 0) {
            // Get afflicted message from move if present
            String afflictedMessage = move.getAfflictedMessage();
            StatusEffect effect = new StatusEffect(move.getEffect(), move.getDuration(), moveName, afflictedMessage);
            target.addStatusEffect(effect);
            
            // Display status effect applied message
            if (guiNotifier != null) {
                guiNotifier.displayStatusEffectApplied(attacker, target, moveName, move, move.getEffect(), move.getDuration(), damage);
            }
        } else {
            // Display move execution without status effect
            if (guiNotifier != null) {
                guiNotifier.displayMove(attacker, target, moveName, move, damage);
            }
        }
    }
    
    /**
     * Handles battle end and triggers appropriate scenario.
     */
    private void handleBattleEnd() {
        battleState.setBattleEnded(true);
        
        // Save persistent stats if battle_strategy is "persist_stats"
        Scenario mainScenario = controller.getMainScenario();
        String battleStrategy = (mainScenario != null) ? mainScenario.getBattleStrategy() : null;
        if ("persist_stats".equals(battleStrategy)) {
            savePersistentStats();
        }
        
        BattleTeam winner = battleState.getWinner();
        boolean playerWon = (winner != null && winner.containsPlayer());
        
        // Display battle end using GuiNotifier
        if (guiNotifier != null) {
            guiNotifier.displayBattleEnd(playerWon);
        }
        
        // Battle outcome will be handled by GameController when user clicks
        // The scenario loading will be handled by GameController.handleBattleEnd()
    }
    
    /**
     * Saves persistent stats for all characters in the battle.
     * Called when battle ends and battle_strategy is "persist_stats".
     */
    private void savePersistentStats() {
        for (BattleTeam team : battleState.getTeams()) {
            for (BattleCharacter character : team.getCharacters()) {
                CharacterStats stats = new CharacterStats(
                    character.getCurrentHealth(),
                    character.getMaxHealth(),
                    character.getCurrentEnergy(),
                    character.getMaxEnergy()
                );
                controller.savePersistentStats(character.getCharacterId(), stats);
            }
        }
    }
    
    /**
     * Checks if the battle manager is waiting for user input.
     */
    public boolean isWaitingForUserInput() {
        return waitingForUserInput;
    }
    
    /**
     * Sets whether the battle manager is waiting for user input.
     */
    public void setWaitingForUserInput(boolean waiting) {
        this.waitingForUserInput = waiting;
    }
    
    /**
     * Gets the battle state.
     */
    public BattleState getBattleState() {
        return battleState;
    }
    
    /**
     * Checks if the battle has ended.
     */
    public boolean isBattleEnded() {
        return battleState.isBattleEnded();
    }
    
    /**
     * Gets whether the player won the battle.
     */
    public boolean didPlayerWin() {
        BattleTeam winner = battleState.getWinner();
        return (winner != null && winner.containsPlayer());
    }
}

