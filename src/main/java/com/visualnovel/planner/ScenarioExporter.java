package com.visualnovel.planner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.visualnovel.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Handles serialization of Scenario objects to JSON format.
 * Provides custom serializers to match the expected JSON structure.
 */
public class ScenarioExporter {
    private Gson gson;
    
    public ScenarioExporter() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(Scenario.class, new ScenarioSerializer());
        builder.registerTypeAdapter(ScenarioAction.class, new ScenarioActionSerializer());
        builder.registerTypeAdapter(Timing.class, new TimingSerializer());
        this.gson = builder.create();
    }
    
    /**
     * Exports a Scenario to a JSON file.
     * 
     * @param scenario The scenario to export
     * @param outputPath The output file path
     * @throws IOException If the file cannot be written
     */
    public void export(Scenario scenario, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileWriter writer = new FileWriter(outputFile)) {
            gson.toJson(scenario, writer);
        }
    }
    
    /**
     * Exports a Scenario to a JSON file in the specified output directory.
     * 
     * @param scenario The scenario to export
     * @param outputDirectory The output directory
     * @param filename The filename (e.g., "scenario.json")
     * @throws IOException If the file cannot be written
     */
    public void export(Scenario scenario, String outputDirectory, String filename) throws IOException {
        String outputPath = outputDirectory + File.separator + filename;
        export(scenario, outputPath);
    }
    
    /**
     * Custom serializer for Scenario objects.
     */
    private static class ScenarioSerializer implements JsonSerializer<Scenario> {
        @Override
        public JsonElement serialize(Scenario scenario, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject root = new JsonObject();
            JsonObject scenarioObj = new JsonObject();
            
            // Serialize metadata
            if (scenario.getEntryPoint() != null) {
                scenarioObj.addProperty("entry_point", scenario.getEntryPoint());
            }
            if (scenario.getName() != null) {
                scenarioObj.addProperty("name", scenario.getName());
            }
            if (scenario.getDescription() != null) {
                scenarioObj.addProperty("description", scenario.getDescription());
            }
            if (scenario.getAuthor() != null) {
                scenarioObj.addProperty("author", scenario.getAuthor());
            }
            if (scenario.getVersion() != null) {
                scenarioObj.addProperty("version", scenario.getVersion());
            }
            if (scenario.getDate() != null) {
                scenarioObj.addProperty("date", scenario.getDate());
            }
            if (scenario.getTags() != null) {
                scenarioObj.add("tags", context.serialize(scenario.getTags()));
            }
            if (scenario.getCategory() != null) {
                scenarioObj.addProperty("category", scenario.getCategory());
            }
            if (scenario.getSubcategory() != null) {
                scenarioObj.addProperty("subcategory", scenario.getSubcategory());
            }
            if (scenario.getBattleStrategy() != null) {
                scenarioObj.addProperty("battle_strategy", scenario.getBattleStrategy());
            }
            
            // Serialize ids dictionary
            if (scenario.getIds() != null && !scenario.getIds().isEmpty()) {
                JsonObject idsObj = new JsonObject();
                for (Map.Entry<String, IdentifiableObject> entry : scenario.getIds().entrySet()) {
                    idsObj.add(entry.getKey(), context.serialize(entry.getValue()));
                }
                scenarioObj.add("ids", idsObj);
            }
            
            // Serialize characters (if present)
            if (scenario.getCharacters() != null && !scenario.getCharacters().isEmpty()) {
                JsonObject charactersObj = new JsonObject();
                for (Map.Entry<String, com.visualnovel.model.Character> entry : scenario.getCharacters().entrySet()) {
                    charactersObj.add(entry.getKey(), context.serialize(entry.getValue()));
                }
                scenarioObj.add("characters", charactersObj);
            }
            
            // Serialize sequence
            List<ScenarioAction> sequence = scenario.getSequence();
            if (sequence != null && !sequence.isEmpty()) {
                scenarioObj.add("sequence", context.serialize(sequence));
            }
            
            root.add("scenario", scenarioObj);
            return root;
        }
    }
    
    /**
     * Custom serializer for ScenarioAction objects.
     */
    private static class ScenarioActionSerializer implements JsonSerializer<ScenarioAction> {
        @Override
        public JsonElement serialize(ScenarioAction action, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            
            // Serialize action type
            if (action.getActionType() != null) {
                jsonObject.addProperty("action", action.getActionType().getJsonValue());
            }
            
            // Serialize timing
            if (action.getTiming() != null) {
                jsonObject.add("timing", context.serialize(action.getTiming()));
            }
            
            // Serialize ID (optional)
            if (action.getId() != null) {
                jsonObject.addProperty("id", action.getId());
            }
            
            // Serialize parameters
            Map<String, Object> parameters = action.getParameters();
            if (parameters != null && !parameters.isEmpty()) {
                JsonObject paramsObj = new JsonObject();
                
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    
                    // Handle special cases for Battle action
                    if (key.equals("OnWin") || key.equals("OnLose")) {
                        // These are handled separately via action.setOnWin/onLose
                        continue;
                    }
                    
                    // Serialize the value
                    paramsObj.add(key, context.serialize(value));
                }
                
                jsonObject.add("parameters", paramsObj);
            }
            
            // Serialize OnWin and OnLose (for Battle actions)
            // These can be String (file path) or Map (action object)
            if (action.getOnWin() != null) {
                if (action.getOnWin() instanceof String) {
                    // It's a file path - add to parameters
                    if (jsonObject.has("parameters")) {
                        jsonObject.getAsJsonObject("parameters").addProperty("OnWin", (String) action.getOnWin());
                    } else {
                        JsonObject paramsObj = new JsonObject();
                        paramsObj.addProperty("OnWin", (String) action.getOnWin());
                        jsonObject.add("parameters", paramsObj);
                    }
                } else if (action.getOnWin() instanceof Map) {
                    // It's an action map - serialize as nested action
                    @SuppressWarnings("unchecked")
                    Map<String, Object> actionMap = (Map<String, Object>) action.getOnWin();
                    JsonObject onWinObj = serializeActionMap(actionMap, context);
                    if (jsonObject.has("parameters")) {
                        jsonObject.getAsJsonObject("parameters").add("OnWin", onWinObj);
                    } else {
                        JsonObject paramsObj = new JsonObject();
                        paramsObj.add("OnWin", onWinObj);
                        jsonObject.add("parameters", paramsObj);
                    }
                }
            }
            
            if (action.getOnLose() != null) {
                if (action.getOnLose() instanceof String) {
                    // It's a file path - add to parameters
                    if (jsonObject.has("parameters")) {
                        jsonObject.getAsJsonObject("parameters").addProperty("OnLose", (String) action.getOnLose());
                    } else {
                        JsonObject paramsObj = new JsonObject();
                        paramsObj.addProperty("OnLose", (String) action.getOnLose());
                        jsonObject.add("parameters", paramsObj);
                    }
                } else if (action.getOnLose() instanceof Map) {
                    // It's an action map - serialize as nested action
                    @SuppressWarnings("unchecked")
                    Map<String, Object> actionMap = (Map<String, Object>) action.getOnLose();
                    JsonObject onLoseObj = serializeActionMap(actionMap, context);
                    if (jsonObject.has("parameters")) {
                        jsonObject.getAsJsonObject("parameters").add("OnLose", onLoseObj);
                    } else {
                        JsonObject paramsObj = new JsonObject();
                        paramsObj.add("OnLose", onLoseObj);
                        jsonObject.add("parameters", paramsObj);
                    }
                }
            }
            
            return jsonObject;
        }
        
        /**
         * Serializes an action map (for nested actions like onPress in ChooseAction).
         */
        private JsonObject serializeActionMap(Map<String, Object> actionMap, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            
            if (actionMap.containsKey("action")) {
                jsonObject.addProperty("action", actionMap.get("action").toString());
            }
            
            if (actionMap.containsKey("timing")) {
                Object timingObj = actionMap.get("timing");
                if (timingObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> timingMap = (Map<String, Object>) timingObj;
                    JsonObject timingJson = new JsonObject();
                    for (Map.Entry<String, Object> entry : timingMap.entrySet()) {
                        timingJson.add(entry.getKey(), context.serialize(entry.getValue()));
                    }
                    jsonObject.add("timing", timingJson);
                } else {
                    jsonObject.add("timing", context.serialize(timingObj));
                }
            }
            
            if (actionMap.containsKey("parameters")) {
                Object paramsObj = actionMap.get("parameters");
                jsonObject.add("parameters", context.serialize(paramsObj));
            }
            
            return jsonObject;
        }
    }
    
    /**
     * Custom serializer for Timing objects.
     */
    private static class TimingSerializer implements JsonSerializer<Timing> {
        @Override
        public JsonElement serialize(Timing timing, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            
            if (timing.getType() != null) {
                jsonObject.addProperty("type", timing.getType());
            }
            
            if (timing.getDurationMs() != null) {
                jsonObject.addProperty("durationMs", timing.getDurationMs());
            }
            
            if (timing.getAnimation() != null) {
                jsonObject.addProperty("animation", timing.getAnimation());
            }
            
            // Serialize additional properties if present
            if (timing.getAdditionalProperties() != null) {
                for (Map.Entry<String, Object> entry : timing.getAdditionalProperties().entrySet()) {
                    jsonObject.add(entry.getKey(), context.serialize(entry.getValue()));
                }
            }
            
            return jsonObject;
        }
    }
}

