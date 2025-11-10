package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;

/**
 * Action that clears all sprites from the screen.
 */
public class ClearAllSpritesAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "ClearAllSprites"
     */
    public static String getName() {
        return "ClearAllSprites";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        VisualNovelGUI gui = controller.getGUI();
        if (gui != null) {
            gui.clearSprites();
        }
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

