/*
 * Copyright 2020 Régis BERTHELOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.uvt.regisb.magpie.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ro.uvt.regisb.magpie.utils.ProcessAttributes;

import javax.swing.*;
import java.awt.*;

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
        $$$setupUI$$$();
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
     *
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(9, 3, new Insets(0, 0, 0, 0), -1, -1));
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        defaultListModel1.addElement("Genre: Rock");
        defaultListModel1.addElement("Genre: Electro");
        defaultListModel1.addElement("Genre: Classical");
        defaultListModel1.addElement("Genre: Contemporary");
        defaultListModel1.addElement("Feel: Action");
        defaultListModel1.addElement("Feel: Calm");
        defaultListModel1.addElement("Feel: Mystical");
        defaultListModel1.addElement("Feel: Relaxed");
        defaultListModel1.addElement("Feel: Uplifting");
        defaultListModel1.addElement("High BPM");
        defaultListModel1.addElement("Low BPM");
        tagsList.setModel(defaultListModel1);
        tagsList.setSelectionMode(0);
        panel.add(tagsList, new GridConstraints(3, 0, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(103, 50), null, 0, false));
        includeList.setSelectionMode(0);
        panel.add(includeList, new GridConstraints(3, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        excludeList.setSelectionMode(0);
        panel.add(excludeList, new GridConstraints(7, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        toIncludeButton = new JButton();
        Font toIncludeButtonFont = this.$$$getFont$$$(null, -1, -1, toIncludeButton.getFont());
        if (toIncludeButtonFont != null) toIncludeButton.setFont(toIncludeButtonFont);
        toIncludeButton.setText("→");
        panel.add(toIncludeButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fromIncludeButton = new JButton();
        fromIncludeButton.setText("←");
        panel.add(fromIncludeButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toExcludeButton = new JButton();
        toExcludeButton.setText("→");
        panel.add(toExcludeButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fromExcludeButton = new JButton();
        fromExcludeButton.setText("←");
        panel.add(fromExcludeButton, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel.add(spacer1, new GridConstraints(5, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        TagsLabel = new JLabel();
        TagsLabel.setText("Tags");
        panel.add(TagsLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 16), null, 0, false));
        IncludeLabel = new JLabel();
        IncludeLabel.setText("Include");
        panel.add(IncludeLabel, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ExcludeLabel = new JLabel();
        ExcludeLabel.setText("Exclude");
        panel.add(ExcludeLabel, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processNameTextField = new JTextField();
        panel.add(processNameTextField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(375, 35), null, 1, false));
        processNameLabel = new JLabel();
        processNameLabel.setText("Process Name");
        panel.add(processNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel.add(separator1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
