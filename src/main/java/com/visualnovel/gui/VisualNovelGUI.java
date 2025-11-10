package com.visualnovel.gui;

import com.visualnovel.GameController;
import com.visualnovel.model.SpriteRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Main GUI window for the Visual Novel game.
 */
public class VisualNovelGUI extends JFrame {
    private GameController controller;
    private GamePanel gamePanel;
    private TextBoxPanel textBoxPanel;
    
    private static final int DEFAULT_WINDOW_WIDTH = 1280;
    private static final int DEFAULT_WINDOW_HEIGHT = 720;
    private static final int TEXT_BOX_HEIGHT = 120;
    private static final int BASE_WIDTH = 1280;
    
    private int currentWindowWidth = DEFAULT_WINDOW_WIDTH;
    private int currentWindowHeight = DEFAULT_WINDOW_HEIGHT;
    
    public VisualNovelGUI(GameController controller) {
        this.controller = controller;
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Visual Novel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(currentWindowWidth, currentWindowHeight);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Create main layout
        setLayout(new BorderLayout());
        
        // Create game panel for sprites
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);
        
        // Create text box panel at the bottom
        textBoxPanel = new TextBoxPanel();
        add(textBoxPanel, BorderLayout.SOUTH);
        
        // Add click listener to advance text
        gamePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                controller.onUserClick();
            }
        });
        
        // Add keyboard listener for inventory display
        setFocusable(true);
        gamePanel.setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_I) {
                    controller.showPlayerInventory();
                }
            }
        });
        gamePanel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_I) {
                    controller.showPlayerInventory();
                }
            }
        });
        
        setVisible(true);
    }
    
    /**
     * Resizes the window to match the background image dimensions.
     */
    private void resizeToBackground(BufferedImage backgroundImage) {
        if (backgroundImage == null) {
            return;
        }
        
        int bgWidth = backgroundImage.getWidth();
        int bgHeight = backgroundImage.getHeight();
        
        // Calculate new window size: background height + text box height
        int newHeight = bgHeight + TEXT_BOX_HEIGHT;
        int newWidth = bgWidth;
        
        // Ensure minimum size
        if (newWidth < 800) {
            double scale = 800.0 / newWidth;
            newWidth = 800;
            newHeight = (int)(newHeight * scale);
        }
        if (newHeight < 600) {
            double scale = 600.0 / newHeight;
            newHeight = 600;
            newWidth = (int)(newWidth * scale);
        }
        
        // Ensure it fits on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double maxWidth = screenSize.getWidth() * 0.95;
        double maxHeight = screenSize.getHeight() * 0.95;
        
        if (newWidth > maxWidth) {
            double scale = maxWidth / newWidth;
            newWidth = (int)maxWidth;
            newHeight = (int)(newHeight * scale);
        }
        if (newHeight > maxHeight) {
            double scale = maxHeight / newHeight;
            newHeight = (int)maxHeight;
            newWidth = (int)(newWidth * scale);
        }
        
        currentWindowWidth = newWidth;
        currentWindowHeight = newHeight;
        
        setSize(currentWindowWidth, currentWindowHeight);
        setLocationRelativeTo(null);
        
        // Calculate scale factor for text box
        double textBoxScale = (double) newWidth / BASE_WIDTH;
        textBoxPanel.setScaleFactor(textBoxScale);
        
        // Update panel sizes
        gamePanel.setPreferredSize(new Dimension(newWidth, bgHeight));
        textBoxPanel.setPreferredSize(new Dimension(newWidth, TEXT_BOX_HEIGHT));
        
        revalidate();
        repaint();
    }
    
    /**
     * Unified sprite rendering method that handles all sprite types.
     * 
     * @param request The sprite request containing all rendering parameters
     */
    public void renderSprite(SpriteRequest request) {
        if (request == null) {
            return;
        }
        
        // Handle window resizing for backgrounds
        if ("background".equals(request.getType()) && request.getImage() != null) {
            resizeToBackground(request.getImage());
        }
        
        gamePanel.renderSprite(request);
    }
    
    /**
     * Sets the background image and resizes the window to match.
     */
    public void setBackgroundImage(BufferedImage image) {
        gamePanel.setBackgroundImage(image);
        resizeToBackground(image);
    }
    
    /**
     * Sets the background image with animation support.
     * 
     * @param image The background image
     * @param animationType The type of animation (e.g., "fadeIn")
     * @param durationMs The duration of the animation in milliseconds
     * @param onComplete Callback when animation completes
     */
    public void setBackgroundImageWithAnimation(BufferedImage image, String animationType, 
                                               int durationMs, Runnable onComplete) {
        gamePanel.setBackgroundImageWithAnimation(image, animationType, durationMs, onComplete);
        resizeToBackground(image);
    }
    
    /**
     * Sets a character sprite at the specified position.
     * This is a convenience method that uses default layer (0) and offset (0).
     */
    public void setCharacterSprite(BufferedImage image, String position) {
        gamePanel.setCharacterSprite(image, position);
    }
    
    /**
     * Adds a sprite with the specified properties including layer and offset.
     * 
     * @param image The sprite image
     * @param position The position ("Left", "Right", "Center", "Back")
     * @param depth The depth (z-order: higher values are drawn on top)
     * @param offset The horizontal offset in pixels
     */
    public void addSprite(BufferedImage image, String position, int depth, int offset) {
        gamePanel.addSprite(image, position, depth, offset);
    }
    
    /**
     * Adds a sprite with the specified properties including layer, offset, and character ID.
     * 
     * @param image The sprite image
     * @param position The position ("Left", "Right", "Center", "Back")
     * @param depth The depth (z-order: higher values are drawn on top)
     * @param offset The horizontal offset in pixels
     * @param characterId The character ID associated with this sprite (null if not a character sprite)
     */
    public void addSprite(BufferedImage image, String position, int depth, int offset, String characterId) {
        gamePanel.addSprite(image, position, depth, offset, characterId);
    }
    
    /**
     * Adds a sprite with animation support.
     * 
     * @param image The sprite image
     * @param position The position ("Left", "Right", "Center", "Back")
     * @param depth The depth (z-order: higher values are drawn on top)
     * @param offset The horizontal offset in pixels
     * @param animationType The type of animation (e.g., "fadeIn")
     * @param durationMs The duration of the animation in milliseconds
     * @param onComplete Callback when animation completes
     */
    public void addSpriteWithAnimation(BufferedImage image, String position, int depth, int offset,
                                      String animationType, int durationMs, Runnable onComplete) {
        gamePanel.addSpriteWithAnimation(image, position, depth, offset, animationType, durationMs, onComplete);
    }
    
    /**
     * Adds a sprite with animation support and character ID.
     * 
     * @param image The sprite image
     * @param position The position ("Left", "Right", "Center", "Back")
     * @param depth The depth (z-order: higher values are drawn on top)
     * @param offset The horizontal offset in pixels
     * @param animationType The type of animation (e.g., "fadeIn")
     * @param durationMs The duration of the animation in milliseconds
     * @param onComplete Callback when animation completes
     * @param characterId The character ID associated with this sprite (null if not a character sprite)
     */
    public void addSpriteWithAnimation(BufferedImage image, String position, int depth, int offset,
                                      String animationType, int durationMs, Runnable onComplete, String characterId) {
        gamePanel.addSpriteWithAnimation(image, position, depth, offset, animationType, durationMs, onComplete, characterId);
    }
    
    /**
     * Shows a message in the text box.
     */
    public void showMessage(String context, String text) {
        textBoxPanel.showMessage(context, text);
    }
    
    /**
     * Clears all sprites from the screen.
     */
    public void clearSprites() {
        gamePanel.clearSprites();
    }
    
    /**
     * Shows a lose/win screen.
     */
    public void showScreen(BufferedImage image) {
        gamePanel.showScreen(image);
    }
    
    /**
     * Shows a lose/win screen with animation support.
     * 
     * @param image The screen image
     * @param animationType The type of animation (e.g., "fadeIn")
     * @param durationMs The duration of the animation in milliseconds
     * @param onComplete Callback when animation completes
     */
    public void showScreenWithAnimation(BufferedImage image, String animationType, 
                                       int durationMs, Runnable onComplete) {
        gamePanel.showScreenWithAnimation(image, animationType, durationMs, onComplete);
    }
    
    /**
     * Shows an attack image at center screen.
     * Used during battles to display attack animations.
     * 
     * @param image The attack image to display
     */
    public void showAttackImage(BufferedImage image) {
        gamePanel.showAttackImage(image);
    }
    
    /**
     * Clears the center screen (attack/win/lose images).
     */
    public void clearScreen() {
        gamePanel.clearScreen();
    }
    
    /**
     * Gets a copy of the current sprites list.
     * 
     * @return A new list containing copies of all current sprites
     */
    public java.util.List<com.visualnovel.model.SpriteData> getSpritesCopy() {
        return gamePanel.getSpritesCopy();
    }
    
    /**
     * Restores sprites from a list.
     * 
     * @param spritesToRestore The list of sprites to restore
     */
    public void restoreSprites(java.util.List<com.visualnovel.model.SpriteData> spritesToRestore) {
        gamePanel.restoreSprites(spritesToRestore);
    }
    
    /**
     * Hides sprites for a specific character ID.
     * 
     * @param characterId The character ID whose sprites should be hidden
     */
    public void hideSpritesByCharacterId(String characterId) {
        gamePanel.hideSpritesByCharacterId(characterId);
    }
    
    /**
     * Hides excess sprites for a specific character ID, keeping only the specified count.
     * 
     * @param characterId The character ID whose sprites should be hidden
     * @param keepCount The number of sprites to keep (0 means hide all)
     */
    public void hideExcessSpritesByCharacterId(String characterId, int keepCount) {
        gamePanel.hideExcessSpritesByCharacterId(characterId, keepCount);
    }
    
    /**
     * Hides all sprites at a specific position.
     * 
     * @param position The position to clear ("Left", "Right", "Center", "Back")
     */
    public void hideSpritesAtPosition(String position) {
        gamePanel.hideSpritesAtPosition(position);
    }
    
    /**
     * Counts the number of visible sprites for a specific character ID.
     * 
     * @param characterId The character ID to count sprites for
     * @return The number of visible sprites for the character
     */
    public int countVisibleSpritesByCharacterId(String characterId) {
        return gamePanel.countVisibleSpritesByCharacterId(characterId);
    }
    
    /**
     * Updates the health bar display for a character.
     * 
     * @param characterId The ID of the character
     * @param teamId The ID of the team the character belongs to
     * @param characterName The name of the character
     * @param currentHealth The current health value
     * @param maxHealth The maximum health value
     * @param currentEnergy The current energy value
     * @param maxEnergy The maximum energy value
     */
    public void updateHealthBar(String characterId, String teamId, String characterName, int currentHealth, int maxHealth, int currentEnergy, int maxEnergy) {
        gamePanel.updateHealthBar(characterId, teamId, characterName, currentHealth, maxHealth, currentEnergy, maxEnergy);
    }
    
    /**
     * Clears all health bar displays.
     */
    public void clearHealthBars() {
        gamePanel.clearHealthBars();
    }
    
    /**
     * Clears the health bar for a specific character.
     * 
     * @param characterId The ID of the character
     */
    public void clearHealthBar(String characterId) {
        gamePanel.clearHealthBar(characterId);
    }
    
    /**
     * Clears the text box.
     */
    public void clearTextBox() {
        textBoxPanel.clear();
    }
    
    /**
     * Repaints the GUI.
     */
    public void refresh() {
        gamePanel.repaint();
        textBoxPanel.repaint();
    }
    
    /**
     * Skips the current animation if one is running.
     * This allows players to skip through animated actions quickly.
     */
    public void skipCurrentAnimation() {
        gamePanel.skipCurrentAnimation();
    }
    
    /**
     * Checks if an animation is currently running.
     * 
     * @return true if an animation is running, false otherwise
     */
    public boolean isAnimationRunning() {
        return gamePanel.isAnimationRunning();
    }
    
    /**
     * Shows the inventory dialog for a character.
     * 
     * @param inventory The inventory map (item name -> InventoryItem) to display, or null if empty
     */
    public void showInventoryDialog(java.util.Map<String, com.visualnovel.model.InventoryItem> inventory) {
        InventoryDialog dialog = new InventoryDialog(this, inventory);
        dialog.showDialog();
    }
}

