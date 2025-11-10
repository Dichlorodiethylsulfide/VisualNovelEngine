package com.visualnovel.gui;

import com.visualnovel.model.battle.BattleCharacter;
import com.visualnovel.model.battle.Move;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Dialog that displays move selection options for a character in battle.
 * Shows move name, description, energy cost, and disables moves that are too expensive.
 */
public class MoveSelectionDialog extends JDialog {
    private String selectedMoveName;
    
    public MoveSelectionDialog(JFrame parent, BattleCharacter character) {
        super(parent, true); // Modal dialog
        setUndecorated(true); // No window decorations for overlay effect
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Prevent closing without selection
        setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
        selectedMoveName = null;
        
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
        
        // Message label with character name
        String characterName = character != null ? character.getName() : "Character";
        String message = "<html><div style='text-align: center;'>" +
            "<b>" + characterName + "</b><br>" +
            "Choose a move:</div></html>";
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(messageLabel, BorderLayout.NORTH);
        
        // Options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Get moves from character
        Map<String, Move> moves = character != null ? character.getMoves() : null;
        List<String> affordableMoves = character != null ? character.getAffordableMoves() : null;
        
        if (moves != null && !moves.isEmpty()) {
            for (Map.Entry<String, Move> entry : moves.entrySet()) {
                String moveName = entry.getKey();
                Move move = entry.getValue();
                
                if (move == null) continue;
                
                // Create button text with move info
                String buttonText = "<html><div style='text-align: center;'>" +
                    "<b>" + moveName + "</b><br>" +
                    move.getDescription() + "<br>" +
                    "Energy Cost: " + move.getEnergyCost();
                
                // Add move type info
                if (move.getType() != null) {
                    buttonText += " | Type: " + move.getType();
                }
                
                buttonText += "</div></html>";
                
                JButton button = new JButton(buttonText);
                button.setFont(new Font("Arial", Font.PLAIN, 14));
                button.setPreferredSize(new Dimension(350, 80));
                button.setMaximumSize(new Dimension(350, 80));
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.setFocusPainted(false);
                
                // Check if move is affordable
                boolean isAffordable = affordableMoves != null && affordableMoves.contains(moveName);
                
                if (isAffordable) {
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
                } else {
                    button.setBackground(new Color(40, 40, 40));
                    button.setForeground(new Color(150, 150, 150));
                    button.setEnabled(false);
                }
                
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 1),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
                
                // Action listener
                if (isAffordable) {
                    button.addActionListener(e -> {
                        selectedMoveName = moveName;
                        dispose();
                    });
                }
                
                optionsPanel.add(button);
                optionsPanel.add(Box.createVerticalStrut(10));
            }
        } else {
            // No moves available
            JLabel noMovesLabel = new JLabel("<html><div style='text-align: center;'>No moves available</div></html>");
            noMovesLabel.setForeground(Color.WHITE);
            noMovesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noMovesLabel.setHorizontalAlignment(SwingConstants.CENTER);
            optionsPanel.add(noMovesLabel);
        }
        
        contentPanel.add(optionsPanel, BorderLayout.CENTER);
        
        // Add content panel directly to main panel (centered)
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Set content pane
        setContentPane(mainPanel);
        
        // Calculate dialog size based on content
        int dialogWidth = 450;
        int moveCount = moves != null ? moves.size() : 0;
        int dialogHeight = Math.max(300, 150 + moveCount * 100);
        
        // Set size to just fit the content
        setSize(dialogWidth, dialogHeight);
        
        // Center the dialog on the parent window
        setLocationRelativeTo(parent);
        
        // Make sure dialog is on top and visible
        setAlwaysOnTop(true);
    }
    
    /**
     * Shows the dialog and returns the selected move name.
     * 
     * @return The selected move name, or null if dialog was closed or no move selected
     */
    public String showDialog() {
        setVisible(true);
        return selectedMoveName;
    }
}

