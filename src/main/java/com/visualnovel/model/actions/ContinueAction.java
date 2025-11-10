package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;

/**
 * Action that continues to the next action in the sequence.
 * Used in onWin/onLose to continue the current scenario instead of loading a new file.
 */
public class ContinueAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "ContinueAction"
     */
    public static String getName() {
        return "ContinueAction";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        // Simply continue to the next action in the sequence
        controller.processNextAction();
    }
    
    @Override
    public boolean requiresUserInput() {
        return false;
    }
    
    @Override
    public boolean shouldContinueImmediately() {
        return false; // execute() calls processNextAction() directly
    }
}

