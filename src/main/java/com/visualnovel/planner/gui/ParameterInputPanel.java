package com.visualnovel.planner.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Panel that dynamically generates parameter input fields based on the selected action type.
 */
public class ParameterInputPanel extends JPanel {
    private String currentActionType;
    private Map<String, JComponent> parameterFields;
    private JPanel fieldsPanel;
    private JScrollPane scrollPane;
    
    public ParameterInputPanel() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Action Parameters"));
        
        parameterFields = new HashMap<>();
        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        
        scrollPane = new JScrollPane(fieldsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Initial state - no action selected
        showNoActionSelected();
    }
    
    /**
     * Updates the panel to show parameters for the specified action type.
     * 
     * @param actionType The action type name (JSON value)
     */
    public void setActionType(String actionType) {
        this.currentActionType = actionType;
        parameterFields.clear();
        fieldsPanel.removeAll();
        
        if (actionType == null || actionType.isEmpty()) {
            showNoActionSelected();
            revalidate();
            repaint();
            return;
        }
        
        List<ActionParameterMetadata.ParameterDef> params = 
            ActionParameterMetadata.getParametersForAction(actionType);
        
        if (params.isEmpty()) {
            JLabel noParamsLabel = new JLabel("This action has no parameters.");
            noParamsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            fieldsPanel.add(noParamsLabel);
        } else {
            for (ActionParameterMetadata.ParameterDef param : params) {
                addParameterField(param);
            }
        }
        
        // Add spacing at the end
        fieldsPanel.add(Box.createVerticalGlue());
        
        revalidate();
        repaint();
    }
    
    /**
     * Adds a parameter input field based on the parameter definition.
     */
    private void addParameterField(ActionParameterMetadata.ParameterDef param) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel label = new JLabel(param.getName() + (param.isRequired() ? " *" : ""));
        label.setToolTipText(param.getDescription());
        fieldPanel.add(label, BorderLayout.NORTH);
        
        JComponent inputField = createInputField(param);
        fieldPanel.add(inputField, BorderLayout.CENTER);
        
        fieldsPanel.add(fieldPanel);
        fieldsPanel.add(Box.createVerticalStrut(10));
        
