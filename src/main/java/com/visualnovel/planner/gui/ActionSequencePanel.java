package com.visualnovel.planner.gui;

import com.visualnovel.model.ScenarioAction;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel that displays the sequence of actions and allows editing, removing, and reordering.
 */
public class ActionSequencePanel extends JPanel {
    private DefaultListModel<ActionItem> listModel;
    private JList<ActionItem> actionList;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private ActionSequenceListener listener;
    
    /**
     * Represents an action item in the sequence.
     */
    public static class ActionItem {
        private final int index;
        private final ScenarioAction action;
        
        public ActionItem(int index, ScenarioAction action) {
            this.index = index;
            this.action = action;
        }
        
        public int getIndex() {
            return index;
        }
        
        public ScenarioAction getAction() {
            return action;
        }
        
        @Override
        public String toString() {
            if (action == null || action.getActionType() == null) {
                return "Unknown Action";
            }
            
            String actionName = action.getActionType().getJsonValue();
            StringBuilder sb = new StringBuilder((index + 1) + ". " + actionName);
            
            // Add key parameter info
            if (action.getParameters() != null) {
                java.util.Map<String, Object> params = action.getParameters();
                
                // ShowMessage: show message preview
                if ("ShowMessage".equals(actionName) && params.containsKey("message")) {
                    String message = params.get("message").toString();
                    if (message.length() > 40) {
                        message = message.substring(0, 37) + "...";
                    }
                    sb.append(": \"").append(message).append("\"");
                }
                // ShowSprite: show id and position
                else if ("ShowSprite".equals(actionName)) {
                    if (params.containsKey("id")) {
                        sb.append(" - ").append(params.get("id"));
                    }
                    if (params.containsKey("position")) {
                        sb.append(" (").append(params.get("position")).append(")");
                    }
                }
                // Battle: show teams
                else if ("Battle".equals(actionName)) {
                    if (params.containsKey("team1") && params.get("team1") instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> team1 = (List<String>) params.get("team1");
                        sb.append(" - Team1: ").append(team1.size()).append(" chars");
                    }
                    if (params.containsKey("team2") && params.get("team2") instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> team2 = (List<String>) params.get("team2");
                        sb.append(", Team2: ").append(team2.size()).append(" chars");
                    }
                }
                // Delay: show duration
                else if ("Delay".equals(actionName) && params.containsKey("delayMs")) {
                    sb.append(" - ").append(params.get("delayMs")).append("ms");
                }
            }
            
            // Add timing info
            if (action.getTiming() != null && action.getTiming().getType() != null) {
                sb.append(" [").append(action.getTiming().getType()).append("]");
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Listener interface for action sequence events.
     */
    public interface ActionSequenceListener {
        void onActionSelected(ScenarioAction action, int index);
        void onActionRemoved(int index);
        void onActionMoved(int fromIndex, int toIndex);
    }
    
    public ActionSequencePanel() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Action Sequence"));
        
        listModel = new DefaultListModel<>();
        actionList = new JList<>(listModel);
        actionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        actionList.setCellRenderer(new ActionListCellRenderer());
        
        // Add selection listener
        actionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
                if (listener != null) {
                    int selectedIndex = actionList.getSelectedIndex();
                    if (selectedIndex >= 0 && selectedIndex < listModel.size()) {
                        ActionItem item = listModel.getElementAt(selectedIndex);
                        listener.onActionSelected(item.getAction(), item.getIndex());
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(actionList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        moveUpButton = new JButton("↑");
        moveUpButton.setToolTipText("Move selected action up");
        moveUpButton.addActionListener(e -> moveSelectedUp());
        buttonPanel.add(moveUpButton);
        
        moveDownButton = new JButton("↓");
        moveDownButton.setToolTipText("Move selected action down");
        moveDownButton.addActionListener(e -> moveSelectedDown());
        buttonPanel.add(moveDownButton);
        
        removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> removeSelected());
        buttonPanel.add(removeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateButtonStates();
    }
    
    /**
     * Sets the listener for action sequence events.
     */
    public void setListener(ActionSequenceListener listener) {
        this.listener = listener;
    }
    
    /**
     * Adds an action to the sequence.
     */
    public void addAction(ScenarioAction action) {
        int index = listModel.size();
        listModel.addElement(new ActionItem(index, action));
        actionList.setSelectedIndex(index);
        updateButtonStates();
    }
    
    /**
     * Updates an action at the specified index.
     */
    public void updateAction(int index, ScenarioAction action) {
        if (index >= 0 && index < listModel.size()) {
            ActionItem item = listModel.getElementAt(index);
            listModel.setElementAt(new ActionItem(item.getIndex(), action), index);
            actionList.setSelectedIndex(index);
            updateButtonStates();
        }
    }
    
    /**
     * Removes the selected action.
     */
    private void removeSelected() {
        int selectedIndex = actionList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < listModel.size()) {
            ActionItem item = listModel.getElementAt(selectedIndex);
            listModel.remove(selectedIndex);
            
            // Update indices
            for (int i = 0; i < listModel.size(); i++) {
                ActionItem currentItem = listModel.getElementAt(i);
                listModel.setElementAt(new ActionItem(i, currentItem.getAction()), i);
            }
            
            if (listener != null) {
                listener.onActionRemoved(item.getIndex());
            }
            
            updateButtonStates();
        }
    }
    
    /**
     * Moves the selected action up.
     */
    private void moveSelectedUp() {
        int selectedIndex = actionList.getSelectedIndex();
        if (selectedIndex > 0 && selectedIndex < listModel.size()) {
            ActionItem item = listModel.getElementAt(selectedIndex);
            ActionItem prevItem = listModel.getElementAt(selectedIndex - 1);
            
            listModel.set(selectedIndex - 1, new ActionItem(selectedIndex - 1, item.getAction()));
            listModel.set(selectedIndex, new ActionItem(selectedIndex, prevItem.getAction()));
            
            actionList.setSelectedIndex(selectedIndex - 1);
            
            if (listener != null) {
                listener.onActionMoved(selectedIndex, selectedIndex - 1);
            }
            
            updateButtonStates();
        }
    }
    
    /**
     * Moves the selected action down.
     */
    private void moveSelectedDown() {
        int selectedIndex = actionList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < listModel.size() - 1) {
            ActionItem item = listModel.getElementAt(selectedIndex);
            ActionItem nextItem = listModel.getElementAt(selectedIndex + 1);
            
            listModel.set(selectedIndex, new ActionItem(selectedIndex, nextItem.getAction()));
            listModel.set(selectedIndex + 1, new ActionItem(selectedIndex + 1, item.getAction()));
            
            actionList.setSelectedIndex(selectedIndex + 1);
            
            if (listener != null) {
                listener.onActionMoved(selectedIndex, selectedIndex + 1);
            }
            
            updateButtonStates();
        }
    }
    
    /**
     * Updates button states based on selection.
     */
    private void updateButtonStates() {
        int selectedIndex = actionList.getSelectedIndex();
        boolean hasSelection = selectedIndex >= 0 && selectedIndex < listModel.size();
        
        removeButton.setEnabled(hasSelection);
        moveUpButton.setEnabled(hasSelection && selectedIndex > 0);
        moveDownButton.setEnabled(hasSelection && selectedIndex < listModel.size() - 1);
    }
    
    /**
     * Gets all actions in the sequence.
     */
    public List<ScenarioAction> getActions() {
        List<ScenarioAction> actions = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            actions.add(listModel.getElementAt(i).getAction());
        }
        return actions;
    }
    
    /**
     * Clears all actions from the sequence.
     */
    public void clear() {
        listModel.clear();
        updateButtonStates();
    }
    
    /**
     * Gets the currently selected index, or -1 if no selection.
     */
    public int getSelectedIndex() {
        int selectedIndex = actionList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < listModel.size()) {
            ActionItem item = listModel.getElementAt(selectedIndex);
            return item.getIndex();
        }
        return -1;
    }
    
    /**
     * Gets the currently selected action, or null if no selection.
     */
    public ScenarioAction getSelectedAction() {
        int selectedIndex = actionList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < listModel.size()) {
            ActionItem item = listModel.getElementAt(selectedIndex);
            return item.getAction();
        }
        return null;
    }
    
    /**
     * Custom cell renderer for action list items.
     */
    private static class ActionListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof ActionItem) {
                setText(value.toString());
            }
            
            return this;
        }
    }
}

