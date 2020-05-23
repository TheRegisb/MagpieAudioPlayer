package ro.uvt.regisb.magpie.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ro.uvt.regisb.magpie.utils.TimeInterval;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Panel for time slot monitoring registration.
 * Intended to be used as a modal.
 */
public class TimeSlotDialog extends JPanel {
    private JList tagsList;
    private JList includeList;
    private JList excludeList;
    private JButton toIncludeButton;
    private JButton fromIncludeButton;
    private JButton toExcludeButton;
    private JButton fromExcludeButton;
    private JSpinner timeEndSpinner;
    private JSpinner timeStartSpinner;
    private JLabel timeLabel;
    private JLabel includeLabel;
    private JLabel excludeLabel;
    private JPanel panel1;

    private boolean isChanging = false;

    /**
     * Default constructor.
     */
    public TimeSlotDialog() {
        $$$setupUI$$$();
        add(panel1);
        // Listener for tags selection: From the shared list to inclusion/exclusion list and vice versa.
        tagsList.addListSelectionListener(e -> { // TODO consider making a common 'Tags Selection' panel
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
     * Override form's default components creation.
     */
    private void createUIComponents() {
        timeStartSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        timeStartSpinner.setEditor(new JSpinner.DateEditor(timeStartSpinner, "HH:mm"));
        timeEndSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        timeEndSpinner.setEditor(new JSpinner.DateEditor(timeEndSpinner, "HH:mm"));
        tagsList = new JList<>(new DefaultListModel<>());
        includeList = new JList<>(new DefaultListModel<>());
        excludeList = new JList<>(new DefaultListModel<>());
    }

    /**
     * Generate a modal from the instance.
     *
     * @return The termination status of an OK/Cancel plain-message modal.
     * @see JOptionPane
     */
    public int showDialog() {
        return JOptionPane.showOptionDialog(null, this,
                "Time Slot Definition",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"OK", "Cancel"}, "OK");
    }

    /**
     * Generate a TimeInterval.
     *
     * @return A TimeInterval based on the user-given attributes.
     */
    public TimeInterval getTimeInterval() {
        try {
            Calendar start = Calendar.getInstance();
            Calendar stop = Calendar.getInstance();

            start.setTime((Date) timeStartSpinner.getValue());
            stop.setTime((Date) timeEndSpinner.getValue());

            TimeInterval result = new TimeInterval(
                    String.format("%d:%d", start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE)),
                    String.format("%d:%d", stop.get(Calendar.HOUR_OF_DAY), stop.get(Calendar.MINUTE))
            );

            for (int i = 0; i != includeList.getModel().getSize(); i++) {
                result.getTags().parse((String) includeList.getModel().getElementAt(i), 1);
            }
            for (int i = 0; i != excludeList.getModel().getSize(); i++) {
                result.getTags().parse((String) excludeList.getModel().getElementAt(i), -1);
            }
            return result;
        } catch (ParseException e) {
            return null;
        }
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
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(12, 4, new Insets(0, 0, 0, 0), -1, -1));
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
        panel1.add(tagsList, new GridConstraints(3, 0, 9, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        panel1.add(includeList, new GridConstraints(3, 2, 4, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        panel1.add(excludeList, new GridConstraints(8, 2, 4, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        toIncludeButton = new JButton();
        toIncludeButton.setText("→");
        panel1.add(toIncludeButton, new GridConstraints(3, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toExcludeButton = new JButton();
        toExcludeButton.setText("→");
        panel1.add(toExcludeButton, new GridConstraints(8, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        includeLabel = new JLabel();
        includeLabel.setText("Include");
        panel1.add(includeLabel, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Tags");
        panel1.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel1.add(separator1, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        timeLabel = new JLabel();
        timeLabel.setText("Time Interval");
        panel1.add(timeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(timeEndSpinner, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(timeStartSpinner, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        excludeLabel = new JLabel();
        excludeLabel.setText("Exclude");
        panel1.add(excludeLabel, new GridConstraints(7, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fromIncludeButton = new JButton();
        fromIncludeButton.setText("←");
        panel1.add(fromIncludeButton, new GridConstraints(5, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fromExcludeButton = new JButton();
        fromExcludeButton.setText("←");
        panel1.add(fromExcludeButton, new GridConstraints(10, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
