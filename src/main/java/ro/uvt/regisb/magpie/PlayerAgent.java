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
import ro.uvt.regisb.magpie.utils.ProcessAttributes;
import ro.uvt.regisb.magpie.utils.TimeInterval;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

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
                            byte[] b = Base64.getDecoder().decode(serializedConf.getBytes());
                            ByteArrayInputStream bi = new ByteArrayInputStream(b);
                            ObjectInputStream si = new ObjectInputStream(bi);

                            Configuration conf = (Configuration) si.readObject();

                            if (gui != null) {
                                gui.getCurrentMoodBox().setSelectedItem(conf.getMood());
                                gui.getInfoLabel().setText("Info: Successfully restored preferences.");
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

    private void setupUi() {
        gui = new PlayerGui(this); // Create a new PlayerGui owned by this agent.

        gui.setVisible(true);
        gui.validate();
    }

    public void broadcastNewMood(String mood) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent("mood:" + mood);
        msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        send(msg);
    }

    public void broadcastProcessMonitored(ProcessAttributes proc) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

            msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
            msg.addReceiver(new AID("magpie_processesmonitor", AID.ISLOCALNAME));
            msg.setContent("process:add:" + deserializeToBase64(proc));
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
            msg.setContent("timeslot:add:" + deserializeToBase64(interval));
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

    public void requestPlaylistExpansion() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

        msg.setContent("expand:2"); // TODO replace 2 with user-defined batch size
        msg.addReceiver(new AID("magpie_playlist", AID.ISLOCALNAME));
        send(msg);
        System.out.println("Player: Sent playlist expansion request.");
    }

    private String deserializeToBase64(Object obj) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream so = new ObjectOutputStream(bo);

        so.writeObject(obj);
        so.flush();
        return new String(Base64.getEncoder().encode(bo.toByteArray()));
    }
}
