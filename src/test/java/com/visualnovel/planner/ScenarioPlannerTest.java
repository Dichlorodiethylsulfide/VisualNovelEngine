package com.visualnovel.planner;

import com.visualnovel.model.Scenario;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.util.ScenarioLoader;
import com.visualnovel.validation.JsonValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the ScenarioPlanner module.
 * Tests that scenarios built programmatically can be exported and validated.
 */
public class ScenarioPlannerTest {
    
    @TempDir
    Path tempDir;
    
    /**
     * Tests building a simple scenario with basic actions and exporting it.
     */
    @Test
    void testSimpleScenarioExport() throws IOException {
        String outputPath = tempDir.resolve("simple_scenario.json").toString();
        
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("Simple Test Scenario")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setDate("2024-01-01")
            .setEntryPoint(true)
            .setCategory("test")
            .setSubcategory("simple")
            .addTag("test")
            .addTag("simple");
        
        // Add at least one ID (required for entry point scenarios)
        planner.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        // Add at least one action (required for all scenarios)
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Hello World!")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction at the end
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Validate the exported file
        JsonValidator.ValidationResult result = JsonValidator.validateFile(outputPath);
        assertFalse(result.hasErrors(), "Validation failed:\n" + result.getErrorSummary());
        
        // Verify file exists
        assertTrue(Files.exists(Path.of(outputPath)), "Exported file does not exist");
    }
    
    /**
     * Tests building a complete scenario with IDs, characters, and actions.
     */
    @Test
    void testCompleteScenarioExport() throws IOException {
        String outputPath = tempDir.resolve("complete_scenario.json").toString();
        
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("Complete Test Scenario")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setDate("2024-01-01")
            .setDescription("A complete test scenario")
            .setEntryPoint(true)
            .setCategory("test")
            .setSubcategory("complete")
            .addTag("test")
            .addTag("complete");
        
        // Add background
        planner.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        // Add player character
        planner.addId("Player", idBuilder -> idBuilder
            .type("character")
            .category("player")
            .displayName("Player")
            .sprite("default", "main")
            .commonStats(100, 10, 5, 10, 5, 100)
            .move("Sword Slash", moveBuilder -> moveBuilder
                .physical(10, 0, 0, "Attack with sword")
                .build())
            .build());
        
        // Add enemy character
        planner.addId("Enemy", idBuilder -> idBuilder
            .type("character")
            .category("enemy")
            .displayName("Enemy")
            .sprite("default", "orc")
            .commonStats(50, 8, 3, 8, 3, 100)
            .move("Punch", moveBuilder -> moveBuilder
                .physical(8, 0, 0, "Punch attack")
                .build())
            .build());
        
        // Add actions
        planner.addAction(actionBuilder -> actionBuilder
            .showSprite("TestBackground", "Back")
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showSprite("Player", "Right")
            .timing(timingBuilder -> timingBuilder
                .animated(1000, "fadeIn")
                .build())
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Welcome to the test scenario!")
            .timing(TimingType.INTERACTION)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showSprite("Enemy", "Left")
            .timing(timingBuilder -> timingBuilder
                .animated(1000, "fadeIn")
                .build())
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "An enemy appears!")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction at the end
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Validate the exported file
        JsonValidator.ValidationResult result = JsonValidator.validateFile(outputPath);
        assertFalse(result.hasErrors(), "Validation failed:\n" + result.getErrorSummary());
    }
    
    /**
     * Tests building a scenario with Battle action and nested actions.
     */
    @Test
    void testBattleScenarioExport() throws IOException {
        String outputPath = tempDir.resolve("battle_scenario.json").toString();
        
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("Battle Test Scenario")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test")
            .setSubcategory("battle");
        
        // Add characters
        planner.addId("Player", idBuilder -> idBuilder
            .type("character")
            .category("player")
            .displayName("Player")
            .sprite("default", "main")
            .commonStats(100, 10, 5, 10, 5, 100)
            .build());
        
        planner.addId("Enemy", idBuilder -> idBuilder
            .type("character")
            .category("enemy")
            .displayName("Enemy")
            .sprite("default", "orc")
            .commonStats(50, 8, 3, 8, 3, 100)
            .build());
        
        // Add actions
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "A battle begins!")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add Battle action with onWin and onLose
        planner.addAction(actionBuilder -> actionBuilder
            .battle(Arrays.asList("Enemy"), Arrays.asList("Player"), "team2")
            .timing(TimingType.IMMEDIATE)
            .onWin(actionBuilder2 -> actionBuilder2
                .continueAction()
                .timing(TimingType.IMMEDIATE)
                .build())
            .onLose("lose.json")
            .build());
        
