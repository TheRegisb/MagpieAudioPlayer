package ro.uvt.regisb.magpie.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.commons.lang3.time.DateFormatUtils;
import ro.uvt.regisb.magpie.agent.PlayerAgent;
import ro.uvt.regisb.magpie.utils.ProcessAttributes;
import ro.uvt.regisb.magpie.utils.TimeInterval;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * Audio player GUI.
 */
public class PlayerGui extends JFrame {
    // From the .form
    private JPanel panel1;
    private JButton stopButton;
    private JButton playButton;
    private JButton pauseButton;
    private JButton previousButton;
    private JButton nextButton;
    private JTextPane playlistTextPane;
    private JTabbedPane tabbedPane1;
    private JList playList = new JList<>(new DefaultListModel<>());
    private JLabel currentMoodLabel;
    private JComboBox currentMoodBox;
    private JList timeSlotList;
    private JLabel timeSlotLabel;
    private JButton timeSlotNewButton;
    private JList processesList;
    private JButton processesNewButton;
    private JLabel processesLabel;
    private JLabel infoLabel;
    private JButton timeSlotDeleteButton;
    private JButton processesDeleteButton;
    private JSlider volumeSlider;
    private JSlider musicProgressSlider;
    private JScrollPane playlistScrollPane;
    private JSpinner batchSizeSpinner;
    private JLabel playtimeLabel;

    private boolean sliderDragged = false;
    private boolean stillPlaying = false;
    private boolean remoteError = false;
    private String errorDesc;

    protected PlayerAgent owner;
    protected MediaPlayer mediaPlayer = null;

