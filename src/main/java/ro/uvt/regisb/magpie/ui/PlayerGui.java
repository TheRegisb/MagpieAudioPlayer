package ro.uvt.regisb.magpie.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.commons.lang3.time.DateFormatUtils;
import ro.uvt.regisb.magpie.PlayerAgent;
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
    private JLabel infoLabel; // todo hide after set amount of time
    private JButton timeSlotDeleteButton;
    private JButton processesDeleteButton;
    private JSlider volumeSlider;
    private JSlider musicProgressSlider;
    private JScrollPane playlistScrollPane;
    private JSpinner batchSizeSpinner;
    private JLabel playtimeLabel;
    private String onStopPressed;

    private boolean sliderDragged = false;
    private boolean stillPlaying = false;

    protected PlayerAgent owner;
    protected MediaPlayer mediaPlayer = null;

    public PlayerGui(final PlayerAgent owner) {
        super("Magpie Audio Player");

        this.owner = owner;

        // Adding components missing from the IntelliJ GUI designer.
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
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
                    Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                    mediaPlayer = autoPlayerFrom(hit);
                    mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
                    mediaPlayer.play();
                    stillPlaying = true;
                }
            }
        });
        playList.getModel().addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                System.out.println("!" + playList.getSelectedIndex());
                if (mediaPlayer == null || mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                    if (!stillPlaying) {
                        playList.setSelectedIndex(playList.getSelectedIndex() + 1);
                    }
                    Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                    mediaPlayer = autoPlayerFrom(hit);
                    mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
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
                owner.requestPlaylistExpansion();
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
                    infoLabel.setText("Info: Downloading more titles.");
                    owner.requestPlaylistExpansion();
                    return;
                }
                Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                mediaPlayer = autoPlayerFrom(hit);
                mediaPlayer.setVolume((double) volumeSlider.getValue() / volumeSlider.getMaximum());
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.stop();
            }
            mediaPlayer.play();
            stillPlaying = true;
        });
        stopButton.addActionListener(e -> mediaPlayer.stop());
        pauseButton.addActionListener(e -> mediaPlayer.pause());
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


    }

    public JComboBox getCurrentMoodBox() {
        return currentMoodBox;
    }

    public JLabel getInfoLabel() {
        return infoLabel;
    }

    public void addProcessLabel(String label) {
        ((DefaultListModel) processesList.getModel()).addElement(label);
    }

    public void addTimeLabel(String label) {
        ((DefaultListModel) timeSlotList.getModel()).addElement(label);
    }

    public void addMediaPaths(java.util.List<String> medias) {
        DefaultListModel listContent = ((DefaultListModel) playList.getModel());

        for (String path : medias) {
            listContent.addElement(path);
        }
        infoLabel.setText("Info: Added " + medias.size() + " more track" + (medias.size() > 1 ? "s." : "."));
    }

    private MediaPlayer autoPlayerFrom(Media hit) {
        mediaPlayer = new MediaPlayer(hit);

        mediaPlayer.setOnEndOfMedia(() -> {
            if (playList.getSelectedIndex() + 1 == playList.getModel().getSize()) {
                mediaPlayer.stop();
                infoLabel.setText("Info: Downloading more titles.");
                owner.requestPlaylistExpansion();
                stillPlaying = false;
            } else { // Play next music in list
                playList.setSelectedIndex(playList.getSelectedIndex() + 1);
                Media hit1 = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                mediaPlayer = new MediaPlayer(hit1);
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
        return mediaPlayer;
    }

    private void createUIComponents() {
        processesList = new JList<>(new DefaultListModel<>());
        timeSlotList = new JList<>(new DefaultListModel<>());
        batchSizeSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 9, 1));
    }
}
