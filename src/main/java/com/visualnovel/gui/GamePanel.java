package com.visualnovel.gui;

import com.visualnovel.model.SpriteData;
import com.visualnovel.model.SpriteRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Panel that displays the game sprites (background, characters, etc.).
 * Supports layering and offset for multiple sprites.
 */
public class GamePanel extends JPanel {
    private BufferedImage backgroundImage;
    private float backgroundAlpha; // Alpha for background animation
    private float backgroundFlashIntensity; // Flash intensity for background animation (0.0 = normal, 1.0 = completely white)
    private List<SpriteData> sprites; // List of sprites with layering support
    private BufferedImage centerScreen; // For lose/win screens
    private float screenAlpha; // Alpha for screen animation
    private float screenFlashIntensity; // Flash intensity for screen animation (0.0 = normal, 1.0 = completely white)
    
    // Health bar display for battle (one per character)
    private static class HealthBarData {
        String teamId;
        String characterName;
        int currentHealth;
        int maxHealth;
        int currentEnergy;
        int maxEnergy;
        boolean visible;
        
        HealthBarData() {
            this.visible = false;
            this.teamId = null;
            this.characterName = null;
            this.currentHealth = 0;
            this.maxHealth = 0;
            this.currentEnergy = 0;
            this.maxEnergy = 0;
        }
    }
    
    private Map<String, HealthBarData> characterHealthBars; // Map of character ID to health bar data
    
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    
    // Animation tracking for skipping animations
    private Timer currentAnimationTimer;
    private Runnable currentAnimationOnComplete;
    private Consumer<Float> currentAnimationAlphaUpdater;
    private float currentAnimationFinalValue; // Final value for the animation (1.0f for fadeIn, 0.0f for flash)
    
