package com.visualnovel;

import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.model.*;
import com.visualnovel.model.Character;
import com.visualnovel.model.actions.BattleAction;
import com.visualnovel.model.battle.BattleManager;
import com.visualnovel.model.battle.CharacterStats;
import com.visualnovel.util.ActionHandler;
import com.visualnovel.util.ScenarioLoader;
import com.visualnovel.validation.ScenarioPathValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main game controller that processes scenario actions and manages game state.
 */
public class GameController {
    private VisualNovelGUI gui;
    private Scenario scenario;
    private Scenario mainScenario; // Reference to the main entry point scenario (contains characters)
    private int currentActionIndex;
    private List<ScenarioAction> currentActionList;
    private boolean waitingForUserInput;
    private String currentScenarioBasePath; // Base directory of the current scenario file
    private BattleManager currentBattleManager; // Current battle manager if in battle
    private ScenarioAction currentBattleAction; // Current battle action (for tracking OnWin/OnLose)
    private javax.swing.Timer currentDelayTimer; // Current delay timer for DelayAction (for skipping)
    private ActionFlowController actionFlowController; // Centralized action flow control
    private Map<String, CharacterStats> persistentCharacterStats; // Persistent stats for characters (when battle_strategy is "persist_stats")
    private Map<String, Map<String, InventoryItem>> persistentCharacterInventories; // Persistent inventories for characters
    private String entryPointScenarioPath; // Path to the original entry point scenario file
    private String pendingEndScenarioRestart; // Pending EndScenarioAction restart value (null if not pending)
    private Deque<ActionSequenceState> actionSequenceStack; // Stack to restore previous action sequences after branching
    
    /**
     * Represents a saved action sequence state for restoration after branching.
     */
    private static class ActionSequenceState {
        final List<ScenarioAction> actionList;
        final int actionIndex;
        final String basePath;
        
        ActionSequenceState(List<ScenarioAction> actionList, int actionIndex, String basePath) {
            this.actionList = actionList;
            this.actionIndex = actionIndex;
            this.basePath = basePath;
        }
    }
    
    public GameController() {
        currentActionIndex = 0;
        waitingForUserInput = false;
        currentBattleManager = null;
        currentBattleAction = null;
        currentDelayTimer = null;
        this.actionFlowController = new ActionFlowController(this);
        this.persistentCharacterStats = new HashMap<>();
        this.persistentCharacterInventories = new HashMap<>();
        this.entryPointScenarioPath = null;
        this.pendingEndScenarioRestart = null;
        this.actionSequenceStack = new ArrayDeque<>();
    }
    
    public void setGUI(VisualNovelGUI gui) {
        this.gui = gui;
    }
    
