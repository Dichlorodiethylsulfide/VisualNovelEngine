package com.visualnovel.model;

import java.util.Map;

/**
 * Represents a single action in the scenario.
 */
public class ScenarioAction {
    private ActionType actionType;
    private Timing timing;
    private String id; // Optional ID for restart points
    private Map<String, Object> parameters;
    
    // For Battle action - can be a String (file path) or Map<String, Object> (action object like ContinueAction)
    private Object onLose;
    private Object onWin;
    
    /**
     * Gets the action type for this scenario action.
     * 
     * @return The ActionType instance
     */
    public ActionType getActionType() {
        return actionType;
    }
    
    /**
     * Sets the action type for this scenario action.
     * 
     * @param actionType The ActionType instance
     */
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public Timing getTiming() {
        return timing;
    }
    
    public void setTiming(Timing timing) {
        this.timing = timing;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public Object getOnLose() {
        return onLose;
    }
    
    public void setOnLose(Object onLose) {
        this.onLose = onLose;
    }
    
    public Object getOnWin() {
        return onWin;
    }
    
    public void setOnWin(Object onWin) {
        this.onWin = onWin;
    }
}

