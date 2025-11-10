package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;

import java.util.Map;

/**
 * Action that displays a message in the text box.
 */
public class ShowMessageAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "ShowMessage"
     */
    public static String getName() {
        return "ShowMessage";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        Map<String, Object> params = action.getParameters();
        String context = (String) params.get("context");
        String message = (String) params.get("message");
        
        VisualNovelGUI gui = controller.getGUI();
        gui.showMessage(context, message);
    }
    
    @Override
    public boolean requiresUserInput() {
        return true;
    }
    
    @Override
    public boolean shouldContinueImmediately() {
        return false;
    }
}

