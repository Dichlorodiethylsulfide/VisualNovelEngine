package com.visualnovel.model;

import java.util.List;
import java.util.Map;

/**
 * Represents the complete scenario with metadata, characters, and a sequence of actions.
 */
public class Scenario {
    private Boolean entryPoint; // Whether this scenario can be loaded as a starting point
    private String name;
    private String description;
    private String author;
    private String version;
    private String date;
    private List<String> tags;
    private String category;
    private String subcategory;
    private String battleStrategy; // Battle strategy: "reset_stats" or "persist_stats"
    private Map<String, IdentifiableObject> ids; // Map of object IDs to IdentifiableObject instances
    private Map<String, Character> characters; // Map of character IDs to Character objects
    private List<ScenarioAction> sequence; // The main sequence of actions

    
    public Boolean getEntryPoint() {
        return entryPoint;
    }
    
    public void setEntryPoint(Boolean entryPoint) {
        this.entryPoint = entryPoint;
    }
    
    public boolean isEntryPoint() {
        return entryPoint != null && entryPoint;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSubcategory() {
        return subcategory;
    }
    
    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }
    
    public String getBattleStrategy() {
        return battleStrategy;
    }
    
    public void setBattleStrategy(String battleStrategy) {
        this.battleStrategy = battleStrategy;
    }
    
    public Map<String, IdentifiableObject> getIds() {
        return ids;
    }
    
    public void setIds(Map<String, IdentifiableObject> ids) {
        this.ids = ids;
    }
    
    public Map<String, Character> getCharacters() {
        return characters;
    }
    
    public void setCharacters(Map<String, Character> characters) {
        this.characters = characters;
    }
    
    /**
     * Gets the sequence of actions. This is the primary field for action lists.
     * 
     * @return The sequence of actions or null
     */
    public List<ScenarioAction> getSequence() {
        if (sequence != null) {
            return sequence;
        }
        return null;
    }
    
    public void setSequence(List<ScenarioAction> sequence) {
        this.sequence = sequence;
    }
}

