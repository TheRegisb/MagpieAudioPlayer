package ro.uvt.regisb.magpie.agent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Configuration saver and restorer.
 * Store the current configuration of the Magpie player and its updates at runtime to a Preferences node.
 * Used to transmit Configuration object to restore agents previous setup.
 *
 * @see Preferences
 * @see Configuration
 */
public class PreferencesAgent extends Agent {
    private Preferences magpiePrefs = Preferences.userRoot().node("ro/uvt/regisb/magpie/preferences"); // For Windows, HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Prefs must be created by hand
    private Configuration conf = new Configuration(
            magpiePrefs.get("mood", "Neutral"),
            importProcessesAttributes(),
            importTimeIntervals(),
            importBatchSize()
    );

    /**
     * Agent setup.
     * Setup the ACL message handling, from which start all other methods invocation.
     */
    @Override
    protected void setup() {
        // ACL messages handler
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    // Reacting to a mood change
                    if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith(C.MOOD_ACL)) {
                        changeCurrentMood(msg.getContent().split(C.SEPARATOR)[1]);
                    }
                    // Reacting to a "restore configuration" request
                    else if (msg.getPerformative() == ACLMessage.REQUEST && msg.getContent().equals(C.CONFIGURATION_ACL)) {
                        ACLMessage response = new ACLMessage(ACLMessage.INFORM);

                        response.addReceiver(msg.getSender());
                        try {
                            response.setContent((C.CONFIGURATION_RESTORE_ACL + IOUtil.serializeToBase64(conf)));
                        } catch (IOException e) {
                            response.setContent(C.CONFIGURATION_RESTORE_ACL);
                        }
                        send(response);
                    }
                    // Adding a new process
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^" + C.PROCESS_ADD_ACL + ".*$")) {
                        try {
                            conf.addProcessAttributes((ProcessAttributes) IOUtil.deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[2]));
                            exportProcessesAttributes();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    // Removing a process
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith(C.PROCESS_REMOVE_ACL)) {
                        if (conf.removeProcessAttributesByName(msg.getContent().split(C.SEPARATOR)[2])) {
                            exportProcessesAttributes();
                        }
                    }
                    // Adding a time slot
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^" + C.TIMESLOT_ADD_ACL + ".*$")) {
                        try {
                            conf.addTimeInterval((TimeInterval) IOUtil.deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[2]));
                            exportTimeIntervals();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    // Removing time slot
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^"
                            + C.TIMESLOT_REMOVE_ACL
                            + "\\[[012]\\d:[0-5]\\d, [012]\\d:[0-5]\\d]$")) { // timeslot is like [03:40, 13:20]
                        if (conf.removeTimeIntervalByName(msg.getContent().substring(C.TIMESLOT_REMOVE_ACL.length()))) {
                            exportTimeIntervals();
                        }
                    }
                    // Editing download batch size
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^" + C.BATCH_SIZE_ACL + "(?!(-))\\d+$")) {
                        try {
                            int batchSize = Integer.parseInt(msg.getContent().split(C.SEPARATOR)[1]);

                            conf.setBatchSize(batchSize);
                            exportBatchSize();
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    /**
     * Update the mood.
     * Updates the mood in the active Configuration object and in the preferences node.
     *
     * @param mood Current mood literal.
     */
    private void changeCurrentMood(String mood) {
        conf.setCurrentMood(mood);
        magpiePrefs.put("mood", mood);
    }

    /**
     * Persist time slots.
     * Serialize the current state of the Configuration's time slot to the preferences node.
     */
    private void exportTimeIntervals() {
        try {
            magpiePrefs.put("timeslots", IOUtil.serializeToBase64(conf.getTimeIntervals()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Persist processes attributes.
     * Serialize the current state of the Configuration's processes attributes to the preferences node.
     */
    private void exportProcessesAttributes() {
        try {
            magpiePrefs.put("procsattrs", IOUtil.serializeToBase64(conf.getProcessesAttributes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Persist batch size.
     * Serialize the current state of the Configuration's batch size to the preferences node.
     */
    private void exportBatchSize() {
        magpiePrefs.put("dlbufsize", Integer.toString(conf.getBatchSize()));
    }

    /**
     * Import time slots.
     * Deserialize the content of the preferences' time slot into a list of TimeInterval.
     * @return A list of TimeInterval. Empty on error.
     * @see TimeInterval
     */
    private List<TimeInterval> importTimeIntervals() {
        String nodeContent = magpiePrefs.get("timeslots", "");

        if (!nodeContent.isEmpty()) {
            try {
                return (List<TimeInterval>) IOUtil.deserializeFromBase64(nodeContent);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Import processes attributes.
     * Deserialize the content of the preferences' processes attributes into a list of ProcessAttributes.
     * @return A list of ProcessAttributes. Empty on error.
     * @see ProcessAttributes
     */
    private List<ProcessAttributes> importProcessesAttributes() {
        String nodeContent = magpiePrefs.get("procsattrs", "");

        if (!nodeContent.isEmpty()) {
            try {
                return (List<ProcessAttributes>) IOUtil.deserializeFromBase64(nodeContent);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Import download batch size.
     * @return A batch size. Default batch size constant on error.
     * @see C
     */
    private int importBatchSize() {
        String nodeContent = magpiePrefs.get("dlbufsize", C.DEFAULT_BATCH_SIZE);

        try {
            return Integer.parseInt(nodeContent);
        } catch (NumberFormatException e) {
            return Integer.parseInt(C.DEFAULT_BATCH_SIZE);
        }
    }
}