    public GamePanel() {
        setPreferredSize(new Dimension(1280, 600));
        setBackground(Color.BLACK);
        sprites = new ArrayList<>();
        backgroundAlpha = 1.0f;
        backgroundFlashIntensity = 0.0f;
        screenAlpha = 1.0f;
        screenFlashIntensity = 0.0f;
        characterHealthBars = new HashMap<>();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for better image quality
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate scale factors based on background image
        if (backgroundImage != null) {
            scaleX = (double) getWidth() / backgroundImage.getWidth();
            scaleY = (double) getHeight() / backgroundImage.getHeight();
        }
        
        // Draw background - scale to fit panel
        if (backgroundImage != null) {
            // Apply alpha for animations
            if (backgroundAlpha < 1.0f) {
                AlphaComposite alphaComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, backgroundAlpha);
                g2d.setComposite(alphaComposite);
            }
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
            // Apply flash effect if needed
            if (backgroundFlashIntensity > 0.0f) {
                BufferedImage whiteTinted = createWhiteTintedImage(backgroundImage);
                AlphaComposite flashComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, backgroundFlashIntensity);
                g2d.setComposite(flashComposite);
                g2d.drawImage(whiteTinted, 0, 0, getWidth(), getHeight(), null);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            // Reset composite
            if (backgroundAlpha < 1.0f) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
        
        // Draw sprites sorted by depth (lower depths first)
        List<SpriteData> sortedSprites = new ArrayList<>(sprites);
        sortedSprites.sort(Comparator.comparingInt(SpriteData::getDepth));
        
        for (SpriteData sprite : sortedSprites) {
            if (sprite.getImage() == null) continue;
            
            BufferedImage img = sprite.getImage();
            int scaledWidth = (int)(img.getWidth() * scaleX);
            int scaledHeight = (int)(img.getHeight() * scaleY);
            
            int x, y;
            String position = sprite.getPosition();
            
            if (position.equals("Left")) {
                x = (int)(50 * scaleX) + (int)(sprite.getOffset() * scaleX);
                // Position sprite at the bottom of the panel (sitting on top of text box)
                y = getHeight() - scaledHeight;
            } else if (position.equals("Right")) {
                x = getWidth() - scaledWidth - (int)(50 * scaleX) + (int)(sprite.getOffset() * scaleX);
                // Position sprite at the bottom of the panel (sitting on top of text box)
                y = getHeight() - scaledHeight;
            } else if (position.equals("Center")) {
                x = (getWidth() - scaledWidth) / 2 + (int)(sprite.getOffset() * scaleX);
                y = (getHeight() - scaledHeight) / 2;
            } else {
                // Default to left if position is unknown
                x = (int)(50 * scaleX) + (int)(sprite.getOffset() * scaleX);
                // Position sprite at the bottom of the panel (sitting on top of text box)
                y = getHeight() - scaledHeight;
            }
            
            // Apply alpha/opacity for animations
            if (sprite.getAlpha() < 1.0f) {
                AlphaComposite alphaComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, sprite.getAlpha());
                g2d.setComposite(alphaComposite);
            }
            
            g2d.drawImage(img, x, y, scaledWidth, scaledHeight, null);
            
            // Apply flash effect if needed
            if (sprite.getFlashIntensity() > 0.0f) {
                BufferedImage whiteTinted = createWhiteTintedImage(img);
                AlphaComposite flashComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, sprite.getFlashIntensity());
                g2d.setComposite(flashComposite);
                g2d.drawImage(whiteTinted, x, y, scaledWidth, scaledHeight, null);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            
            // Reset composite to fully opaque
            if (sprite.getAlpha() < 1.0f) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
        
        // Draw center screen (lose/win) - scale proportionally (always on top)
        if (centerScreen != null) {
            int scaledWidth = (int)(centerScreen.getWidth() * scaleX);
            int scaledHeight = (int)(centerScreen.getHeight() * scaleY);
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;
            
            // Apply alpha for animations
            if (screenAlpha < 1.0f) {
                AlphaComposite alphaComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, screenAlpha);
                g2d.setComposite(alphaComposite);
            }
            g2d.drawImage(centerScreen, x, y, scaledWidth, scaledHeight, null);
            // Apply flash effect if needed
            if (screenFlashIntensity > 0.0f) {
                BufferedImage whiteTinted = createWhiteTintedImage(centerScreen);
                AlphaComposite flashComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, screenFlashIntensity);
                g2d.setComposite(flashComposite);
                g2d.drawImage(whiteTinted, x, y, scaledWidth, scaledHeight, null);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            // Reset composite
            if (screenAlpha < 1.0f) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
        
        // Draw health bars (on top of everything, grouped by team)
        if (!characterHealthBars.isEmpty()) {
            // Group health bars by team ID
            Map<String, List<HealthBarData>> healthBarsByTeam = new HashMap<>();
            for (HealthBarData healthBar : characterHealthBars.values()) {
                if (healthBar != null && healthBar.visible && healthBar.characterName != null && healthBar.teamId != null) {
                    healthBarsByTeam.computeIfAbsent(healthBar.teamId, k -> new ArrayList<>()).add(healthBar);
                }
            }
            
            // Draw health bars grouped by team
            List<String> teamIds = new ArrayList<>(healthBarsByTeam.keySet());
            for (int teamIndex = 0; teamIndex < teamIds.size(); teamIndex++) {
                String teamId = teamIds.get(teamIndex);
                List<HealthBarData> teamHealthBars = healthBarsByTeam.get(teamId);
                
                // Position health bars: first team on left, second on right, etc.
                boolean isLeft = (teamIndex % 2 == 0);
                
                // Stack health bars vertically within each team's side
                for (int charIndex = 0; charIndex < teamHealthBars.size(); charIndex++) {
                    HealthBarData healthBar = teamHealthBars.get(charIndex);
                    drawHealthBar(g2d, healthBar, isLeft, teamIndex, charIndex);
                }
            }
        }
    }
    
