package com.visualnovel;

import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;

/**
 * Centralizes action flow continuation logic.
 * Determines whether to auto-advance or wait for user input after executing an action.
 */
public class ActionFlowController {
    private GameController gameController;
    
    public ActionFlowController(GameController gameController) {
        this.gameController = gameController;
    }
    
    /**
     * Handles continuation logic after executing an action.
     * Determines auto-advance vs waiting based on action type properties.
     * 
     * @param actionType The type of action that was executed
     * @param action The action that was executed
     */
    public void advanceAfter(ActionType actionType, ScenarioAction action) {
        if (actionType == null) {
            // Skip null action types by continuing to next action
            gameController.processNextAction();
            return;
        }
        
        // Determine if we should continue immediately or wait
        if (actionType.requiresUserInput()) {
            gameController.setWaitingForUserInput(true);
        } else if (actionType.shouldContinueImmediately()) {
            gameController.processNextAction();
        }
        // Otherwise, the action will call processNextAction() when ready (e.g., Delay, Battle, animated sprites)
    }
}