    /**
     * Default constructor.
     * Create the frame and assign its owner.
     *
     * @param owner A valid PlayerAgent.
     * @see PlayerAgent
     */
    public PlayerGui(final PlayerAgent owner) {
        super("Magpie Audio Player");

        this.owner = owner;

        // Adding components missing from the IntelliJ GUI designer.
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        $$$setupUI$$$();
        JMenuItem exitItem = new JMenuItem(new AbstractAction("Quit") {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        playList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistScrollPane.setViewportView(playList);
        // Changing default icon.
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/magpie_icon.png")));

        // Adding events listener.

        playList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (e.getClickCount() == 2) {
                    playList.setSelectedIndex(playList.locationToIndex(e.getPoint()));
                    if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.stop();
                    }
                    if (playList.getSelectedIndex() == playList.getModel().getSize() - 1) {
                        owner.requestPlaylistExpansion((int) batchSizeSpinner.getValue());
                    }
                    Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                    mediaPlayer = autoPlayerFrom(hit);
                    mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
                    infoLabel.setText("Starting playback.");
                    mediaPlayer.play();
                    stillPlaying = true;
                }
            }
        });
        playList.getModel().addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                if (mediaPlayer == null || mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                    if (!stillPlaying) {
                        playList.setSelectedIndex(playList.getSelectedIndex() + 1);
                    }
                    Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                    mediaPlayer = autoPlayerFrom(hit);
                    mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
                    infoLabel.setText("Info: Starting playback.");
                    mediaPlayer.play();
                }
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
            }
        });
        nextButton.addActionListener(e -> {
            if (playList.getSelectedIndex() + 1 >= playList.getModel().getSize() - 1) { // One or zero medias left
                owner.requestPlaylistExpansion((int) batchSizeSpinner.getValue());
            }
            if (playList.getSelectedIndex() == playList.getModel().getSize() - 1) {
                stillPlaying = false;
            }
            if (mediaPlayer != null && playList.getSelectedIndex() != playList.getModel().getSize() - 1) {
                playList.setSelectedIndex(playList.getSelectedValue() == null ? 0 : playList.getSelectedIndex() + 1);
                mediaPlayer.stop();
                Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                mediaPlayer = autoPlayerFrom(hit);
                mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
                infoLabel.setText("Info: Starting playback.");
                mediaPlayer.play();
                stillPlaying = true;
            }
        });
        previousButton.addActionListener(e -> {
            if (playList.getSelectedValue() == null) {
                playList.setSelectedIndex(0);
            }
            if (mediaPlayer != null) {
                playList.setSelectedIndex(playList.getSelectedIndex() == 0 ? 0 : playList.getSelectedIndex() - 1);
                mediaPlayer.stop();
                Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                mediaPlayer = autoPlayerFrom(hit);
                mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
                infoLabel.setText("Info: Starting playback.");
                mediaPlayer.play();
                stillPlaying = true;
            }
        });
        currentMoodBox.addActionListener(e -> {
            assert currentMoodBox.getSelectedItem() != null;
            owner.broadcastNewMood(currentMoodBox.getSelectedItem().toString());
        });
        playButton.addActionListener(e -> {
            if (mediaPlayer == null) {
                if (playList.getSelectedIndex() + 1 >= playList.getModel().getSize() - 1) { // One or zero medias left
                    if (!remoteError) {
                        infoLabel.setText("Info: Downloading more titles.");
                        owner.requestPlaylistExpansion((int) batchSizeSpinner.getValue());
                    } else {
                        infoLabel.setText(errorDesc);
                    }
                    return;
                }
                Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                mediaPlayer = autoPlayerFrom(hit);
                mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.stop();
            }
            infoLabel.setText("Info: Starting playback.");
            mediaPlayer.play();
            stillPlaying = true;
        });
        stopButton.addActionListener(e -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.STOPPED) {
                mediaPlayer.stop();
                infoLabel.setText("Info: Playback stopped.");
            }
        });
        pauseButton.addActionListener(e -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            }
        });
        processesNewButton.addActionListener(e -> {
            ProcessWatchlistDialog form = new ProcessWatchlistDialog();

            if (form.showDialog() == JOptionPane.OK_OPTION) {
                ProcessAttributes proc = form.getProcessAttributes();

                if (proc.getName().isBlank()) {
                    JOptionPane.showMessageDialog(null,
                            "Missing process name.", "Monitor Process",
                            JOptionPane.WARNING_MESSAGE);
                } else if (proc.getTags().areEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Missing process attributes.", "Monitor Process",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    ((DefaultListModel) processesList.getModel()).addElement(form.getProcessName());
                    owner.broadcastProcessMonitored(proc);
                }
            }

        });
        processesDeleteButton.addActionListener(e -> {
            if (processesList.getSelectedIndex() != -1) {
                owner.broadcastProcessUnmonitored((String) processesList.getSelectedValue());
                ((DefaultListModel) processesList.getModel()).remove(processesList.getSelectedIndex());
            }
        });
        timeSlotNewButton.addActionListener(e -> {
            TimeSlotDialog form = new TimeSlotDialog();

            if (form.showDialog() == JOptionPane.OK_OPTION) {
                TimeInterval interval = form.getTimeInterval();

                if (interval == null) {
                    JOptionPane.showMessageDialog(null,
                            "Unable to create time slot.", "Time Slot Definition",
                            JOptionPane.ERROR_MESSAGE);
                } else if (interval.getTags().areEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Missing slot attributes.", "Time Slot Definition",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    ((DefaultListModel) timeSlotList.getModel()).addElement(interval.toString());
                    owner.broadcastTimeSlotRegister(interval);
                }
            }
        });
        timeSlotDeleteButton.addActionListener(e -> {
            if (timeSlotList.getSelectedIndex() != -1) {
                owner.broadcastTimeSlotUnregister((String) timeSlotList.getSelectedValue());
                ((DefaultListModel) timeSlotList.getModel()).remove(timeSlotList.getSelectedIndex());
            }
        });
        volumeSlider.addChangeListener(evt -> {
            if (!sliderDragged) {
                mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
            }
        });
        musicProgressSlider.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sliderDragged = true;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                sliderDragged = true;
                BasicSliderUI ui = (BasicSliderUI) musicProgressSlider.getUI();
                musicProgressSlider.setValue(ui.valueForXPosition(e.getX()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (mediaPlayer != null) {
                    BasicSliderUI ui = (BasicSliderUI) musicProgressSlider.getUI();
                    musicProgressSlider.setValue(ui.valueForXPosition(e.getX()));
                    mediaPlayer.seek(
                            mediaPlayer.getTotalDuration()
                                    .divide(100.0)
                                    .multiply(ui.valueForXPosition(e.getX()))
                    );
                }
                sliderDragged = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        // Final graphic setup.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        pack();


        batchSizeSpinner.addChangeListener(e -> {
            owner.broadcastBatchSizeChange((int) batchSizeSpinner.getValue());
        });
    }

    /**
     * Get "Current Mood" box.
     *
     * @return Combo box for the "Current Mood".
     */
    public JComboBox getCurrentMoodBox() {
        return currentMoodBox;
    }

    /**
     * Get "Download Batch Size" spinner.
     *
     * @return Spinner for the "Download Batch Size".
     */
    public JSpinner getBatchSizeSpinner() {
        return batchSizeSpinner;
    }

    /**
     * Get the "Information" label.
     *
     * @return The "Information" label.
     */
    public JLabel getInfoLabel() {
        return infoLabel;
    }

    /**
     * Add a process label to the processes list.
     * Process integration should be synchronized with the ProcessesAgent by the caller.
     *
     * @param label Name of the process.
     */
    public void addProcessLabel(String label) {
        ((DefaultListModel) processesList.getModel()).addElement(label);
    }

    /**
     * Add a time interval literal to the time slot list.
     * Time slot integration should be synchronized with the TimeAgent by the caller.
     *
     * @param label Time interval literal.
     * @see TimeInterval
     */
    public void addTimeLabel(String label) {
        ((DefaultListModel) timeSlotList.getModel()).addElement(label);
    }

    /**
     * Add local paths to a valid media.
     *
     * @param medias Paths literal.
     */
    public void addMediaPaths(java.util.List<String> medias) {
        DefaultListModel listContent = ((DefaultListModel) playList.getModel());

        for (String path : medias) {
            listContent.addElement(path);
        }
        infoLabel.setText("Info: Added " + medias.size() + " more track" + (medias.size() > 1 ? "s." : "."));
    }

    /**
     * Enable or disable remote error state.
     *
     * @param state Is the remote error active.
     * @param what  Description of the error.
     */
    public void setErrorState(boolean state, String what) {
        remoteError = true;
        errorDesc = what;
        infoLabel.setText(what);
    }

    /**
     * Create a new MediaPlayer for the given Media.
     * Create a new MediaPlayer that load the next media in playlist on end and
     * update both the info and time-lapse labels at playtime?
     *
     * @param hit Media to be played immediately.
     * @return A MediaPlayer with the described properties.
     */
    private MediaPlayer autoPlayerFrom(Media hit) {
        mediaPlayer = new MediaPlayer(hit);

        mediaPlayer.setOnEndOfMedia(() -> {
            if (playList.getSelectedIndex() + 1 == playList.getModel().getSize()) {
                mediaPlayer.stop();
                if (!remoteError) {
                    infoLabel.setText("Info: Downloading more titles.");
                    owner.requestPlaylistExpansion((int) batchSizeSpinner.getValue());
                } else {
                    infoLabel.setText(errorDesc);
                }
                stillPlaying = false;
            } else { // Play next music in list
                playList.setSelectedIndex(playList.getSelectedIndex() + 1);
                Media hit1 = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                mediaPlayer = new MediaPlayer(hit1);
                infoLabel.setText("Info: Starting playback.");
                mediaPlayer.play();

                stillPlaying = true;
            }
        });
        mediaPlayer.currentTimeProperty().addListener(e -> {
            boolean greaterThanAHour = mediaPlayer.getTotalDuration().greaterThanOrEqualTo(Duration.hours(1));

            playtimeLabel.setText(String.format("%s / %s",
                    DateFormatUtils.format((int) mediaPlayer.getCurrentTime().toMillis(), greaterThanAHour ? "HH:mm:ss" : "mm:ss"),
                    DateFormatUtils.format((int) mediaPlayer.getTotalDuration().toMillis(), greaterThanAHour ? "HH:mm:ss" : "mm:ss")
            ));
            musicProgressSlider.setValue((int) (100 * mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds()));
        });
        mediaPlayer.setOnPlaying(() -> {
            infoLabel.setText(String.format("%s by %s",
                    hit.getMetadata().getOrDefault("title", "song"),
                    hit.getMetadata().getOrDefault("artist", "unknown artist")));
        });
        return mediaPlayer;
    }

    /**
     * Override the form's default components creation.
     */
    private void createUIComponents() {
        processesList = new JList<>(new DefaultListModel<>());
        timeSlotList = new JList<>(new DefaultListModel<>());
        batchSizeSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 9, 1));
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
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1 = new JTabbedPane();
        panel1.add(tabbedPane1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 10, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Player", panel2);
        stopButton = new JButton();
        stopButton.setText("⏹");
        panel2.add(stopButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pauseButton = new JButton();
        pauseButton.setText("⏸");
        panel2.add(pauseButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        previousButton = new JButton();
        previousButton.setText("⏮");
        panel2.add(previousButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nextButton = new JButton();
        nextButton.setText("⏭");
        panel2.add(nextButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        playButton = new JButton();
        playButton.setText("▶");
        panel2.add(playButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        volumeSlider = new JSlider();
        volumeSlider.setInverted(false);
        volumeSlider.setMaximum(100);
        volumeSlider.setOrientation(0);
        volumeSlider.setValue(100);
        volumeSlider.setValueIsAdjusting(false);
        panel2.add(volumeSlider, new GridConstraints(0, 7, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Vol:");
        panel2.add(label1, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        musicProgressSlider = new JSlider();
        musicProgressSlider.setMaximum(100);
        musicProgressSlider.setValue(0);
        panel2.add(musicProgressSlider, new GridConstraints(1, 2, 1, 8, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        playlistScrollPane = new JScrollPane();
        panel2.add(playlistScrollPane, new GridConstraints(2, 0, 1, 10, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        playtimeLabel = new JLabel();
        playtimeLabel.setText("00:00 / 00:00");
        panel2.add(playtimeLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(9, 5, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Configuration", panel3);
        currentMoodBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Neurtal");
        defaultComboBoxModel1.addElement("Energetic");
        defaultComboBoxModel1.addElement("Calm");
        currentMoodBox.setModel(defaultComboBoxModel1);
        panel3.add(currentMoodBox, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel3.add(timeSlotList, new GridConstraints(2, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(245, 50), null, 0, false));
        timeSlotNewButton = new JButton();
        timeSlotNewButton.setText("New");
        panel3.add(timeSlotNewButton, new GridConstraints(2, 2, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel3.add(processesList, new GridConstraints(5, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(245, 50), null, 0, false));
        currentMoodLabel = new JLabel();
        currentMoodLabel.setEnabled(true);
        currentMoodLabel.setHorizontalTextPosition(11);
        currentMoodLabel.setText("Current Mood");
        panel3.add(currentMoodLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeSlotLabel = new JLabel();
        timeSlotLabel.setText("Time Slots");
        panel3.add(timeSlotLabel, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processesLabel = new JLabel();
        processesLabel.setText("Processes Watchlist");
        panel3.add(processesLabel, new GridConstraints(5, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processesNewButton = new JButton();
        processesNewButton.setText("New");
        panel3.add(processesNewButton, new GridConstraints(5, 2, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeSlotDeleteButton = new JButton();
        timeSlotDeleteButton.setText("Delete Selected");
        panel3.add(timeSlotDeleteButton, new GridConstraints(3, 2, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processesDeleteButton = new JButton();
        processesDeleteButton.setText("Delete Selected");
        panel3.add(processesDeleteButton, new GridConstraints(6, 2, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel3.add(separator1, new GridConstraints(4, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        panel3.add(separator2, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Download batch size");
        panel3.add(label2, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator3 = new JSeparator();
        panel3.add(separator3, new GridConstraints(7, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panel3.add(batchSizeSpinner, new GridConstraints(8, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("Info: Loading components");
        panel1.add(infoLabel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
