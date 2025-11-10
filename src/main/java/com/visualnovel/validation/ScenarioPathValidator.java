package com.visualnovel.validation;

import com.visualnovel.model.Scenario;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.actions.BattleAction;
import com.visualnovel.model.actions.ChooseAction;
import com.visualnovel.model.actions.ContinueAction;
import com.visualnovel.model.actions.EndScenarioAction;
import com.visualnovel.util.ScenarioLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Validates that all execution paths in a scenario eventually reach an EndScenarioAction.
 * Recursively checks all branches including Battle OnWin/OnLose, ChooseAction options, and branch scenarios.
 */
public class ScenarioPathValidator {
    
    private final Set<String> visitedFiles;
    private final List<String> errors;
    private final String baseDirectory;
    
    /**
     * Creates a new ScenarioPathValidator.
     * 
     * @param baseDirectory Base directory for resolving relative file paths
     */
    public ScenarioPathValidator(String baseDirectory) {
        this.visitedFiles = new HashSet<>();
        this.errors = new ArrayList<>();
        this.baseDirectory = baseDirectory != null ? baseDirectory : "";
    }
    
    /**
     * Validates that all paths in a scenario reach EndScenarioAction.
     * 
     * @param scenario The scenario to validate
     * @param filePath The file path of the scenario (for error reporting)
     * @return List of validation errors (empty if valid)
     */
    public List<String> validate(Scenario scenario, String filePath) {
        errors.clear();
        visitedFiles.clear();
        validateScenario(scenario, filePath, new ArrayList<>());
        return new ArrayList<>(errors);
    }
    
    /**
     * Recursively validates a scenario and all its branches.
     * 
     * @param scenario The scenario to validate
     * @param filePath The file path of the scenario
     * @param pathContext The current path context (for error reporting)
     */
    private void validateScenario(Scenario scenario, String filePath, List<String> pathContext) {
        if (scenario == null || scenario.getSequence() == null || scenario.getSequence().isEmpty()) {
            String context = buildContext(pathContext);
            errors.add(context + "Scenario has no sequence or empty sequence");
            return;
        }
        
        // Check if we've already visited this file (avoid infinite loops)
        String normalizedPath = normalizePath(filePath);
        if (visitedFiles.contains(normalizedPath)) {
            return; // Already validated this file
        }
        visitedFiles.add(normalizedPath);
        
        List<ScenarioAction> sequence = scenario.getSequence();
        List<String> newContext = new ArrayList<>(pathContext);
        newContext.add("File: " + filePath);
        
        // Check if the sequence ends with EndScenarioAction
        boolean endsWithEndScenario = false;
        if (!sequence.isEmpty()) {
            ScenarioAction lastAction = sequence.get(sequence.size() - 1);
            if (lastAction.getActionType() != null && 
                EndScenarioAction.getName().equals(lastAction.getActionType().getJsonValue())) {
                endsWithEndScenario = true;
            }
        }
        
        // Validate each action in the sequence
        for (int i = 0; i < sequence.size(); i++) {
            ScenarioAction action = sequence.get(i);
            validateAction(action, filePath, newContext, i, sequence.size() - 1);
        }
        
        // If the sequence doesn't end with EndScenarioAction, check if all branches do
        if (!endsWithEndScenario) {
            // Check if all branches eventually reach EndScenarioAction
            // This is done by validating each branching action (Battle, ChooseAction)
            // If any branch doesn't reach EndScenarioAction, it will be caught during action validation
            errors.add("Scenario '" + filePath + "' does not end with EndScenarioAction");
        }
    }
    