        parameterFields.put(param.getName(), inputField);
    }
    
    /**
     * Creates an appropriate input field based on the parameter type.
     */
    private JComponent createInputField(ActionParameterMetadata.ParameterDef param) {
        Class<?> type = param.getType();
        
        // Handle enum-like parameters with allowed values
        if (param.getAllowedValues() != null && !param.getAllowedValues().isEmpty()) {
            JComboBox<String> comboBox = new JComboBox<>(param.getAllowedValues().toArray(new String[0]));
            comboBox.setEditable(false); // Non-editable to enforce validation
            comboBox.setToolTipText("Allowed values: " + String.join(", ", param.getAllowedValues()));
            return comboBox;
        }
        
        // Handle List types
        if (List.class.isAssignableFrom(type)) {
            return createListEditor(param);
        }
        
        // Handle Integer types
        if (type == Integer.class || type == int.class) {
            JTextField textField = new JTextField();
            textField.setInputVerifier(new IntegerInputVerifier());
            return textField;
        }
        
        // Handle Object types (for complex nested structures)
        if (type == Object.class) {
            return createComplexTypeEditor(param);
        }
        
        // Default: String or other types as text field
        return new JTextField();
    }
    
    /**
     * Creates an editor for List parameters.
     */
    private JComponent createListEditor(ActionParameterMetadata.ParameterDef param) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea textArea = new JTextArea(3, 20);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setToolTipText("Enter one item per line");
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Store the text area for value retrieval
        parameterFields.put(param.getName() + "_textarea", textArea);
        
        return panel;
    }
    
    /**
     * Creates an editor for complex types (like OnWin/OnLose, choice options).
     */
    private JComponent createComplexTypeEditor(ActionParameterMetadata.ParameterDef param) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea textArea = new JTextArea(4, 20);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setToolTipText("Enter file path (String) or leave empty");
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Store the text area for value retrieval
        parameterFields.put(param.getName() + "_textarea", textArea);
        
        return panel;
    }
    
    /**
     * Shows a message when no action is selected.
     */
    private void showNoActionSelected() {
        JLabel label = new JLabel("Select an action to configure parameters");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsPanel.add(label);
    }
    
    /**
     * Gets the parameter values as a Map.
     * 
     * @return Map of parameter names to values
     */
    public Map<String, Object> getParameterValues() {
        Map<String, Object> values = new HashMap<>();
        
        if (currentActionType == null) {
            return values;
        }
        
        List<ActionParameterMetadata.ParameterDef> params = 
            ActionParameterMetadata.getParametersForAction(currentActionType);
        
        for (ActionParameterMetadata.ParameterDef param : params) {
            JComponent field = parameterFields.get(param.getName());
            if (field == null) {
                // Check for text area fields (for List or complex types)
                JComponent textAreaField = parameterFields.get(param.getName() + "_textarea");
                if (textAreaField != null && textAreaField instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) textAreaField;
                    JViewport viewport = scrollPane.getViewport();
                    Component view = viewport.getView();
                    if (view instanceof JTextArea) {
                        JTextArea textArea = (JTextArea) view;
                        String text = textArea.getText().trim();
                        if (!text.isEmpty()) {
                            if (param.getType() == List.class) {
                                // Parse list from lines
                                List<String> list = new ArrayList<>();
                                for (String line : text.split("\n")) {
                                    String trimmed = line.trim();
                                    if (!trimmed.isEmpty()) {
                                        list.add(trimmed);
                                    }
                                }
                                values.put(param.getName(), list);
                            } else {
                                // Complex type - store as String for now
                                values.put(param.getName(), text);
                            }
                        }
                    }
                }
                continue;
            }
            
            Object value = getFieldValue(field, param);
            if (value != null || param.isRequired()) {
                values.put(param.getName(), value);
            }
        }
        
        return values;
    }
    
    /**
     * Gets the value from an input field.
     */
    private Object getFieldValue(JComponent field, ActionParameterMetadata.ParameterDef param) {
        if (field instanceof JTextField) {
            String text = ((JTextField) field).getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            if (param.getType() == Integer.class || param.getType() == int.class) {
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return text;
        } else if (field instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) field;
            return comboBox.getSelectedItem();
        }
        return null;
    }
    
    /**
     * Sets parameter values from a Map.
     * 
     * @param values Map of parameter names to values
     */
    public void setParameterValues(Map<String, Object> values) {
        if (values == null) {
            return;
        }
        
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String paramName = entry.getKey();
            Object value = entry.getValue();
            
            JComponent field = parameterFields.get(paramName);
            if (field != null) {
                setFieldValue(field, value);
            } else {
                // Check for text area fields
                JComponent textAreaField = parameterFields.get(paramName + "_textarea");
                if (textAreaField != null && textAreaField instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) textAreaField;
                    JViewport viewport = scrollPane.getViewport();
                    Component view = viewport.getView();
                    if (view instanceof JTextArea) {
                        JTextArea textArea = (JTextArea) view;
                        if (value instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> list = (List<String>) value;
                            textArea.setText(String.join("\n", list));
                        } else {
                            textArea.setText(value != null ? value.toString() : "");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sets the value of an input field.
     */
    private void setFieldValue(JComponent field, Object value) {
        if (field instanceof JTextField) {
            ((JTextField) field).setText(value != null ? value.toString() : "");
        } else if (field instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) field;
            if (value != null) {
                comboBox.setSelectedItem(value.toString());
            }
        }
    }
    
    /**
     * Validates that all required parameters have values.
     * 
     * @return List of missing required parameter names, or empty list if valid
     */
    public List<String> validateRequired() {
        List<String> missing = new ArrayList<>();
        
        if (currentActionType == null) {
            return missing;
        }
        
        List<ActionParameterMetadata.ParameterDef> params = 
            ActionParameterMetadata.getParametersForAction(currentActionType);
        
        for (ActionParameterMetadata.ParameterDef param : params) {
            if (param.isRequired()) {
                Map<String, Object> values = getParameterValues();
                if (!values.containsKey(param.getName()) || values.get(param.getName()) == null) {
                    missing.add(param.getName());
                }
            }
        }
        
        return missing;
    }
    
    /**
     * Input verifier for integer fields.
     */
    private static class IntegerInputVerifier extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            if (input instanceof JTextField) {
                String text = ((JTextField) input).getText().trim();
                if (text.isEmpty()) {
                    return true; // Allow empty (for optional fields)
                }
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
    }
}

