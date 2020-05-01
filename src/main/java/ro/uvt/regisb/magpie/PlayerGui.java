package ro.uvt.regisb.magpie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
                    // TODO replay selected music
                    // ((JList)e.getSource()).getSelectedValue();
                }
            }
        });
        currentMoodBox.addActionListener(new ActionListener() { // Change global mood on configuration change.
            public void actionPerformed(ActionEvent e) {
                assert currentMoodBox.getSelectedItem() != null;
                owner.broadcastNewMood(currentMoodBox.getSelectedItem().toString());
            }
        });
        // TODO add Play button event listener
        // TODO Play music at current playList index
        // TODO ask PlaylistManager to download more if playing last element of the playlist
        // TODO ask PlaylistManager to download if playList is empty

        // Final graphic setup.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        pack();
    }

    JComboBox getCurrentMoodBox() {
        return currentMoodBox;
    }

    JLabel getInfoLabel() {
        return infoLabel;
    }
}
