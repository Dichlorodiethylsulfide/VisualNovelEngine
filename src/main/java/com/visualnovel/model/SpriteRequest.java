package com.visualnovel.model;

import java.awt.image.BufferedImage;

/**
 * Represents a request to render a sprite with all its properties.
 * Uses builder pattern for optional fields.
 */
public class SpriteRequest {
    private final BufferedImage image;
    private final String type; // "background", "character", "screen"
    private final String position; // "Left", "Right", "Center", "Back"
    private final int depth;
    private final int offset;
    private final String characterId;
    private final String animationType;
    private final Integer durationMs;
    private final Runnable onComplete;
    
    private SpriteRequest(Builder builder) {
        this.image = builder.image;
        this.type = builder.type;
        this.position = builder.position;
        this.depth = builder.depth;
        this.offset = builder.offset;
        this.characterId = builder.characterId;
        this.animationType = builder.animationType;
        this.durationMs = builder.durationMs;
        this.onComplete = builder.onComplete;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public String getType() {
        return type;
    }
    
    public String getPosition() {
        return position;
    }
    
    public int getDepth() {
        return depth;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public String getCharacterId() {
        return characterId;
    }
    
    public String getAnimationType() {
        return animationType;
    }
    
    public Integer getDurationMs() {
        return durationMs;
    }
    
    public Runnable getOnComplete() {
        return onComplete;
    }
    
    public boolean isAnimated() {
        return animationType != null && durationMs != null;
    }
    
    public static class Builder {
        private BufferedImage image;
        private String type;
        private String position;
        private int depth = 0;
        private int offset = 0;
        private String characterId;
        private String animationType;
        private Integer durationMs;
        private Runnable onComplete;
        
        public Builder(BufferedImage image, String type) {
            this.image = image;
            this.type = type;
        }
        
        public Builder position(String position) {
            this.position = position;
            return this;
        }
        
        public Builder depth(int depth) {
            this.depth = depth;
            return this;
        }
        
        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }
        
        public Builder characterId(String characterId) {
            this.characterId = characterId;
            return this;
        }
        
        public Builder animation(String animationType, Integer durationMs) {
            this.animationType = animationType;
            this.durationMs = durationMs;
            return this;
        }
        
        public Builder onComplete(Runnable onComplete) {
            this.onComplete = onComplete;
            return this;
        }
        
        public SpriteRequest build() {
            if (image == null) {
                throw new IllegalStateException("Image is required");
            }
            if (type == null) {
                throw new IllegalStateException("Type is required");
            }
            return new SpriteRequest(this);
        }
    }
}

