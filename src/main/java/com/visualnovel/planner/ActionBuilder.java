package com.visualnovel.planner;

import com.visualnovel.model.ActionRegistry;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.Timing;
import com.visualnovel.model.actions.BattleAction;
import com.visualnovel.model.actions.ChooseAction;
import com.visualnovel.model.actions.ContinueAction;
import com.visualnovel.model.actions.EndScenarioAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating ScenarioAction instances.
 * Provides helper methods for common action types.
 */
public class ActionBuilder {
    private ScenarioAction action;
    
    public ActionBuilder() {
        this.action = new ScenarioAction();
    }
    
    /**
     * Sets the action type directly.
     * 
     * @param actionType The ActionType instance
     * @return This builder for method chaining
     */
    public ActionBuilder actionType(ActionType actionType) {
        action.setActionType(actionType);
        return this;
    }
    
    /**
     * Sets the action type by JSON value.
     * 
     * @param jsonValue The JSON string identifier (e.g., "ShowMessage", "Battle")
     * @return This builder for method chaining
     */
    public ActionBuilder action(String jsonValue) {
        ActionRegistry registry = ActionRegistry.getInstance();
        ActionType actionType = registry.get(jsonValue);
        if (actionType == null) {
            throw new IllegalArgumentException("Unknown action type: " + jsonValue);
        }
        action.setActionType(actionType);
        return this;
    }
    
    /**
     * Sets the timing using a TimingBuilder.
     * 
     * @param builderFunction Function that builds the timing
     * @return This builder for method chaining
     */
    public ActionBuilder timing(java.util.function.Function<TimingBuilder, Timing> builderFunction) {
        TimingBuilder timingBuilder = new TimingBuilder();
        Timing timing = builderFunction.apply(timingBuilder);
        action.setTiming(timing);
        return this;
    }
    
    /**
     * Sets the timing directly.
     * 
     * @param timing The Timing object
     * @return This builder for method chaining
     */
    public ActionBuilder timing(Timing timing) {
        action.setTiming(timing);
        return this;
    }
    
    /**
     * Sets the timing type using TimingType enum.
     * 
     * @param type The timing type
     * @return This builder for method chaining
     */
    public ActionBuilder timing(TimingType type) {
        TimingBuilder timingBuilder = new TimingBuilder();
        timingBuilder.type(type);
        action.setTiming(timingBuilder.build());
        return this;
    }
    
    /**
     * Sets the action ID (for restart points).
     * 
     * @param id The action ID
     * @return This builder for method chaining
     */
    public ActionBuilder id(String id) {
        action.setId(id);
        return this;
    }
    
    /**
     * Sets a parameter value.
     * 
     * @param key The parameter key
     * @param value The parameter value
     * @return This builder for method chaining
     */
    public ActionBuilder parameter(String key, Object value) {
        if (action.getParameters() == null) {
            action.setParameters(new HashMap<>());
        }
        action.getParameters().put(key, value);
        return this;
    }
    
    /**
     * Sets multiple parameters at once.
     * 
     * @param parameters Map of parameter keys to values
     * @return This builder for method chaining
     */
    public ActionBuilder parameters(Map<String, Object> parameters) {
        action.setParameters(parameters);
        return this;
    }
    
    /**
     * Sets the onWin handler (for Battle actions).
     * Can be a String (file path) or a Map (action object).
     * 
     * @param onWin The onWin handler
     * @return This builder for method chaining
     */
    public ActionBuilder onWin(Object onWin) {
        action.setOnWin(onWin);
        return this;
    }
    
    /**
     * Sets the onLose handler (for Battle actions).
     * Can be a String (file path) or a Map (action object).
     * 
     * @param onLose The onLose handler
     * @return This builder for method chaining
     */
    public ActionBuilder onLose(Object onLose) {
        action.setOnLose(onLose);
        return this;
    }
    
    // Helper methods for specific action types
    
    /**
     * Creates a ShowMessage action.
     * 
     * @param context The message context (e.g., "Narrator", "Player")
     * @param message The message text
     * @return This builder for method chaining
     */
    public ActionBuilder showMessage(String context, String message) {
        action("ShowMessage");
        parameter("context", context);
        parameter("message", message);
        return this;
    }
    
    /**
     * Creates a ShowSprite action.
     * 
     * @param id The object ID
     * @param position The sprite position (e.g., "Left", "Right", "Back")
     * @return This builder for method chaining
     */
    public ActionBuilder showSprite(String id, String position) {
        action("ShowSprite");
        parameter("id", id);
        parameter("position", position);
        return this;
    }
    
    /**
     * Creates a ShowSprite action with sprite key.
     * 
     * @param id The object ID
     * @param position The sprite position
     * @param spriteKey The sprite key (e.g., "default", "extra")
     * @return This builder for method chaining
     */
    public ActionBuilder showSprite(String id, String position, String spriteKey) {
        showSprite(id, position);
        parameter("sprite_key", spriteKey);
        return this;
    }
    
