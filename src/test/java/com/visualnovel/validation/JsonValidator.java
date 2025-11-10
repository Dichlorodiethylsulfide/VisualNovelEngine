package com.visualnovel.validation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.visualnovel.model.ActionRegistry;
import com.visualnovel.model.Character;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.Scenario;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.Timing;
import com.visualnovel.model.actions.BattleAction;
import com.visualnovel.model.actions.ChooseAction;
import com.visualnovel.model.battle.Move;
import com.visualnovel.util.ScenarioLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for validating JSON scenario files.
 * Provides comprehensive validation checks for syntax, structure, data types,
 * references, and constraints.
 */
public class JsonValidator {
    
    private static final Set<String> VALID_TIMING_TYPES = Set.of("Immediate", "Interaction", "Animated");
    private static final Set<String> VALID_ANIMATION_TYPES = Set.of("fadeIn", "flash");
    private static final Set<String> VALID_SPRITE_POSITIONS = Set.of("Left", "Right", "Center", "Back");
    private static final Set<String> VALID_MOVE_TYPES = Set.of("physical", "defense", "healing", "status_effect");
    
    /**
     * Validation result containing errors found during validation.
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public String getErrorSummary() {
            if (errors.isEmpty()) {
                return "No errors found.";
            }
            return String.join("\n", errors);
        }
    }
    
    /**
     * Validates a JSON scenario file.
     * 
     * @param filePath Path to the JSON file
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validateFile(String filePath) {
        return validateFile(filePath, null);
    }
    
    /**
     * Validates a JSON scenario file, optionally with an entry point scenario for reference validation.
     * 
     * @param filePath Path to the JSON file
     * @param entryPointScenario Optional entry point scenario for validating references in nested scenarios
     * @return ValidationResult containing any errors found
     */
    public static ValidationResult validateFile(String filePath, Scenario entryPointScenario) {
        ValidationResult result = new ValidationResult();
        File file = new File(filePath);
        
        if (!file.exists()) {
            result.addError("File does not exist: " + filePath);
            return result;
        }
        
        // Validate JSON syntax
        validateJsonSyntax(filePath, result);
        if (result.hasErrors()) {
            return result; // Stop if syntax is invalid
        }
        
        // Load and validate structure
        try {
            ScenarioLoader loader = new ScenarioLoader();
            Scenario scenario = loader.loadScenario(filePath);
            
            // Validate structure
            validateStructure(scenario, filePath, result);
            
            // Validate data types
            validateDataTypes(scenario, result);
            
            // Validate references (use entry point scenario if this is a nested scenario)
            Scenario refScenario = scenario.isEntryPoint() ? scenario : entryPointScenario;
            if (refScenario == null && !scenario.isEntryPoint()) {
                // For nested scenarios without entry point, we can't fully validate references
                // This is acceptable - cross-file validation will catch missing references
            } else {
                validateReferences(scenario, filePath, refScenario, result);
            }
            
            // Validate constraints
            validateConstraints(scenario, result);
            
            // Validate that all paths reach EndScenarioAction
            validateScenarioPaths(scenario, filePath, result);
            
        } catch (IOException e) {
            result.addError("Failed to load scenario: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Validates JSON syntax.
     */
    private static void validateJsonSyntax(String filePath, ValidationResult result) {
        try {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(filePath)) {
                gson.fromJson(reader, JsonObject.class);
            }
        } catch (JsonParseException e) {
            result.addError("Invalid JSON syntax: " + e.getMessage());
        } catch (IOException e) {
            result.addError("Failed to read file: " + e.getMessage());
        }
    }
    
    /**
     * Validates scenario structure.
     */
    private static void validateStructure(Scenario scenario, String filePath, ValidationResult result) {
        // Check if it's an entry point scenario
        if (scenario.isEntryPoint()) {
            // Entry point scenarios must have certain fields
            if (scenario.getName() == null || scenario.getName().isEmpty()) {
                result.addError("Entry point scenario missing required field: name");
            }
            if (scenario.getIds() == null || scenario.getIds().isEmpty()) {
                result.addError("Entry point scenario missing required field: ids");
            }
        }
        
        // All scenarios must have a sequence
        if (scenario.getSequence() == null || scenario.getSequence().isEmpty()) {
            result.addError("Scenario missing required field: sequence (or sequence is empty)");
        }
    }
    
    /**
     * Validates data types.
     */
    private static void validateDataTypes(Scenario scenario, ValidationResult result) {
        if (scenario.getSequence() == null) {
            return;
        }
        
        for (int i = 0; i < scenario.getSequence().size(); i++) {
            ScenarioAction action = scenario.getSequence().get(i);
            String actionPrefix = "Action at index " + i;
            
            // Validate action type
            if (action.getActionType() == null) {
                result.addError(actionPrefix + ": Missing action type");
                continue;
            }
            
            // Validate timing
            if (action.getTiming() == null) {
                result.addError(actionPrefix + ": Missing timing");
            } else {
                Timing timing = action.getTiming();
                if (timing.getType() == null) {
                    result.addError(actionPrefix + ": Timing missing type");
                } else if (!VALID_TIMING_TYPES.contains(timing.getType())) {
                    result.addError(actionPrefix + ": Invalid timing type: " + timing.getType());
                }
                
                // Validate animation-related fields
                if ("Animated".equals(timing.getType())) {
                    if (timing.getDurationMs() == null || timing.getDurationMs() <= 0) {
                        result.addError(actionPrefix + ": Animated timing must have positive durationMs");
                    }
                    if (timing.getAnimation() != null && !VALID_ANIMATION_TYPES.contains(timing.getAnimation())) {
                        result.addError(actionPrefix + ": Invalid animation type: " + timing.getAnimation());
                    }
                }
            }
            
            // Validate parameters based on action type
            validateActionParameters(action, actionPrefix, result);
        }
    }
    
    /**
     * Validates action-specific parameters.
     */
    private static void validateActionParameters(ScenarioAction action, String prefix, ValidationResult result) {
        Map<String, Object> params = action.getParameters();
        if (params == null) {
            return;
        }
        
        String actionType = action.getActionType().getJsonValue();
        
        if ("ShowSprite".equals(actionType)) {
            // Validate position
            Object positionObj = params.get("position");
            if (positionObj != null) {
                String position = positionObj.toString();
                if (!VALID_SPRITE_POSITIONS.contains(position)) {
                    result.addError(prefix + ": Invalid sprite position: " + position);
                }
            }
            
            // Validate depth if present
            Object depthObj = params.get("depth");
            if (depthObj != null && !(depthObj instanceof Number)) {
                result.addError(prefix + ": depth must be a number");
            }
            
            // Validate offset if present
            Object offsetObj = params.get("offset");
            if (offsetObj != null && !(offsetObj instanceof Number)) {
                result.addError(prefix + ": offset must be a number");
            }
        }
    }
    
    /**
     * Validates references (character IDs, action types, sprite references, etc.).
     * 
     * @param scenario The scenario to validate
     * @param filePath Path to the scenario file
     * @param referenceScenario The scenario to use for reference validation (entry point scenario for nested scenarios)
     * @param result Validation result to add errors to
     */
    private static void validateReferences(Scenario scenario, String filePath, Scenario referenceScenario, ValidationResult result) {
        ActionRegistry registry = ActionRegistry.getInstance();
        Set<String> availableCharacterIds = new HashSet<>();
        Set<String> availableIds = new HashSet<>();
        
        // Use reference scenario if provided (for nested scenarios), otherwise use the scenario itself
        Scenario refScenario = referenceScenario != null ? referenceScenario : scenario;
        
        // Collect available character IDs and IDs from reference scenario
        if (refScenario != null) {
            if (refScenario.getCharacters() != null) {
                availableCharacterIds.addAll(refScenario.getCharacters().keySet());
            }
            if (refScenario.getIds() != null) {
                availableIds.addAll(refScenario.getIds().keySet());
            }
        }
        
        if (scenario.getSequence() == null) {
            return;
        }
        
        for (int i = 0; i < scenario.getSequence().size(); i++) {
            ScenarioAction action = scenario.getSequence().get(i);
            String actionPrefix = "Action at index " + i;
            
            // Validate action type is registered
            if (action.getActionType() != null) {
                String actionTypeStr = action.getActionType().getJsonValue();
                if (!registry.isRegistered(actionTypeStr)) {
                    result.addError(actionPrefix + ": Unknown action type: " + actionTypeStr);
                }
            }
            
            // Validate references in parameters
            Map<String, Object> params = action.getParameters();
            if (params == null) {
                continue;
            }
            
            String actionType = action.getActionType() != null ? action.getActionType().getJsonValue() : null;
            
            // Validate ShowSprite references
            if ("ShowSprite".equals(actionType)) {
                Object idObj = params.get("id");
                if (idObj != null) {
                    String id = idObj.toString();
                    if (!availableIds.contains(id) && !availableCharacterIds.contains(id)) {
                        result.addError(actionPrefix + ": Referenced ID '" + id + "' does not exist in scenario");
                    }
                }
            }
            
            // Validate Battle action references
            if (BattleAction.getName().equals(actionType)) {
                validateBattleReferences(action, actionPrefix, availableCharacterIds, availableIds, result);
            }
        }
    }
    
    /**
     * Validates Battle action references.
     */
    private static void validateBattleReferences(ScenarioAction action, String prefix, 
                                                 Set<String> availableCharacterIds, Set<String> availableIds,
                                                 ValidationResult result) {
        Map<String, Object> params = action.getParameters();
        if (params == null) {
            return;
        }
        
        // Validate team1
        Object team1Obj = params.get("team1");
        if (team1Obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> team1 = (List<Object>) team1Obj;
            for (Object charIdObj : team1) {
                String charId = charIdObj.toString();
                if (!availableCharacterIds.contains(charId) && !availableIds.contains(charId)) {
                    result.addError(prefix + ": Battle team1 contains unknown character ID: " + charId);
                }
            }
        }
        
        // Validate team2
        Object team2Obj = params.get("team2");
        if (team2Obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> team2 = (List<Object>) team2Obj;
            for (Object charIdObj : team2) {
                String charId = charIdObj.toString();
                if (!availableCharacterIds.contains(charId) && !availableIds.contains(charId)) {
                    result.addError(prefix + ": Battle team2 contains unknown character ID: " + charId);
                }
            }
        }
        
        // Validate playerTeam
        Object playerTeamObj = params.get("playerTeam");
        if (playerTeamObj != null) {
            String playerTeam = playerTeamObj.toString();
            if (!"team1".equals(playerTeam) && !"team2".equals(playerTeam)) {
                result.addError(prefix + ": Invalid playerTeam value: " + playerTeam + " (must be 'team1' or 'team2')");
            }
        }
    }
    
    /**
     * Validates data constraints (stats, timing, moves, etc.).
     */
    private static void validateConstraints(Scenario scenario, ValidationResult result) {
        // Validate character stats
        if (scenario.getCharacters() != null) {
            for (Map.Entry<String, Character> entry : scenario.getCharacters().entrySet()) {
                String charId = entry.getKey();
                Character character = entry.getValue();
                
                // Validate stats are non-negative
                if (character.getStats() != null) {
                    for (Map.Entry<String, Number> statEntry : character.getStats().entrySet()) {
                        String statName = statEntry.getKey();
                        Number statValue = statEntry.getValue();
                        if (statValue.doubleValue() < 0) {
                            result.addError("Character '" + charId + "' has negative stat '" + statName + "': " + statValue);
                        }
                    }
                }
                
                // Validate moves
                // Note: Moves are stored in IdentifiableObject, not Character directly
                // We'll validate them when checking IDs
            }
        }
        
        // Validate IDs (including moves and stats)
        if (scenario.getIds() != null) {
            for (Map.Entry<String, IdentifiableObject> entry : scenario.getIds().entrySet()) {
                String id = entry.getKey();
                IdentifiableObject obj = entry.getValue();
                
                // Validate stats are non-negative
                if (obj.getStats() != null) {
                    for (Map.Entry<String, Number> statEntry : obj.getStats().entrySet()) {
                        Number statValue = statEntry.getValue();
                        if (statValue.doubleValue() < 0) {
                            result.addError("ID '" + id + "' has negative stat: " + statEntry.getKey() + " = " + statValue);
                        }
                    }
                }
                
                // Validate moves
                if (obj.getMoves() != null) {
                    for (Map.Entry<String, Move> moveEntry : obj.getMoves().entrySet()) {
                        String moveName = moveEntry.getKey();
                        Move move = moveEntry.getValue();
                        String movePrefix = "ID '" + id + "', Move '" + moveName + "'";
                        
                        // Validate move type
                        if (move.getType() == null || move.getType().isEmpty()) {
                            result.addError(movePrefix + ": Missing move type");
                        } else if (!VALID_MOVE_TYPES.contains(move.getType())) {
                            result.addError(movePrefix + ": Invalid move type: " + move.getType());
                        }
                        
                        // Validate move values are non-negative
                        if (move.getDamage() != null && move.getDamage() < 0) {
                            result.addError(movePrefix + ": damage must be non-negative, got: " + move.getDamage());
                        }
                        if (move.getDefense() != null && move.getDefense() < 0) {
                            result.addError(movePrefix + ": defense must be non-negative, got: " + move.getDefense());
                        }
                        if (move.getHealth() != null && move.getHealth() < 0) {
                            result.addError(movePrefix + ": health must be non-negative, got: " + move.getHealth());
                        }
                        if (move.getCooldown() < 0) {
                            result.addError(movePrefix + ": cooldown must be non-negative, got: " + move.getCooldown());
                        }
                        if (move.getEnergyCost() < 0) {
                            result.addError(movePrefix + ": energy_cost must be non-negative, got: " + move.getEnergyCost());
                        }
                    }
                }
            }
        }
        
        // Validate moves in characters
        if (scenario.getCharacters() != null) {
            // Note: Characters don't have moves directly in the Character class,
            // but they might be defined in the ids map. Moves validation is handled above.
        }
        
        // Validate timing constraints in actions
        if (scenario.getSequence() != null) {
            for (int i = 0; i < scenario.getSequence().size(); i++) {
                ScenarioAction action = scenario.getSequence().get(i);
                if (action.getTiming() != null) {
                    Timing timing = action.getTiming();
                    if (timing.getDurationMs() != null && timing.getDurationMs() <= 0) {
                        result.addError("Action at index " + i + ": durationMs must be positive, got: " + timing.getDurationMs());
                    }
                }
            }
        }
    }
    
    /**
     * Validates that exactly one entry point exists across all JSON files.
     * 
     * @param jsonFiles List of JSON file paths to check
     * @return ValidationResult containing errors if multiple or no entry points found
     */
    public static ValidationResult validateSingleEntryPoint(List<String> jsonFiles) {
        ValidationResult result = new ValidationResult();
        List<String> entryPointFiles = new ArrayList<>();
        
        for (String filePath : jsonFiles) {
            try {
                ScenarioLoader loader = new ScenarioLoader();
                Scenario scenario = loader.loadScenario(filePath);
                if (scenario.isEntryPoint()) {
                    entryPointFiles.add(filePath);
                }
            } catch (IOException e) {
                // Skip files that can't be loaded - they'll be caught by other tests
            }
        }
        
        if (entryPointFiles.isEmpty()) {
            result.addError("No entry point scenario found. Exactly one scenario must have entry_point: true");
        } else if (entryPointFiles.size() > 1) {
            result.addError("Multiple entry point scenarios found (" + entryPointFiles.size() + "): " + 
                          String.join(", ", entryPointFiles) + 
                          ". Exactly one scenario must have entry_point: true");
        }
        
        return result;
    }
    
    /**
     * Validates cross-file references (OnLose, OnWin file references).
     * 
     * @param jsonFiles List of all JSON file paths
     * @param baseDirectory Base directory for resolving relative paths
     * @return ValidationResult containing errors for missing or invalid file references
     */
    public static ValidationResult validateCrossFileReferences(List<String> jsonFiles, String baseDirectory) {
        ValidationResult result = new ValidationResult();
        Set<String> availableFiles = new HashSet<>();
        
        // Collect all available JSON file names
        for (String filePath : jsonFiles) {
            File file = new File(filePath);
            availableFiles.add(file.getName());
            availableFiles.add(filePath);
        }
        
        // Check each file for references
        for (String filePath : jsonFiles) {
            try {
                ScenarioLoader loader = new ScenarioLoader();
                Scenario scenario = loader.loadScenario(filePath);
                
                if (scenario.getSequence() != null) {
                    for (int i = 0; i < scenario.getSequence().size(); i++) {
                        ScenarioAction action = scenario.getSequence().get(i);
                        String actionPrefix = "File: " + new File(filePath).getName() + ", Action at index " + i;
                        
                        // Check OnLose references
                        Object onLose = action.getOnLose();
                        if (onLose instanceof String) {
                            String onLoseFile = (String) onLose;
                            if (!validateFileReference(onLoseFile, filePath, baseDirectory, availableFiles)) {
                                result.addError(actionPrefix + ": OnLose references non-existent file: " + onLoseFile);
                            }
                        }
                        
                        // Check OnWin references
                        Object onWin = action.getOnWin();
                        if (onWin instanceof String) {
                            String onWinFile = (String) onWin;
                            if (!validateFileReference(onWinFile, filePath, baseDirectory, availableFiles)) {
                                result.addError(actionPrefix + ": OnWin references non-existent file: " + onWinFile);
                            }
                        }
                        
                        // Check nested action references (in ChooseAction options)
                        Map<String, Object> params = action.getParameters();
                        if (params != null && ChooseAction.getName().equals(action.getActionType().getJsonValue())) {
                            Object optionsObj = params.get("options");
                            if (optionsObj instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> options = (List<Map<String, Object>>) optionsObj;
                                for (int optIdx = 0; optIdx < options.size(); optIdx++) {
                                    Map<String, Object> option = options.get(optIdx);
                                    Object onPressObj = option.get("onPress");
                                    if (onPressObj instanceof String) {
                                        String onPressFile = (String) onPressObj;
                                        if (!validateFileReference(onPressFile, filePath, baseDirectory, availableFiles)) {
                                            result.addError(actionPrefix + ", Option " + optIdx + 
                                                          ": onPress references non-existent file: " + onPressFile);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                // Skip files that can't be loaded
            }
        }
        
        return result;
    }
    
    /**
     * Validates that a file reference exists.
     */
    private static boolean validateFileReference(String referencedFile, String currentFilePath, 
                                                String baseDirectory, Set<String> availableFiles) {
        // Check if it's an absolute path or relative
        File refFile = new File(referencedFile);
        if (refFile.isAbsolute()) {
            return availableFiles.contains(referencedFile) || refFile.exists();
        }
        
        // Relative path - resolve from current file's directory
        File currentFile = new File(currentFilePath);
        File currentDir = currentFile.getParentFile();
        if (currentDir != null) {
            File resolvedFile = new File(currentDir, referencedFile);
            if (resolvedFile.exists()) {
                return true;
            }
        }
        
        // Try from base directory
        if (baseDirectory != null) {
            File baseDir = new File(baseDirectory);
            File resolvedFile = new File(baseDir, referencedFile);
            if (resolvedFile.exists()) {
                return true;
            }
        }
        
        // Check if it matches any available file name
        return availableFiles.contains(referencedFile);
    }
    
    /**
     * Validates that all execution paths in a scenario eventually reach an EndScenarioAction.
     * 
     * @param scenario The scenario to validate
     * @param filePath The file path of the scenario
     * @param result The validation result to add errors to
     */
    private static void validateScenarioPaths(Scenario scenario, String filePath, ValidationResult result) {
        if (scenario == null || scenario.getSequence() == null || scenario.getSequence().isEmpty()) {
            return; // Already handled by structure validation
        }
        
        // Get the base directory for resolving relative paths
        File file = new File(filePath);
        String baseDirectory = file.getParent();
        if (baseDirectory == null) {
            baseDirectory = "";
        }
        
        // Create validator and validate
        ScenarioPathValidator validator = new ScenarioPathValidator(baseDirectory);
        List<String> pathErrors = validator.validate(scenario, filePath);
        
        // Add errors to result
        for (String error : pathErrors) {
            result.addError("Path validation: " + error);
        }
    }
    
}

