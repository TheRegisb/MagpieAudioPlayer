package ro.uvt.regisb.magpie;

import javax.swing.*;
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
    private JButton timeSlotEditButton;
    private JList processesList;
    private JButton processesEditButton;
    private JLabel processesLabel;
    private JLabel infoLabel;
    private String onStopPressed;

    protected PlayerAgent owner;

    public PlayerGui(final PlayerAgent owner) {
        super("Magpie Audio Player");

        this.owner = owner;

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

        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel1);
        currentMoodBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                assert currentMoodBox.getSelectedItem() != null;
                owner.broadcastNewMood(currentMoodBox.getSelectedItem().toString());
            }
        });
        pack();
    }

    JComboBox getCurrentMoodBox() {
        return currentMoodBox;
    }

    JLabel getInfoLabel() {
        return infoLabel;
    }
}
