package ro.uvt.regisb.magpie;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.Configuration;
import ro.uvt.regisb.magpie.utils.IOUtil;

import java.io.IOException;
import java.util.prefs.Preferences;

public class PreferencesAgent extends Agent {
    private Preferences magpiePrefs = Preferences.userRoot().node("ro/uvt/regisb/magpie/preferences"); // For Windows, HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Prefs must be created by hand

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
                        Configuration oldConf = new Configuration(magpiePrefs.get("mood", "Neutral"));
                        String oldConfSerialized = "";

                        try {
                            oldConfSerialized = IOUtil.serializeToBase64(oldConf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ACLMessage response = new ACLMessage(ACLMessage.INFORM);

                        response.addReceiver(msg.getSender());
                        response.setContent("conf:" + oldConfSerialized);
                        send(response);
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

    // TODO add procs watchlist and time slot
}
