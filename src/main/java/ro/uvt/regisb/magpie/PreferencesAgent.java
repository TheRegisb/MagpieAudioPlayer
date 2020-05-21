package ro.uvt.regisb.magpie;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.Configuration;
import ro.uvt.regisb.magpie.utils.IOUtil;
import ro.uvt.regisb.magpie.utils.ProcessAttributes;
import ro.uvt.regisb.magpie.utils.TimeInterval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class PreferencesAgent extends Agent {
    private Preferences magpiePrefs = Preferences.userRoot().node("ro/uvt/regisb/magpie/preferences"); // For Windows, HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Prefs must be created by hand
    private Configuration conf = new Configuration(
            magpiePrefs.get("mood", "Neutral"),
            importProcessesAttributes(),
            importTimeIntervals()
    );

    @Override
    protected void setup() {
        System.out.println("Preferences Agent online.");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith("mood:")) {
                        changeCurrentMood(msg.getContent().split(":")[1]);
                    } else if (msg.getPerformative() == ACLMessage.REQUEST && msg.getContent().equals("configuration")) {
                        ACLMessage response = new ACLMessage(ACLMessage.INFORM);

                        response.addReceiver(msg.getSender());
                        try {
                            response.setContent(("conf:" + IOUtil.serializeToBase64(conf)));
                        } catch (IOException e) {
                            response.setContent("conf;");
                        }
                        send(response);
                    }
                    // Adding a new process
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^process:add:.*$")) {
                        try {
                            conf.addProcessAttributes((ProcessAttributes) IOUtil.deserializeFromBase64(msg.getContent().split(":")[2]));
                            exportProcessesAttributes();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    // Removing a process
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith("process:remove:")) {
                        if (conf.removeProcessAttributesByName(msg.getContent().split(":")[2])) {
                            exportProcessesAttributes();
                        }
                    }
                    // Adding a time slot
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^timeslot:add:.*$")) {
                        try {
                            conf.addTimeInterval((TimeInterval) IOUtil.deserializeFromBase64(msg.getContent().split(":")[2]));
                            exportTimeIntervals();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    // Removing time slot
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^timeslot:remove:\\[[01]\\d:[0-5]\\d, [01]\\d:[0-5]\\d]$")) { // timeslot is like [03:40, 13:20]
                        if (conf.removeTimeIntervalByName(msg.getContent().substring(16))) {
                            exportTimeIntervals();
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

    // TODO add procs watchlist and time slot
}
