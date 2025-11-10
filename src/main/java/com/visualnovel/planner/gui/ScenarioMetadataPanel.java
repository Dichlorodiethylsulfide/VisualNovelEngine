package com.visualnovel.planner.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for entering scenario metadata.
 */
public class ScenarioMetadataPanel extends JPanel {
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField authorField;
    private JTextField versionField;
    private JTextField dateField;
    private JTextField categoryField;
    private JTextField subcategoryField;
    private JComboBox<String> battleStrategyCombo;
    private JCheckBox entryPointCheckbox;
    private JTextField tagsField;
    
    public ScenarioMetadataPanel() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Scenario Metadata"));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Basic info
        JPanel basicPanel = createLabeledField("Name:", nameField = new JTextField(20));
        mainPanel.add(basicPanel);
        
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descPanel.add(descScroll, BorderLayout.CENTER);
        mainPanel.add(descPanel);
        
        JPanel authorPanel = createLabeledField("Author:", authorField = new JTextField(20));
        mainPanel.add(authorPanel);
        
        JPanel versionPanel = createLabeledField("Version:", versionField = new JTextField(20));
        mainPanel.add(versionPanel);
        
        JPanel datePanel = createLabeledField("Date:", dateField = new JTextField(20));
        mainPanel.add(datePanel);
        
        // Category info
        JPanel categoryPanel = createLabeledField("Category:", categoryField = new JTextField(20));
        mainPanel.add(categoryPanel);
        
        JPanel subcategoryPanel = createLabeledField("Subcategory:", subcategoryField = new JTextField(20));
        mainPanel.add(subcategoryPanel);
        
        // Battle strategy
        JPanel battlePanel = new JPanel(new BorderLayout());
        battlePanel.add(new JLabel("Battle Strategy:"), BorderLayout.NORTH);
        battleStrategyCombo = new JComboBox<>(new String[]{"reset_stats", "persist_stats"});
        battleStrategyCombo.setEditable(false); // Non-editable to enforce validation
        battleStrategyCombo.setToolTipText("Battle strategy: reset_stats or persist_stats");
        battlePanel.add(battleStrategyCombo, BorderLayout.CENTER);
        mainPanel.add(battlePanel);
        
        // Entry point
        entryPointCheckbox = new JCheckBox("Entry Point");
        mainPanel.add(entryPointCheckbox);
        
        // Tags
        JPanel tagsPanel = createLabeledField("Tags (comma-separated):", tagsField = new JTextField(20));
        mainPanel.add(tagsPanel);
        
        mainPanel.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates a panel with a label and field.
     */
    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Gets the scenario name.
     */
    public String getName() {
        if (nameField == null) {
            return super.getName() != null ? super.getName() : "";
        }
        return nameField.getText().trim();
    }
    
    /**
     * Sets the scenario name.
     */
    public void setName(String name) {
        if (nameField != null) {
            nameField.setText(name != null ? name : "");
        }
    }
    
    /**
     * Gets the scenario description.
     */
    public String getDescription() {
        if (descriptionArea == null) {
            return "";
        }
        return descriptionArea.getText().trim();
    }
    
    /**
     * Sets the scenario description.
     */
    public void setDescription(String description) {
        if (descriptionArea != null) {
            descriptionArea.setText(description != null ? description : "");
        }
    }
    
    /**
     * Gets the scenario author.
     */
    public String getAuthor() {
        if (authorField == null) {
            return "";
        }
        return authorField.getText().trim();
    }
    
    /**
     * Sets the scenario author.
     */
    public void setAuthor(String author) {
        if (authorField != null) {
            authorField.setText(author != null ? author : "");
        }
    }
    
    /**
     * Gets the scenario version.
     */
    public String getVersion() {
        if (versionField == null) {
            return "";
        }
        return versionField.getText().trim();
    }
    
    /**
     * Sets the scenario version.
     */
    public void setVersion(String version) {
        if (versionField != null) {
            versionField.setText(version != null ? version : "");
        }
    }
    
    /**
     * Gets the scenario date.
     */
    public String getDate() {
        if (dateField == null) {
            return "";
        }
        return dateField.getText().trim();
    }
    
    /**
     * Sets the scenario date.
     */
    public void setDate(String date) {
        if (dateField != null) {
            dateField.setText(date != null ? date : "");
        }
    }
    
    /**
     * Gets the scenario category.
     */
    public String getCategory() {
        if (categoryField == null) {
            return "";
        }
        return categoryField.getText().trim();
    }
    
    /**
     * Sets the scenario category.
     */
    public void setCategory(String category) {
        if (categoryField != null) {
            categoryField.setText(category != null ? category : "");
        }
    }
    
    /**
     * Gets the scenario subcategory.
     */
    public String getSubcategory() {
        if (subcategoryField == null) {
            return "";
        }
        return subcategoryField.getText().trim();
    }
    
    /**
     * Sets the scenario subcategory.
     */
    public void setSubcategory(String subcategory) {
        if (subcategoryField != null) {
            subcategoryField.setText(subcategory != null ? subcategory : "");
        }
    }
    
    /**
     * Gets the battle strategy.
     */
    public String getBattleStrategy() {
        if (battleStrategyCombo == null) {
            return null;
        }
        return (String) battleStrategyCombo.getSelectedItem();
    }
    
    /**
     * Sets the battle strategy.
     */
    public void setBattleStrategy(String strategy) {
        if (battleStrategyCombo != null && strategy != null) {
            battleStrategyCombo.setSelectedItem(strategy);
        }
    }
    
    /**
     * Gets whether this is an entry point scenario.
     */
    public boolean isEntryPoint() {
        if (entryPointCheckbox == null) {
            return false;
        }
        return entryPointCheckbox.isSelected();
    }
    
    /**
     * Sets whether this is an entry point scenario.
     */
    public void setEntryPoint(boolean entryPoint) {
        if (entryPointCheckbox != null) {
            entryPointCheckbox.setSelected(entryPoint);
        }
    }
    
    /**
     * Gets the tags as a list.
     */
    public List<String> getTags() {
        if (tagsField == null) {
            return new ArrayList<>();
        }
        String tagsText = tagsField.getText().trim();
        if (tagsText.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> tags = new ArrayList<>();
        for (String tag : tagsText.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                tags.add(trimmed);
            }
        }
        return tags;
    }
    
    /**
     * Sets the tags from a list.
     */
    public void setTags(List<String> tags) {
        if (tagsField != null) {
            if (tags == null || tags.isEmpty()) {
                tagsField.setText("");
            } else {
                tagsField.setText(String.join(", ", tags));
            }
        }
    }
}

