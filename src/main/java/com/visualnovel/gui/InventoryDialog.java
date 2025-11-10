package com.visualnovel.gui;

import com.visualnovel.model.InventoryItem;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Dialog that displays a character's inventory items.
 * Shows item name, description, type, and quantity.
 */
public class InventoryDialog extends JDialog {
    
    public InventoryDialog(JFrame parent, Map<String, InventoryItem> inventory) {
        super(parent, false); // Non-modal dialog
        setUndecorated(true); // No window decorations for overlay effect
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
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
        
        // Title label
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>" +
            "<b>Inventory</b></div></html>");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Items panel
        JPanel itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        if (inventory != null && !inventory.isEmpty()) {
            // Display items
            for (Map.Entry<String, InventoryItem> entry : inventory.entrySet()) {
                InventoryItem item = entry.getValue();
                
                // Create item panel
                JPanel itemPanel = new JPanel(new BorderLayout(10, 5));
                itemPanel.setOpaque(false);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                itemPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
                
                // Item name and quantity
                String nameText = "<html><b>" + escapeHtml(item.getName()) + "</b>";
                if (item.getQuantity() > 1) {
                    nameText += " <span style='color: #cccccc;'>(x" + item.getQuantity() + ")</span>";
                }
                nameText += "</html>";
                JLabel nameLabel = new JLabel(nameText);
                nameLabel.setForeground(Color.WHITE);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
                itemPanel.add(nameLabel, BorderLayout.NORTH);
                
                // Item description and type
                JPanel detailsPanel = new JPanel();
                detailsPanel.setOpaque(false);
                detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
                
                if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                    JLabel descLabel = new JLabel("<html><div style='color: #cccccc;'>" +
                        escapeHtml(item.getDescription()) + "</div></html>");
                    descLabel.setForeground(new Color(200, 200, 200));
                    descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                    detailsPanel.add(descLabel);
                }
                
                if (item.getType() != null && !item.getType().isEmpty()) {
                    JLabel typeLabel = new JLabel("<html><div style='color: #aaaaaa; font-style: italic;'>Type: " +
                        escapeHtml(item.getType()) + "</div></html>");
                    typeLabel.setForeground(new Color(170, 170, 170));
                    typeLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                    detailsPanel.add(typeLabel);
                }
                
                itemPanel.add(detailsPanel, BorderLayout.CENTER);
                
                itemsPanel.add(itemPanel);
                itemsPanel.add(Box.createVerticalStrut(10));
            }
        } else {
            // Empty inventory message
            JLabel emptyLabel = new JLabel("<html><div style='text-align: center; color: #aaaaaa;'>" +
                "Inventory is empty</div></html>");
            emptyLabel.setForeground(new Color(170, 170, 170));
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            itemsPanel.add(emptyLabel);
        }
        
        // Scroll pane for items
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(500, 400));
        scrollPane.setMaximumSize(new Dimension(500, 400));
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 16));
        closeButton.setPreferredSize(new Dimension(100, 40));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.setBackground(new Color(60, 60, 60));
        closeButton.setForeground(Color.BLACK);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        closeButton.setFocusPainted(false);
        
        // Hover effect
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeButton.setBackground(new Color(80, 80, 80));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeButton.setBackground(new Color(60, 60, 60));
            }
        });
        
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add content panel directly to main panel (centered)
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Set content pane
        setContentPane(mainPanel);
        
        // Calculate dialog size based on content
        int dialogWidth = 550;
        int dialogHeight = 500;
        
        // Set size
        setSize(dialogWidth, dialogHeight);
        
        // Center the dialog on the parent window
        setLocationRelativeTo(parent);
        
        // Make sure dialog is on top and visible
        setAlwaysOnTop(true);
    }
    
    /**
     * Escapes HTML special characters in a string.
     * 
     * @param text The text to escape
     * @return The escaped text
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
     * Shows the dialog.
     */
    public void showDialog() {
        setVisible(true);
    }
}

