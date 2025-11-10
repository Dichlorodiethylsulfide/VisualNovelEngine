package com.visualnovel.util;

import com.visualnovel.GameController;
import com.visualnovel.model.ActionRegistry;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.Timing;
import com.visualnovel.model.actions.ContinueAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unified handler for executing action sequences.
 * Handles onPress, OnWin, OnLose and similar parameters that can be:
 * - String (file path) → loads a scenario file
 * - Map (single action object) → executes a single action
 * - List (array of actions) → executes a sequence of actions
 */
public class ActionHandler {
    
    /**
     * Executes an action sequence. The actionSequence parameter can be:
     * - String: A file path to a scenario file (loads the scenario)
     * - Map: A single action object (executes the action)
     * - List: An array of action objects (executes each action in sequence)
     * 
     * @param actionSequence The action sequence to execute (String, Map, or List)
     * @param controller The GameController instance
     * @param basePath The base path for resolving relative file paths (can be null)
     */
    @SuppressWarnings("unchecked")
    public static void executeActionSequence(Object actionSequence, GameController controller, String basePath) {
        if (actionSequence == null) {
            // No action specified, just continue to next action
            controller.processNextAction();
            return;
        }
        
        if (actionSequence instanceof String) {
            // It's a file path - load the scenario file
            String filePath = (String) actionSequence;
            if (!filePath.isEmpty()) {
                String fullPath;
                if (basePath != null && !basePath.isEmpty()) {
                    fullPath = basePath + File.separator + filePath;
                } else {
                    fullPath = filePath;
                }
                
                try {
                    controller.loadBranchScenario(fullPath);
                } catch (IOException e) {
                    System.err.println("Error loading branch scenario: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with next action if loading fails
                    controller.processNextAction();
                }
            } else {
                controller.processNextAction();
            }
        } else if (actionSequence instanceof Map) {
            // It's a single action object - execute it
            Map<String, Object> actionMap = (Map<String, Object>) actionSequence;
            String actionType = (String) actionMap.get("action");
            
            if (ContinueAction.getName().equals(actionType)) {
                // ContinueAction - just continue to next action in sequence
                controller.processNextAction();
            } else {
                // Execute the action (could be any action type)
                controller.executeRawAction(actionMap);
            }
        } else if (actionSequence instanceof List) {
            // It's an array of actions - execute each action in sequence
            List<Object> actionList = (List<Object>) actionSequence;
            
            if (actionList.isEmpty()) {
                // Empty list, just continue
                controller.processNextAction();
                return;
            }
            
            // Execute actions sequentially by creating a temporary scenario
            // This ensures actions execute in order and handle async actions properly
            executeActionListSequentially(actionList, controller, basePath);
        } else {
            System.err.println("Warning: Invalid action sequence type: " + actionSequence.getClass().getName());
            controller.processNextAction();
        }
    }
    
    /**
     * Executes a list of actions sequentially.
     * Converts Map actions to ScenarioAction objects and executes them using executeActionList.
     * 
     * @param actionList The list of actions to execute
     * @param controller The GameController instance
     * @param basePath The base path for resolving relative file paths
     */
    @SuppressWarnings("unchecked")
    private static void executeActionListSequentially(List<Object> actionList, GameController controller, String basePath) {
        if (actionList.isEmpty()) {
            controller.processNextAction();
            return;
        }
        
        // Convert Map actions to ScenarioAction objects
        List<ScenarioAction> scenarioActions = new ArrayList<>();
        
        for (Object actionObj : actionList) {
            if (actionObj instanceof Map) {
                Map<String, Object> actionMap = (Map<String, Object>) actionObj;
                String actionType = (String) actionMap.get("action");
                
                // Skip ContinueAction
                if (ContinueAction.getName().equals(actionType)) {
                    continue;
                }
                
                // Convert Map to ScenarioAction
                ScenarioAction action = convertMapToScenarioAction(actionMap);
                if (action != null) {
                    scenarioActions.add(action);
                }
            } else if (actionObj instanceof String) {
                // String - load scenario (this will replace the current sequence)
                executeActionSequence(actionObj, controller, basePath);
                return; // String loads a scenario, which replaces the current sequence
            }
        }
        
        if (scenarioActions.isEmpty()) {
            controller.processNextAction();
            return;
        }
        
        // Execute the list of actions
        controller.executeActionList(scenarioActions, basePath);
    }
    
    /**
     * Converts a Map action to a ScenarioAction object.
     * This is similar to the logic in GameController.executeRawAction.
     * 
     * @param actionMap The map containing action data (action, timing, parameters)
     * @return The ScenarioAction object, or null if conversion fails
     */
    @SuppressWarnings("unchecked")
    private static ScenarioAction convertMapToScenarioAction(Map<String, Object> actionMap) {
        if (actionMap == null) {
            return null;
        }
        
        ScenarioAction action = new ScenarioAction();
        
        // Parse action type
        String actionStr = (String) actionMap.get("action");
        if (actionStr == null) {
            System.err.println("Warning: Action map has no action type, skipping.");
            return null;
        }
        
        ActionRegistry registry = ActionRegistry.getInstance();
        ActionType actionType = registry.get(actionStr);
        if (actionType == null) {
            System.err.println("Warning: Unknown action type in action map: " + actionStr);
            return null;
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
        
        return action;
    }
}
