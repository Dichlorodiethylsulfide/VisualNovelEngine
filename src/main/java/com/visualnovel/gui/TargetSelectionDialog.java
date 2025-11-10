package com.visualnovel.gui;

import com.visualnovel.model.battle.BattleCharacter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog that displays target selection options for a character in battle.
 * Shows enemy targets with their stats (health, defense, attack, speed, energy).
 */
public class TargetSelectionDialog extends JDialog {
    private BattleCharacter selectedTarget;
    
    public TargetSelectionDialog(JFrame parent, List<BattleCharacter> targets) {
        super(parent, true); // Modal dialog
        setUndecorated(true); // No window decorations for overlay effect
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Prevent closing without selection
        setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
        selectedTarget = null;
        
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
        String message = "<html><div style='text-align: center;'>" +
            "<b>Select Target</b><br>" +
            "Choose an enemy to attack:</div></html>";
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(messageLabel, BorderLayout.NORTH);
        
        // Options panel with scroll pane
        JPanel optionsPanel = new JPanel();
        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        if (targets != null && !targets.isEmpty()) {
            for (BattleCharacter target : targets) {
                if (target == null || !target.isAlive()) {
                    continue;
                }
                
                // Create button with target info
                JButton button = createTargetButton(target);
                
                // Action listener
                button.addActionListener(e -> {
                    selectedTarget = target;
                    dispose();
                });
                
                optionsPanel.add(button);
                optionsPanel.add(Box.createVerticalStrut(10));
            }
        } else {
            // No targets available
            JLabel noTargetsLabel = new JLabel("<html><div style='text-align: center;'>No targets available</div></html>");
            noTargetsLabel.setForeground(Color.WHITE);
            noTargetsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noTargetsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            optionsPanel.add(noTargetsLabel);
        }
        
        // Wrap in scroll pane if needed
        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add content panel directly to main panel (centered)
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Set content pane
        setContentPane(mainPanel);
        
        // Calculate dialog size based on content
        // Account for padding (20px on each side = 40px total) and border (2px on each side = 4px total)
        int dialogWidth = 520; // Fixed width that accommodates button width (480px) + padding + border
        int targetCount = targets != null ? targets.size() : 0;
        // Each button is 110px tall + 10px spacing = 120px per target
        // Header is ~80px, padding is 40px top/bottom = 120px
        int minHeight = 300;
        int maxHeight = 500;
        int calculatedHeight = 120 + targetCount * 120;
        int dialogHeight = Math.min(maxHeight, Math.max(minHeight, calculatedHeight));
        
        // Set size to just fit the content
        setSize(dialogWidth, dialogHeight);
        
        // Center the dialog on the parent window
        setLocationRelativeTo(parent);
        
        // Make sure dialog is on top and visible
        setAlwaysOnTop(true);
    }
    
    /**
     * Creates a button displaying target character information.
     * 
     * @param target The target character
     * @return A styled button with target stats
     */
    private JButton createTargetButton(BattleCharacter target) {
        String characterName = target != null ? target.getName() : "Unknown";
        int currentHealth = target != null ? target.getCurrentHealth() : 0;
        int maxHealth = target != null ? target.getMaxHealth() : 0;
        int defense = target != null ? target.getDefense() : 0;
        int attack = target != null ? target.getAttack() : 0;
        int speed = target != null ? target.getSpeed() : 0;
        int currentEnergy = target != null ? target.getCurrentEnergy() : 0;
        int maxEnergy = target != null ? target.getMaxEnergy() : 100;
        
        // Calculate health percentage for color
        double healthPercent = maxHealth > 0 ? (double) currentHealth / maxHealth : 0.0;
        String healthColor;
        if (healthPercent > 0.6) {
            healthColor = "#00C800"; // Green
        } else if (healthPercent > 0.3) {
            healthColor = "#FFC800"; // Yellow
        } else {
            healthColor = "#C80000"; // Red
        }
        
        // Create button text with target stats
        // Use a more compact layout that fits within the dialog
        String buttonText = "<html><div style='text-align: left; padding: 2px;'>" +
            "<b style='font-size: 14px;'>" + characterName + "</b><br>" +
            "<table style='width: 100%; margin-top: 3px; font-size: 11px;'>" +
            "<tr><td style='padding-right: 8px;'>Health:</td><td><span style='color: " + healthColor + ";'>" + 
            currentHealth + " / " + maxHealth + "</span></td>" +
            "<td style='padding-left: 12px; padding-right: 8px;'>Defense:</td><td>" + defense + "</td></tr>" +
            "<tr><td>Attack:</td><td>" + attack + "</td>" +
            "<td style='padding-left: 12px;'>Speed:</td><td>" + speed + "</td></tr>" +
            "<tr><td>Energy:</td><td colspan='3'>" + currentEnergy + " / " + maxEnergy + "</td></tr>" +
            "</table>" +
            // Visual health bar
            "<div style='margin-top: 4px; background-color: #3C3C3C; border: 1px solid #666; height: 10px; border-radius: 2px;'>" +
            "<div style='background-color: " + healthColor + "; height: 100%; width: " + 
            (int)(healthPercent * 100) + "%; border-radius: 2px;'></div>" +
            "</div>" +
            "</div></html>";
        
        JButton button = new JButton(buttonText);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        // Button width: dialog width (520) - content padding (40) - border (4) = 476px
        // But account for scrollbar if present, so use 480px to be safe
        button.setPreferredSize(new Dimension(480, 110));
        button.setMaximumSize(new Dimension(480, 110));
        button.setMinimumSize(new Dimension(480, 110));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(Color.WHITE);
        button.setEnabled(true);
        
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
        
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        return button;
    }
    
    /**
     * Shows the dialog and returns the selected target character.
     * 
     * @return The selected target character, or null if dialog was closed or no target selected
     */
    public BattleCharacter showDialog() {
        setVisible(true);
        return selectedTarget;
    }
}

