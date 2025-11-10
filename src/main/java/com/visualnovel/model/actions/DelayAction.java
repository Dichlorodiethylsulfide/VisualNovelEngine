package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;

import javax.swing.Timer;
import java.util.Map;

/**
 * Action that delays execution for a specified duration.
 */
public class DelayAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "Delay"
     */
    public static String getName() {
        return "Delay";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        Map<String, Object> params = action.getParameters();
        
        // Check if parameters is null
        if (params == null) {
            System.err.println("Warning: DelayAction has no parameters. Skipping delay.");
            controller.processNextAction();
            return;
        }
        
        // Get duration with null check
        Object durationObj = params.get("durationMs");
        if (durationObj == null) {
            System.err.println("Warning: DelayAction missing 'durationMs' parameter. Skipping delay.");
            controller.processNextAction();
            return;
        }
        
        // Check if duration is a Number
        if (!(durationObj instanceof Number)) {
            System.err.println("Warning: DelayAction 'durationMs' parameter is not a number. Skipping delay.");
            controller.processNextAction();
            return;
        }
        
        Number duration = (Number) durationObj;
        int durationMs = duration.intValue();
        
        // Validate duration is non-negative
        if (durationMs < 0) {
            System.err.println("Warning: DelayAction 'durationMs' is negative (" + durationMs + "). Using 0ms.");
            durationMs = 0;
        }
        
        // Use a timer to delay
        Timer timer = new Timer(durationMs, e -> {
            controller.setCurrentDelayTimer(null); // Clear timer reference
            controller.processNextAction();
        });
        timer.setRepeats(false);
        
        // Store timer in controller so it can be skipped
        controller.setCurrentDelayTimer(timer);
        timer.start();
    }
    
    @Override
    public boolean requiresUserInput() {
        return false;
    }
    
    @Override
    public boolean shouldContinueImmediately() {
        return false; // Delay will call processNextAction when timer fires
    }
}

