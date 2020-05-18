package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class MoodAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("Mood agent online");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().matches("^mood:[a-zA-Z]+$")) {
                        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);

                        response.addReceiver(new AID("magpie_playlist", AID.ISLOCALNAME));
                        switch (msg.getContent().split(":")[1]) {
                            case "Calm":
                                response.setContent("mood:-2");
                                break;
                            case "Neurtal":
                                response.setContent("mood:0");
                                break;
                            case "Energetic":
                                response.setContent("mood:2");
                                break;
                            default: // Remove speculative parameters and setup error message
                                response.setContent("mood:unknown");
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