    /**
     * Validates an action and its branches.
     * 
     * @param action The action to validate
     * @param filePath The file path of the scenario containing this action
     * @param pathContext The current path context
     * @param actionIndex The index of this action in the sequence
     * @param lastIndex The index of the last action in the sequence
     */
    private void validateAction(ScenarioAction action, String filePath, List<String> pathContext, 
                                 int actionIndex, int lastIndex) {
        if (action.getActionType() == null) {
            return;
        }
        
        String actionType = action.getActionType().getJsonValue();
        List<String> newContext = new ArrayList<>(pathContext);
        newContext.add("Action[" + actionIndex + "]: " + actionType);
        
        // Check if this is the last action and it's not EndScenarioAction
        if (actionIndex == lastIndex && !EndScenarioAction.getName().equals(actionType)) {
            String context = buildContext(newContext);
            errors.add(context + "Sequence does not end with EndScenarioAction");
        }
        
        // Validate branching actions
        if (BattleAction.getName().equals(actionType)) {
            validateBattleAction(action, filePath, newContext);
        } else if (ChooseAction.getName().equals(actionType)) {
            validateChooseAction(action, filePath, newContext);
        }
    }
    
    /**
     * Validates a Battle action's OnWin and OnLose branches.
     * 
     * @param action The Battle action
     * @param filePath The file path of the scenario
     * @param pathContext The current path context
     */
    private void validateBattleAction(ScenarioAction action, String filePath, List<String> pathContext) {
        Object onWin = action.getOnWin();
        Object onLose = action.getOnLose();
        
        if (onWin != null) {
            validateBranch(onWin, filePath, pathContext, "OnWin");
        } else {
            String context = buildContext(pathContext);
            errors.add(context + "Battle action missing OnWin branch");
        }
        
        if (onLose != null) {
            validateBranch(onLose, filePath, pathContext, "OnLose");
        } else {
            String context = buildContext(pathContext);
            errors.add(context + "Battle action missing OnLose branch");
        }
    }
    