    /**
     * Unified sprite rendering method that handles all sprite types.
     * 
     * @param request The sprite request containing all rendering parameters
     */
    public void renderSprite(SpriteRequest request) {
        if (request == null || request.getImage() == null) {
            return;
        }
        
        String type = request.getType();
        
        if ("background".equals(type)) {
            if (request.isAnimated()) {
                setBackgroundImageWithAnimation(
                    request.getImage(),
                    request.getAnimationType(),
                    request.getDurationMs(),
                    request.getOnComplete()
                );
            } else {
                setBackgroundImage(request.getImage());
                if (request.getOnComplete() != null) {
                    request.getOnComplete().run();
                }
            }
        } else if ("character".equals(type)) {
            if (request.isAnimated()) {
                addSpriteWithAnimation(
                    request.getImage(),
                    request.getPosition(),
                    request.getDepth(),
                    request.getOffset(),
                    request.getAnimationType(),
                    request.getDurationMs(),
                    request.getOnComplete(),
                    request.getCharacterId()
                );
            } else {
                if (request.getCharacterId() != null) {
                    addSprite(
                        request.getImage(),
                        request.getPosition(),
                        request.getDepth(),
                        request.getOffset(),
                        request.getCharacterId()
                    );
                } else {
                    addSprite(
                        request.getImage(),
                        request.getPosition(),
                        request.getDepth(),
                        request.getOffset()
                    );
                }
                if (request.getOnComplete() != null) {
                    request.getOnComplete().run();
                }
            }
        } else if ("screen".equals(type)) {
            if (request.isAnimated()) {
                showScreenWithAnimation(
                    request.getImage(),
                    request.getAnimationType(),
                    request.getDurationMs(),
                    request.getOnComplete()
                );
            } else {
                showScreen(request.getImage());
                if (request.getOnComplete() != null) {
                    request.getOnComplete().run();
                }
            }
        } else {
            System.err.println("Warning: Unknown sprite type: " + type);
        }
    }
    
    /**
     * Draws a health bar for a character.
     * 
     * @param g2d The graphics context
     * @param healthBar The health bar data
     * @param isLeft Whether to position on the left (true) or right (false)
     * @param teamIndex The index of the team (for horizontal positioning)
     * @param charIndex The index of the character within the team (for vertical stacking)
     */
    private void drawHealthBar(Graphics2D g2d, HealthBarData healthBar, boolean isLeft, int teamIndex, int charIndex) {
        int barWidth = 250;
        int barHeight = 100; // Increased height to accommodate energy bar
        int padding = 10;
        int verticalSpacing = 90; // Increased spacing to accommodate taller bars
        int y = 20 + charIndex * verticalSpacing; // Stack vertically within team
        int x = isLeft ? 20 : getWidth() - barWidth - 20;
        
        // Draw background panel (semi-transparent black)
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRoundRect(x, y, barWidth, barHeight, 8, 8);
        
        // Draw border
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, barWidth, barHeight, 8, 8);
        
        // Draw character name
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int nameY = y + padding + fm.getAscent();
        g2d.drawString(healthBar.characterName, x + padding, nameY);
        
        // Draw health text
        String healthText = String.format("%d / %d HP", healthBar.currentHealth, healthBar.maxHealth);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        fm = g2d.getFontMetrics();
        int healthTextY = nameY + fm.getHeight() + 5;
        g2d.drawString(healthText, x + padding, healthTextY);
        
        // Draw health bar background
        int barX = x + padding;
        int barY = healthTextY + 5;
        int barInnerWidth = barWidth - (padding * 2);
        int barInnerHeight = 12;
        
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRoundRect(barX, barY, barInnerWidth, barInnerHeight, 4, 4);
        
        // Draw health bar fill
        if (healthBar.maxHealth > 0) {
            double healthPercent = (double) healthBar.currentHealth / healthBar.maxHealth;
            int fillWidth = (int)(barInnerWidth * healthPercent);
            
            // Color based on health percentage
            Color healthColor;
            if (healthPercent > 0.6) {
                healthColor = new Color(0, 200, 0); // Green
            } else if (healthPercent > 0.3) {
                healthColor = new Color(255, 200, 0); // Yellow
            } else {
                healthColor = new Color(200, 0, 0); // Red
            }
            
            g2d.setColor(healthColor);
            g2d.fillRoundRect(barX, barY, fillWidth, barInnerHeight, 4, 4);
            
            // Draw border on health bar
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(barX, barY, barInnerWidth, barInnerHeight, 4, 4);
        }
        
        // Draw energy bar underneath health bar
        int energyBarY = barY + barInnerHeight + 16; // Position below health bar with spacing
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRoundRect(barX, energyBarY, barInnerWidth, barInnerHeight, 4, 4);

