package com.visualnovel.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for discovering scenario.json files in the assets/json directory.
 */
public class ScenarioDiscovery {
    private static final String ASSETS_JSON_DIR = "assets/json";
    
    /**
     * Represents a discovered scenario file with its path and metadata.
     */
    public static class ScenarioInfo {
        private String filePath;
        private String folderName;
        private String name;
        private String description;
        private String author;
        private String version;
        private Boolean entryPoint;
        
        public ScenarioInfo(String filePath, String folderName) {
            this.filePath = filePath;
            this.folderName = folderName;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public String getFolderName() {
            return folderName;
        }
        
        public String getName() {
            return name != null ? name : folderName;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description != null ? description : "";
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getAuthor() {
            return author != null ? author : "";
        }
        
        public void setAuthor(String author) {
            this.author = author;
        }
        
        public String getVersion() {
            return version != null ? version : "";
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
        public Boolean getEntryPoint() {
            return entryPoint;
        }
        
        public void setEntryPoint(Boolean entryPoint) {
            this.entryPoint = entryPoint;
        }
        
        public boolean isEntryPoint() {
            return entryPoint != null && entryPoint;
        }
    }
    
    /**
     * Discovers all scenario.json files in the assets/json directory.
     * 
     * @return List of ScenarioInfo objects representing found scenarios
     * @throws IOException If there's an error reading the directory
     */
    public static List<ScenarioInfo> discoverScenarios() throws IOException {
        List<ScenarioInfo> scenarios = new ArrayList<>();
        Path assetsPath = Paths.get(ASSETS_JSON_DIR);
        
        if (!Files.exists(assetsPath)) {
            return scenarios;
        }
        
        // Walk through all subdirectories in assets/json
        Files.walk(assetsPath)
            .filter(Files::isDirectory)
            .forEach(dir -> {
                Path scenarioFile = dir.resolve("scenario.json");
                if (Files.exists(scenarioFile) && Files.isRegularFile(scenarioFile)) {
                    String filePath = scenarioFile.toString().replace(File.separator, "/");
                    String folderName = dir.getFileName().toString();
                    ScenarioInfo info = new ScenarioInfo(filePath, folderName);
                    
                    // Try to load metadata from the scenario file
                    try {
                        loadScenarioMetadata(info, scenarioFile.toFile());
                    } catch (Exception e) {
                        // If metadata loading fails, just use folder name
                        System.err.println("Warning: Could not load metadata for " + filePath + ": " + e.getMessage());
                    }
                    
                    scenarios.add(info);
                }
            });
        
        return scenarios;
    }
    
    /**
     * Loads metadata from a scenario.json file without fully loading the scenario.
     * 
     * @param info The ScenarioInfo object to populate
     * @param file The scenario.json file
     * @throws IOException If there's an error reading the file
     */
    private static void loadScenarioMetadata(ScenarioInfo info, File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            // Check if this is an entry point scenario (has "scenario" object)
            if (root.has("scenario") && root.get("scenario").isJsonObject()) {
                JsonObject scenarioObj = root.getAsJsonObject("scenario");
                
                // Parse entry_point
                if (scenarioObj.has("entry_point")) {
                    info.setEntryPoint(scenarioObj.get("entry_point").getAsBoolean());
                }
                
                // Parse metadata
                if (scenarioObj.has("name")) {
                    info.setName(scenarioObj.get("name").getAsString());
                }
                if (scenarioObj.has("description")) {
                    info.setDescription(scenarioObj.get("description").getAsString());
                }
                if (scenarioObj.has("author")) {
                    info.setAuthor(scenarioObj.get("author").getAsString());
                }
                if (scenarioObj.has("version")) {
                    info.setVersion(scenarioObj.get("version").getAsString());
                }
            }
        }
    }
}