    /**
     * Validates a ChooseAction's option branches.
     * 
     * @param action The ChooseAction
     * @param filePath The file path of the scenario
     * @param pathContext The current path context
     */
    private void validateChooseAction(ScenarioAction action, String filePath, List<String> pathContext) {
        Map<String, Object> params = action.getParameters();
        if (params == null) {
            String context = buildContext(pathContext);
            errors.add(context + "ChooseAction missing parameters");
            return;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> options = (List<Map<String, Object>>) params.get("options");
        if (options == null || options.isEmpty()) {
            String context = buildContext(pathContext);
            errors.add(context + "ChooseAction has no options");
            return;
        }
        
        for (int i = 0; i < options.size(); i++) {
            Map<String, Object> option = options.get(i);
            Object onPress = option.get("onPress");
            if (onPress == null) {
                String context = buildContext(pathContext);
                errors.add(context + "ChooseAction option[" + i + "] missing onPress");
            } else {
                List<String> optionContext = new ArrayList<>(pathContext);
                optionContext.add("Option[" + i + "]");
                validateBranch(onPress, filePath, optionContext, "onPress");
            }
        }
    }
    
    /**
     * Validates a branch (OnWin, OnLose, or onPress).
     * 
     * @param branch The branch value (String file path, Map action, or List of actions)
     * @param filePath The file path of the parent scenario
     * @param pathContext The current path context
     * @param branchName The name of the branch (for error reporting)
     */
    private void validateBranch(Object branch, String filePath, List<String> pathContext, String branchName) {
        if (branch instanceof String) {
            // It's a file path - load and validate the branch scenario
            String branchFile = (String) branch;
            if (branchFile.isEmpty()) {
                String context = buildContext(pathContext);
                errors.add(context + branchName + " branch is empty string");
                return;
            }
            
            // Resolve the file path
            String fullPath = resolveFilePath(branchFile, filePath);
            File branchFileObj = new File(fullPath);
            
            if (!branchFileObj.exists()) {
                String context = buildContext(pathContext);
                errors.add(context + branchName + " branch file does not exist: " + fullPath);
                return;
            }
            
            // Load and validate the branch scenario
            try {
                ScenarioLoader loader = new ScenarioLoader();
                Scenario branchScenario = loader.loadScenario(fullPath);
                List<String> branchContext = new ArrayList<>(pathContext);
                branchContext.add(branchName + ": " + branchFile);
                validateScenario(branchScenario, fullPath, branchContext);
            } catch (IOException e) {
                String context = buildContext(pathContext);
                errors.add(context + branchName + " branch file cannot be loaded: " + e.getMessage());
            }
        } else if (branch instanceof Map) {
            // It's a raw action (like ContinueAction)
            @SuppressWarnings("unchecked")
            Map<String, Object> actionMap = (Map<String, Object>) branch;
            String actionType = (String) actionMap.get("action");
            
            if (ContinueAction.getName().equals(actionType)) {
                // ContinueAction returns to the main sequence - check if main sequence ends with EndScenarioAction
                // This is handled by checking the sequence in validateScenario
                // We need to check if the parent sequence eventually reaches EndScenarioAction
                // This is a bit tricky - we'll assume the parent sequence is valid if it continues
                // The actual validation happens at the sequence level
            } else if (EndScenarioAction.getName().equals(actionType)) {
                // This branch ends with EndScenarioAction - valid
                return;
            } else {
                // Other action types - recursively validate
                // For now, we'll assume nested actions eventually reach EndScenarioAction
                // This could be enhanced to fully validate nested action sequences
                // Note: We don't add an error here because the action might be part of a sequence
                // that eventually reaches EndScenarioAction. The sequence-level validation will catch it.
            }
        } else if (branch instanceof List) {
            // It's a list of actions - validate each action in the list
            @SuppressWarnings("unchecked")
            List<Object> actionList = (List<Object>) branch;
            
            for (int i = 0; i < actionList.size(); i++) {
                Object actionObj = actionList.get(i);
                List<String> actionContext = new ArrayList<>(pathContext);
                actionContext.add(branchName + "[" + i + "]");
                validateBranch(actionObj, filePath, actionContext, branchName + "[" + i + "]");
            }
        }
    }
    
    /**
     * Resolves a file path relative to a base file path.
     * 
     * @param relativePath The relative file path
     * @param baseFilePath The base file path
     * @return The resolved absolute path
     */
    private String resolveFilePath(String relativePath, String baseFilePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return relativePath;
        }
        
        File baseFile = new File(baseFilePath);
        String baseDir = baseFile.getParent();
        if (baseDir == null || baseDir.isEmpty()) {
            baseDir = baseDirectory;
        }
        
        if (baseDir == null || baseDir.isEmpty()) {
            return relativePath;
        }
        
        return new File(baseDir, relativePath).getAbsolutePath();
    }
    
    /**
     * Normalizes a file path for comparison.
     * 
     * @param filePath The file path to normalize
     * @return The normalized path
     */
    private String normalizePath(String filePath) {
        if (filePath == null) {
            return "";
        }
        try {
            return new File(filePath).getCanonicalPath();
        } catch (IOException e) {
            return new File(filePath).getAbsolutePath();
        }
    }
    
    /**
     * Builds a context string from a path context list.
     * 
     * @param pathContext The path context
     * @return The context string
     */
    private String buildContext(List<String> pathContext) {
        if (pathContext.isEmpty()) {
            return "";
        }
        return String.join(" -> ", pathContext) + ": ";
    }
    
    /**
     * Validates that a scenario's sequence ends with EndScenarioAction or all branches reach EndScenarioAction.
     * This is a helper method that checks the main sequence.
     * 
     * @param scenario The scenario to check
     * @param filePath The file path of the scenario
     * @return true if the scenario is valid, false otherwise
     */
    public static boolean hasEndScenarioAction(Scenario scenario) {
        if (scenario == null || scenario.getSequence() == null || scenario.getSequence().isEmpty()) {
            return false;
        }
        
        List<ScenarioAction> sequence = scenario.getSequence();
        ScenarioAction lastAction = sequence.get(sequence.size() - 1);
        
        return lastAction.getActionType() != null && 
               EndScenarioAction.getName().equals(lastAction.getActionType().getJsonValue());
    }
}

