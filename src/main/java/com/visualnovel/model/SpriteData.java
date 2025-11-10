package com.visualnovel.model;

import java.awt.image.BufferedImage;

/**
 * Represents a sprite with its display properties including layer and offset.
 */
public class SpriteData {
    private BufferedImage image;
    private String position; // "Left", "Right", "Center", "Back"
    private int depth; // Z-order: higher values are drawn on top
    private int offset; // Horizontal offset in pixels (positive = right, negative = left)
    private float alpha; // Opacity for animations (0.0 to 1.0)
    private float flashIntensity; // Flash intensity for flash animation (0.0 = normal, 1.0 = completely white)
    private boolean isAnimating; // Whether this sprite is currently animating
    private String animationType; // Type of animation (e.g., "fadeIn")
    private String characterId; // Character ID associated with this sprite (null if not a character sprite)
    
    public SpriteData(BufferedImage image, String position, int depth, int offset) {
        this.image = image;
        this.position = position;
        this.depth = depth;
        this.offset = offset;
        this.alpha = 1.0f; // Fully opaque by default
        this.flashIntensity = 0.0f; // No flash by default
        this.isAnimating = false;
        this.animationType = null;
        this.characterId = null;
    }
    
    public SpriteData(BufferedImage image, String position, int depth, int offset, String characterId) {
        this.image = image;
        this.position = position;
        this.depth = depth;
        this.offset = offset;
        this.alpha = 1.0f; // Fully opaque by default
        this.flashIntensity = 0.0f; // No flash by default
        this.isAnimating = false;
        this.animationType = null;
        this.characterId = characterId;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public int getDepth() {
        return depth;
    }
    
    public void setDepth(int depth) {
        this.depth = depth;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public void setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha)); // Clamp between 0 and 1
    }
    
    public boolean isAnimating() {
        return isAnimating;
    }
    
    public void setAnimating(boolean isAnimating) {
        this.isAnimating = isAnimating;
    }
    
    public String getAnimationType() {
        return animationType;
    }
    
    public void setAnimationType(String animationType) {
        this.animationType = animationType;
    }
    
    public String getCharacterId() {
        return characterId;
    }
    
    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }
    
    public float getFlashIntensity() {
        return flashIntensity;
    }
    
    public void setFlashIntensity(float flashIntensity) {
        this.flashIntensity = Math.max(0.0f, Math.min(1.0f, flashIntensity)); // Clamp between 0 and 1
    }
}

