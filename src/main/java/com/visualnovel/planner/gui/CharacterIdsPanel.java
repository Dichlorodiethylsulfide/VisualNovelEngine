package com.visualnovel.planner.gui;

import com.visualnovel.model.IdentifiableObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

/**
 * Panel for managing character IDs (IdentifiableObject entries in the ids dictionary).
 */
public class CharacterIdsPanel extends JPanel {
    private DefaultListModel<String> idListModel;
    private JList<String> idList;
    private JPanel detailPanel;
    
    // Detail fields
    private JTextField idField;
    private JComboBox<String> typeCombo;
    private JTextField categoryField;
    private JTextField displayNameField;
    private DefaultTableModel spriteTableModel;
    private JTable spriteTable;
    private JTextField healthField;
    private JTextField attackField;
    private JTextField defenseField;
    private JTextField speedField;
    private JTextField luckField;
    private JTextField energyField;
    
    private Map<String, IdentifiableObject> idObjects;
    private String currentEditingId;
    
    public CharacterIdsPanel() {
        idObjects = new HashMap<>();
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Character IDs"));
        
        createComponents();
        layoutComponents();
        setupEventHandlers();
        
        // Initial state - no ID selected
        showNoSelection();
    }
    
    /**
     * Creates all GUI components.
     */
    private void createComponents() {
        // ID list
        idListModel = new DefaultListModel<>();
        idList = new JList<>(idListModel);
        idList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Detail panel fields
        idField = new JTextField(20);
        idField.setEditable(false); // ID is set when adding
        
        typeCombo = new JComboBox<>(new String[]{"character", "background", "screen"});
        typeCombo.setSelectedItem("character");
        
        categoryField = new JTextField(20);
        displayNameField = new JTextField(20);
        
        // Sprite table
        spriteTableModel = new DefaultTableModel(new String[]{"Key", "Sprite Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        spriteTable = new JTable(spriteTableModel);
        spriteTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Stats fields
        healthField = new JTextField(10);
        attackField = new JTextField(10);
        defenseField = new JTextField(10);
        speedField = new JTextField(10);
        luckField = new JTextField(10);
        energyField = new JTextField(10);
    }
    
    /**
     * Lays out all components.
     */
    private void layoutComponents() {
        // Left panel: ID list with buttons
        JPanel leftPanel = new JPanel(new BorderLayout());
        JScrollPane listScroll = new JScrollPane(idList);
        leftPanel.add(listScroll, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add ID");
        JButton removeButton = new JButton("Remove");
        addButton.addActionListener(e -> addNewId());
        removeButton.addActionListener(e -> removeSelectedId());
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Right panel: Detail editor
        detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(new TitledBorder("ID Details"));
        
        // Basic info
        JPanel idPanel = createLabeledField("ID:", idField);
        detailPanel.add(idPanel);
        
        JPanel typePanel = new JPanel(new BorderLayout());
        typePanel.add(new JLabel("Type:"), BorderLayout.NORTH);
        typePanel.add(typeCombo, BorderLayout.CENTER);
        detailPanel.add(typePanel);
        
        JPanel categoryPanel = createLabeledField("Category:", categoryField);
        detailPanel.add(categoryPanel);
        
        JPanel displayNamePanel = createLabeledField("Display Name:", displayNameField);
        detailPanel.add(displayNamePanel);
        
        // Sprites section
        JPanel spritesPanel = new JPanel(new BorderLayout());
        spritesPanel.setBorder(new TitledBorder("Sprites"));
        JScrollPane spriteScroll = new JScrollPane(spriteTable);
        spriteScroll.setPreferredSize(new Dimension(0, 150));
        spritesPanel.add(spriteScroll, BorderLayout.CENTER);
        
        JPanel spriteButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addSpriteButton = new JButton("Add Sprite");
        JButton removeSpriteButton = new JButton("Remove Sprite");
        addSpriteButton.addActionListener(e -> addSprite());
        removeSpriteButton.addActionListener(e -> removeSprite());
        spriteButtonPanel.add(addSpriteButton);
        spriteButtonPanel.add(removeSpriteButton);
        spritesPanel.add(spriteButtonPanel, BorderLayout.SOUTH);
        detailPanel.add(spritesPanel);
        
        // Stats section
        JPanel statsPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        statsPanel.setBorder(new TitledBorder("Battle Stats"));
        statsPanel.add(createLabeledField("Health:", healthField));
        statsPanel.add(createLabeledField("Attack:", attackField));
        statsPanel.add(createLabeledField("Defense:", defenseField));
        statsPanel.add(createLabeledField("Speed:", speedField));
        statsPanel.add(createLabeledField("Luck:", luckField));
        statsPanel.add(createLabeledField("Energy:", energyField));
        detailPanel.add(statsPanel);
        
        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveCurrentId());
        detailPanel.add(saveButton);
        
        detailPanel.add(Box.createVerticalGlue());
        
        JScrollPane detailScroll = new JScrollPane(detailPanel);
        detailScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(detailScroll);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);
        
        add(splitPane, BorderLayout.CENTER);
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
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        idList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = idList.getSelectedValue();
                if (selected != null) {
                    editId(selected);
                }
            }
        });
    }
    
    /**
     * Adds a new ID.
     */
    private void addNewId() {
        String newId = JOptionPane.showInputDialog(this, "Enter ID name:", "New ID", JOptionPane.PLAIN_MESSAGE);
        if (newId != null && !newId.trim().isEmpty()) {
            newId = newId.trim();
            if (idObjects.containsKey(newId)) {
                JOptionPane.showMessageDialog(this, "ID already exists: " + newId, 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create new IdentifiableObject with defaults
            IdentifiableObject obj = new IdentifiableObject();
            obj.setType("character");
            obj.setCategory("");
            obj.setDisplayName("");
            obj.setSprites(new HashMap<>());
            obj.setStats(new HashMap<>());
            
            idObjects.put(newId, obj);
            idListModel.addElement(newId);
            idList.setSelectedValue(newId, true);
        }
    }
    
    /**
     * Removes the selected ID.
     */
    private void removeSelectedId() {
        String selected = idList.getSelectedValue();
        if (selected != null) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Remove ID: " + selected + "?", 
                "Confirm Removal", 
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                idObjects.remove(selected);
                idListModel.removeElement(selected);
                showNoSelection();
            }
        }
    }
    
    /**
     * Edits an existing ID.
     */
    private void editId(String id) {
        currentEditingId = id;
        IdentifiableObject obj = idObjects.get(id);
        if (obj == null) {
            showNoSelection();
            return;
        }
        
        // Set fields
        idField.setText(id);
        typeCombo.setSelectedItem(obj.getType() != null ? obj.getType() : "character");
        categoryField.setText(obj.getCategory() != null ? obj.getCategory() : "");
        displayNameField.setText(obj.getDisplayName() != null ? obj.getDisplayName() : "");
        
        // Load sprites into table
        spriteTableModel.setRowCount(0);
        if (obj.getSprites() != null) {
            for (Map.Entry<String, String> entry : obj.getSprites().entrySet()) {
                spriteTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        
        // Load stats
        if (obj.getStats() != null) {
            healthField.setText(getStatValue(obj, "health"));
            attackField.setText(getStatValue(obj, "attack"));
            defenseField.setText(getStatValue(obj, "defense"));
            speedField.setText(getStatValue(obj, "speed"));
            luckField.setText(getStatValue(obj, "luck"));
            energyField.setText(getStatValue(obj, "energy"));
        } else {
            healthField.setText("");
            attackField.setText("");
            defenseField.setText("");
            speedField.setText("");
            luckField.setText("");
            energyField.setText("");
        }
    }
    
    /**
     * Gets a stat value as a string.
     */
    private String getStatValue(IdentifiableObject obj, String statName) {
        if (obj.getStats() != null && obj.getStats().containsKey(statName)) {
            Number value = obj.getStats().get(statName);
            return value != null ? value.toString() : "";
        }
        return "";
    }
    
    /**
     * Shows the "no selection" state.
     */
    private void showNoSelection() {
        currentEditingId = null;
        idField.setText("");
        typeCombo.setSelectedItem("character");
        categoryField.setText("");
        displayNameField.setText("");
        spriteTableModel.setRowCount(0);
        healthField.setText("");
        attackField.setText("");
        defenseField.setText("");
        speedField.setText("");
        luckField.setText("");
        energyField.setText("");
    }
    
    /**
     * Adds a sprite to the table.
     */
    private void addSprite() {
        String key = JOptionPane.showInputDialog(this, "Enter sprite key (e.g., 'default'):", 
            "Add Sprite", JOptionPane.PLAIN_MESSAGE);
        if (key != null && !key.trim().isEmpty()) {
            String name = JOptionPane.showInputDialog(this, "Enter sprite name:", 
                "Add Sprite", JOptionPane.PLAIN_MESSAGE);
            if (name != null) {
                spriteTableModel.addRow(new Object[]{key.trim(), name.trim()});
            }
        }
    }
    
    /**
     * Removes the selected sprite from the table.
     */
    private void removeSprite() {
        int selectedRow = spriteTable.getSelectedRow();
        if (selectedRow >= 0) {
            spriteTableModel.removeRow(selectedRow);
        }
    }
    
    /**
     * Saves the current ID.
     */
    private void saveCurrentId() {
        if (currentEditingId == null) {
            return;
        }
        
        IdentifiableObject obj = idObjects.get(currentEditingId);
        if (obj == null) {
            return;
        }
        
        // Validate required fields
        String type = (String) typeCombo.getSelectedItem();
        String category = categoryField.getText().trim();
        
        if (type == null || type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type is required.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category is required.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update object
        obj.setType(type);
        obj.setCategory(category);
        obj.setDisplayName(displayNameField.getText().trim());
        
        // Update sprites
        Map<String, String> sprites = new HashMap<>();
        for (int i = 0; i < spriteTableModel.getRowCount(); i++) {
            String key = (String) spriteTableModel.getValueAt(i, 0);
            String name = (String) spriteTableModel.getValueAt(i, 1);
            if (key != null && !key.trim().isEmpty() && name != null && !name.trim().isEmpty()) {
                sprites.put(key.trim(), name.trim());
            }
        }
        obj.setSprites(sprites);
        
        // Update stats
        Map<String, Number> stats = new HashMap<>();
        addStatIfValid(stats, "health", healthField.getText().trim());
        addStatIfValid(stats, "attack", attackField.getText().trim());
        addStatIfValid(stats, "defense", defenseField.getText().trim());
        addStatIfValid(stats, "speed", speedField.getText().trim());
        addStatIfValid(stats, "luck", luckField.getText().trim());
        addStatIfValid(stats, "energy", energyField.getText().trim());
        obj.setStats(stats.isEmpty() ? null : stats);
        
        JOptionPane.showMessageDialog(this, "ID saved successfully.", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Adds a stat if the value is valid.
     */
    private void addStatIfValid(Map<String, Number> stats, String statName, String value) {
        if (value != null && !value.isEmpty()) {
            try {
                int intValue = Integer.parseInt(value);
                stats.put(statName, intValue);
            } catch (NumberFormatException e) {
                // Ignore invalid numbers
            }
        }
    }
    
    /**
     * Saves the current ID without showing dialogs (for auto-save during export).
     * Returns true if save was successful, false otherwise.
     */
    public boolean saveCurrent() {
        if (currentEditingId == null) {
            return true; // Nothing to save
        }
        
        IdentifiableObject obj = idObjects.get(currentEditingId);
        if (obj == null) {
            return false;
        }
        
        // Validate required fields
        String type = (String) typeCombo.getSelectedItem();
        String category = categoryField.getText().trim();
        
        if (type == null || type.isEmpty()) {
            return false; // Validation failed, but don't show error
        }
        
        if (category.isEmpty()) {
            return false; // Validation failed, but don't show error
        }
        
        // Update object
        obj.setType(type);
        obj.setCategory(category);
        obj.setDisplayName(displayNameField.getText().trim());
        
        // Update sprites
        Map<String, String> sprites = new HashMap<>();
        for (int i = 0; i < spriteTableModel.getRowCount(); i++) {
            String key = (String) spriteTableModel.getValueAt(i, 0);
            String name = (String) spriteTableModel.getValueAt(i, 1);
            if (key != null && !key.trim().isEmpty() && name != null && !name.trim().isEmpty()) {
                sprites.put(key.trim(), name.trim());
            }
        }
        obj.setSprites(sprites);
        
        // Update stats
        Map<String, Number> stats = new HashMap<>();
        addStatIfValid(stats, "health", healthField.getText().trim());
        addStatIfValid(stats, "attack", attackField.getText().trim());
        addStatIfValid(stats, "defense", defenseField.getText().trim());
        addStatIfValid(stats, "speed", speedField.getText().trim());
        addStatIfValid(stats, "luck", luckField.getText().trim());
        addStatIfValid(stats, "energy", energyField.getText().trim());
        obj.setStats(stats.isEmpty() ? null : stats);
        
        return true; // Save successful
    }
    
    /**
     * Gets all ID objects.
     */
    public Map<String, IdentifiableObject> getIdObjects() {
        return new HashMap<>(idObjects);
    }
    
    /**
     * Sets the ID objects (for loading existing data).
     */
    public void setIdObjects(Map<String, IdentifiableObject> objects) {
        idObjects.clear();
        idListModel.clear();
        
        if (objects != null) {
            idObjects.putAll(objects);
            for (String id : objects.keySet()) {
                idListModel.addElement(id);
            }
        }
        
        showNoSelection();
    }
    
    /**
     * Clears all IDs.
     */
    public void clear() {
        idObjects.clear();
        idListModel.clear();
        showNoSelection();
    }
}

