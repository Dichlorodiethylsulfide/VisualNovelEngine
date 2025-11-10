package com.visualnovel.gui;

import com.visualnovel.GameController;
import com.visualnovel.model.ChoiceOption;
import com.visualnovel.util.RequirementEvaluator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog that displays choice options as buttons in the center of the screen.
 */
public class ChoiceDialog extends JDialog {
    private Object selectedOnPress;
    
    public ChoiceDialog(JFrame parent, String context, String message, List<ChoiceOption> options, GameController controller) {
        super(parent, true); // Modal dialog
        setUndecorated(true); // No window decorations for overlay effect
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Prevent closing without selection
        setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
        selectedOnPress = null;
        
        // Create main panel (no overlay background - game will be visible behind)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        
        // Create content panel with rounded background
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(new Color(0, 0, 0, 240));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Message label
        if (message != null && !message.isEmpty()) {
            JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + 
                message.replace("\n", "<br>") + "</div></html>");
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
            messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            contentPanel.add(messageLabel, BorderLayout.NORTH);
        }
        
        // Options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        for (ChoiceOption option : options) {
            JButton button = new JButton(option.getText());
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.setPreferredSize(new Dimension(300, 50));
            button.setMaximumSize(new Dimension(300, 50));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setFocusPainted(false);
            
            // Evaluate requirement if present
            boolean requirementMet = true;
            String requires = option.getRequires();
            if (requires != null && !requires.trim().isEmpty()) {
                requirementMet = RequirementEvaluator.evaluate(requires, controller);
            }
            
            if (requirementMet) {
                // Requirement met or not present - enable button
                button.setEnabled(true);
                button.setBackground(new Color(60, 60, 60));
                button.setForeground(Color.BLACK);
                
                // Hover effect
                button.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        button.setBackground(new Color(80, 80, 80));
                    }
                    
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        button.setBackground(new Color(60, 60, 60));
                    }
                });
                
                // Action listener
                button.addActionListener(e -> {
                    selectedOnPress = option.getOnPress();
                    dispose();
                });
            } else {
                // Requirement not met - disable and grey out button
                button.setEnabled(false);
                button.setBackground(new Color(40, 40, 40));
                button.setForeground(new Color(120, 120, 120));
                
                // No hover effect or action listener for disabled buttons
            }
            
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, requirementMet ? 150 : 80), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
            ));
            
            optionsPanel.add(button);
            optionsPanel.add(Box.createVerticalStrut(10));
        }
        
        contentPanel.add(optionsPanel, BorderLayout.CENTER);
        
        // Add content panel directly to main panel (centered)
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Set content pane
        setContentPane(mainPanel);
        
        // Calculate dialog size based on content
        int dialogWidth = 400;
        int dialogHeight = Math.max(200, 100 + options.size() * 70);
        if (message != null && !message.isEmpty()) {
            dialogHeight += 60; // Add space for message
        }
        
        // Set size to just fit the content
        setSize(dialogWidth, dialogHeight);
        
        // Center the dialog on the parent window
        setLocationRelativeTo(parent);
        
        // Make sure dialog is on top and visible
        setAlwaysOnTop(true);
    }
    
    /**
     * Shows the dialog and returns the selected option's onPress value.
     * onPress can be either a String (file path) or a Map (raw JSON action).
     * 
     * @return The onPress value of the selected option (String or Map), or null if dialog was closed
     */
    public Object showDialog() {
        setVisible(true);
        return selectedOnPress;
    }
}

