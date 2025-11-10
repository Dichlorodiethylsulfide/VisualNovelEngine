package com.visualnovel.gui;

import com.visualnovel.util.ScenarioDiscovery.ScenarioInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog that displays available scenarios and lets the user choose which one to load.
 */
public class ScenarioSelectionDialog extends JDialog {
    private String selectedScenarioPath;
    
    public ScenarioSelectionDialog(JFrame parent, List<ScenarioInfo> scenarios) {
        super(parent, true); // Modal dialog
        setTitle("Select Scenario");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        
        selectedScenarioPath = null;
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title label
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>" +
            "<b>Multiple scenarios found</b><br>" +
            "Please select which scenario to load:</div></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create scrollable panel for scenario buttons
        JPanel scenariosPanel = new JPanel();
        scenariosPanel.setLayout(new BoxLayout(scenariosPanel, BoxLayout.Y_AXIS));
        scenariosPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        for (ScenarioInfo scenario : scenarios) {
            JButton button = createScenarioButton(scenario);
            button.addActionListener(e -> {
                selectedScenarioPath = scenario.getFilePath();
                dispose();
            });
            
            scenariosPanel.add(button);
            scenariosPanel.add(Box.createVerticalStrut(10));
        }
        
        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(scenariosPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Cancel button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            selectedScenarioPath = null;
            dispose();
        });
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(parent);
        setResizable(true);
    }
    
    /**
     * Creates a button for a scenario with its metadata displayed.
     */
    private JButton createScenarioButton(ScenarioInfo scenario) {
        // Build button text with metadata
        StringBuilder buttonText = new StringBuilder("<html><div style='text-align: left; padding: 5px;'>");
        buttonText.append("<b>").append(escapeHtml(scenario.getName())).append("</b>");
        
        if (scenario.getDescription() != null && !scenario.getDescription().isEmpty()) {
            String description = scenario.getDescription();
            // Truncate long descriptions
            if (description.length() > 100) {
                description = description.substring(0, 97) + "...";
            }
            buttonText.append("<br><i>").append(escapeHtml(description)).append("</i>");
        }
        
        buttonText.append("<br><small>");
        buttonText.append("Folder: ").append(escapeHtml(scenario.getFolderName()));
        
        if (scenario.getAuthor() != null && !scenario.getAuthor().isEmpty()) {
            buttonText.append(" | Author: ").append(escapeHtml(scenario.getAuthor()));
        }
        
        if (scenario.getVersion() != null && !scenario.getVersion().isEmpty()) {
            buttonText.append(" | Version: ").append(escapeHtml(scenario.getVersion()));
        }
        
        if (scenario.isEntryPoint()) {
            buttonText.append(" | [Entry Point]");
        }
        
        buttonText.append("</small></div></html>");
        
        JButton button = new JButton(buttonText.toString());
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(220, 240, 255));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(240, 240, 240));
            }
        });
        
        return button;
    }
    
    /**
     * Escapes HTML special characters for safe display.
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Shows the dialog and returns the selected scenario path.
     * 
     * @return The path to the selected scenario file, or null if cancelled
     */
    public String showDialog() {
        setVisible(true);
        return selectedScenarioPath;
    }
}