        // Draw energy text
        String energyText = String.format("%d / %d Energy", healthBar.currentEnergy, healthBar.maxEnergy);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        fm = g2d.getFontMetrics();
        int energyTextY = barY + barInnerHeight - 8 + fm.getHeight() + 5;
        g2d.drawString(energyText, x + padding, energyTextY);
        
        // Draw energy bar fill (yellow)
        if (healthBar.maxEnergy > 0) {
            double energyPercent = (double) healthBar.currentEnergy / healthBar.maxEnergy;
            int fillWidth = (int)(barInnerWidth * energyPercent);
            
            // Yellow color for energy bar
            g2d.setColor(Color.YELLOW);
            g2d.fillRoundRect(barX, energyBarY, fillWidth, barInnerHeight, 4, 4);
            
            // Draw border on energy bar
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(barX, energyBarY, barInnerWidth, barInnerHeight, 4, 4);
        }
    }
    
    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
        this.backgroundAlpha = 1.0f;
        this.backgroundFlashIntensity = 0.0f;
        repaint();
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
        this.backgroundImage = image;
        
        if ("fadeIn".equals(animationType)) {
            this.backgroundAlpha = 0.0f; // Start fully transparent
            startFadeInAnimation(durationMs, 
                alpha -> this.backgroundAlpha = alpha,
                () -> {
                    this.backgroundAlpha = 1.0f;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
        } else if ("flash".equals(animationType)) {
            this.backgroundFlashIntensity = 1.0f; // Start completely white
            startFlashAnimation(durationMs,
                intensity -> this.backgroundFlashIntensity = intensity,
                () -> {
                    this.backgroundFlashIntensity = 0.0f;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
        } else {
            // Unknown animation type, just set normally
            this.backgroundAlpha = 1.0f;
        }
        
        repaint();
    }
    
    /**
     * Adds a sprite with the specified properties.
     * 
     * @param image The sprite image
     * @param position The position ("Left", "Right", "Center", "Back")
     * @param depth The depth (z-order: higher values are drawn on top)
     * @param offset The horizontal offset in pixels
     */
    public void addSprite(BufferedImage image, String position, int depth, int offset) {
        sprites.add(new SpriteData(image, position, depth, offset));
        repaint();
    }
    
    /**
     * Adds a sprite with the specified properties and character ID.
     * 
     * @param image The sprite image
     * @param position The position ("Left", "Right", "Center", "Back")
     * @param depth The depth (z-order: higher values are drawn on top)
     * @param offset The horizontal offset in pixels
     * @param characterId The character ID associated with this sprite (null if not a character sprite)
     */
    public void addSprite(BufferedImage image, String position, int depth, int offset, String characterId) {
        sprites.add(new SpriteData(image, position, depth, offset, characterId));
        repaint();
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
        addSpriteWithAnimation(image, position, depth, offset, animationType, durationMs, onComplete, null);
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
        SpriteData sprite = new SpriteData(image, position, depth, offset, characterId);
        sprite.setAnimationType(animationType);
        
        if ("fadeIn".equals(animationType)) {
            sprite.setAlpha(0.0f); // Start fully transparent
            sprite.setAnimating(true);
            
            startFadeInAnimation(durationMs,
                alpha -> sprite.setAlpha(alpha),
                () -> {
                    sprite.setAlpha(1.0f);
                    sprite.setAnimating(false);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
        } else if ("flash".equals(animationType)) {
            sprite.setFlashIntensity(1.0f); // Start completely white
            sprite.setAnimating(true);
            
            startFlashAnimation(durationMs,
                intensity -> sprite.setFlashIntensity(intensity),
                () -> {
                    sprite.setFlashIntensity(0.0f);
                    sprite.setAnimating(false);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
        } else {
            // Unknown animation type, just add normally
            sprite.setAlpha(1.0f);
        }
        
        sprites.add(sprite);
        repaint();
    }
    
    /**
     * Sets a character sprite at the specified position.
     * This is a convenience method that uses default layer (0) and offset (0).
     * 
     * @param image The sprite image
     * @param position The position ("Left", "Right", "Center")
     */
    public void setCharacterSprite(BufferedImage image, String position) {
        addSprite(image, position, 0, 0);
    }
    
    /**
     * Removes all sprites from the specified position.
     * 
     * @param position The position to clear
     */
    public void clearSpritesAtPosition(String position) {
        sprites.removeIf(sprite -> sprite.getPosition().equals(position));
        repaint();
    }
    
    /**
     * Removes all sprites at the specified position and depth.
     * 
     * @param position The position to clear
     * @param depth The depth to clear
     */
    public void clearSpritesAtPositionAndDepth(String position, int depth) {
        sprites.removeIf(sprite -> sprite.getPosition().equals(position) && sprite.getDepth() == depth);
        repaint();
    }
    
    public void showScreen(BufferedImage image) {
        this.centerScreen = image;
        this.screenAlpha = 1.0f;
        this.screenFlashIntensity = 0.0f;
        repaint();
    }
    
    /**
     * Shows a screen (lose/win) with animation support.
     * 
     * @param image The screen image
     * @param animationType The type of animation (e.g., "fadeIn")
     * @param durationMs The duration of the animation in milliseconds
     * @param onComplete Callback when animation completes
     */
    public void showScreenWithAnimation(BufferedImage image, String animationType, 
                                       int durationMs, Runnable onComplete) {
        this.centerScreen = image;
        
        if ("fadeIn".equals(animationType)) {
            this.screenAlpha = 0.0f; // Start fully transparent
            
            startFadeInAnimation(durationMs,
                alpha -> this.screenAlpha = alpha,
                () -> {
                    this.screenAlpha = 1.0f;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
        } else if ("flash".equals(animationType)) {
            this.screenFlashIntensity = 1.0f; // Start completely white
            
            startFlashAnimation(durationMs,
                intensity -> this.screenFlashIntensity = intensity,
                () -> {
                    this.screenFlashIntensity = 0.0f;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
        } else {
            // Unknown animation type, just show normally
            this.screenAlpha = 1.0f;
        }
        
        repaint();
    }
    
    /**
     * Starts a fadeIn animation with the specified duration.
     * This is a reusable helper method that handles the common fadeIn animation logic.
     * 
     * @param durationMs The duration of the animation in milliseconds
     * @param alphaUpdater A consumer that updates the alpha value during animation
     * @param onComplete Callback when animation completes
     */
    private void startFadeInAnimation(int durationMs, Consumer<Float> alphaUpdater, Runnable onComplete) {
        // Stop any existing animation
        if (currentAnimationTimer != null && currentAnimationTimer.isRunning()) {
            currentAnimationTimer.stop();
        }
        
        // Store current animation state for skipping
        currentAnimationAlphaUpdater = alphaUpdater;
        currentAnimationOnComplete = onComplete;
        currentAnimationFinalValue = 1.0f; // Final value for fadeIn is 1.0f
        
        // Create animation timer
        Timer animationTimer = new Timer(16, null); // ~60 FPS
        currentAnimationTimer = animationTimer;
        long startTime = System.currentTimeMillis();
        
        animationTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float) elapsed / durationMs);
            
            // Update alpha using the provided updater
            alphaUpdater.accept(progress);
            repaint();
            
            if (progress >= 1.0f) {
                animationTimer.stop();
                // Clear animation tracking
                currentAnimationTimer = null;
                currentAnimationAlphaUpdater = null;
                currentAnimationOnComplete = null;
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        
        animationTimer.start();
    }
    
    /**
     * Starts a flash animation with the specified duration.
     * The sprite flashes completely white and then fades back to normal color.
     * 
     * @param durationMs The duration of the animation in milliseconds
     * @param flashIntensityUpdater A consumer that updates the flash intensity during animation
     * @param onComplete Callback when animation completes
     */
    private void startFlashAnimation(int durationMs, Consumer<Float> flashIntensityUpdater, Runnable onComplete) {
        // Stop any existing animation
        if (currentAnimationTimer != null && currentAnimationTimer.isRunning()) {
            currentAnimationTimer.stop();
        }
        
        // Store current animation state for skipping
        currentAnimationAlphaUpdater = flashIntensityUpdater;
        currentAnimationOnComplete = onComplete;
        currentAnimationFinalValue = 0.0f; // Final value for flash is 0.0f (normal color)
        
        // Create animation timer
        Timer animationTimer = new Timer(16, null); // ~60 FPS
        currentAnimationTimer = animationTimer;
        long startTime = System.currentTimeMillis();
        
        animationTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float) elapsed / durationMs);
            
            // Flash animation: start at 1.0 (completely white) and fade to 0.0 (normal)
            float intensity = 1.0f - progress;
            
            // Update flash intensity using the provided updater
            flashIntensityUpdater.accept(intensity);
            repaint();
            
            if (progress >= 1.0f) {
                animationTimer.stop();
                // Clear animation tracking
                currentAnimationTimer = null;
                currentAnimationAlphaUpdater = null;
                currentAnimationOnComplete = null;
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        
        animationTimer.start();
    }
    
    /**
     * Skips the current animation by jumping to the end state.
     * This allows players to skip through animated actions quickly.
     */
    public void skipCurrentAnimation() {
        if (currentAnimationTimer != null && currentAnimationTimer.isRunning()) {
            // Stop the timer
            currentAnimationTimer.stop();
            
            // Set to final state (1.0f for fadeIn, 0.0f for flash)
            if (currentAnimationAlphaUpdater != null) {
                currentAnimationAlphaUpdater.accept(currentAnimationFinalValue);
            }
            
            // Call the onComplete callback
            Runnable onComplete = currentAnimationOnComplete;
            
            // Clear animation tracking
            currentAnimationTimer = null;
            currentAnimationAlphaUpdater = null;
            currentAnimationOnComplete = null;
            currentAnimationFinalValue = 1.0f; // Reset to default
            
            // Repaint to show final state
            repaint();
            
            // Call onComplete after clearing state
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    
    /**
     * Checks if an animation is currently running.
     * 
     * @return true if an animation is running, false otherwise
     */
    public boolean isAnimationRunning() {
        return currentAnimationTimer != null && currentAnimationTimer.isRunning();
    }
    
    /**
     * Clears all sprites from the screen.
     */
    public void clearSprites() {
        sprites.clear();
        this.centerScreen = null;
        repaint();
    }
    
    /**
     * Gets a copy of the current sprites list.
     * 
     * @return A new list containing copies of all current sprites
     */
    public List<SpriteData> getSpritesCopy() {
        List<SpriteData> copy = new ArrayList<>();
        for (SpriteData sprite : sprites) {
            // Create a new SpriteData with the same properties
            SpriteData spriteCopy = new SpriteData(
                sprite.getImage(),
                sprite.getPosition(),
                sprite.getDepth(),
                sprite.getOffset(),
                sprite.getCharacterId()
            );
            spriteCopy.setAlpha(sprite.getAlpha());
            spriteCopy.setFlashIntensity(sprite.getFlashIntensity());
            spriteCopy.setAnimating(sprite.isAnimating());
            spriteCopy.setAnimationType(sprite.getAnimationType());
            copy.add(spriteCopy);
        }
        return copy;
    }
    
    /**
     * Restores sprites from a list.
     * 
     * @param spritesToRestore The list of sprites to restore
     */
    public void restoreSprites(List<SpriteData> spritesToRestore) {
        if (spritesToRestore == null) {
            return;
        }
        // Clear current sprites and restore from the list
        sprites.clear();
        for (SpriteData sprite : spritesToRestore) {
            // Create a new SpriteData with the same properties
            SpriteData spriteCopy = new SpriteData(
                sprite.getImage(),
                sprite.getPosition(),
                sprite.getDepth(),
                sprite.getOffset(),
                sprite.getCharacterId()
            );
            spriteCopy.setAlpha(sprite.getAlpha());
            spriteCopy.setFlashIntensity(sprite.getFlashIntensity());
            spriteCopy.setAnimating(sprite.isAnimating());
            spriteCopy.setAnimationType(sprite.getAnimationType());
            sprites.add(spriteCopy);
        }
        repaint();
    }
    
    /**
     * Clears the center screen (attack/win/lose images).
     */
    public void clearScreen() {
        this.centerScreen = null;
        this.screenAlpha = 1.0f;
        this.screenFlashIntensity = 0.0f;
        repaint();
    }
    
    /**
     * Shows an attack image at center screen temporarily.
     * This is used during battles to display attack animations.
     * 
     * @param image The attack image to display
     */
    public void showAttackImage(BufferedImage image) {
        this.centerScreen = image;
        this.screenAlpha = 1.0f;
        repaint();
    }
    
    /**
     * Hides sprites for a specific character ID.
     * 
     * @param characterId The character ID whose sprites should be hidden
     */
    public void hideSpritesByCharacterId(String characterId) {
        if (characterId == null) {
            return;
        }
        sprites.removeIf(sprite -> characterId.equals(sprite.getCharacterId()));
        repaint();
    }
    
    /**
     * Hides excess sprites for a specific character ID, keeping only the specified count.
     * If there are fewer sprites than the count, none are hidden.
     * 
     * @param characterId The character ID whose sprites should be hidden
     * @param keepCount The number of sprites to keep (0 means hide all)
     */
    public void hideExcessSpritesByCharacterId(String characterId, int keepCount) {
        if (characterId == null) {
            return;
        }
        
        // Collect all sprites for this character
        List<SpriteData> characterSprites = new ArrayList<>();
        for (SpriteData sprite : sprites) {
            if (characterId.equals(sprite.getCharacterId())) {
                characterSprites.add(sprite);
            }
        }
        
        // If we need to hide some, remove the excess (keep the first 'keepCount' sprites)
        if (characterSprites.size() > keepCount) {
            // Remove the excess sprites (from the end of the list)
            for (int i = keepCount; i < characterSprites.size(); i++) {
                sprites.remove(characterSprites.get(i));
            }
            repaint();
        }
    }
    
    /**
     * Hides all sprites at a specific position.
     * 
     * @param position The position to clear ("Left", "Right", "Center", "Back")
     */
    public void hideSpritesAtPosition(String position) {
        clearSpritesAtPosition(position);
    }
    
    /**
     * Counts the number of visible sprites for a specific character ID.
     * 
     * @param characterId The character ID to count sprites for
     * @return The number of visible sprites for the character
     */
    public int countVisibleSpritesByCharacterId(String characterId) {
        if (characterId == null) {
            return 0;
        }
        int count = 0;
        for (SpriteData sprite : sprites) {
            if (characterId.equals(sprite.getCharacterId())) {
                count++;
            }
        }
        return count;
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
        HealthBarData healthBar = characterHealthBars.computeIfAbsent(characterId, k -> new HealthBarData());
        healthBar.teamId = teamId;
        healthBar.characterName = characterName;
        healthBar.currentHealth = currentHealth;
        healthBar.maxHealth = maxHealth;
        healthBar.currentEnergy = currentEnergy;
        healthBar.maxEnergy = maxEnergy;
        healthBar.visible = true;
        repaint();
    }
    
    /**
     * Clears all health bar displays.
     */
    public void clearHealthBars() {
        characterHealthBars.clear();
        repaint();
    }
    
    /**
     * Clears the health bar for a specific character.
     * 
     * @param characterId The ID of the character
     */
    public void clearHealthBar(String characterId) {
        characterHealthBars.remove(characterId);
        repaint();
    }
    
    /**
     * Creates a white-tinted version of an image that preserves the alpha channel.
     * This is used for flash animations to only affect pixels that have color (alpha > 0).
     * 
     * @param original The original image
     * @return A white-tinted version of the image with the same alpha channel
     */
    private BufferedImage createWhiteTintedImage(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage whiteTinted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = whiteTinted.createGraphics();
        try {
            // Draw white background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            
            // Copy the alpha channel from the original image
            // This preserves transparency - only colored pixels will be white
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int originalPixel = original.getRGB(x, y);
                    int alpha = (originalPixel >> 24) & 0xFF;
                    
                    if (alpha > 0) {
                        // Set white color with original alpha
                        int whitePixel = (alpha << 24) | 0x00FFFFFF;
                        whiteTinted.setRGB(x, y, whitePixel);
                    } else {
                        // Keep transparent
                        whiteTinted.setRGB(x, y, 0x00000000);
                    }
                }
            }
        } finally {
            g2d.dispose();
        }
        
        return whiteTinted;
    }
}

