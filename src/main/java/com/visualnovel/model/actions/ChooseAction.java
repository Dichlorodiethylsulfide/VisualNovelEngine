package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.gui.ChoiceDialog;
import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ChoiceOption;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.util.ActionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Action that displays a choice dialog with multiple options.
 * When an option is selected, loads the scenario file specified in "onPress".
 */
public class ChooseAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "ChooseAction"
     */
    public static String getName() {
        return "ChooseAction";
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
        
        // Get options from parameters
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> optionsList = (List<Map<String, Object>>) params.get("options");
        
        if (optionsList == null || optionsList.isEmpty()) {
            System.err.println("Warning: ChooseAction has no options, continuing.");
            controller.processNextAction();
            return;
        }
        
        // Convert to ChoiceOption objects
        List<ChoiceOption> choiceOptions = new ArrayList<>();
        for (Map<String, Object> optionMap : optionsList) {
            String text = (String) optionMap.get("text");
            Object onPress = optionMap.get("onPress"); // Can be String or Map
            if (text != null && onPress != null) {
                ChoiceOption option = new ChoiceOption(text, onPress);
                
                // Parse optional "requires" parameter
                Object requiresObj = optionMap.get("requires");
                if (requiresObj instanceof String) {
                    option.setRequires((String) requiresObj);
                }
                
                choiceOptions.add(option);
            }
        }
        
        if (choiceOptions.isEmpty()) {
            System.err.println("Warning: ChooseAction has no valid options, continuing.");
            controller.processNextAction();
            return;
        }
        
        // Show choice dialog
        VisualNovelGUI gui = controller.getGUI();
        ChoiceDialog dialog = new ChoiceDialog(gui, context, message, choiceOptions, controller);
        Object selectedOnPress = dialog.showDialog();
        
        if (selectedOnPress != null) {
            // Use unified ActionHandler to execute the action sequence
            // onPress can be a String (file path), Map (single action), or List (array of actions)
            String basePath = controller.getCurrentScenarioBasePath();
            ActionHandler.executeActionSequence(selectedOnPress, controller, basePath);
        } else {
            // Dialog was closed without selection, continue with next action
            controller.processNextAction();
        }
    }
    
    @Override
    public boolean requiresUserInput() {
        return true; // User must make a choice
    }
    
    @Override
    public boolean shouldContinueImmediately() {
        return false; // Wait for user to make a choice
    }
}

