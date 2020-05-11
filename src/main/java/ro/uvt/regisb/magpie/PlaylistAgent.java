package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.WeightedMediaFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

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
                    if (msg.getPerformative() == ACLMessage.REQUEST
                            && msg.getContent().startsWith("expand:")
                            && msg.getContent().length() >= 8) { // At least a single digit expected
                        requestMoreContent(msg);
                    }
                    if ((msg.getPerformative() == ACLMessage.PROPOSE
                            || msg.getPerformative() == ACLMessage.FAILURE)
                            && msg.getContent().startsWith("content:")) {
                        msg.removeReceiver(this.getAgent().getAID());
                        msg.addReceiver(new AID("magpie_player", AID.ISLOCALNAME));
                        send(msg);
                    }

                } else {
                    block();
                }
            }
        });
    }

    private void requestMoreContent(ACLMessage msg) {
        try {
            int totalToBeAdded = Integer.parseInt(msg.getContent().split(":")[1]);
            // TODO gather moods from other agents
            WeightedMediaFilter mf = new WeightedMediaFilter();

            mf.addFeel("Calm");
            mf.addGenre("Contemporary", 2);
            mf.tweakLowBPMCount(2);

            ACLMessage contentRequest = new ACLMessage(ACLMessage.REQUEST);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);

            so.writeObject(mf);
            so.flush();
            contentRequest.addReceiver(new AID("magpie_contentmanager", AID.ISLOCALNAME));
            // Writing the weighed filter object as a safe base 64 string for streaming over networks.
            contentRequest.setContent(String.format("content:%d:%s", totalToBeAdded, new String(Base64.getEncoder().encode(bo.toByteArray()))));
            send(contentRequest);
        } catch (NumberFormatException e) {
            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

            res.setContent("expand:unknown");
            res.addReceiver(msg.getSender());
            send(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
