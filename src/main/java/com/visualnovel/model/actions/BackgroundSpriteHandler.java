package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.SpriteRequest;

/**
 * Handler for background sprite rendering.
 */
public class BackgroundSpriteHandler implements SpriteHandler {
    
    @Override
    public void handle(SpriteRequest request, GameController controller) {
        if (request == null || request.getImage() == null) {
            controller.processNextAction();
            return;
        }
        
        if (request.isAnimated()) {
            // Use animated background - animation will call processNextAction when done
            controller.getGUI().setBackgroundImageWithAnimation(
                request.getImage(),
                request.getAnimationType(),
                request.getDurationMs(),
                () -> controller.processNextAction()
            );
            controller.getGUI().refresh();
        } else {
            // Use normal background - continue immediately
            controller.getGUI().setBackgroundImage(request.getImage());
            controller.getGUI().refresh();
            controller.processNextAction();
        }
    }
}

