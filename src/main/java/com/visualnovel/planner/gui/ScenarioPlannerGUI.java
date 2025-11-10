package com.visualnovel.planner.gui;

import com.visualnovel.model.ActionType;
import com.visualnovel.model.ActionRegistry;
import com.visualnovel.model.IdentifiableObject;
import com.visualnovel.model.Scenario;
import com.visualnovel.model.ScenarioAction;
import com.visualnovel.model.Timing;
import com.visualnovel.planner.ActionBuilder;
import com.visualnovel.planner.ScenarioPlanner;
import com.visualnovel.planner.TimingBuilder;
import com.visualnovel.util.ScenarioLoader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Main GUI window for the ScenarioPlanner.
 * Allows users to build scenarios visually with button clicks and text input.
 */
public class ScenarioPlannerGUI extends JFrame {
    private ScenarioPlanner planner;
    
    // Panels
    private JList<String> actionList;
    private DefaultListModel<String> actionListModel;
    private ActionSequencePanel sequencePanel;
    private ParameterInputPanel parameterPanel;
    private ScenarioMetadataPanel metadataPanel;
    private CharacterIdsPanel characterIdsPanel;
    
    // Timing panel
    private JPanel timingPanel;
    private ButtonGroup timingGroup;
    private JRadioButton immediateRadio;
    private JRadioButton interactionRadio;
    private JRadioButton animatedRadio;
    private JTextField durationField;
    private JComboBox<String> animationCombo;
    
    // Buttons
    private JButton addActionButton;
    private JButton updateActionButton;
    private JButton loadButton;
    private JButton exportButton;
    
    // Current editing state
    private int editingIndex = -1;
    private String currentActionType = null;
    
