package com.visualnovel.planner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visualnovel.model.Character;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.Scenario;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.util.IdentifiableObjectParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Main entry point for building scenario JSON files programmatically.
 * Provides a fluent builder API for constructing scenarios and exporting them.
 */
public class ScenarioPlanner {
    private Scenario scenario;
    private ScenarioExporter exporter;
    
    public ScenarioPlanner() {
        this.scenario = new Scenario();
        this.exporter = new ScenarioExporter();
    }
    
    // Metadata methods
    
    /**
     * Sets the scenario name.
     * 
     * @param name The scenario name
     * @return This planner for method chaining
     */
    public ScenarioPlanner setName(String name) {
        scenario.setName(name);
        return this;
    }
    
    /**
     * Sets the scenario description.
     * 
     * @param description The description
     * @return This planner for method chaining
     */
    public ScenarioPlanner setDescription(String description) {
        scenario.setDescription(description);
        return this;
    }
    
    /**
     * Sets the scenario author.
     * 
     * @param author The author name
     * @return This planner for method chaining
     */
    public ScenarioPlanner setAuthor(String author) {
        scenario.setAuthor(author);
        return this;
    }
    
    /**
     * Sets the scenario version.
     * 
     * @param version The version string
     * @return This planner for method chaining
     */
    public ScenarioPlanner setVersion(String version) {
        scenario.setVersion(version);
        return this;
    }
    
    /**
     * Sets the scenario date.
     * 
     * @param date The date string
     * @return This planner for method chaining
     */
    public ScenarioPlanner setDate(String date) {
        scenario.setDate(date);
        return this;
    }
    
    /**
     * Sets the scenario tags.
     * 
     * @param tags List of tag strings
     * @return This planner for method chaining
     */
    public ScenarioPlanner setTags(List<String> tags) {
        scenario.setTags(tags);
        return this;
    }
    
    /**
     * Adds a tag to the scenario.
     * 
     * @param tag The tag to add
     * @return This planner for method chaining
     */
    public ScenarioPlanner addTag(String tag) {
        if (scenario.getTags() == null) {
            scenario.setTags(new ArrayList<>());
        }
        scenario.getTags().add(tag);
        return this;
    }
    
    /**
     * Sets the scenario category.
     * 
     * @param category The category
     * @return This planner for method chaining
     */
    public ScenarioPlanner setCategory(String category) {
        scenario.setCategory(category);
        return this;
    }
    
    /**
     * Sets the scenario subcategory.
     * 
     * @param subcategory The subcategory
     * @return This planner for method chaining
     */
    public ScenarioPlanner setSubcategory(String subcategory) {
        scenario.setSubcategory(subcategory);
        return this;
    }
    
    /**
     * Sets the battle strategy for this scenario.
     * 
     * @param battleStrategy The battle strategy ("reset_stats" or "persist_stats")
     * @return This planner for method chaining
     */
    public ScenarioPlanner setBattleStrategy(String battleStrategy) {
        scenario.setBattleStrategy(battleStrategy);
        return this;
    }
    
    /**
     * Sets whether this scenario is an entry point.
     * 
     * @param entryPoint True if this is an entry point scenario
     * @return This planner for method chaining
     */
    public ScenarioPlanner setEntryPoint(boolean entryPoint) {
        scenario.setEntryPoint(entryPoint);
        return this;
    }
    
    // ID management methods
    
    /**
     * Adds an identifiable object to the ids dictionary.
     * 
     * @param id The object ID
     * @param builderFunction Function that builds the IdentifiableObject
     * @return This planner for method chaining
     */
    public ScenarioPlanner addId(String id, Function<IdentifiableObjectBuilder, IdentifiableObject> builderFunction) {
        if (scenario.getIds() == null) {
            scenario.setIds(new HashMap<>());
        }
        IdentifiableObjectBuilder builder = new IdentifiableObjectBuilder();
        IdentifiableObject object = builderFunction.apply(builder);
        scenario.getIds().put(id, object);
        return this;
    }
    
    /**
     * Adds an identifiable object directly.
     * 
     * @param id The object ID
     * @param object The IdentifiableObject
     * @return This planner for method chaining
     */
    public ScenarioPlanner addId(String id, IdentifiableObject object) {
        if (scenario.getIds() == null) {
            scenario.setIds(new HashMap<>());
        }
        scenario.getIds().put(id, object);
        return this;
    }
    
