package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import javafx.embed.swing.JFXPanel;
import ro.uvt.regisb.magpie.ui.PlayerGui;
import ro.uvt.regisb.magpie.utils.Configuration;
import ro.uvt.regisb.magpie.utils.IOUtil;
import ro.uvt.regisb.magpie.utils.ProcessAttributes;
import ro.uvt.regisb.magpie.utils.TimeInterval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PlayerAgent extends Agent {
    final JFXPanel jfxPanel = new JFXPanel();
    private PlayerGui gui = null;

    @Override
    protected void setup() {
        System.out.println("Player online.");
        addBehaviour(new CyclicBehaviour(this) { // ACL Message reader
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith("conf:")
                            && msg.getContent().length() > 5) {
                        String serializedConf = msg.getContent().split(":")[1];

                        try { // Re-creating Configuration object from its Base64 serialized form
                            Configuration conf = (Configuration) IOUtil.deserializeFromBase64(serializedConf);

                            if (gui != null) {
                                applyConfiguration(conf);
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    if (msg.getPerformative() == ACLMessage.PROPOSE
                            && msg.getContent().startsWith("content:")) {
                        gui.addMediaPaths(new ArrayList<>(Arrays.asList(msg.getContent().split(":")[1].split(", "))));
                    }
                } else {
                    block();
                }
            }
        }); // End of message handler behavior.

        // Spawning required agents
        PlatformController controller = getContainerController();

        try {
            controller.createNewAgent("magpie_preferences", "ro.uvt.regisb.magpie.PreferencesAgent", null).start();
            controller.createNewAgent("magpie_playlist", "ro.uvt.regisb.magpie.PlaylistAgent", null).start();
            controller.createNewAgent("magpie_contentmanager", "ro.uvt.regisb.magpie.ContentManagerAgent", new String[]{"local", "audiosample.sqlite.db"}).start(); // TODO change to variables
            controller.createNewAgent("magpie_processesmonitor", "ro.uvt.regisb.magpie.ProcessesAgent", null).start();
            controller.createNewAgent("magpie_time", "ro.uvt.regisb.magpie.TimeAgent", null).start();
            controller.createNewAgent("magpie_mood", "ro.uvt.regisb.magpie.MoodAgent", null).start();
        } catch (ControllerException e) {
            e.printStackTrace();
            doDelete();
        }

        setupUi();
        gui.getInfoLabel().setText("Info: Startup complete.");
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

        // Retrieving last session configuration by asking the PreferencesAgent
        msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        msg.setContent("configuration");
        send(msg);
    }

    private void applyConfiguration(Configuration conf) {
        gui.getCurrentMoodBox().setSelectedItem(conf.getMood());
        for (ProcessAttributes e : conf.getProcessesAttributes()) {
            gui.addProcessLabel(e.getName());
        }
        for (TimeInterval e : conf.getTimeIntervals()) {
            gui.addTimeLabel(e.toString());
        }
        gui.getBatchSizeSpinner().setValue(conf.getBatchSize());
        gui.getInfoLabel().setText("Info: Successfully restored preferences.");
    }

    private void setupUi() {
        gui = new PlayerGui(this); // Create a new PlayerGui owned by this agent.

        gui.setVisible(true);
        gui.validate();
    }

    public void broadcastNewMood(String mood) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent("mood:" + mood);
        msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        msg.addReceiver(new AID("magpie_mood", AID.ISLOCALNAME));
        send(msg);
    }

    public void broadcastProcessMonitored(ProcessAttributes proc) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

            msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
            msg.addReceiver(new AID("magpie_processesmonitor", AID.ISLOCALNAME));
            msg.setContent("process:add:" + IOUtil.serializeToBase64(proc));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastProcessUnmonitored(String procName) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        msg.addReceiver(new AID("magpie_processesmonitor", AID.ISLOCALNAME));
        msg.setContent("process:remove:" + procName);
        send(msg);
    }

    public void broadcastTimeSlotRegister(TimeInterval interval) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

            msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
            msg.addReceiver(new AID("magpie_time", AID.ISLOCALNAME));
            msg.setContent("timeslot:add:" + IOUtil.serializeToBase64(interval));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastTimeSlotUnregister(String slotName) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        msg.addReceiver(new AID("magpie_time", AID.ISLOCALNAME));
        msg.setContent("timeslot:remove:" + slotName);
        send(msg);
    }

    public void broadcastBatchSizeChange(int batchSize) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent("batch:" + batchSize);
        msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        send(msg);
    }

    public void requestPlaylistExpansion(int batchSize) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

        msg.setContent("expand:" + batchSize);
        msg.addReceiver(new AID("magpie_playlist", AID.ISLOCALNAME));
        send(msg);
    }
}
