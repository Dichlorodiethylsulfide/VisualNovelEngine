package com.visualnovel;

import com.visualnovel.gui.ScenarioSelectionDialog;
import com.visualnovel.gui.VisualNovelGUI;
import com.visualnovel.util.ScenarioDiscovery;
import com.visualnovel.util.ScenarioDiscovery.ScenarioInfo;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

/**
 * Main application entry point for the Visual Novel game.
 */
public class VisualNovelApp {
    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread safety
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            GameController controller = new GameController();
            VisualNovelGUI gui = new VisualNovelGUI(controller);
            controller.setGUI(gui);
            
            // Discover available scenarios
            try {
                List<ScenarioInfo> scenarios = ScenarioDiscovery.discoverScenarios();
                
                String scenarioPath;
                if (scenarios.isEmpty()) {
                    // No scenarios found, show error
                    JOptionPane.showMessageDialog(null,
                        "No scenario.json files found in assets/json directories.",
                        "No Scenarios Found",
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                    return;
                } else if (scenarios.size() == 1) {
                    // Only one scenario, load it directly
                    scenarioPath = scenarios.get(0).getFilePath();
                } else {
                    // Multiple scenarios, show selection dialog
                    ScenarioSelectionDialog dialog = new ScenarioSelectionDialog(gui, scenarios);
                    scenarioPath = dialog.showDialog();
                    
                    if (scenarioPath == null) {
                        // User cancelled
                        System.exit(0);
                        return;
                    }
                }
                
                // Load and start the scenario (require entry_point for starting scenarios)
                controller.loadScenario(scenarioPath, true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                    "Error discovering scenarios: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}

