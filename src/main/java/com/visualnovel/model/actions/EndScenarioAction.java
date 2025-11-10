package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.Timing;

import java.util.Map;

/**
 * Action that ends the current scenario.
 * If the "restart" parameter is set to "allowed", the scenario will restart from the beginning.
 * Otherwise, the application will quit.
 * Respects timing: if timing is "Interaction", waits for user input before ending.
 */
public class EndScenarioAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "EndScenarioAction"
     */
    public static String getName() {
        return "EndScenarioAction";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        Map<String, Object> params = action.getParameters();
        String restart = params != null ? (String) params.get("restart") : null;
        
        // Check if timing requires user interaction
        Timing timing = action.getTiming();
        boolean requiresInteraction = timing != null && timing.requiresInteraction();
        
        if (requiresInteraction) {
            // Store the restart/quit logic to execute when user clicks
            controller.setEndScenarioActionPending(restart);
            // Wait for user input - ActionFlowController will handle this via requiresUserInput()
        } else {
            // Execute immediately
            executeEndScenario(restart, controller);
        }
    }
    
    /**
     * Executes the end scenario logic (restart or quit).
     * 
     * @param restart The restart parameter value
     * @param controller The game controller
     */
    private void executeEndScenario(String restart, GameController controller) {
        if ("allowed".equals(restart)) {
            // Restart the scenario
            controller.restartScenario();
        } else {
            // Quit the application
            System.out.println("Scenario ended. Quitting application.");
            System.exit(0);
        }
    }
    
    @Override
    public boolean requiresUserInput() {
        // We check timing in execute() and set waitingForUserInput there
        // This method is called by ActionFlowController, but we handle timing in execute()
        return false;
    }
    
    @Override
    public boolean shouldContinueImmediately() {
        return false; // Scenario has ended
    }
}

