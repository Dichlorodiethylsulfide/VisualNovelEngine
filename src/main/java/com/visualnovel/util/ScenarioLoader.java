package com.visualnovel.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.visualnovel.model.ActionRegistry;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.Character;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.Scenario;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.Timing;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and parses scenario JSON files.
 */
public class ScenarioLoader {
    private Gson gson;
    
    public ScenarioLoader() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ScenarioAction.class, new ScenarioActionDeserializer());
        gson = builder.create();
    }
    
    /**
     * Loads a scenario from a JSON file.
     * Supports both entry point scenarios (with full metadata) and nested scenarios (sequence only).
     * 
     * @param filePath Path to the scenario JSON file
     * @return The loaded Scenario object
     * @throws IOException If the file cannot be read
     */
    public Scenario loadScenario(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Scenario file not found: " + filePath);
        }
        
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            Scenario scenario = new Scenario();
            
            // Check if this is an entry point scenario (has "scenario" object)
            if (root.has("scenario") && root.get("scenario").isJsonObject()) {
                JsonObject scenarioObj = root.getAsJsonObject("scenario");
                
                // Parse entry_point
                Boolean entryPoint = JsonObjectReader.parseOptionalBoolean(scenarioObj, "entry_point");
                if (entryPoint != null) {
                    scenario.setEntryPoint(entryPoint);
                }
                
                // Parse metadata using JsonObjectReader
                String name = JsonObjectReader.parseOptionalString(scenarioObj, "name");
                if (name != null) {
                    scenario.setName(name);
                }
                String description = JsonObjectReader.parseOptionalString(scenarioObj, "description");
                if (description != null) {
                    scenario.setDescription(description);
                }
                String author = JsonObjectReader.parseOptionalString(scenarioObj, "author");
                if (author != null) {
                    scenario.setAuthor(author);
                }
                String version = JsonObjectReader.parseOptionalString(scenarioObj, "version");
                if (version != null) {
                    scenario.setVersion(version);
                }
                String date = JsonObjectReader.parseOptionalString(scenarioObj, "date");
                if (date != null) {
                    scenario.setDate(date);
                }
                List<String> tags = JsonObjectReader.parseStringList(scenarioObj, "tags");
                if (tags != null) {
                    scenario.setTags(tags);
                }
                String category = JsonObjectReader.parseOptionalString(scenarioObj, "category");
                if (category != null) {
                    scenario.setCategory(category);
                }
                String subcategory = JsonObjectReader.parseOptionalString(scenarioObj, "subcategory");
                if (subcategory != null) {
                    scenario.setSubcategory(subcategory);
                }
                String battleStrategy = JsonObjectReader.parseOptionalString(scenarioObj, "battle_strategy");
                if (battleStrategy != null) {
                    scenario.setBattleStrategy(battleStrategy);
                }
                
                // Parse ids dictionary using IdentifiableObjectParser
                // Supports both JSONObject (inline) and string (file path)
                if (scenarioObj.has("ids")) {
                    JsonElement idsElement = scenarioObj.get("ids");
                    Map<String, IdentifiableObject> ids = new HashMap<>();
                    
                    if (idsElement.isJsonObject()) {
                        // IDs are provided inline as JSONObject
                        JsonObject idsObj = idsElement.getAsJsonObject();
                        
                        for (Map.Entry<String, JsonElement> idEntry : idsObj.entrySet()) {
                            String objectId = idEntry.getKey();
                            JsonElement idElement = idEntry.getValue();
                            
                            if (idElement.isJsonObject()) {
                                JsonObject idObj = idElement.getAsJsonObject();
                                IdentifiableObject identifiableObject = IdentifiableObjectParser.parse(idObj);
                                ids.put(objectId, identifiableObject);
                            }
                        }
                    } else if (idsElement.isJsonPrimitive() && idsElement.getAsJsonPrimitive().isString()) {
                        // IDs are provided as a file path string
                        String idsFilePath = idsElement.getAsString();
                        
                        // Resolve the file path relative to the scenario file's directory
                        File scenarioFile = new File(filePath);
                        File idsFile;
                        if (scenarioFile.getParent() != null) {
                            idsFile = new File(scenarioFile.getParent(), idsFilePath);
                        } else {
                            idsFile = new File(idsFilePath);
                        }
                        
                        if (!idsFile.exists()) {
                            throw new IOException("IDs file not found: " + idsFile.getAbsolutePath());
                        }
                        
                        // Load and parse the IDs file
                        try (FileReader idsReader = new FileReader(idsFile)) {
                            JsonObject idsFileRoot = gson.fromJson(idsReader, JsonObject.class);
                            
                            // The IDs file should contain a JSON object with ID entries
                            for (Map.Entry<String, JsonElement> idEntry : idsFileRoot.entrySet()) {
                                String objectId = idEntry.getKey();
                                JsonElement idElement = idEntry.getValue();
                                
                                if (idElement.isJsonObject()) {
                                    JsonObject idObj = idElement.getAsJsonObject();
                                    IdentifiableObject identifiableObject = IdentifiableObjectParser.parse(idObj);
                                    ids.put(objectId, identifiableObject);
                                }
                            }
                        }
                    }
                    
                    if (!ids.isEmpty()) {
                        scenario.setIds(ids);
                    }
                }
                
                // Parse characters
                if (scenarioObj.has("characters") && scenarioObj.get("characters").isJsonObject()) {
                    JsonObject charactersObj = scenarioObj.getAsJsonObject("characters");
                    Map<String, Character> characters = new HashMap<>();
                    
                    for (Map.Entry<String, JsonElement> charEntry : charactersObj.entrySet()) {
                        String charId = charEntry.getKey();
                        JsonElement charElement = charEntry.getValue();
                        
                        if (charElement.isJsonObject()) {
                            JsonObject charObj = charElement.getAsJsonObject();
                            Character character = new Character();
                            
                            // Parse character name
                            if (charObj.has("name")) {
                                character.setName(charObj.get("name").getAsString());
                            } else {
                                character.setName(charId); // Default to ID if name not provided
                            }
                            
                            // Parse sprites using SpriteDefinitionParser
                            if (charObj.has("sprites") && charObj.get("sprites").isJsonObject()) {
                                JsonObject spritesObj = charObj.getAsJsonObject("sprites");
                                SpriteDefinitionParser.SpriteParseResult spriteResult = 
                                    SpriteDefinitionParser.parseSprites(spritesObj);
                                character.setSprites(spriteResult.getSprites());
                                if (spriteResult.getRandomSprites() != null && !spriteResult.getRandomSprites().isEmpty()) {
                                    character.setRandomSprites(spriteResult.getRandomSprites());
                                }
                            }
                            
                            // Parse stats using JsonObjectReader
                            Map<String, Number> stats = JsonObjectReader.parseNumberMap(charObj, "stats");
                            if (stats != null) {
                                character.setStats(stats);
                            }
                            
                            characters.put(charId, character);
                        }
                    }
                    
                    scenario.setCharacters(characters);
                }
                
                // Parse sequence (primary field for actions)
                if (scenarioObj.has("sequence") && scenarioObj.get("sequence").isJsonArray()) {
                    List<ScenarioAction> actions = new ArrayList<>();
                    for (JsonElement element : scenarioObj.getAsJsonArray("sequence")) {
                        ScenarioAction action = gson.fromJson(element, ScenarioAction.class);
                        actions.add(action);
                    }
                    scenario.setSequence(actions);
                }
            } else if (root.has("sequence") && root.get("sequence").isJsonArray()) {
                // Nested scenario format: just has "sequence" array
                List<ScenarioAction> actions = new ArrayList<>();
                for (JsonElement element : root.getAsJsonArray("sequence")) {
                    ScenarioAction action = gson.fromJson(element, ScenarioAction.class);
                    actions.add(action);
                }
                scenario.setSequence(actions);
                // Nested scenarios are not entry points
                scenario.setEntryPoint(false);
            } else {
                throw new IOException("Invalid scenario format: expected 'sequence' array");
            }
            
            return scenario;
        }
    }
    
    /**
     * Custom deserializer for ScenarioAction to handle the complex structure.
     */
    private static class ScenarioActionDeserializer implements JsonDeserializer<ScenarioAction> {
        
        /**
         * Parses a timing JsonObject into a Map representation.
         * 
         * @param timingObj The timing JsonObject to parse
         * @return A Map containing the timing data
         */
        private static Map<String, Object> parseTimingToMap(JsonObject timingObj) {
            Map<String, Object> timingMap = new HashMap<>();
            if (timingObj.has("type")) {
                timingMap.put("type", timingObj.get("type").getAsString());
            }
            if (timingObj.has("durationMs")) {
                timingMap.put("durationMs", timingObj.get("durationMs").getAsInt());
            }
            if (timingObj.has("animation")) {
                timingMap.put("animation", timingObj.get("animation").getAsString());
            }
            return timingMap;
        }
        
        /**
         * Parses a parameters JsonObject into a Map representation.
         * Handles primitive values (String, Number, Boolean) and arrays.
         * 
         * @param paramsObj The parameters JsonObject to parse
         * @return A Map containing the parameter data
         */
        private static Map<String, Object> parseParametersToMap(JsonObject paramsObj) {
            Map<String, Object> paramsMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> paramEntry : paramsObj.entrySet()) {
                String paramKey = paramEntry.getKey();
                JsonElement paramValue = paramEntry.getValue();
                if (paramValue.isJsonPrimitive()) {
                    if (paramValue.getAsJsonPrimitive().isString()) {
                        paramsMap.put(paramKey, paramValue.getAsString());
                    } else if (paramValue.getAsJsonPrimitive().isNumber()) {
                        paramsMap.put(paramKey, paramValue.getAsNumber());
                    } else if (paramValue.getAsJsonPrimitive().isBoolean()) {
                        paramsMap.put(paramKey, paramValue.getAsBoolean());
                    }
                } else if (paramValue.isJsonArray()) {
                    // Handle arrays (like team arrays in Battle action)
                    List<Object> arrayList = new ArrayList<>();
                    for (JsonElement arrayElement : paramValue.getAsJsonArray()) {
                        if (arrayElement.isJsonPrimitive()) {
                            if (arrayElement.getAsJsonPrimitive().isString()) {
                                arrayList.add(arrayElement.getAsString());
                            } else if (arrayElement.getAsJsonPrimitive().isNumber()) {
                                arrayList.add(arrayElement.getAsNumber());
                            } else if (arrayElement.getAsJsonPrimitive().isBoolean()) {
                                arrayList.add(arrayElement.getAsBoolean());
                            }
                        }
                    }
                    paramsMap.put(paramKey, arrayList);
                }
            }
            return paramsMap;
        }
        
        /**
         * Parses a JsonObject representing an action into a Map representation.
         * Used for nested actions (e.g., in ChooseAction's onPress).
         * 
         * @param actionObj The action JsonObject to parse
         * @return A Map containing the action data (action, timing, parameters)
         */
        private static Map<String, Object> parseActionToMap(JsonObject actionObj) {
            Map<String, Object> actionMap = new HashMap<>();
            
            // Parse action type
            if (actionObj.has("action")) {
                actionMap.put("action", actionObj.get("action").getAsString());
            }
            
            // Parse timing
            if (actionObj.has("timing")) {
                JsonElement timingElement = actionObj.get("timing");
                if (timingElement.isJsonObject()) {
                    JsonObject timingObj = timingElement.getAsJsonObject();
                    actionMap.put("timing", parseTimingToMap(timingObj));
                }
            }
            
            // Parse parameters
            if (actionObj.has("parameters")) {
                JsonObject nestedParamsObj = actionObj.getAsJsonObject("parameters");
                actionMap.put("parameters", parseParametersToMap(nestedParamsObj));
            }
            
            return actionMap;
        }
        
        @Override
        public ScenarioAction deserialize(JsonElement json, Type typeOfT, 
                                         JsonDeserializationContext context) 
                                         throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            ScenarioAction action = new ScenarioAction();
            
            // Parse action type
            if (jsonObject.has("action")) {
                String actionStr = jsonObject.get("action").getAsString();
                ActionRegistry registry = ActionRegistry.getInstance();
                ActionType actionType = registry.get(actionStr);
                if (actionType == null) {
                    throw new JsonParseException("Unknown action type: " + actionStr);
                }
                action.setActionType(actionType);
            }
            
            // Parse timing
            if (jsonObject.has("timing")) {
                JsonElement timingElement = jsonObject.get("timing");
                Timing timing;
                
                if (timingElement.isJsonObject()) {
                    // New format: timing is an object
                    JsonObject timingObj = timingElement.getAsJsonObject();
                    timing = new Timing();
                    
                    if (timingObj.has("type")) {
                        timing.setType(timingObj.get("type").getAsString());
                    }
                    
                    if (timingObj.has("durationMs")) {
                        timing.setDurationMs(timingObj.get("durationMs").getAsInt());
                    }
                    
                    if (timingObj.has("animation")) {
                        timing.setAnimation(timingObj.get("animation").getAsString());
                    }
                    
                    // Store any additional properties
                    Map<String, Object> additionalProps = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : timingObj.entrySet()) {
                        String key = entry.getKey();
                        if (!key.equals("type") && !key.equals("durationMs") && !key.equals("animation")) {
                            JsonElement value = entry.getValue();
                            if (value.isJsonPrimitive()) {
                                if (value.getAsJsonPrimitive().isString()) {
                                    additionalProps.put(key, value.getAsString());
                                } else if (value.getAsJsonPrimitive().isNumber()) {
                                    additionalProps.put(key, value.getAsNumber());
                                } else if (value.getAsJsonPrimitive().isBoolean()) {
                                    additionalProps.put(key, value.getAsBoolean());
                                }
                            }
                        }
                    }
                    if (!additionalProps.isEmpty()) {
                        timing.setAdditionalProperties(additionalProps);
                    }
                } else {
                    // Invalid format, default to Immediate
                    timing = new Timing("Immediate");
                }
                
                action.setTiming(timing);
            }
            
            // Parse ID (optional)
            if (jsonObject.has("id")) {
                action.setId(jsonObject.get("id").getAsString());
            }
            
            // Parse parameters
            if (jsonObject.has("parameters")) {
                JsonObject paramsObj = jsonObject.getAsJsonObject("parameters");
                Map<String, Object> parameters = new HashMap<>();
                
                for (Map.Entry<String, JsonElement> entry : paramsObj.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    
                    if (value.isJsonPrimitive()) {
                        if (value.getAsJsonPrimitive().isString()) {
                            String stringValue = value.getAsString();
                            // Handle OnLose and OnWin as file paths (extract from parameters)
                            if (key.equals("OnLose")) {
                                action.setOnLose(stringValue);
                            } else if (key.equals("OnWin")) {
                                action.setOnWin(stringValue);
                            } else {
                                parameters.put(key, stringValue);
                            }
                        } else if (value.getAsJsonPrimitive().isNumber()) {
                            parameters.put(key, value.getAsNumber());
                        } else if (value.getAsJsonPrimitive().isBoolean()) {
                            parameters.put(key, value.getAsBoolean());
                        }
                    } else if (value.isJsonObject()) {
                        // Handle OnLose and OnWin as action objects (like ContinueAction)
                        if (key.equals("OnLose")) {
                            JsonObject actionObj = value.getAsJsonObject();
                            Map<String, Object> actionMap = parseActionToMap(actionObj);
                            action.setOnLose(actionMap);
                        } else if (key.equals("OnWin")) {
                            JsonObject actionObj = value.getAsJsonObject();
                            Map<String, Object> actionMap = parseActionToMap(actionObj);
                            action.setOnWin(actionMap);
                        } else {
                            // Other objects - store as map (for nested parameter objects)
                            JsonObject obj = value.getAsJsonObject();
                            Map<String, Object> objMap = parseParametersToMap(obj);
                            parameters.put(key, objMap);
                        }
                    } else if (value.isJsonArray()) {
                        // Handle OnLose and OnWin as arrays of actions
                        if (key.equals("OnLose")) {
                            List<Map<String, Object>> actionList = new ArrayList<>();
                            for (JsonElement arrayElement : value.getAsJsonArray()) {
                                if (arrayElement.isJsonObject()) {
                                    JsonObject actionObj = arrayElement.getAsJsonObject();
                                    Map<String, Object> actionMap = parseActionToMap(actionObj);
                                    actionList.add(actionMap);
                                }
                            }
                            action.setOnLose(actionList);
                        } else if (key.equals("OnWin")) {
                            List<Map<String, Object>> actionList = new ArrayList<>();
                            for (JsonElement arrayElement : value.getAsJsonArray()) {
                                if (arrayElement.isJsonObject()) {
                                    JsonObject actionObj = arrayElement.getAsJsonObject();
                                    Map<String, Object> actionMap = parseActionToMap(actionObj);
                                    actionList.add(actionMap);
                                }
                            }
                            action.setOnWin(actionList);
                        } else {
                            // Handle arrays (like options in ChooseAction)
                            if (key.equals("options")) {
                                List<Map<String, Object>> optionsList = new ArrayList<>();
                                for (JsonElement optionElement : value.getAsJsonArray()) {
                                    if (optionElement.isJsonObject()) {
                                        JsonObject optionObj = optionElement.getAsJsonObject();
                                        Map<String, Object> optionMap = new HashMap<>();
                                        
                                        for (Map.Entry<String, JsonElement> optionEntry : optionObj.entrySet()) {
                                            String optionKey = optionEntry.getKey();
                                            JsonElement optionValue = optionEntry.getValue();
                                            
                                            if (optionKey.equals("onPress")) {
                                                // onPress can be a string (file path), an object (raw JSON action), or an array (list of actions)
                                                if (optionValue.isJsonPrimitive() && optionValue.getAsJsonPrimitive().isString()) {
                                                    optionMap.put(optionKey, optionValue.getAsString());
                                                } else if (optionValue.isJsonObject()) {
                                                    // Parse as a nested action object using helper method
                                                    JsonObject actionObj = optionValue.getAsJsonObject();
                                                    Map<String, Object> actionMap = parseActionToMap(actionObj);
                                                    optionMap.put(optionKey, actionMap);
                                                } else if (optionValue.isJsonArray()) {
                                                    // Parse as an array of actions
                                                    List<Map<String, Object>> actionList = new ArrayList<>();
                                                    for (JsonElement arrayElement : optionValue.getAsJsonArray()) {
                                                        if (arrayElement.isJsonObject()) {
                                                            JsonObject actionObj = arrayElement.getAsJsonObject();
                                                            Map<String, Object> actionMap = parseActionToMap(actionObj);
                                                            actionList.add(actionMap);
                                                        }
                                                    }
                                                    optionMap.put(optionKey, actionList);
                                                }
                                            } else {
                                                // Other option fields (like "text")
                                                if (optionValue.isJsonPrimitive()) {
                                                    if (optionValue.getAsJsonPrimitive().isString()) {
                                                        optionMap.put(optionKey, optionValue.getAsString());
                                                    } else if (optionValue.getAsJsonPrimitive().isNumber()) {
                                                        optionMap.put(optionKey, optionValue.getAsNumber());
                                                    } else if (optionValue.getAsJsonPrimitive().isBoolean()) {
                                                        optionMap.put(optionKey, optionValue.getAsBoolean());
                                                    }
                                                }
                                            }
                                        }
                                        
                                        optionsList.add(optionMap);
                                    }
                                }
                                parameters.put(key, optionsList);
                            } else {
                                // Other arrays - store as list of objects
                                List<Object> arrayList = new ArrayList<>();
                                for (JsonElement arrayElement : value.getAsJsonArray()) {
                                    if (arrayElement.isJsonPrimitive()) {
                                        if (arrayElement.getAsJsonPrimitive().isString()) {
                                            arrayList.add(arrayElement.getAsString());
                                        } else if (arrayElement.getAsJsonPrimitive().isNumber()) {
                                            arrayList.add(arrayElement.getAsNumber());
                                        } else if (arrayElement.getAsJsonPrimitive().isBoolean()) {
                                            arrayList.add(arrayElement.getAsBoolean());
                                        }
                                    }
                                }
                                parameters.put(key, arrayList);
                            }
                        }
                    }
                }
                
                action.setParameters(parameters);
            }
            
            return action;
        }
    }
}

