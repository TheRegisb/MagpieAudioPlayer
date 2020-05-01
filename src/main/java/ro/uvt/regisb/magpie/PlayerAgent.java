package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import ro.uvt.regisb.magpie.utils.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

public class PlayerAgent extends Agent {
    protected PlayerGui gui = null;

    @Override
    protected void setup() {
        System.out.println("Player online.");
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    System.out.println("Player: " + msg.getContent());
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
                } else {
                    block();
                }
            }
        });

        PlatformController controller = getContainerController();

        // Spawning required agents
        try {
            controller.createNewAgent("magpie_preferences", "ro.uvt.regisb.magpie.PreferencesAgent", null).start();
            controller.createNewAgent("magpie_playlist", "ro.uvt.regisb.magpie.PlaylistAgent", null).start();
        } catch (ControllerException e) {
            e.printStackTrace();
            doDelete();
        }

        setupUi();
        gui.getInfoLabel().setText("Info: Startup complete.");
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

        msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        msg.setContent("configuration");
        send(msg);
    }

    private void setupUi() {
        gui = new PlayerGui(this); // Create a new PlayerGui owned by this agent.

        gui.setVisible(true);
        gui.validate();
    }

    void broadcastNewMood(String mood) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent("mood:" + mood);
        msg.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        send(msg);
        System.out.println("Msg sent");
    }
}