    /**
     * Loads a scenario from a JSON file.
     * For entry point scenarios, validates that entry_point is true.
     * 
     * @param filePath Path to the scenario JSON file
     * @param requireEntryPoint If true, validates that the scenario is an entry point
     */
    public void loadScenario(String filePath, boolean requireEntryPoint) {
        try {
            ScenarioLoader loader = new ScenarioLoader();
            scenario = loader.loadScenario(filePath);
            
            // Validate entry point if required
            if (requireEntryPoint && !scenario.isEntryPoint()) {
                throw new IOException("Scenario file is not marked as an entry point: " + filePath);
            }
            
            // Store as main scenario if it's an entry point (contains characters)
            if (scenario.isEntryPoint()) {
                mainScenario = scenario;
                // Store the entry point scenario path for potential restart
                entryPointScenarioPath = filePath;
                // Clear the action sequence stack when loading a new main scenario
                actionSequenceStack.clear();
                
                // Initialize inventories from IdentifiableObject definitions
                if (scenario.getIds() != null) {
                    for (Map.Entry<String, IdentifiableObject> entry : scenario.getIds().entrySet()) {
                        String objectId = entry.getKey();
                        IdentifiableObject obj = entry.getValue();
                        
                        // Only initialize inventory for characters
                        if (obj != null && "character".equals(obj.getType()) && obj.getInventory() != null) {
                            initializeCharacterInventory(objectId);
                        }
                    }
                }
            }

            // Validate scenario
            ScenarioPathValidator validator = new ScenarioPathValidator(filePath);
            List<String> errors = validator.validate(scenario, filePath);
            if (!errors.isEmpty()) {
                System.err.println("Errors found in scenario: " + String.join("\n", errors));
                return;
            }

            currentActionList = scenario.getSequence();
            currentActionIndex = 0;
            
            // Store the base directory of the scenario file for resolving relative paths
            File scenarioFile = new File(filePath);
            currentScenarioBasePath = scenarioFile.getParent();
            if (currentScenarioBasePath == null) {
                currentScenarioBasePath = "";
            }
            
            processNextAction();
        } catch (IOException e) {
            System.err.println("Error loading scenario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Called when the user clicks to advance the game.
     */
    public void onUserClick() {
        // If an animation is running, skip it first
        if (gui != null && gui.isAnimationRunning()) {
            gui.skipCurrentAnimation();
            return; // Animation skip will call onComplete which continues execution
        }
        
        // If a delay is running, skip it first
        if (currentDelayTimer != null && currentDelayTimer.isRunning()) {
            currentDelayTimer.stop();
            currentDelayTimer = null;
            processNextAction();
            return;
        }
        
        // If in battle, process next turn
        if (currentBattleManager != null && currentBattleManager.isWaitingForUserInput()) {
            if (currentBattleManager.isBattleEnded()) {
                // Battle ended, handle outcome
                handleBattleEnd();
            } else {
                // Process next battle turn
                currentBattleManager.processNextTurn();
                if (currentBattleManager.isWaitingForUserInput()) {
                    waitingForUserInput = true;
                }
            }
            return;
        }
        
        if (waitingForUserInput) {
            waitingForUserInput = false;
            
            // Check if there's a pending EndScenarioAction
            if (pendingEndScenarioRestart != null) {
                String restart = pendingEndScenarioRestart;
                pendingEndScenarioRestart = null; // Clear pending
                executeEndScenario(restart);
                return;
            }
            
            processNextAction();
        }
    }
    
    /**
     * Executes the end scenario logic (restart or quit).
     * 
     * @param restart The restart parameter value (null if not restarting)
     */
    private void executeEndScenario(String restart) {
        if ("allowed".equals(restart)) {
            // Restart the scenario
            restartScenario();
        } else {
            // Quit the application
            System.out.println("Scenario ended. Quitting application.");
            System.exit(0);
        }
    }
    
    /**
     * Handles battle end and loads appropriate scenario or continues to next action.
     */
    private void handleBattleEnd() {
        if (currentBattleManager == null) {
            return;
        }
        
        boolean playerWon = currentBattleManager.didPlayerWin();
        Object outcomeAction = null;
        
        // Get outcome action from the battle action
        // First check if we have a stored battle action (from executeRawAction)
        if (currentBattleAction != null) {
            outcomeAction = playerWon ? currentBattleAction.getOnWin() : currentBattleAction.getOnLose();
        } else if (currentActionList != null && currentActionIndex > 0) {
            // Look back at the battle action in the action list
            for (int i = currentActionIndex - 1; i >= 0; i--) {
                ScenarioAction action = currentActionList.get(i);
                if (action.getActionType() != null && 
                    BattleAction.getName().equals(action.getActionType().getJsonValue())) {
                    outcomeAction = playerWon ? action.getOnWin() : action.getOnLose();
                    break;
                }
            }
        }
        
        // Clear battle manager and battle action
        currentBattleManager = null;
        currentBattleAction = null;
        
        // Use unified ActionHandler to execute the outcome action
        // outcomeAction can be a String (file path), Map (single action), or List (array of actions)
        String basePath = getCurrentScenarioBasePath();
        ActionHandler.executeActionSequence(outcomeAction, this, basePath);
    }
    
    /**
     * Processes the next action in the scenario.
     * This method is public so that actions can call it to continue execution.
     * Uses a loop instead of recursion to handle null action types.
     */
    public void processNextAction() {
        // Loop to skip null action types without recursion
        while (true) {
            if (currentActionList == null || currentActionIndex >= currentActionList.size()) {
                // Sequence exhausted - try to restore previous state from stack
                if (popActionSequenceState()) {
                    // State restored, continue processing the restored sequence
                    continue;
                } else {
                    // No state to restore, truly out of actions
                    return;
                }
            }
            
            ScenarioAction action = currentActionList.get(currentActionIndex);
            currentActionIndex++;
            
            ActionType actionType = action.getActionType();
            if (actionType == null) {
                System.err.println("Warning: Action has no action type, skipping.");
                // Continue loop to process next action
                continue;
            }
            
            // Store Battle action for handleBattleEnd to find OnWin/OnLose
            if (BattleAction.getName().equals(actionType.getJsonValue())) {
                currentBattleAction = action;
            }
            
            // Execute the action
            actionType.execute(action, this);
            
            // Use ActionFlowController to handle continuation logic
            actionFlowController.advanceAfter(actionType, action);
            return;
        }
    }
    
    /**
     * Gets the GUI instance. Used by actions to interact with the UI.
     * 
     * @return The VisualNovelGUI instance
     */
    public VisualNovelGUI getGUI() {
        return gui;
    }
    
    /**
     * Gets the base path of the current scenario file. Used for resolving relative paths.
     * 
     * @return The base directory path of the current scenario
     */
    public String getCurrentScenarioBasePath() {
        return currentScenarioBasePath;
    }
    
    /**
     * Gets a character by ID from the main scenario's character dictionary.
     * This allows branch scenarios to reference characters defined in the main scenario.
     * 
     * @param characterId The ID of the character to look up
     * @return The Character object, or null if not found
     */
    public Character getCharacter(String characterId) {
        if (mainScenario == null || mainScenario.getCharacters() == null) {
            return null;
        }
        return mainScenario.getCharacters().get(characterId);
    }
    
    /**
     * Gets an identifiable object by ID from the main scenario's ids dictionary.
     * This allows branch scenarios to reference objects defined in the main scenario.
     * 
     * @param objectId The ID of the object to look up
     * @return The IdentifiableObject instance, or null if not found
     */
    public com.visualnovel.model.IdentifiableObject getIdentifiableObject(String objectId) {
        if (mainScenario == null || mainScenario.getIds() == null) {
            return null;
        }
        return mainScenario.getIds().get(objectId);
    }
    
    /**
     * Gets the current scenario.
     * 
     * @return The current Scenario object
     */
    public Scenario getScenario() {
        return scenario;
    }
    
    /**
     * Gets the main scenario (entry point scenario with characters).
     * 
     * @return The main Scenario object, or null if not loaded
     */
    public Scenario getMainScenario() {
        return mainScenario;
    }
    
    /**
     * Pushes the current action sequence state onto the stack for later restoration.
     * Called before branching to save the current state.
     */
    private void pushActionSequenceState() {
        if (currentActionList != null) {
            actionSequenceStack.push(new ActionSequenceState(
                currentActionList,
                currentActionIndex,
                currentScenarioBasePath
            ));
        }
    }
    
    /**
     * Pops and restores the previous action sequence state from the stack.
     * Called when a branch sequence is exhausted to return to the previous sequence.
     * 
     * @return true if state was restored, false if stack was empty
     */
    private boolean popActionSequenceState() {
        if (actionSequenceStack.isEmpty()) {
            return false;
        }
        
        ActionSequenceState state = actionSequenceStack.pop();
        currentActionList = state.actionList;
        currentActionIndex = state.actionIndex;
        currentScenarioBasePath = state.basePath;
        return true;
    }
    
    /**
     * Loads a branch scenario (e.g., win.json or lose.json) and continues execution.
     * This method is used by actions like BattleAction to load new scenario files.
     * Branch scenarios are nested scenarios and should not be entry points.
     * 
     * @param filePath The path to the scenario file to load
     * @throws IOException If the file cannot be read
     */
    public void loadBranchScenario(String filePath) throws IOException {
        ScenarioLoader loader = new ScenarioLoader();
        Scenario branchScenario = loader.loadScenario(filePath);
        
        // Branch scenarios should not be entry points (they're nested)
        if (branchScenario.isEntryPoint()) {
            System.err.println("Warning: Branch scenario is marked as entry point: " + filePath);
        }
        
        // Push current state onto stack before branching
        pushActionSequenceState();
        
        currentActionList = branchScenario.getSequence();
        currentActionIndex = 0;
        
        // Update the base path to the newly loaded scenario's directory
        File branchFile = new File(filePath);
        currentScenarioBasePath = branchFile.getParent();
        if (currentScenarioBasePath == null) {
            currentScenarioBasePath = "";
        }
        
        processNextAction();
    }
    
    /**
     * Executes a list of actions directly without loading from a file.
     * This is used by ActionHandler to execute action sequences.
     * 
     * @param actionList The list of ScenarioAction objects to execute
     * @param basePath The base path for resolving relative file paths (can be null)
     */
    public void executeActionList(List<ScenarioAction> actionList, String basePath) {
        if (actionList == null || actionList.isEmpty()) {
            processNextAction();
            return;
        }
        
        // Push current state onto stack before branching
        pushActionSequenceState();
        
        // Set the new action list
        currentActionList = actionList;
        currentActionIndex = 0;
        
        // Update the base path if provided
        if (basePath != null) {
            currentScenarioBasePath = basePath;
        }
        
        // Execute the first action
        processNextAction();
        
        // Note: The action list will continue executing until it's done or replaced
        // We don't restore the previous state here because the actions may continue
        // executing asynchronously. The previous state will be restored when the
        // action list is exhausted or replaced.
    }
    
    /**
     * Jumps to an action in the current scenario by its ID.
     * Searches through the current action list to find an action with the matching ID.
     * 
     * @param targetId The ID of the action to jump to
     */
    public void jumpToActionById(String targetId) {
        if (currentActionList == null) {
            System.err.println("Warning: Cannot jump to action - no scenario loaded.");
            return;
        }
        
        // Search for the action with the matching ID
        for (int i = 0; i < currentActionList.size(); i++) {
            ScenarioAction action = currentActionList.get(i);
            if (targetId.equals(action.getId())) {
                // Found the target action - set the index to it
                currentActionIndex = i;
                processNextAction();
                return;
            }
        }
        
        // Action with the specified ID not found
        System.err.println("Warning: Action with ID '" + targetId + "' not found in current scenario.");
    }
    
    /**
     * Sets the current battle manager.
     * 
     * @param battleManager The battle manager
     */
    public void setBattleManager(BattleManager battleManager) {
        this.currentBattleManager = battleManager;
    }
    
    /**
     * Sets whether the game is waiting for user input.
     * 
     * @param waiting true if waiting for user input, false otherwise
     */
    public void setWaitingForUserInput(boolean waiting) {
        this.waitingForUserInput = waiting;
    }
    
    /**
     * Gets the current battle manager.
     * 
     * @return The battle manager, or null if not in battle
     */
    public BattleManager getBattleManager() {
        return currentBattleManager;
    }
    
    /**
     * Sets the current delay timer.
     * Used by DelayAction to allow skipping delays.
     * 
     * @param timer The delay timer, or null to clear
     */
    public void setCurrentDelayTimer(javax.swing.Timer timer) {
        this.currentDelayTimer = timer;
    }
    
    /**
     * Executes a raw action from a Map (typically from a ChooseAction's onPress).
     * Creates a ScenarioAction from the map and executes it.
     * 
     * @param actionMap The map containing action data (action, timing, parameters)
     */
    @SuppressWarnings("unchecked")
    public void executeRawAction(Map<String, Object> actionMap) {
        if (actionMap == null) {
            System.err.println("Warning: Cannot execute raw action - action map is null.");
            return;
        }
        
        // Create a ScenarioAction from the map
        ScenarioAction action = new ScenarioAction();
        
        // Parse action type
        String actionStr = (String) actionMap.get("action");
        if (actionStr == null) {
            System.err.println("Warning: Raw action has no action type, skipping.");
            return;
        }
        
        ActionRegistry registry = ActionRegistry.getInstance();
        ActionType actionType = registry.get(actionStr);
        if (actionType == null) {
            System.err.println("Warning: Unknown action type in raw action: " + actionStr);
            return;
        }
        action.setActionType(actionType);
        
        // Parse timing
        Object timingObj = actionMap.get("timing");
        if (timingObj instanceof Map) {
            Map<String, Object> timingMap = (Map<String, Object>) timingObj;
            Timing timing = new Timing();
            
            if (timingMap.containsKey("type")) {
                timing.setType((String) timingMap.get("type"));
            }
            if (timingMap.containsKey("durationMs")) {
                Object duration = timingMap.get("durationMs");
                if (duration instanceof Number) {
                    timing.setDurationMs(((Number) duration).intValue());
                }
            }
            if (timingMap.containsKey("animation")) {
                timing.setAnimation((String) timingMap.get("animation"));
            }
            
            action.setTiming(timing);
        }
        
        // Parse parameters
        Object paramsObj = actionMap.get("parameters");
        if (paramsObj instanceof Map) {
            Map<String, Object> paramsMap = (Map<String, Object>) paramsObj;
            action.setParameters(paramsMap);
            
            // Extract OnWin and OnLose from parameters (for Battle actions)
            if (paramsMap.containsKey("OnWin")) {
                action.setOnWin(paramsMap.get("OnWin"));
            }
            if (paramsMap.containsKey("OnLose")) {
                action.setOnLose(paramsMap.get("OnLose"));
            }
        }
        
        // Store Battle action for handleBattleEnd to find OnWin/OnLose
        if (BattleAction.getName().equals(actionStr)) {
            currentBattleAction = action;
        }
        
        // Execute the action
        actionType.execute(action, this);
        
        // Use ActionFlowController to handle continuation logic
        actionFlowController.advanceAfter(actionType, action);
    }
    
    /**
     * Gets persistent stats for a character.
     * Used when battle_strategy is "persist_stats" to maintain state between battles.
     * 
     * @param characterId The character ID
     * @return The CharacterStats object, or null if not found
     */
    public CharacterStats getPersistentStats(String characterId) {
        return persistentCharacterStats.get(characterId);
    }
    
    /**
     * Saves persistent stats for a character.
     * Used when battle_strategy is "persist_stats" to maintain state between battles.
     * 
     * @param characterId The character ID
     * @param stats The CharacterStats object to save
     */
    public void savePersistentStats(String characterId, CharacterStats stats) {
        if (characterId != null && stats != null) {
            persistentCharacterStats.put(characterId, stats);
        }
    }
    
    /**
     * Clears all persistent stats.
     * Should be called when starting a new scenario or resetting the game.
     */
    public void clearPersistentStats() {
        persistentCharacterStats.clear();
    }
    
    /**
     * Gets persistent inventory for a character.
     * 
     * @param characterId The character ID
     * @return The inventory map (item name -> InventoryItem), or null if not found
     */
    public Map<String, InventoryItem> getCharacterInventory(String characterId) {
        return persistentCharacterInventories.get(characterId);
    }
    
    /**
     * Saves persistent inventory for a character.
     * 
     * @param characterId The character ID
     * @param inventory The inventory map (item name -> InventoryItem) to save
     */
    public void setCharacterInventory(String characterId, Map<String, InventoryItem> inventory) {
        if (characterId != null) {
            if (inventory != null) {
                persistentCharacterInventories.put(characterId, inventory);
            } else {
                persistentCharacterInventories.remove(characterId);
            }
        }
    }
    
    /**
     * Initializes character inventory from IdentifiableObject definition.
     * Only initializes if inventory doesn't already exist (to preserve runtime changes).
     * 
     * @param characterId The character ID
     */
    public void initializeCharacterInventory(String characterId) {
        // Only initialize if inventory doesn't already exist
        if (persistentCharacterInventories.containsKey(characterId)) {
            return;
        }
        
        IdentifiableObject obj = getIdentifiableObject(characterId);
        if (obj != null && obj.getInventory() != null) {
            // Create a deep copy of the inventory
            Map<String, InventoryItem> inventoryCopy = new HashMap<>();
            for (Map.Entry<String, InventoryItem> entry : obj.getInventory().entrySet()) {
                InventoryItem original = entry.getValue();
                InventoryItem copy = new InventoryItem(
                    original.getName(),
                    original.getDescription(),
                    original.getType(),
                    original.getQuantity()
                );
                inventoryCopy.put(entry.getKey(), copy);
            }
            persistentCharacterInventories.put(characterId, inventoryCopy);
        }
    }
    
    /**
     * Shows the player's inventory.
     * Gets the player character ID and displays their inventory.
     */
    public void showPlayerInventory() {
        // Try to find the player character - typically "Player" ID
        String playerId = "Player";
        Map<String, InventoryItem> inventory = getCharacterInventory(playerId);
        
        if (gui != null) {
            gui.showInventoryDialog(inventory);
        }
    }
    
    /**
     * Sets the pending EndScenarioAction restart value.
     * Called by EndScenarioAction when timing is "Interaction" to defer execution until user clicks.
     * 
     * @param restart The restart parameter value
     */
    public void setEndScenarioActionPending(String restart) {
        this.pendingEndScenarioRestart = restart;
        this.waitingForUserInput = true;
    }
    
    /**
     * Resets the game state to initial conditions.
     * Clears persistent stats, resets action indices, and clears battle state.
     */
    public void resetGameState() {
        clearPersistentStats();
        currentActionIndex = 0;
        currentActionList = null;
        waitingForUserInput = false;
        currentBattleManager = null;
        currentBattleAction = null;
        pendingEndScenarioRestart = null;
        actionSequenceStack.clear(); // Clear the action sequence stack
        if (currentDelayTimer != null) {
            currentDelayTimer.stop();
            currentDelayTimer = null;
        }
    }
    
    /**
     * Restarts the scenario from the beginning.
     * Resets all game state and reloads the entry point scenario.
     */
    public void restartScenario() {
        if (entryPointScenarioPath == null) {
            System.err.println("Warning: Cannot restart scenario - no entry point scenario path stored.");
            return;
        }
        
        // Clear GUI state (sprites, screen, health bars, text box)
        if (gui != null) {
            gui.clearSprites();
            gui.clearScreen();
            gui.clearHealthBars();
            gui.clearTextBox();
            gui.refresh();
        }
        
        // Reset game state
        resetGameState();
        
        // Reload the entry point scenario
        try {
            loadScenario(entryPointScenarioPath, true);
        } catch (Exception e) {
            System.err.println("Error restarting scenario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}

