package com.visualnovel.model.actions;

import com.visualnovel.GameController;
import com.visualnovel.model.ActionType;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.SpriteRequest;
import com.visualnovel.model.Timing;
import com.visualnovel.util.AssetManager;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Action that displays a sprite (background, character, or screen).
 */
public class ShowSpriteAction extends ActionType {
    /**
     * Gets the action name as a string.
     * 
     * @return The action name "ShowSprite"
     */
    public static String getName() {
        return "ShowSprite";
    }

    @Override
    public String getJsonValue() {
        return getName();
    }
    
    @Override
    public void execute(ScenarioAction action, GameController controller) {
        Map<String, Object> params = action.getParameters();
        if (params == null) {
            System.err.println("Warning: ShowSprite action has no parameters.");
            controller.processNextAction();
            return;
        }
        
        String id = (String) params.get("id");
        String position = (String) params.get("position");
        String spriteKey = (String) params.get("sprite_key");
        
        // Default sprite_key to "default" if not specified
        if (spriteKey == null || spriteKey.isEmpty()) {
            spriteKey = "default";
        }

        // Extract depth and offset parameters (optional)
        int depth = 0; // Default depth
        int offset = 0; // Default offset
        
        if (params.containsKey("depth")) {
            Object depthObj = params.get("depth");
            if (depthObj instanceof Number) {
                depth = ((Number) depthObj).intValue();
            } else if (depthObj instanceof String) {
                try {
                    depth = Integer.parseInt((String) depthObj);
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid depth value: " + depthObj);
                }
            }
        }
        
        if (params.containsKey("offset")) {
            Object offsetObj = params.get("offset");
            if (offsetObj instanceof Number) {
                offset = ((Number) offsetObj).intValue();
            } else if (offsetObj instanceof String) {
                try {
                    offset = Integer.parseInt((String) offsetObj);
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid offset value: " + offsetObj);
                }
            }
        }
        
        // Look up the object in the ids dictionary
        if (id == null || id.isEmpty()) {
            System.err.println("Warning: ShowSprite action requires an 'id' parameter.");
            controller.processNextAction();
            return;
        }
        
        IdentifiableObject identifiableObject = controller.getIdentifiableObject(id);
        if (identifiableObject == null) {
            System.err.println("Warning: Object with id '" + id + "' not found in ids dictionary.");
            controller.processNextAction();
            return;
        }
        
        String category = identifiableObject.getCategory();
        String type = identifiableObject.getType();
        
        if (category == null || category.isEmpty()) {
            System.err.println("Warning: Object with id '" + id + "' has no category.");
            controller.processNextAction();
            return;
        }
        
        if (type == null || type.isEmpty()) {
            System.err.println("Warning: Object with id '" + id + "' has no type.");
            controller.processNextAction();
            return;
        }
        
        // Look up sprite name from sprites dictionary
        String spriteName = null;
        if (identifiableObject.getSprites() != null) {
            spriteName = identifiableObject.getSprites().get(spriteKey);
        }
        
        if (spriteName == null || spriteName.isEmpty()) {
            System.err.println("Warning: Sprite key '" + spriteKey + "' not found in object '" + id + "' sprites dictionary.");
            controller.processNextAction();
            return;
        }
        
        if (controller.getGUI() == null) {
            controller.processNextAction();
            return;
        }
        
        AssetManager assetManager = AssetManager.getInstance();
        
        // Check if timing is animated (applies to all sprite types)
        Timing timing = action.getTiming();
        System.out.println("Timing Type: " + (timing != null ? timing.getType() : "null"));
        boolean isAnimated = timing != null && timing.isAnimated() && timing.getAnimation() != null;
        String animationType = isAnimated ? timing.getAnimation() : null;
        int durationMs = isAnimated && timing.getDurationMs() != null ? timing.getDurationMs() : 1000;

        // Load image using type (background, character, screen) and category (forest, player, enemy, etc.)
        BufferedImage image = assetManager.loadImage(type, category, spriteName);
        
        if (image == null) {
            System.err.println("Warning: Failed to load sprite image for object '" + id + 
                             "' with category '" + category + "', type '" + type + 
                             "', and sprite name '" + spriteName + "'");
            controller.processNextAction();
            return;
        }
        
        // Build SpriteRequest from parsed parameters
        SpriteRequest.Builder requestBuilder = new SpriteRequest.Builder(image, type)
            .depth(depth)
            .offset(offset);
        
        // Add position for character sprites
        if (position != null && !position.isEmpty()) {
            requestBuilder.position(position);
        }
        
        // Add character ID for character sprites
        if ("character".equals(type)) {
            requestBuilder.characterId(id);
        }
        
        // Add animation if present
        if (isAnimated) {
            requestBuilder.animation(animationType, durationMs);
        }
        
        SpriteRequest request = requestBuilder.build();
        
        // Look up handler from registry by sprite type
        SpriteHandler handler = SpriteHandlerRegistry.getHandler(type);
        if (handler == null) {
            System.err.println("Warning: Unknown type '" + type + "' for object '" + id + "'");
            controller.processNextAction();
            return;
        }
        
        // Delegate to handler
        handler.handle(request, controller);
    }
    
    @Override
    public boolean requiresUserInput() {
        return false;
    }
    
    @Override
    public boolean shouldContinueImmediately() {
        // Don't continue immediately - execute() method handles continuation:
        // - If animated: animation callback will call processNextAction
        // - If not animated: execute() calls processNextAction directly
        return false;
    }
}

