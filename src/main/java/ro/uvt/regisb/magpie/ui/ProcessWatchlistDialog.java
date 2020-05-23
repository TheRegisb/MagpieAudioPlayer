package ro.uvt.regisb.magpie.ui;

import ro.uvt.regisb.magpie.utils.ProcessAttributes;

import javax.swing.*;

/**
 * Panel for process monitoring registration.
 * Intended to be used as a modal.
 */
public class ProcessWatchlistDialog extends JPanel {
    private JList tagsList;
    private JList includeList;
    private JList excludeList;
    private JButton toIncludeButton;
    private JButton fromIncludeButton;
    private JButton toExcludeButton;
    private JButton fromExcludeButton;
    private JLabel TagsLabel;
    private JLabel IncludeLabel;
    private JLabel ExcludeLabel;
    private JTextField processNameTextField;
    private JPanel panel;
    private JLabel processNameLabel;

    private boolean isChanging = false;

    /**
     * Default constructor.
     */
    public ProcessWatchlistDialog() {
        add(panel);
        // Listener for tag permutation: from the shared list to the inclusion or exclusion list.
        tagsList.addListSelectionListener(e -> {
            if (!isChanging) {
                isChanging = true;
                includeList.clearSelection();
                excludeList.clearSelection();
                isChanging = false;
            }

        });
        includeList.addListSelectionListener(e -> {
            if (!isChanging) {
                isChanging = true;
                tagsList.clearSelection();
                excludeList.clearSelection();
                isChanging = false;
            }
        });
        excludeList.addListSelectionListener(e -> {
            if (!isChanging) {
                isChanging = true;
                includeList.clearSelection();
                tagsList.clearSelection();
                isChanging = false;
            }
        });
        toIncludeButton.addActionListener(e -> {
            if (tagsList.getSelectedIndex() != -1) {
                ((DefaultListModel) includeList.getModel()).addElement(tagsList.getSelectedValue());
                ((DefaultListModel) tagsList.getModel()).remove(tagsList.getSelectedIndex());
            }
        });
        fromIncludeButton.addActionListener(e -> {
            if (includeList.getSelectedIndex() != -1) {
                ((DefaultListModel) tagsList.getModel()).addElement(includeList.getSelectedValue());
                ((DefaultListModel) includeList.getModel()).remove(includeList.getSelectedIndex());
            }
        });
        toExcludeButton.addActionListener(e -> {
            if (tagsList.getSelectedIndex() != -1) {
                ((DefaultListModel) excludeList.getModel()).addElement(tagsList.getSelectedValue());
                ((DefaultListModel) tagsList.getModel()).remove(tagsList.getSelectedIndex());
            }
        });
        fromExcludeButton.addActionListener(e -> {
            if (excludeList.getSelectedIndex() != -1) {
                ((DefaultListModel) tagsList.getModel()).addElement(excludeList.getSelectedValue());
                ((DefaultListModel) excludeList.getModel()).remove(excludeList.getSelectedIndex());
            }
        });
    }

    /**
     * Generate a modal from the instance.
     *
     * @return The termination status of an OK/Cancel plain-message modal.
     * @see JOptionPane
     */
    public int showDialog() {
        return JOptionPane.showOptionDialog(null, this,
                "Monitor Process",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"OK", "Cancel"}, "OK");
    }

    /**
     * Get process name.
     *
     * @return The user-provided process name.
     */
    public String getProcessName() {
        return processNameTextField.getText();
    }

    /**
     * Get the process attributes.
     * @return The user-provided attributes.
     */
    public ProcessAttributes getProcessAttributes() {
        ProcessAttributes attrs = new ProcessAttributes(processNameTextField.getText());

        for (int i = 0; i != includeList.getModel().getSize(); i++) {
            String attrString = (String) includeList.getModel().getElementAt(i);

            attrs.getTags().parse(attrString, 1);
        }
        for (int i = 0; i != excludeList.getModel().getSize(); i++) {
            String attrString = (String) excludeList.getModel().getElementAt(i);

            attrs.getTags().parse(attrString, -1);
        }
        return attrs;
    }

    /**
     * Override the form's default components creation.
     */
    private void createUIComponents() {
        includeList = new JList<>(new DefaultListModel<>());
        excludeList = new JList<>(new DefaultListModel<>());
        tagsList = new JList<>(new DefaultListModel<>());
    }
}
