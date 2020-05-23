package ro.uvt.regisb.magpie.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import javafx.embed.swing.JFXPanel;
import ro.uvt.regisb.magpie.ui.PlayerGui;
import ro.uvt.regisb.magpie.utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Audio player agent.
 * Core agent acting as a bridge between the UI and the other agents.
 * Must be the first and only agent to be spawned externally.
 */
public class PlayerAgent extends Agent {
    final JFXPanel jfxPanel = new JFXPanel(); // Call required to boot JavaFX, even if variable is unused
    private PlayerGui gui = null;

    /**
     * Agent setup.
     * Setup ACL messages handlers, spawn required agents, setup UI and restore previous configuration.
     */
    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) { // ACL Message reader
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    // Reacting to a configuration restoration
                    if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^" + C.CONFIGURATION_RESTORE_ACL + ".*$")) {
                        String serializedConf = msg.getContent().split(C.SEPARATOR)[1];

                        try { // Re-creating Configuration object from its Base64 serialized form
                            Configuration conf = (Configuration) IOUtil.deserializeFromBase64(serializedConf);

                            if (gui != null) {
                                applyConfiguration(conf);
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    // Reacting to a playlist content expansion
                    else if (msg.getPerformative() == ACLMessage.PROPOSE
                            && msg.getContent().startsWith(C.CONTENT_ACL)) {
                        gui.addMediaPaths(new ArrayList<>(Arrays.asList(msg.getContent().split(C.SEPARATOR)[1].split(", "))));
                    }
                    // Reacting to a ContentManagerAgent failure
                    else if (msg.getPerformative() == ACLMessage.FAILURE
                            && msg.getContent().startsWith(C.MANAGEMENT_FAILURE_ACL)) {
                        gui.setErrorState(true,
                                "Error: Cannot connect to media source: " + msg.getContent().split(C.SEPARATOR)[2]);
                    }
                } else {
                    block();
                }
            }
        }); // End of message handler behavior.

        // Spawning required agents
        PlatformController controller = getContainerController();
        String[] args = (String[]) getArguments();

        if (args == null) {
            args = new String[]{"audiosample.sqlite.db", "local"};
        }

        try {
            controller.createNewAgent(C.PREFERENCES_AID, PreferencesAgent.class.getCanonicalName(), null).start();
            controller.createNewAgent(C.PLAYLIST_AID, PlaylistAgent.class.getCanonicalName(), null).start();
            controller.createNewAgent(C.CONTENTMNGR_AID, ContentManagerAgent.class.getCanonicalName(),
                    new String[]{ // Trying to use the provided arguments, or fallback to default
                            args.length >= 2 && args[1] != null ? args[1] : "local",
                            args.length >= 1 && args[0] != null ? args[0] : "audiosample.sqlite.db"
                    }).start(); // TODO respond to exception from bad adapter
            controller.createNewAgent(C.PROCESSES_AID, ProcessesAgent.class.getCanonicalName(), null).start();
            controller.createNewAgent(C.TIME_AID, TimeAgent.class.getCanonicalName(), null).start();
            controller.createNewAgent(C.MOOD_AID, MoodAgent.class.getCanonicalName(), null).start();
        } catch (ControllerException e) {
            e.printStackTrace();
            doDelete();
        }

        setupUi();
        gui.getInfoLabel().setText("Info: Startup complete.");
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

        // Retrieving last session configuration by asking the PreferencesAgent
        msg.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
        msg.setContent(C.CONFIGURATION_ACL);
        send(msg);
    }

    /**
     * Apply a configuration preset to the UI.
     *
     * @param conf Configuration preset.
     * @see Configuration
     */
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

    /**
     * Spawn a PlayerGui assigned to this instance.
     *
     * @see PlayerGui
     */
    private void setupUi() {
        gui = new PlayerGui(this); // Create a new PlayerGui owned by this agent.

        gui.setVisible(true);
        gui.validate();
    }

    /**
     * Notify agents of mood change.
     * Broadcast the new mood literal to concerned agents.
     * @param mood Mood literal.
     */
    public void broadcastNewMood(String mood) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent(C.MOOD_ACL + mood);
        msg.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
        msg.addReceiver(new AID(C.MOOD_AID, AID.ISLOCALNAME));
        send(msg);
    }

    /**
     * Notify agents of process registration.
     * Broadcast the attributes to concerned agents.
     * @param proc New process' attributes.
     */
    public void broadcastProcessMonitored(ProcessAttributes proc) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

            msg.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
            msg.addReceiver(new AID(C.PROCESSES_AID, AID.ISLOCALNAME));
            msg.setContent(C.PROCESS_ADD_ACL + IOUtil.serializeToBase64(proc));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify agents of process un-registration.
     * Broadcast the process name to the concerned agents.
     * @param procName Name of the removed process.
     */
    public void broadcastProcessUnmonitored(String procName) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
        msg.addReceiver(new AID(C.PROCESSES_AID, AID.ISLOCALNAME));
        msg.setContent(C.PROCESS_REMOVE_ACL + procName);
        send(msg);
    }

    /**
     * Notify agents of time slot registration.
     * Broadcast the attributes to concerned agents.
     * @param interval New time slot' attributes.
     */
    public void broadcastTimeSlotRegister(TimeInterval interval) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

            msg.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
            msg.addReceiver(new AID(C.TIME_AID, AID.ISLOCALNAME));
            msg.setContent(C.TIMESLOT_ADD_ACL + IOUtil.serializeToBase64(interval));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify agents of time slot un-regitration.
     * Broadcast the time slot name to concerned agents.
     * @param slotName TimeInterval literal.
     * @see TimeInterval
     */
    public void broadcastTimeSlotUnregister(String slotName) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
        msg.addReceiver(new AID(C.TIME_AID, AID.ISLOCALNAME));
        msg.setContent(C.TIMESLOT_REMOVE_ACL + slotName);
        send(msg);
    }

    /**
     * Notify agents of download batch size change.
     * Broadcast the new batch size to concerned agents.
     * @param batchSize New download batch size.
     */
    public void broadcastBatchSizeChange(int batchSize) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent(C.BATCH_SIZE_ACL + batchSize);
        msg.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
        send(msg);
    }

    /**
     * Request more medias from PlaylistAgent.
     * @param batchSize Number of medias to retrieve.
     */
    public void requestPlaylistExpansion(int batchSize) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

        msg.setContent(C.PLAYLIST_EXPANSION_ACL + batchSize);
        msg.addReceiver(new AID(C.PLAYLIST_AID, AID.ISLOCALNAME));
        send(msg);
    }
}
