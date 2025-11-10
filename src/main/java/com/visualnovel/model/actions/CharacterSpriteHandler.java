package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.SpriteRequest;

/**
 * Handler for character sprite rendering.
 */
public class CharacterSpriteHandler implements SpriteHandler {
    
    @Override
    public void handle(SpriteRequest request, GameController controller) {
        if (request == null || request.getImage() == null) {
            controller.processNextAction();
            return;
        }
        
        if (request.isAnimated()) {
            // Use animated sprite addition - animation will call processNextAction when done
            controller.getGUI().addSpriteWithAnimation(
                request.getImage(),
                request.getPosition(),
                request.getDepth(),
                request.getOffset(),
                request.getAnimationType(),
                request.getDurationMs(),
                () -> controller.processNextAction(),
                request.getCharacterId()
            );
            controller.getGUI().refresh();
        } else {
            // Use normal sprite addition - continue immediately
            if (request.getCharacterId() != null) {
                controller.getGUI().addSprite(
                    request.getImage(),
                    request.getPosition(),
                    request.getDepth(),
                    request.getOffset(),
                    request.getCharacterId()
                );
            } else {
                controller.getGUI().addSprite(
                    request.getImage(),
                    request.getPosition(),
                    request.getDepth(),
                    request.getOffset()
                );
            }
            controller.getGUI().refresh();
            controller.processNextAction();
        }
    }
}

