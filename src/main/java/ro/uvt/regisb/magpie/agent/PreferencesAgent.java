package ro.uvt.regisb.magpie.agent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class PreferencesAgent extends Agent {
    private Preferences magpiePrefs = Preferences.userRoot().node("ro/uvt/regisb/magpie/preferences"); // For Windows, HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Prefs must be created by hand
    private Configuration conf = new Configuration(
            magpiePrefs.get("mood", "Neutral"),
            importProcessesAttributes(),
            importTimeIntervals(),
            importBatchSize()
    );

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
                            && msg.getContent().matches("^" + C.BATCH_SIZE_ACL + "\\d+$")) {
                        try {
                            exportBatchSize(Integer.parseInt(msg.getContent().split(C.SEPARATOR)[1]));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void changeCurrentMood(String mood) {
        magpiePrefs.put("mood", mood);
    }

    private void exportTimeIntervals() {
        try {
            magpiePrefs.put("timeslots", IOUtil.serializeToBase64(conf.getTimeIntervals()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportProcessesAttributes() {
        try {
            magpiePrefs.put("procsattrs", IOUtil.serializeToBase64(conf.getProcessesAttributes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportBatchSize(int batchSize) {
        magpiePrefs.put("dlbufsize", Integer.toString(batchSize));
    }

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

    private int importBatchSize() {
        String nodeContent = magpiePrefs.get("dlbufsize", C.DEFAULT_BATCH_SIZE);

        try {
            return Integer.parseInt(nodeContent);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