    /**
     * Creates a ShowSprite action with depth and offset.
     * 
     * @param id The object ID
     * @param position The sprite position
     * @param depth The depth value
     * @param offset The offset value
     * @return This builder for method chaining
     */
    public ActionBuilder showSprite(String id, String position, int depth, int offset) {
        showSprite(id, position);
        parameter("depth", depth);
        parameter("offset", offset);
        return this;
    }
    
    /**
     * Creates a HideSprite action.
     * 
     * @param id The object ID to hide
     * @return This builder for method chaining
     */
    public ActionBuilder hideSprite(String id) {
        action("HideSprite");
        parameter("id", id);
        return this;
    }
    
    /**
     * Creates a Battle action.
     * 
     * @param team1 List of character IDs for team 1
     * @param team2 List of character IDs for team 2
     * @param playerTeam The player team ID ("team1" or "team2")
     * @return This builder for method chaining
     */
    public ActionBuilder battle(List<String> team1, List<String> team2, String playerTeam) {
        action(BattleAction.getName());
        parameter("team1", team1);
        parameter("team2", team2);
        parameter("playerTeam", playerTeam);
        return this;
    }
    
    /**
     * Sets the onWin handler for a Battle action as a file path.
     * 
     * @param filePath The scenario file path
     * @return This builder for method chaining
     */
    public ActionBuilder onWin(String filePath) {
        action.setOnWin(filePath);
        return this;
    }
    
    /**
     * Sets the onWin handler for a Battle action as an action.
     * 
     * @param builderFunction Function that builds the action
     * @return This builder for method chaining
     */
    public ActionBuilder onWin(java.util.function.Function<ActionBuilder, ScenarioAction> builderFunction) {
        ActionBuilder actionBuilder = new ActionBuilder();
        ScenarioAction nestedAction = builderFunction.apply(actionBuilder);
        Map<String, Object> actionMap = buildActionMap(nestedAction);
        action.setOnWin(actionMap);
        return this;
    }
    
    /**
     * Sets the onLose handler for a Battle action as a file path.
     * 
     * @param filePath The scenario file path
     * @return This builder for method chaining
     */
    public ActionBuilder onLose(String filePath) {
        action.setOnLose(filePath);
        return this;
    }
    
    /**
     * Sets the onLose handler for a Battle action as an action.
     * 
     * @param builderFunction Function that builds the action
     * @return This builder for method chaining
     */
    public ActionBuilder onLose(java.util.function.Function<ActionBuilder, ScenarioAction> builderFunction) {
        ActionBuilder actionBuilder = new ActionBuilder();
        ScenarioAction nestedAction = builderFunction.apply(actionBuilder);
        Map<String, Object> actionMap = buildActionMap(nestedAction);
        action.setOnLose(actionMap);
        return this;
    }
    
    /**
     * Converts a ScenarioAction to a Map representation for JSON serialization.
     */
    private Map<String, Object> buildActionMap(ScenarioAction action) {
        Map<String, Object> actionMap = new HashMap<>();
        if (action.getActionType() != null) {
            actionMap.put("action", action.getActionType().getJsonValue());
        }
        if (action.getTiming() != null) {
            Map<String, Object> timingMap = buildTimingMap(action.getTiming());
            actionMap.put("timing", timingMap);
        }
        if (action.getParameters() != null) {
            actionMap.put("parameters", action.getParameters());
        }
        return actionMap;
    }
    
    /**
     * Converts a Timing to a Map representation for JSON serialization.
     */
    private Map<String, Object> buildTimingMap(Timing timing) {
        Map<String, Object> timingMap = new HashMap<>();
        if (timing.getType() != null) {
            timingMap.put("type", timing.getType());
        }
        if (timing.getDurationMs() != null) {
            timingMap.put("durationMs", timing.getDurationMs());
        }
        if (timing.getAnimation() != null) {
            timingMap.put("animation", timing.getAnimation());
        }
        if (timing.getAdditionalProperties() != null) {
            timingMap.putAll(timing.getAdditionalProperties());
        }
        return timingMap;
    }
    
    /**
     * Creates a ChooseAction with options.
     * 
     * @param context The message context
     * @param message The choice message
     * @param options List of choice options (each is a Map with "text" and "onPress")
     * @return This builder for method chaining
     */
    public ActionBuilder choose(String context, String message, List<Map<String, Object>> options) {
        action(ChooseAction.getName());
        parameter("context", context);
        parameter("message", message);
        parameter("options", options);
        return this;
    }
    
    /**
     * Creates a Delay action.
     * 
     * @param delayMs The delay in milliseconds
     * @return This builder for method chaining
     */
    public ActionBuilder delay(int delayMs) {
        action("Delay");
        parameter("delayMs", delayMs);
        return this;
    }
    
