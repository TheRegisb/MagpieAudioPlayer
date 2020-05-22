package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.commons.lang3.tuple.Pair;
import ro.uvt.regisb.magpie.utils.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

public class PlaylistAgent extends Agent {
    // TODO stores current tags weight
    private WeightedMediaFilter filter = new WeightedMediaFilter();

    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    // Playlist expansion requested
                    if (msg.getPerformative() == ACLMessage.REQUEST
                            && msg.getContent().matches("^" + C.PLAYLIST_EXPANSION_ACL + "\\d+$")) { // At least a single digit expected
                        requestMoreContent(msg);
                    }
                    // Forwarding playlist content to the player agent.
                    else if ((msg.getPerformative() == ACLMessage.PROPOSE
                            || msg.getPerformative() == ACLMessage.FAILURE)
                            && msg.getContent().startsWith(C.CONTENT_ACL)) {
                        msg.removeReceiver(this.getAgent().getAID());
                        msg.addReceiver(new AID(C.PLAYER_AID, AID.ISLOCALNAME));
                        send(msg);
                    }
                    // Reacting to a mood update
                    else if (msg.getPerformative() == ACLMessage.PROPOSE
                            && msg.getContent().matches(C.MOOD_ACL + "(-?)\\d+" + C.SEPARATOR + "(-?)\\d+")) {
                        int oldMood = Integer.parseInt(msg.getContent().split(C.SEPARATOR)[2]);
                        int newMood = Integer.parseInt(msg.getContent().split(C.SEPARATOR)[1]);

                        filter.tweakHighBPMCount(newMood + oldMood * -1);
                        filter.tweakLowBPMCount(newMood * -1 + oldMood);
                    }
                    // Reacting to time slot notification
                    else if (msg.getPerformative() == ACLMessage.PROPOSE
                            && msg.getContent().matches("^" + C.TIMESLOT_OBJ_ACL + ".*$")) {
                        TimeInterval ti = (TimeInterval) deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[1]);

                        if (ti != null) {
                            applyTags(ti.getTags());
                        }
                    }
                    // Reacting to a process notification
                    else if (msg.getPerformative() == ACLMessage.PROPOSE
                            && msg.getContent().matches("^" + C.PROCESS_OBJ_ACL + ".*$")) {
                        ProcessAttributes pa = (ProcessAttributes) deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[1]);

                        if (pa != null) {
                            applyTags(pa.getTags());
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void applyTags(Tags tags) {
        for (Pair<String, Integer> f : tags.getFeel()) {
            filter.addFeel(f.getKey(), f.getValue());
        }
        for (Pair<String, Integer> g : tags.getGenre()) {
            filter.addGenre(g.getKey(), g.getValue());
        }
        if (tags.getBpmTweak() < 0) {
            filter.tweakLowBPMCount(tags.getBpmTweak() * -1);
        } else {
            filter.tweakHighBPMCount(tags.getBpmTweak());
        }
    }

    private Object deserializeFromBase64(String serialized) {
        try {
            byte[] b = Base64.getDecoder().decode(serialized.getBytes());
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);

            return si.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void requestMoreContent(ACLMessage msg) {
        try {
            int totalToBeAdded = Integer.parseInt(msg.getContent().split(":")[1]);
            ACLMessage contentRequest = new ACLMessage(ACLMessage.REQUEST);

            contentRequest.addReceiver(new AID(C.CONTENTMNGR_AID, AID.ISLOCALNAME));
            // Writing the weighed filter object as a safe base 64 string for streaming over networks.
            contentRequest.setContent(String.format(C.CONTENT_ACL + "%d:%s", totalToBeAdded, IOUtil.serializeToBase64(filter)));
            send(contentRequest);
        } catch (NumberFormatException e) {
            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

            res.setContent(C.PLAYLIST_EXPANSION_ACL + "unknown");
            res.addReceiver(msg.getSender());
            send(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