    /**
     * Loads IDs from a JSON file and adds them to the scenario.
     * The file should contain a JSON object with ID entries.
     * 
     * @param filePath Path to the IDs JSON file
     * @return This planner for method chaining
     * @throws IOException If the file cannot be read or parsed
     */
    public ScenarioPlanner setIdsFromFile(String filePath) throws IOException {
        File idsFile = new File(filePath);
        if (!idsFile.exists()) {
            throw new IOException("IDs file not found: " + filePath);
        }
        
        if (scenario.getIds() == null) {
            scenario.setIds(new HashMap<>());
        }
        
        // Load and parse the IDs file
        Gson gson = new GsonBuilder().create();
        try (FileReader idsReader = new FileReader(idsFile)) {
            JsonObject idsFileRoot = gson.fromJson(idsReader, JsonObject.class);
            
            // The IDs file should contain a JSON object with ID entries
            for (Map.Entry<String, JsonElement> idEntry : idsFileRoot.entrySet()) {
                String objectId = idEntry.getKey();
                JsonElement idElement = idEntry.getValue();
                
                if (idElement.isJsonObject()) {
                    JsonObject idObj = idElement.getAsJsonObject();
                    IdentifiableObject identifiableObject = IdentifiableObjectParser.parse(idObj);
                    scenario.getIds().put(objectId, identifiableObject);
                }
            }
        }
        
        return this;
    }
    
    // Character management methods
    
    /**
     * Adds a character to the characters dictionary.
     * 
     * @param id The character ID
     * @param builderFunction Function that builds the Character
     * @return This planner for method chaining
     */
    public ScenarioPlanner addCharacter(String id, Function<CharacterBuilder, Character> builderFunction) {
        if (scenario.getCharacters() == null) {
            scenario.setCharacters(new HashMap<>());
        }
        CharacterBuilder builder = new CharacterBuilder();
        Character character = builderFunction.apply(builder);
        scenario.getCharacters().put(id, character);
        return this;
    }
    
    /**
     * Adds a character directly.
     * 
     * @param id The character ID
     * @param character The Character object
     * @return This planner for method chaining
     */
    public ScenarioPlanner addCharacter(String id, Character character) {
        if (scenario.getCharacters() == null) {
            scenario.setCharacters(new HashMap<>());
        }
        scenario.getCharacters().put(id, character);
        return this;
    }
    
    // Action management methods
    
    /**
     * Adds an action to the sequence.
     * 
     * @param builderFunction Function that builds the ScenarioAction
     * @return This planner for method chaining
     */
    public ScenarioPlanner addAction(Function<ActionBuilder, ScenarioAction> builderFunction) {
        if (scenario.getSequence() == null) {
            scenario.setSequence(new ArrayList<>());
        }
        ActionBuilder builder = new ActionBuilder();
        ScenarioAction action = builderFunction.apply(builder);
        scenario.getSequence().add(action);
        return this;
    }
    
    /**
     * Adds an action directly.
     * 
     * @param action The ScenarioAction
     * @return This planner for method chaining
     */
    public ScenarioPlanner addAction(ScenarioAction action) {
        if (scenario.getSequence() == null) {
            scenario.setSequence(new ArrayList<>());
        }
        scenario.getSequence().add(action);
        return this;
    }
    
    /**
     * Adds multiple actions at once.
     * 
     * @param actions List of ScenarioAction objects
     * @return This planner for method chaining
     */
    public ScenarioPlanner addActions(List<ScenarioAction> actions) {
        if (scenario.getSequence() == null) {
            scenario.setSequence(new ArrayList<>());
        }
        scenario.getSequence().addAll(actions);
        return this;
    }
    
    // Export methods
    
    /**
     * Exports the scenario to a JSON file.
     * 
     * @param outputPath The output file path
     * @throws IOException If the file cannot be written
     */
    public void export(String outputPath) throws IOException {
        exporter.export(scenario, outputPath);
    }
    
    /**
     * Exports the scenario to a JSON file in the specified output directory.
     * 
     * @param outputDirectory The output directory
     * @param filename The filename (e.g., "scenario.json")
     * @throws IOException If the file cannot be written
     */
    public void export(String outputDirectory, String filename) throws IOException {
        exporter.export(scenario, outputDirectory, filename);
    }
    
    /**
     * Gets the underlying Scenario object.
     * 
     * @return The Scenario object
     */
    public Scenario getScenario() {
        return scenario;
    }
}

