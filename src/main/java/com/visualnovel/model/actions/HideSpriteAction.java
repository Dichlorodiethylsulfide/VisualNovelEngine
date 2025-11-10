package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;

import java.util.Map;

/**
 * Action that hides sprites (by character ID or position).
 */
public class HideSpriteAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "HideSprite"
     */
    public static String getName() {
        return "HideSprite";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        Map<String, Object> params = action.getParameters();
        if (params == null) {
            System.err.println("Warning: HideSprite action has no parameters.");
            controller.processNextAction();
            return;
        }
        
        VisualNovelGUI gui = controller.getGUI();
        if (gui == null) {
            controller.processNextAction();
            return;
        }
        
        // Check if hiding by character ID
        if (params.containsKey("id")) {
            String characterId = (String) params.get("id");
            if (characterId != null && !characterId.isEmpty()) {
                gui.hideSpritesByCharacterId(characterId);
            }
        }
        
        // Check if hiding by position
        if (params.containsKey("position")) {
            String position = (String) params.get("position");
            if (position != null && !position.isEmpty()) {
                gui.hideSpritesAtPosition(position);
            }
        }
        
        // If neither id nor position is specified, clear all sprites
        if (!params.containsKey("id") && !params.containsKey("position")) {
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