        // Create lose.json file that is referenced
        String losePath = tempDir.resolve("lose.json").toString();
        ScenarioPlanner losePlanner = new ScenarioPlanner()
            .setName("Lose Scenario")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(false)
            .setCategory("test");
        
        losePlanner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "You lost!")
            .timing(TimingType.INTERACTION)
            .build());
        
        losePlanner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        losePlanner.export(losePath);
        
        // Add EndScenarioAction at the end (for the main scenario)
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Validate the exported file
        JsonValidator.ValidationResult result = JsonValidator.validateFile(outputPath);
        assertFalse(result.hasErrors(), "Validation failed:\n" + result.getErrorSummary());
    }
    
    /**
     * Tests building a scenario with ChooseAction.
     */
    @Test
    void testChooseActionScenarioExport() throws IOException {
        String outputPath = tempDir.resolve("choose_scenario.json").toString();
        
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("Choose Test Scenario")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test")
            .setSubcategory("choose");
        
        // Add characters
        planner.addId("Player", idBuilder -> idBuilder
            .type("character")
            .category("player")
            .displayName("Player")
            .sprite("default", "main")
            .commonStats(100, 10, 5, 10, 5, 100)
            .build());
        
        planner.addId("Enemy", idBuilder -> idBuilder
            .type("character")
            .category("enemy")
            .displayName("Enemy")
            .sprite("default", "orc")
            .commonStats(50, 8, 3, 8, 3, 100)
            .build());
        
        // Build choice options
        List<Map<String, Object>> options = new ArrayList<>();
        
        // Option 1: Attack (nested action)
        Map<String, Object> option1 = ActionBuilder.choiceOption()
            .text("Attack")
            .onPress(actionBuilder -> actionBuilder
                .battle(Arrays.asList("Enemy"), Arrays.asList("Player"), "team2")
                .timing(TimingType.IMMEDIATE)
                .build())
            .build();
        options.add(option1);
        
        // Option 2: Flee (file path)
        Map<String, Object> option2 = ActionBuilder.choiceOption()
            .text("Flee")
            .onPress("lose.json")
            .build();
        options.add(option2);
        
        // Create lose.json file that is referenced
        String losePath = tempDir.resolve("lose.json").toString();
        ScenarioPlanner losePlanner = new ScenarioPlanner()
            .setName("Lose Scenario")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(false)
            .setCategory("test");
        
        losePlanner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "You fled!")
            .timing(TimingType.INTERACTION)
            .build());
        
        losePlanner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        losePlanner.export(losePath);
        
        // Add actions
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "What will you do?")
            .timing(TimingType.INTERACTION)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .choose("Player", "Choose your action:", options)
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction at the end (for paths that continue)
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Validate the exported file
        JsonValidator.ValidationResult result = JsonValidator.validateFile(outputPath);
        assertFalse(result.hasErrors(), "Validation failed:\n" + result.getErrorSummary());
    }
    
    /**
     * Tests that exported scenarios can be loaded back.
     * Verifies that IDs are loaded correctly when exported as JSONObject (inline).
     */
    @Test
    void testExportedScenarioCanBeLoaded() throws IOException {
        String outputPath = tempDir.resolve("loadable_scenario.json").toString();
        
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("Loadable Test Scenario")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test");
        
        planner.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showSprite("TestBackground", "Back")
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Test message")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction at the end
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Try to load the exported scenario
        assertDoesNotThrow(() -> {
            ScenarioLoader loader = new ScenarioLoader();
            Scenario loadedScenario = loader.loadScenario(outputPath);
            
            assertNotNull(loadedScenario, "Loaded scenario should not be null");
            assertEquals("Loadable Test Scenario", loadedScenario.getName());
            assertTrue(loadedScenario.isEntryPoint());
            assertNotNull(loadedScenario.getSequence());
            assertEquals(3, loadedScenario.getSequence().size());
            
            // Verify IDs are loaded correctly (as JSONObject)
            assertNotNull(loadedScenario.getIds(), "IDs should not be null");
            assertTrue(loadedScenario.getIds().containsKey("TestBackground"), "TestBackground ID should be loaded");
            assertEquals("background", loadedScenario.getIds().get("TestBackground").getType());
            assertEquals("forest", loadedScenario.getIds().get("TestBackground").getCategory());
        }, "Failed to load exported scenario");
    }
    
    /**
     * Tests loading a scenario with IDs as JSONObject (inline).
     * Verifies that existing behavior still works.
     */
    @Test
    void testLoadScenarioWithJsonObjectIds() throws IOException {
        String scenarioPath = tempDir.resolve("scenario_with_inline_ids.json").toString();
        
        // Create a scenario with inline IDs using the planner
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("Scenario with Inline IDs")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test");
        
        planner.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        planner.addId("TestCharacter", idBuilder -> idBuilder
            .type("character")
            .category("player")
            .displayName("Test Character")
            .sprite("default", "main")
            .commonStats(100, 10, 5, 10, 5, 100)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Test message")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction at the end
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(scenarioPath);
        
        // Load the scenario and verify IDs are loaded correctly
        ScenarioLoader loader = new ScenarioLoader();
        Scenario loadedScenario = loader.loadScenario(scenarioPath);
        
        assertNotNull(loadedScenario.getIds(), "IDs should not be null");
        assertEquals(2, loadedScenario.getIds().size(), "Should have 2 IDs");
        assertTrue(loadedScenario.getIds().containsKey("TestBackground"), "Should contain TestBackground");
        assertTrue(loadedScenario.getIds().containsKey("TestCharacter"), "Should contain TestCharacter");
        assertEquals("background", loadedScenario.getIds().get("TestBackground").getType());
        assertEquals("character", loadedScenario.getIds().get("TestCharacter").getType());
    }
    
    /**
     * Tests loading a scenario with IDs as file path string.
     * Verifies that the new file path loading works correctly.
     */
    @Test
    void testLoadScenarioWithFilepathIds() throws IOException {
        // First, create an IDs file using the planner's setIdsFromFile method
        // But first we need to create the IDs file manually in the correct format
        String idsFilePath = tempDir.resolve("test_ids.json").toString();
        
        // Create IDs using planner and then extract just the IDs part
        ScenarioPlanner idsPlanner = new ScenarioPlanner();
        idsPlanner.addId("FileBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("File Forest")
            .sprite("default", "main")
            .build());
        idsPlanner.addId("FileCharacter", idBuilder -> idBuilder
            .type("character")
            .category("player")
            .displayName("File Character")
            .sprite("default", "main")
            .commonStats(100, 10, 5, 10, 5, 100)
            .build());
        
        // Export to a temp file and extract the IDs JSON object
        String tempScenarioPath = tempDir.resolve("temp_scenario.json").toString();
        idsPlanner.export(tempScenarioPath);
        
        // Read the exported file and extract just the IDs part
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
        com.google.gson.JsonObject root;
        try (java.io.FileReader reader = new java.io.FileReader(tempScenarioPath)) {
            root = gson.fromJson(reader, com.google.gson.JsonObject.class);
        }
        com.google.gson.JsonObject scenarioObj = root.getAsJsonObject("scenario");
        com.google.gson.JsonObject idsObj = scenarioObj.getAsJsonObject("ids");
        
        // Convert displayName to display_name for each ID entry (Gson uses camelCase, but parser expects snake_case)
        for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : idsObj.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                com.google.gson.JsonObject idObj = entry.getValue().getAsJsonObject();
                if (idObj.has("displayName")) {
                    com.google.gson.JsonElement displayNameElem = idObj.remove("displayName");
                    idObj.add("display_name", displayNameElem);
                }
            }
        }
        
        // Write just the IDs object to the IDs file
        try (java.io.FileWriter writer = new java.io.FileWriter(idsFilePath)) {
            gson.toJson(idsObj, writer);
        }
        
        // Now create a scenario JSON file that references the IDs file
        String scenarioPath = tempDir.resolve("scenario_with_file_ids.json").toString();
        String scenarioJson = String.format(
            "{\n" +
            "  \"scenario\": {\n" +
            "    \"entry_point\": true,\n" +
            "    \"name\": \"Scenario with File IDs\",\n" +
            "    \"author\": \"Test Author\",\n" +
            "    \"version\": \"1.0.0\",\n" +
            "    \"category\": \"test\",\n" +
            "    \"ids\": \"test_ids.json\",\n" +
            "    \"sequence\": [\n" +
            "      {\n" +
            "        \"action\": \"ShowMessage\",\n" +
            "        \"timing\": {\n" +
            "          \"type\": \"Interaction\"\n" +
            "        },\n" +
            "        \"parameters\": {\n" +
            "          \"context\": \"Narrator\",\n" +
            "          \"message\": \"Test message\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}"
        );
        Files.write(Path.of(scenarioPath), scenarioJson.getBytes());
        
        // Load the scenario and verify IDs are loaded from the file
        ScenarioLoader loader = new ScenarioLoader();
        Scenario loadedScenario = loader.loadScenario(scenarioPath);
        
        assertNotNull(loadedScenario.getIds(), "IDs should not be null");
        assertEquals(2, loadedScenario.getIds().size(), "Should have 2 IDs loaded from file");
        assertTrue(loadedScenario.getIds().containsKey("FileBackground"), "Should contain FileBackground");
        assertTrue(loadedScenario.getIds().containsKey("FileCharacter"), "Should contain FileCharacter");
        assertEquals("background", loadedScenario.getIds().get("FileBackground").getType());
        assertEquals("character", loadedScenario.getIds().get("FileCharacter").getType());
        assertEquals("File Forest", loadedScenario.getIds().get("FileBackground").getDisplayName());
        assertEquals("File Character", loadedScenario.getIds().get("FileCharacter").getDisplayName());
    }
    
    /**
     * Tests building a scenario with all action types.
     */
    @Test
    void testAllActionTypes() throws IOException {
        String outputPath = tempDir.resolve("all_actions_scenario.json").toString();
        
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("All Actions Test Scenario")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test");
        
        planner.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        planner.addId("TestCharacter", idBuilder -> idBuilder
            .type("character")
            .category("player")
            .displayName("Test Character")
            .sprite("default", "main")
            .commonStats(100, 10, 5, 10, 5, 100)
            .build());
        
        // Test all action types
        planner.addAction(actionBuilder -> actionBuilder
            .showSprite("TestBackground", "Back")
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showSprite("TestCharacter", "Right", "default")
            .timing(timingBuilder -> timingBuilder.animated(1000, "fadeIn").build())
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Test message")
            .timing(TimingType.INTERACTION)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .hideSprite("TestCharacter")
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .delay(500)
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .continueAction()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Validate the exported file
        JsonValidator.ValidationResult result = JsonValidator.validateFile(outputPath);
        assertFalse(result.hasErrors(), "Validation failed:\n" + result.getErrorSummary());
    }
    
    /**
     * Tests that battle_strategy is loaded correctly from JSON.
     * Verifies both "reset_stats" and "persist_stats" values.
     */
    @Test
    void testBattleStrategyLoading() throws IOException {
        // Test loading with "reset_stats"
        String resetStatsPath = tempDir.resolve("reset_stats_scenario.json").toString();
        String resetStatsJson = String.format(
            "{\n" +
            "  \"scenario\": {\n" +
            "    \"entry_point\": true,\n" +
            "    \"name\": \"Reset Stats Scenario\",\n" +
            "    \"author\": \"Test Author\",\n" +
            "    \"version\": \"1.0.0\",\n" +
            "    \"category\": \"test\",\n" +
            "    \"battle_strategy\": \"reset_stats\",\n" +
            "    \"ids\": {\n" +
            "      \"TestBackground\": {\n" +
            "        \"type\": \"background\",\n" +
            "        \"category\": \"forest\",\n" +
            "        \"display_name\": \"Test Forest\",\n" +
            "        \"sprites\": {\n" +
            "          \"default\": \"main\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"sequence\": [\n" +
            "      {\n" +
            "        \"action\": \"ShowMessage\",\n" +
            "        \"timing\": {\n" +
            "          \"type\": \"Interaction\"\n" +
            "        },\n" +
            "        \"parameters\": {\n" +
            "          \"context\": \"Narrator\",\n" +
            "          \"message\": \"Test message\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}"
        );
        Files.write(Path.of(resetStatsPath), resetStatsJson.getBytes());
        
        ScenarioLoader loader = new ScenarioLoader();
        Scenario resetStatsScenario = loader.loadScenario(resetStatsPath);
        assertNotNull(resetStatsScenario, "Reset stats scenario should not be null");
        assertEquals("reset_stats", resetStatsScenario.getBattleStrategy(), "Battle strategy should be reset_stats");
        
        // Test loading with "persist_stats"
        String persistStatsPath = tempDir.resolve("persist_stats_scenario.json").toString();
        String persistStatsJson = String.format(
            "{\n" +
            "  \"scenario\": {\n" +
            "    \"entry_point\": true,\n" +
            "    \"name\": \"Persist Stats Scenario\",\n" +
            "    \"author\": \"Test Author\",\n" +
            "    \"version\": \"1.0.0\",\n" +
            "    \"category\": \"test\",\n" +
            "    \"battle_strategy\": \"persist_stats\",\n" +
            "    \"ids\": {\n" +
            "      \"TestBackground\": {\n" +
            "        \"type\": \"background\",\n" +
            "        \"category\": \"forest\",\n" +
            "        \"display_name\": \"Test Forest\",\n" +
            "        \"sprites\": {\n" +
            "          \"default\": \"main\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"sequence\": [\n" +
            "      {\n" +
            "        \"action\": \"ShowMessage\",\n" +
            "        \"timing\": {\n" +
            "          \"type\": \"Interaction\"\n" +
            "        },\n" +
            "        \"parameters\": {\n" +
            "          \"context\": \"Narrator\",\n" +
            "          \"message\": \"Test message\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}"
        );
        Files.write(Path.of(persistStatsPath), persistStatsJson.getBytes());
        
        Scenario persistStatsScenario = loader.loadScenario(persistStatsPath);
        assertNotNull(persistStatsScenario, "Persist stats scenario should not be null");
        assertEquals("persist_stats", persistStatsScenario.getBattleStrategy(), "Battle strategy should be persist_stats");
        
        // Test loading without battle_strategy (should be null)
        String noStrategyPath = tempDir.resolve("no_strategy_scenario.json").toString();
        String noStrategyJson = String.format(
            "{\n" +
            "  \"scenario\": {\n" +
            "    \"entry_point\": true,\n" +
            "    \"name\": \"No Strategy Scenario\",\n" +
            "    \"author\": \"Test Author\",\n" +
            "    \"version\": \"1.0.0\",\n" +
            "    \"category\": \"test\",\n" +
            "    \"ids\": {\n" +
            "      \"TestBackground\": {\n" +
            "        \"type\": \"background\",\n" +
            "        \"category\": \"forest\",\n" +
            "        \"display_name\": \"Test Forest\",\n" +
            "        \"sprites\": {\n" +
            "          \"default\": \"main\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"sequence\": [\n" +
            "      {\n" +
            "        \"action\": \"ShowMessage\",\n" +
            "        \"timing\": {\n" +
            "          \"type\": \"Interaction\"\n" +
            "        },\n" +
            "        \"parameters\": {\n" +
            "          \"context\": \"Narrator\",\n" +
            "          \"message\": \"Test message\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}"
        );
        Files.write(Path.of(noStrategyPath), noStrategyJson.getBytes());
        
        Scenario noStrategyScenario = loader.loadScenario(noStrategyPath);
        assertNotNull(noStrategyScenario, "No strategy scenario should not be null");
        assertNull(noStrategyScenario.getBattleStrategy(), "Battle strategy should be null when not specified");
    }
    
    /**
     * Tests that battle_strategy is exported correctly when using planner.
     * Verifies that exported scenario can be loaded back with correct battle_strategy value.
     */
    @Test
    void testBattleStrategyExport() throws IOException {
        String outputPath = tempDir.resolve("battle_strategy_export.json").toString();
        
        // Create scenario with battle_strategy using planner
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("Battle Strategy Export Test")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test")
            .setBattleStrategy("persist_stats");
        
        planner.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Test message")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction at the end
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Validate the exported file
        JsonValidator.ValidationResult result = JsonValidator.validateFile(outputPath);
        assertFalse(result.hasErrors(), "Validation failed:\n" + result.getErrorSummary());
        
        // Load the exported scenario and verify battle_strategy
        ScenarioLoader loader = new ScenarioLoader();
        Scenario loadedScenario = loader.loadScenario(outputPath);
        
        assertNotNull(loadedScenario, "Loaded scenario should not be null");
        assertEquals("persist_stats", loadedScenario.getBattleStrategy(), "Battle strategy should be persist_stats");
        
        // Test with reset_stats
        String outputPath2 = tempDir.resolve("battle_strategy_export2.json").toString();
        ScenarioPlanner planner2 = new ScenarioPlanner()
            .setName("Battle Strategy Export Test 2")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test")
            .setBattleStrategy("reset_stats");
        
        planner2.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        planner2.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Test message")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction at the end
        planner2.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner2.export(outputPath2);
        
        Scenario loadedScenario2 = loader.loadScenario(outputPath2);
        assertNotNull(loadedScenario2, "Loaded scenario 2 should not be null");
        assertEquals("reset_stats", loadedScenario2.getBattleStrategy(), "Battle strategy should be reset_stats");
    }
    
    /**
     * Tests EndScenarioAction with restart parameter.
     */
    @Test
    void testEndScenarioWithRestart() throws IOException {
        String outputPath = tempDir.resolve("end_scenario_restart.json").toString();
        
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("End Scenario Restart Test")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test");
        
        planner.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Test message")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction with restart allowed
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario(true)
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Validate the exported file
        JsonValidator.ValidationResult result = JsonValidator.validateFile(outputPath);
        assertFalse(result.hasErrors(), "Validation failed:\n" + result.getErrorSummary());
        
        // Load and verify the restart parameter
        ScenarioLoader loader = new ScenarioLoader();
        Scenario loadedScenario = loader.loadScenario(outputPath);
        assertNotNull(loadedScenario, "Loaded scenario should not be null");
        assertNotNull(loadedScenario.getSequence(), "Sequence should not be null");
        assertEquals(2, loadedScenario.getSequence().size(), "Should have 2 actions");
        
        ScenarioAction endAction = loadedScenario.getSequence().get(1);
        assertNotNull(endAction.getActionType(), "End action should have action type");
        assertEquals("EndScenarioAction", endAction.getActionType().getJsonValue(), "Last action should be EndScenarioAction");
        
        Map<String, Object> params = endAction.getParameters();
        assertNotNull(params, "End action should have parameters");
        assertEquals("allowed", params.get("restart"), "Restart parameter should be 'allowed'");
    }
    
    /**
     * Tests EndScenarioAction without restart parameter.
     */
    @Test
    void testEndScenarioWithoutRestart() throws IOException {
        String outputPath = tempDir.resolve("end_scenario_no_restart.json").toString();
        
        ScenarioPlanner planner = new ScenarioPlanner()
            .setName("End Scenario No Restart Test")
            .setAuthor("Test Author")
            .setVersion("1.0.0")
            .setEntryPoint(true)
            .setCategory("test");
        
        planner.addId("TestBackground", idBuilder -> idBuilder
            .type("background")
            .category("forest")
            .displayName("Test Forest")
            .sprite("default", "main")
            .build());
        
        planner.addAction(actionBuilder -> actionBuilder
            .showMessage("Narrator", "Test message")
            .timing(TimingType.INTERACTION)
            .build());
        
        // Add EndScenarioAction without restart
        planner.addAction(actionBuilder -> actionBuilder
            .endScenario()
            .timing(TimingType.IMMEDIATE)
            .build());
        
        planner.export(outputPath);
        
        // Validate the exported file
        JsonValidator.ValidationResult result = JsonValidator.validateFile(outputPath);
        assertFalse(result.hasErrors(), "Validation failed:\n" + result.getErrorSummary());
        
        // Load and verify no restart parameter
        ScenarioLoader loader = new ScenarioLoader();
        Scenario loadedScenario = loader.loadScenario(outputPath);
        assertNotNull(loadedScenario, "Loaded scenario should not be null");
        assertNotNull(loadedScenario.getSequence(), "Sequence should not be null");
        assertEquals(2, loadedScenario.getSequence().size(), "Should have 2 actions");
        
        ScenarioAction endAction = loadedScenario.getSequence().get(1);
        assertNotNull(endAction.getActionType(), "End action should have action type");
        assertEquals("EndScenarioAction", endAction.getActionType().getJsonValue(), "Last action should be EndScenarioAction");
        
        Map<String, Object> params = endAction.getParameters();
        // Parameters may be null or empty if restart is not set
        if (params != null) {
            assertNull(params.get("restart"), "Restart parameter should not be set");
        }
    }
}

