package ro.uvt.regisb.magpie;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    }

    JComboBox getCurrentMoodBox() {
        return currentMoodBox;
    }

    JLabel getInfoLabel() {
        return infoLabel;
    }

    private MediaPlayer autoPlayerFrom(Media hit) {
        mediaPlayer = new MediaPlayer(hit);

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (playList.getSelectedIndex() + 1 == playList.getModel().getSize()) {
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
}
