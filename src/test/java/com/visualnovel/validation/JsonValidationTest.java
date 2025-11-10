package com.visualnovel.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for validating JSON scenario files.
 * Tests all JSON files in assets/json/ for syntax, structure, data types,
 * references, constraints, and business rules.
 */
public class JsonValidationTest {
    
    private static final String ASSETS_JSON_DIR = "assets/json/base_game";
    
    /**
     * Discovers all JSON files in the assets/json directory recursively.
     * Filters out non-scenario files (like ids.json).
     * 
     * @return Stream of JSON file paths that are scenario files
     */
    static Stream<String> jsonFiles() {
        List<String> jsonFiles = new ArrayList<>();
        try {
            Path assetsPath = Paths.get(ASSETS_JSON_DIR);
            if (Files.exists(assetsPath)) {
                Files.walk(assetsPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .filter(path -> isScenarioFile(path.toString()))
                    .forEach(path -> jsonFiles.add(path.toString().replace(File.separator, "/")));
            }
        } catch (Exception e) {
            fail("Failed to discover JSON files: " + e.getMessage());
        }
        
        assertFalse(jsonFiles.isEmpty(), "No JSON files found in " + ASSETS_JSON_DIR);
        return jsonFiles.stream();
    }
    
    /**
     * Checks if a JSON file is a scenario file (has "scenario" key at root).
     * 
     * @param filePath Path to the JSON file
     * @return true if the file is a scenario file, false otherwise
     */
    private static boolean isScenarioFile(String filePath) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            try (java.io.FileReader reader = new java.io.FileReader(filePath)) {
                com.google.gson.JsonObject root = gson.fromJson(reader, com.google.gson.JsonObject.class);
                return root.has("scenario") && root.get("scenario").isJsonObject();
            }
        } catch (Exception e) {
            // If we can't parse it, assume it's not a scenario file
            return false;
        }
    }
    
    /**
     * Parameterized test that validates each JSON file individually.
     * Tests syntax, structure, data types, references, and constraints.
     */
    @ParameterizedTest(name = "Validate {0}")
    @MethodSource("jsonFiles")
    void testJsonFile(String filePath) {
        // Find entry point scenario for reference validation
        com.visualnovel.model.Scenario entryPointScenario = findEntryPointScenario();
        
        JsonValidator.ValidationResult result = JsonValidator.validateFile(filePath, entryPointScenario);
        
        if (result.hasErrors()) {
            fail("Validation failed for " + filePath + ":\n" + result.getErrorSummary());
        }
    }
    
    /**
     * Finds the entry point scenario from all JSON files.
     * 
     * @return The entry point scenario, or null if not found
     */
    private com.visualnovel.model.Scenario findEntryPointScenario() {
        List<String> jsonFiles = jsonFiles().collect(java.util.stream.Collectors.toList());
        com.visualnovel.util.ScenarioLoader loader = new com.visualnovel.util.ScenarioLoader();
        
        for (String filePath : jsonFiles) {
            try {
                com.visualnovel.model.Scenario scenario = loader.loadScenario(filePath);
                if (scenario.isEntryPoint()) {
                    return scenario;
                }
            } catch (Exception e) {
                // Skip files that can't be loaded
            }
        }
        
        return null;
    }
    
    /**
     * Test that exactly one entry point scenario exists across all JSON files.
     */
    @Test
    void testSingleEntryPoint() {
        List<String> jsonFiles = jsonFiles().collect(java.util.stream.Collectors.toList());
        JsonValidator.ValidationResult result = JsonValidator.validateSingleEntryPoint(jsonFiles);
        
        if (result.hasErrors()) {
            fail("Entry point validation failed:\n" + result.getErrorSummary());
        }
    }
    
    /**
     * Test cross-file references (OnLose, OnWin, onPress file references).
     */
    @Test
    void testCrossFileReferences() {
        List<String> jsonFiles = jsonFiles().collect(java.util.stream.Collectors.toList());
        String baseDirectory = ASSETS_JSON_DIR;
        JsonValidator.ValidationResult result = JsonValidator.validateCrossFileReferences(jsonFiles, baseDirectory);
        
        if (result.hasErrors()) {
            fail("Cross-file reference validation failed:\n" + result.getErrorSummary());
        }
    }
    
    /**
     * Test that all JSON files can be loaded successfully.
     * This is a basic sanity check that files are parseable.
     */
    @ParameterizedTest(name = "Load {0}")
    @MethodSource("jsonFiles")
    void testJsonFileLoadable(String filePath) {
        assertDoesNotThrow(() -> {
            com.visualnovel.util.ScenarioLoader loader = new com.visualnovel.util.ScenarioLoader();
            loader.loadScenario(filePath);
        }, "Failed to load scenario file: " + filePath);
    }
}

