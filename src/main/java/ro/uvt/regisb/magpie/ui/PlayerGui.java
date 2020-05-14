package ro.uvt.regisb.magpie.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import ro.uvt.regisb.magpie.PlayerAgent;
import ro.uvt.regisb.magpie.utils.ProcessAttributes;
import ro.uvt.regisb.magpie.utils.TimeInterval;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
    private JList playList;
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
    private String onStopPressed;

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
                    mediaPlayer.play();
                }
            }
        });
        currentMoodBox.addActionListener(new ActionListener() { // Change global mood on configuration change.
            public void actionPerformed(ActionEvent e) {
                assert currentMoodBox.getSelectedItem() != null;
                owner.broadcastNewMood(currentMoodBox.getSelectedItem().toString());
            }
        });
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mediaPlayer == null) {
                    if (playList.getModel().getSize() == 0) {
                        infoLabel.setText("Info: Downloading more titles.");
                        owner.requestPlaylistExpansion();
                    }
                    if (playList.getSelectedValue() == null) {
                        playList.setSelectedIndex(0);
                    }
                    Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                    mediaPlayer = autoPlayerFrom(hit);
                }
                mediaPlayer.play();
            }
        });

        // Final graphic setup.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        pack();
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.stop();
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.pause();
            }
        });
        processesNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessWatchlistDialog form = new ProcessWatchlistDialog();

                if (form.showDialog() == JOptionPane.OK_OPTION) {
                    ProcessAttributes proc = form.getProcessAttributes();

                    if (proc.getName().isBlank()) {
                        JOptionPane.showMessageDialog(null,
                                "Missing process name.", "Monitor Process",
                                JOptionPane.WARNING_MESSAGE);
                    } else if (proc.getGenre().isEmpty() && proc.getFeel().isEmpty() && proc.getBpmTweak() == 0) {
                        JOptionPane.showMessageDialog(null,
                                "Missing process attributes.", "Monitor Process",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        ((DefaultListModel) processesList.getModel()).addElement(form.getProcessName());
                        owner.broadcastProcessMonitored(proc);
                    }
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
                TimeInterval aled = form.getTimeInterval();

                if (aled != null) {
                    System.out.println(aled);
                    try {
                        System.out.println(aled.isTimeInInterval(new SimpleDateFormat("HH:mm").parse("18:00")));
                        System.out.println(aled.isTimeInInterval(new SimpleDateFormat("HH:mm").parse("03:00")));
                    } catch (ParseException exp) {
                        exp.printStackTrace();
                    }
                } else {
                    System.out.println("null?!");
                }
            }

        });
    }

    public JComboBox getCurrentMoodBox() {
        return currentMoodBox;
    }

    public JLabel getInfoLabel() {
        return infoLabel;
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

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (playList.getSelectedIndex() + 1 == playList.getModel().getSize()) {
                    infoLabel.setText("Info: Downloading more titles.");
                    owner.requestPlaylistExpansion();
                } else { // Play next music in list
                    playList.setSelectedIndex(playList.getSelectedIndex() + 1);
                    Media hit = new Media(new File(playList.getSelectedValue().toString()).toURI().toString());

                    mediaPlayer = new MediaPlayer(hit);
                    mediaPlayer.play();
                }
            }
        });
        return mediaPlayer;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        processesList = new JList<>(new DefaultListModel<>());
        timeSlotList = new JList<>(new DefaultListModel<>());
    }
}
