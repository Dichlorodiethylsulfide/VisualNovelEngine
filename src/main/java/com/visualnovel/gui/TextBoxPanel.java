package com.visualnovel.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that displays the text box with messages and character names.
 */
public class TextBoxPanel extends JPanel {
    private JLabel contextLabel;
    private JTextArea textArea;
    private static final int TEXT_BOX_HEIGHT = 120;
    private static final int BASE_WIDTH = 1280;
    
    private double scaleFactor = 1.0;
    
    public TextBoxPanel() {
        setPreferredSize(new Dimension(BASE_WIDTH, TEXT_BOX_HEIGHT));
        setOpaque(true);
        setBackground(new Color(0, 0, 0, 220));
        setLayout(new BorderLayout());
        
        // Context label (character name/narrator)
        contextLabel = new JLabel(" ");
        contextLabel.setForeground(Color.WHITE);
        contextLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        add(contextLabel, BorderLayout.NORTH);
        
        // Text area for message
        textArea = new JTextArea();
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(0, 0, 0, 0));
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        updateFonts();
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Updates fonts based on current scale factor.
     */
    private void updateFonts() {
        int contextFontSize = (int)(18 * scaleFactor);
        int textFontSize = (int)(16 * scaleFactor);
        
        contextLabel.setFont(new Font("Arial", Font.BOLD, Math.max(12, contextFontSize)));
        textArea.setFont(new Font("Arial", Font.PLAIN, Math.max(10, textFontSize)));
    }
    
    /**
     * Updates the scale factor and adjusts UI accordingly.
     */
    public void setScaleFactor(double scale) {
        this.scaleFactor = scale;
        updateFonts();
        
        // Update borders with scaled padding
        int padding = (int)(20 * scale);
        int topPadding = (int)(10 * scale);
        int bottomPadding = (int)(10 * scale);
        
        contextLabel.setBorder(BorderFactory.createEmptyBorder(
            topPadding, padding, topPadding / 2, padding));
        textArea.setBorder(BorderFactory.createEmptyBorder(
            topPadding / 2, padding, bottomPadding, padding));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw semi-transparent background to ensure it covers what's behind
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
    public void showMessage(String context, String text) {
        // Replace the previous message with the new one
        contextLabel.setText(context);
        textArea.setText(text);
        // Scroll to top to show the message
        textArea.setCaretPosition(0);
        repaint();
    }
    
    public void clear() {
        contextLabel.setText(" ");
        textArea.setText("");
        repaint();
    }
}

