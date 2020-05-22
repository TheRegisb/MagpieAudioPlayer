package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.C;

public class MoodAgent extends Agent {
    private int previousMood = 0;

    @Override
    protected void setup() {
        // ACL messages handler
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    // Reacting to a mood change
                    if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().matches("^" + C.MOOD_ACL + "[a-zA-Z]+$")) {
                        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);

                        // Convert the mood literal to a numeric value for tweaking the BPM of the PlaylistAgent's filter
                        response.addReceiver(new AID(C.PLAYLIST_AID, AID.ISLOCALNAME));
                        switch (msg.getContent().split(C.SEPARATOR)[1]) {
                            case "Calm":
                                response.setContent(C.MOOD_ACL + "-2" + C.SEPARATOR + previousMood);
                                previousMood = -2;
                                break;
                            case "Neutral":
                                response.setContent(C.MOOD_ACL + "0" + C.SEPARATOR + previousMood);
                                previousMood = 0;
                                break;
                            case "Energetic":
                                response.setContent(C.MOOD_ACL + "2" + C.SEPARATOR + previousMood);
                                previousMood = 2;
                                break;
                            default: // Remove speculative parameters and setup error message
                                response.setContent(C.MOOD_ACL + "unknown");
                                response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                                response.clearAllReceiver();
                                response.addReceiver(msg.getSender());
                        }
                        send(response);
                    }
                } else {
                    block();
                }
            }
        });
    }
}
