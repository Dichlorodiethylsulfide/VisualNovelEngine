package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.SpriteRequest;

/**
 * Handler for screen (win/lose) sprite rendering.
 */
public class ScreenSpriteHandler implements SpriteHandler {
    
    @Override
    public void handle(SpriteRequest request, GameController controller) {
        if (request == null || request.getImage() == null) {
            controller.processNextAction();
            return;
        }
        
        if (request.isAnimated()) {
            // Use animated screen - animation will call processNextAction when done
            controller.getGUI().showScreenWithAnimation(
                request.getImage(),
                request.getAnimationType(),
                request.getDurationMs(),
                () -> controller.processNextAction()
            );
            controller.getGUI().refresh();
        } else {
            // Use normal screen - continue immediately
            controller.getGUI().showScreen(request.getImage());
            controller.getGUI().refresh();
            controller.processNextAction();
        }
    }
}

