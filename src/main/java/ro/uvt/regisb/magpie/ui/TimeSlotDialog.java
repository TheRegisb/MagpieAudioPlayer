package ro.uvt.regisb.magpie.ui;

import ro.uvt.regisb.magpie.utils.TimeInterval;

import javax.swing.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

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

    public TimeSlotDialog() {
        add(panel1);
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

    private void createUIComponents() {
        timeStartSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        timeStartSpinner.setEditor(new JSpinner.DateEditor(timeStartSpinner, "HH:mm"));
        timeEndSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        timeEndSpinner.setEditor(new JSpinner.DateEditor(timeEndSpinner, "HH:mm"));
        tagsList = new JList<>(new DefaultListModel<>());
        includeList = new JList<>(new DefaultListModel<>());
        excludeList = new JList<>(new DefaultListModel<>());
    }

    public int showDialog() {
        return JOptionPane.showOptionDialog(null, this,
                "Time Slot Definition",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"OK", "Cancel"}, "OK");
    }

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
}
