package ro.uvt.regisb.magpie;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class PlaylistAgent extends Agent {
    // TODO stores current tags weight
    @Override
    protected void setup() {
        System.out.println("Playlist agent online.");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    System.out.println(msg.getContent());
                    if (msg.getPerformative() == ACLMessage.REQUEST
                            && msg.getContent().startsWith("expand:")
                            && msg.getContent().length() >= 8) { // At least a single digit expected
                        try {
                            int totalToBeAdded = Integer.parseInt(msg.getContent().split(":")[1]);

                            System.out.println("Playlist: Request to expand the playlist by " + totalToBeAdded);
                            // TODO gather moods from other agents
                            // TODO send request to ContentManagerAgent
                        } catch (NumberFormatException e) {
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.setContent("expand:unknown");
                            res.addReceiver(msg.getSender());
                            send(res);
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }
}
