/*
 * Copyright 2020 Régis BERTHELOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.uvt.regisb.magpie.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.commons.lang3.tuple.Pair;
import ro.uvt.regisb.magpie.utils.*;

import java.io.IOException;

/**
 * Playlist manager agent.
 * Make the bridge between the PlayerAgent and the ContentManagerAgent.
 * Uses information provided by the other agent to generate a
 * pertinent media filter.
 */
public class PlaylistAgent extends Agent {
    private WeightedMediaFilter filter = new WeightedMediaFilter();

    /**
     * Agent setup.
     * Setup ACL message handler, to support media query and response and tags effects.
     */
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
                        try {
                            TimeInterval ti = (TimeInterval) IOUtil.deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[1]);

                            applyTags(ti.getTags());
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    // Reacting to a process notification
                    else if (msg.getPerformative() == ACLMessage.PROPOSE
                            && msg.getContent().matches("^" + C.PROCESS_OBJ_ACL + ".*$")) {
                        try {
                            ProcessAttributes pa = (ProcessAttributes) IOUtil.deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[1]);

                            applyTags(pa.getTags());
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    /**
     * Apply a set of tag to the current filter.
     *
     * @param tags Tags to apply.
     * @see Tags
     */
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

    /**
     * Notify the ContentManagerAgent of a content request.
     * Generate a REQUEST ACL message to the ContentManagerAgent
     * using the source message and the instance's current filter.
     *
     * @param msg Source request.
     */
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
