package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.SpriteRequest;

/**
 * Interface for handling sprite rendering based on sprite type.
 * Each sprite type (background, character, screen) has its own handler implementation.
 */
public interface SpriteHandler {
    /**
     * Handles rendering of a sprite request.
     * 
     * @param request The sprite request to render
     * @param controller The game controller for accessing GUI and continuing execution
     */
    void handle(SpriteRequest request, GameController controller);
}