    public ScenarioPlannerGUI() {
        planner = new ScenarioPlanner();
        
        setTitle("Scenario Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        createComponents();
        layoutComponents();
        setupEventHandlers();
        
        pack();
        setSize(1400, 800);
        setLocationRelativeTo(null);
    }
    
    /**
     * Creates all GUI components.
     */
    private void createComponents() {
        // Action selection list
        actionListModel = new DefaultListModel<>();
        List<String> availableActions = ActionParameterMetadata.getAvailableActions();
        for (String action : availableActions) {
            actionListModel.addElement(action);
        }
        actionList = new JList<>(actionListModel);
        actionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        actionList.setBorder(new TitledBorder("Available Actions"));
        
        // Sequence panel
        sequencePanel = new ActionSequencePanel();
        sequencePanel.setListener(new ActionSequencePanel.ActionSequenceListener() {
            @Override
            public void onActionSelected(ScenarioAction action, int index) {
                editAction(action, index);
            }
            
            @Override
            public void onActionRemoved(int index) {
                // Action removed, nothing special needed
            }
            
            @Override
            public void onActionMoved(int fromIndex, int toIndex) {
                // Action moved, nothing special needed
            }
        });
        
        // Parameter panel
        parameterPanel = new ParameterInputPanel();
        
        // Timing panel
        createTimingPanel();
        
        // Metadata panel
        metadataPanel = new ScenarioMetadataPanel();
        
        // Character IDs panel
        characterIdsPanel = new CharacterIdsPanel();
        
        // Buttons
        addActionButton = new JButton("Add Action");
        updateActionButton = new JButton("Update Action");
        updateActionButton.setEnabled(false);
        loadButton = new JButton("Load from JSON");
        exportButton = new JButton("Export to JSON");
    }
    
    /**
     * Creates the timing configuration panel.
     */
    private void createTimingPanel() {
        timingPanel = new JPanel();
        timingPanel.setLayout(new BoxLayout(timingPanel, BoxLayout.Y_AXIS));
        timingPanel.setBorder(new TitledBorder("Timing"));
        
        timingGroup = new ButtonGroup();
        immediateRadio = new JRadioButton("Immediate", true);
        interactionRadio = new JRadioButton("Interaction");
        animatedRadio = new JRadioButton("Animated");
        
        timingGroup.add(immediateRadio);
        timingGroup.add(interactionRadio);
        timingGroup.add(animatedRadio);
        
        timingPanel.add(immediateRadio);
        timingPanel.add(interactionRadio);
        timingPanel.add(animatedRadio);
        
        // Animated timing options
        JPanel animatedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        animatedPanel.add(new JLabel("Duration (ms):"));
        durationField = new JTextField(10);
        animatedPanel.add(durationField);
        
        animatedPanel.add(new JLabel("Animation:"));
        animationCombo = new JComboBox<>(new String[]{"fadeIn", "flash"});
        animatedPanel.add(animationCombo);
        
        timingPanel.add(animatedPanel);
        
        // Enable/disable animated options based on selection
        ActionListener timingListener = e -> updateAnimatedFields();
        immediateRadio.addActionListener(timingListener);
        interactionRadio.addActionListener(timingListener);
        animatedRadio.addActionListener(timingListener);
        
        updateAnimatedFields();
    }
    
    /**
     * Updates the enabled state of animated timing fields.
     */
    private void updateAnimatedFields() {
        boolean enabled = animatedRadio.isSelected();
        durationField.setEnabled(enabled);
        animationCombo.setEnabled(enabled);
    }
    
    /**
     * Lays out all components in the window.
     */
    private void layoutComponents() {
        // Left panel: Action selection
        JScrollPane actionScroll = new JScrollPane(actionList);
        actionScroll.setPreferredSize(new Dimension(200, 0));
        
        // Center panel: Sequence and parameters
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Sequence panel
        sequencePanel.setPreferredSize(new Dimension(400, 0));
        
        // Right side: Parameters and timing
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // Parameter panel
        parameterPanel.setPreferredSize(new Dimension(400, 0));
        
        // Timing panel
        timingPanel.setPreferredSize(new Dimension(400, 120));
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addActionButton);
        buttonPanel.add(updateActionButton);
        
        rightPanel.add(parameterPanel, BorderLayout.CENTER);
        rightPanel.add(timingPanel, BorderLayout.SOUTH);
        rightPanel.add(buttonPanel, BorderLayout.NORTH);
        
        centerSplit.setLeftComponent(sequencePanel);
        centerSplit.setRightComponent(rightPanel);
        centerSplit.setDividerLocation(400);
        centerSplit.setResizeWeight(0.5);
        
        // Main split: Left (actions) and Center (sequence + parameters)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setLeftComponent(actionScroll);
        mainSplit.setRightComponent(centerSplit);
        mainSplit.setDividerLocation(200);
        mainSplit.setResizeWeight(0.0);
        
        // Bottom panel: Tabbed pane with Metadata and Character IDs
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.addTab("Metadata", metadataPanel);
        bottomTabs.addTab("Character IDs", characterIdsPanel);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(bottomTabs, BorderLayout.CENTER);
        
        // Button panel for load and export buttons
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtonPanel.add(loadButton);
        bottomButtonPanel.add(exportButton);
        bottomPanel.add(bottomButtonPanel, BorderLayout.EAST);
        
        // Add to main frame
        add(mainSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        // Action selection
        actionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = actionList.getSelectedValue();
                if (selected != null) {
                    currentActionType = selected;
                    parameterPanel.setActionType(selected);
                    editingIndex = -1;
                    addActionButton.setEnabled(true);
                    updateActionButton.setEnabled(false);
                }
            }
        });
        
        // Add action button
        addActionButton.addActionListener(e -> addAction());
        
        // Update action button
        updateActionButton.addActionListener(e -> updateAction());
        
        // Load button
        loadButton.addActionListener(e -> loadScenario());
        
        // Export button
        exportButton.addActionListener(e -> exportScenario());
    }
    
    /**
     * Adds a new action to the sequence.
     */
    private void addAction() {
        String actionType = actionList.getSelectedValue();
        if (actionType == null) {
            JOptionPane.showMessageDialog(this, "Please select an action type first.", 
                "No Action Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate required parameters
        List<String> missing = parameterPanel.validateRequired();
        if (!missing.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Missing required parameters: " + String.join(", ", missing),
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Build the action
        ScenarioAction action = buildAction(actionType);
        if (action != null) {
            sequencePanel.addAction(action);
            // Clear selection and reset
            actionList.clearSelection();
            parameterPanel.setActionType(null);
            currentActionType = null;
        }
    }
    
    /**
     * Updates the currently editing action.
     */
    private void updateAction() {
        // If editingIndex is -1, try to get the current selection from the sequence panel
        if (editingIndex < 0) {
            int selectedIndex = sequencePanel.getSelectedIndex();
            if (selectedIndex >= 0) {
                editingIndex = selectedIndex;
                // If currentActionType is null, get it from the selected action
                if (currentActionType == null) {
                    ScenarioAction selectedAction = sequencePanel.getSelectedAction();
                    if (selectedAction != null && selectedAction.getActionType() != null) {
                        currentActionType = selectedAction.getActionType().getJsonValue();
                        // Update the parameter panel with the selected action
                        parameterPanel.setActionType(currentActionType);
                        if (selectedAction.getParameters() != null) {
                            parameterPanel.setParameterValues(selectedAction.getParameters());
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No action selected for update. Please select an action from the sequence.", 
                    "No Action Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        String actionType = currentActionType;
        if (actionType == null) {
            // Try to get action type from action list selection
            String selectedActionType = actionList.getSelectedValue();
            if (selectedActionType != null) {
                actionType = selectedActionType;
                currentActionType = actionType;
            } else {
                JOptionPane.showMessageDialog(this, "No action type selected. Please select an action type from the list.", 
                    "No Action Type", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Validate required parameters
        List<String> missing = parameterPanel.validateRequired();
        if (!missing.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Missing required parameters: " + String.join(", ", missing),
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Build the action
        ScenarioAction action = buildAction(actionType);
        if (action != null) {
            sequencePanel.updateAction(editingIndex, action);
            // Reset editing state
            editingIndex = -1;
            currentActionType = null;
            actionList.clearSelection();
            parameterPanel.setActionType(null);
            addActionButton.setEnabled(true);
            updateActionButton.setEnabled(false);
        }
    }
    
    /**
     * Edits an existing action.
     */
    private void editAction(ScenarioAction action, int index) {
        editingIndex = index;
        currentActionType = action.getActionType() != null ? 
            action.getActionType().getJsonValue() : null;
        
        if (currentActionType != null) {
            // Select in action list
            int actionIndex = actionListModel.indexOf(currentActionType);
            if (actionIndex >= 0) {
                actionList.setSelectedIndex(actionIndex);
            }
            
            // Set parameters
            parameterPanel.setActionType(currentActionType);
            if (action.getParameters() != null) {
                parameterPanel.setParameterValues(action.getParameters());
            }
            
            // Set timing
            if (action.getTiming() != null) {
                Timing timing = action.getTiming();
                String type = timing.getType();
                if ("Immediate".equals(type)) {
                    immediateRadio.setSelected(true);
                } else if ("Interaction".equals(type)) {
                    interactionRadio.setSelected(true);
                } else if ("Animated".equals(type)) {
                    animatedRadio.setSelected(true);
                    if (timing.getDurationMs() != null) {
                        durationField.setText(String.valueOf(timing.getDurationMs()));
                    }
                    if (timing.getAnimation() != null) {
                        animationCombo.setSelectedItem(timing.getAnimation());
                    }
                }
            } else {
                immediateRadio.setSelected(true);
            }
            
            updateAnimatedFields();
        }
        
        addActionButton.setEnabled(false);
        updateActionButton.setEnabled(true);
    }
    
    /**
     * Builds a ScenarioAction from the current input.
     */
    private ScenarioAction buildAction(String actionType) {
        try {
            ActionBuilder builder = new ActionBuilder();
            
            // Set action type
            ActionRegistry registry = ActionRegistry.getInstance();
            ActionType type = registry.get(actionType);
            if (type == null) {
                JOptionPane.showMessageDialog(this, 
                    "Unknown action type: " + actionType,
                    "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            builder.actionType(type);
            
            // Set timing
            Timing timing = buildTiming();
            builder.timing(timing);
            
            // Set parameters
            Map<String, Object> params = parameterPanel.getParameterValues();
            if (!params.isEmpty()) {
                builder.parameters(params);
            }
            
            // Handle special cases for Battle action (OnWin/OnLose)
            if ("Battle".equals(actionType)) {
                Object onWin = params.get("OnWin");
                Object onLose = params.get("OnLose");
                
                if (onWin != null) {
                    if (onWin instanceof String && !((String) onWin).trim().isEmpty()) {
                        builder.onWin((String) onWin);
                    }
                }
                
                if (onLose != null) {
                    if (onLose instanceof String && !((String) onLose).trim().isEmpty()) {
                        builder.onLose((String) onLose);
                    }
                }
            }
            
            return builder.build();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error building action: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Builds a Timing object from the timing panel inputs.
     */
    private Timing buildTiming() {
        TimingBuilder builder = new TimingBuilder();
        
        if (immediateRadio.isSelected()) {
            builder.immediate();
        } else if (interactionRadio.isSelected()) {
            builder.interaction();
        } else if (animatedRadio.isSelected()) {
            int duration = 1000; // default
            try {
                String durationText = durationField.getText().trim();
                if (!durationText.isEmpty()) {
                    duration = Integer.parseInt(durationText);
                }
            } catch (NumberFormatException e) {
                // Use default
            }
            String animation = (String) animationCombo.getSelectedItem();
            builder.animated(duration, animation != null ? animation : "fadeIn");
        }
        
        return builder.build();
    }
    
    /**
     * Loads a scenario from a JSON file.
     */
    private void loadScenario() {
        // Show file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Scenario from JSON");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
            }
            
            @Override
            public String getDescription() {
                return "JSON Files (*.json)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            
            try {
                // Load the scenario
                ScenarioLoader loader = new ScenarioLoader();
                Scenario scenario = loader.loadScenario(filePath);
                
                // Reset editing state
                editingIndex = -1;
                currentActionType = null;
                actionList.clearSelection();
                parameterPanel.setActionType(null);
                addActionButton.setEnabled(true);
                updateActionButton.setEnabled(false);
                
                // Populate metadata panel
                if (scenario.getName() != null) {
                    metadataPanel.setName(scenario.getName());
                } else {
                    metadataPanel.setName("");
                }
                if (scenario.getDescription() != null) {
                    metadataPanel.setDescription(scenario.getDescription());
                } else {
                    metadataPanel.setDescription("");
                }
                if (scenario.getAuthor() != null) {
                    metadataPanel.setAuthor(scenario.getAuthor());
                } else {
                    metadataPanel.setAuthor("");
                }
                if (scenario.getVersion() != null) {
                    metadataPanel.setVersion(scenario.getVersion());
                } else {
                    metadataPanel.setVersion("");
                }
                if (scenario.getDate() != null) {
                    metadataPanel.setDate(scenario.getDate());
                } else {
                    metadataPanel.setDate("");
                }
                if (scenario.getCategory() != null) {
                    metadataPanel.setCategory(scenario.getCategory());
                } else {
                    metadataPanel.setCategory("");
                }
                if (scenario.getSubcategory() != null) {
                    metadataPanel.setSubcategory(scenario.getSubcategory());
                } else {
                    metadataPanel.setSubcategory("");
                }
                if (scenario.getBattleStrategy() != null) {
                    metadataPanel.setBattleStrategy(scenario.getBattleStrategy());
                } else {
                    metadataPanel.setBattleStrategy(null);
                }
                metadataPanel.setEntryPoint(scenario.isEntryPoint());
                if (scenario.getTags() != null) {
                    metadataPanel.setTags(scenario.getTags());
                } else {
                    metadataPanel.setTags(new java.util.ArrayList<>());
                }
                
                // Populate action sequence panel
                sequencePanel.clear();
                if (scenario.getSequence() != null) {
                    for (ScenarioAction action : scenario.getSequence()) {
                        sequencePanel.addAction(action);
                    }
                }
                
                // Populate character IDs panel
                if (scenario.getIds() != null) {
                    characterIdsPanel.setIdObjects(scenario.getIds());
                } else {
                    characterIdsPanel.clear();
                }
                
                // Update planner with loaded scenario
                planner = new ScenarioPlanner();
                if (scenario.getName() != null) planner.setName(scenario.getName());
                if (scenario.getDescription() != null) planner.setDescription(scenario.getDescription());
                if (scenario.getAuthor() != null) planner.setAuthor(scenario.getAuthor());
                if (scenario.getVersion() != null) planner.setVersion(scenario.getVersion());
                if (scenario.getDate() != null) planner.setDate(scenario.getDate());
                if (scenario.getCategory() != null) planner.setCategory(scenario.getCategory());
                if (scenario.getSubcategory() != null) planner.setSubcategory(scenario.getSubcategory());
                if (scenario.getBattleStrategy() != null) planner.setBattleStrategy(scenario.getBattleStrategy());
                planner.setEntryPoint(scenario.isEntryPoint());
                if (scenario.getTags() != null) planner.setTags(scenario.getTags());
                if (scenario.getSequence() != null) {
                    for (ScenarioAction action : scenario.getSequence()) {
                        planner.addAction(action);
                    }
                }
                if (scenario.getIds() != null) {
                    for (Map.Entry<String, IdentifiableObject> entry : scenario.getIds().entrySet()) {
                        planner.addId(entry.getKey(), entry.getValue());
                    }
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Scenario loaded successfully from:\n" + filePath,
                    "Load Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading scenario:\n" + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Unexpected error loading scenario:\n" + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Exports the scenario to a JSON file.
     */
    private void exportScenario() {
        // Get metadata
        String name = metadataPanel.getName();
        String description = metadataPanel.getDescription();
        String author = metadataPanel.getAuthor();
        String version = metadataPanel.getVersion();
        String date = metadataPanel.getDate();
        String category = metadataPanel.getCategory();
        String subcategory = metadataPanel.getSubcategory();
        String battleStrategy = metadataPanel.getBattleStrategy();
        boolean entryPoint = metadataPanel.isEntryPoint();
        List<String> tags = metadataPanel.getTags();
        
        // Set metadata on planner
        if (!name.isEmpty()) planner.setName(name);
        if (!description.isEmpty()) planner.setDescription(description);
        if (!author.isEmpty()) planner.setAuthor(author);
        if (!version.isEmpty()) planner.setVersion(version);
        if (!date.isEmpty()) planner.setDate(date);
        if (!category.isEmpty()) planner.setCategory(category);
        if (!subcategory.isEmpty()) planner.setSubcategory(subcategory);
        if (battleStrategy != null) planner.setBattleStrategy(battleStrategy);
        planner.setEntryPoint(entryPoint);
        if (!tags.isEmpty()) planner.setTags(tags);
        
        // Get actions from sequence
        java.util.List<ScenarioAction> actions = sequencePanel.getActions();
        
        // Clear existing actions and add new ones
        planner = new ScenarioPlanner();
        if (!name.isEmpty()) planner.setName(name);
        if (!description.isEmpty()) planner.setDescription(description);
        if (!author.isEmpty()) planner.setAuthor(author);
        if (!version.isEmpty()) planner.setVersion(version);
        if (!date.isEmpty()) planner.setDate(date);
        if (!category.isEmpty()) planner.setCategory(category);
        if (!subcategory.isEmpty()) planner.setSubcategory(subcategory);
        if (battleStrategy != null) planner.setBattleStrategy(battleStrategy);
        planner.setEntryPoint(entryPoint);
        if (!tags.isEmpty()) planner.setTags(tags);
        
        for (ScenarioAction action : actions) {
            planner.addAction(action);
        }
        
        // Save any pending changes in character IDs panel before export
        characterIdsPanel.saveCurrent();
        
        // Add character IDs
        Map<String, IdentifiableObject> idObjects = characterIdsPanel.getIdObjects();
        for (Map.Entry<String, IdentifiableObject> entry : idObjects.entrySet()) {
            planner.addId(entry.getKey(), entry.getValue());
        }
        
        // Show file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Scenario to JSON");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
            }
            
            @Override
            public String getDescription() {
                return "JSON Files (*.json)";
            }
        });
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            
            // Ensure .json extension
            if (!filePath.toLowerCase().endsWith(".json")) {
                filePath += ".json";
            }
            
            try {
                planner.export(filePath);
                JOptionPane.showMessageDialog(this, 
                    "Scenario exported successfully to:\n" + filePath,
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting scenario:\n" + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Main method to launch the GUI.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            ScenarioPlannerGUI gui = new ScenarioPlannerGUI();
            gui.setVisible(true);
        });
    }
}

