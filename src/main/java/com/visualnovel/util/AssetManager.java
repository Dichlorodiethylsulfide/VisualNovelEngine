package com.visualnovel.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages loading and caching of game assets (images).
 */
public class AssetManager {
    private static AssetManager instance;
    private Map<String, BufferedImage> imageCache;
    private String assetsPath;
    
    private AssetManager() {
        imageCache = new HashMap<>();
        assetsPath = "assets";
    }
    
    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }
    
    /**
     * Loads an image from the assets folder.
     * 
     * @param type The type of asset (background, character, screen, etc.)
     * @param category The category/subfolder (e.g., "forest", "player", "enemy")
     * @param spriteName The sprite name (e.g., "main", "orc")
     * @return The loaded BufferedImage, or null if not found
     */
    public BufferedImage loadImage(String type, String category, String spriteName) {
        String key = type + "/" + category + "/" + spriteName;
        
        // Check cache first
        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }
        
        // Build path based on type
        String path = assetsPath + "/images/" + type + "/" + category + "/" + spriteName + ".png";
        try {
            File imageFile = new File(path);
            if (!imageFile.exists()) {
                System.err.println("Image not found: " + path);
                return null;
            }
            
            BufferedImage image = ImageIO.read(imageFile);
            imageCache.put(key, image);
            return image;
        } catch (IOException e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Clears the image cache.
     */
    public void clearCache() {
        imageCache.clear();
    }
}