    /**
     * Creates a ContinueAction.
     * 
     * @return This builder for method chaining
     */
    public ActionBuilder continueAction() {
        action(ContinueAction.getName());
        return this;
    }
    
    /**
     * Creates an EndScenario action.
     * 
     * @return This builder for method chaining
     */
    public ActionBuilder endScenario() {
        action(EndScenarioAction.getName());
        return this;
    }
    
    /**
     * Creates an EndScenario action with restart parameter.
     * 
     * @param allowRestart If true, sets restart parameter to "allowed"
     * @return This builder for method chaining
     */
    public ActionBuilder endScenario(boolean allowRestart) {
        action(EndScenarioAction.getName());
        if (allowRestart) {
            parameter("restart", "allowed");
        }
        return this;
    }
    
    /**
     * Creates an EndScenario action with restart parameter.
     * 
     * @param restart The restart parameter value (typically "allowed" or null)
     * @return This builder for method chaining
     */
    public ActionBuilder endScenario(String restart) {
        action(EndScenarioAction.getName());
        if (restart != null && !restart.isEmpty()) {
            parameter("restart", restart);
        }
        return this;
    }
    
    /**
     * Builds and returns the ScenarioAction object.
     * 
     * @return The constructed ScenarioAction
     */
    public ScenarioAction build() {
        return action;
    }
    
    /**
     * Helper class for building choice options.
     */
    public static class ChoiceOptionBuilder {
        private Map<String, Object> option;
        
        public ChoiceOptionBuilder() {
            this.option = new HashMap<>();
        }
        
        /**
         * Sets the option text.
         * 
         * @param text The option text
         * @return This builder for method chaining
         */
        public ChoiceOptionBuilder text(String text) {
            option.put("text", text);
            return this;
        }
        
        /**
         * Sets the onPress handler as a file path (String).
         * 
         * @param filePath The scenario file path
         * @return This builder for method chaining
         */
        public ChoiceOptionBuilder onPress(String filePath) {
            option.put("onPress", filePath);
            return this;
        }
        
        /**
         * Sets the onPress handler as an action (Map).
         * 
         * @param actionMap The action map
         * @return This builder for method chaining
         */
        public ChoiceOptionBuilder onPress(Map<String, Object> actionMap) {
            option.put("onPress", actionMap);
            return this;
        }
        
        /**
         * Sets the onPress handler using an ActionBuilder.
         * 
         * @param builderFunction Function that builds the action
         * @return This builder for method chaining
         */
        public ChoiceOptionBuilder onPress(java.util.function.Function<ActionBuilder, ScenarioAction> builderFunction) {
            ActionBuilder actionBuilder = new ActionBuilder();
            ScenarioAction action = builderFunction.apply(actionBuilder);
            Map<String, Object> actionMap = buildActionMap(action);
            option.put("onPress", actionMap);
            return this;
        }
        
        /**
         * Builds the choice option map.
         * 
         * @return The choice option map
         */
        public Map<String, Object> build() {
            return option;
        }
        
        /**
         * Converts a ScenarioAction to a Map representation for JSON serialization.
         */
        private Map<String, Object> buildActionMap(ScenarioAction action) {
            Map<String, Object> actionMap = new HashMap<>();
            if (action.getActionType() != null) {
                actionMap.put("action", action.getActionType().getJsonValue());
            }
            if (action.getTiming() != null) {
                Map<String, Object> timingMap = buildTimingMap(action.getTiming());
                actionMap.put("timing", timingMap);
            }
            if (action.getParameters() != null) {
                actionMap.put("parameters", action.getParameters());
            }
            return actionMap;
        }
        
        /**
         * Converts a Timing to a Map representation for JSON serialization.
         */
        private Map<String, Object> buildTimingMap(Timing timing) {
            Map<String, Object> timingMap = new HashMap<>();
            if (timing.getType() != null) {
                timingMap.put("type", timing.getType());
            }
            if (timing.getDurationMs() != null) {
                timingMap.put("durationMs", timing.getDurationMs());
            }
            if (timing.getAnimation() != null) {
                timingMap.put("animation", timing.getAnimation());
            }
            if (timing.getAdditionalProperties() != null) {
                timingMap.putAll(timing.getAdditionalProperties());
            }
            return timingMap;
        }
    }
    
    /**
     * Creates a choice option builder.
     * 
     * @return A new ChoiceOptionBuilder
     */
    public static ChoiceOptionBuilder choiceOption() {
        return new ChoiceOptionBuilder();
    }
    
    /**
     * Helper method to build a list of choice options.
     * 
     * @param options List of choice option maps
     * @return List of choice option maps
     */
    public static List<Map<String, Object>> choiceOptions(List<Map<String, Object>> options) {
        return options;
    }
}

