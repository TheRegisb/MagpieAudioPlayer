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
import ro.uvt.regisb.magpie.content.LocalSqliteAdapter;
import ro.uvt.regisb.magpie.content.MediaRetriever;
import ro.uvt.regisb.magpie.utils.C;
import ro.uvt.regisb.magpie.utils.IOUtil;
import ro.uvt.regisb.magpie.utils.WeightedMediaFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Remote and local content manager agent.
 * Setup media retrievers and respond to playlist expansion requests.
 * Agent self-delete on failure to setup media retrievers.
 */
public class ContentManagerAgent extends Agent {
    private MediaRetriever mr = null;

    /**
     * Agent setup.
     * Instantiate the media retrievers using the agent's arguments
     * and setup the ACL messages handlers.
     * Argument are expected to be [adapter format, address].
     */
    @Override
    protected void setup() {
        String[] args = (String[]) getArguments(); // Expect arguments of style ["local", address_or_path]

        if (args.length != 2 || args[0] == null || args[1] == null) {
            informPlayerOfFailure("Invalid adapter format and/or target");
            doDelete();
        }

        if (args[0].equals("local")) {
            mr = new LocalSqliteAdapter();
            if (!new File(args[1]).exists() || !mr.connect("jdbc:sqlite:" + args[1])) {
                informPlayerOfFailure("Unable to connect to the " + args[1] + " database");
                doDelete();
            }
        } else {
            informPlayerOfFailure("Unknown '" + args[0] + "' adapter format");
            doDelete();
        }

        // ACL messages handler
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    // Reacting to a "Get content" request
                    if (msg.getPerformative() == ACLMessage.REQUEST
                            && msg.getContent().matches("^" + C.CONTENT_ACL + "\\d+:.*$")) { // Regex of form 'content:[NUMBER]:[Serialized Object]'
                        try {

                            // Parsing message content
                            String[] parts = msg.getContent().split(C.SEPARATOR);
                            int count = Integer.parseInt(parts[1]);
                            WeightedMediaFilter mf = (WeightedMediaFilter) IOUtil.deserializeFromBase64(parts[2]);
                            // Fetching results from external source
                            List<String> results = mr.download(count, mf);
                            ACLMessage res = new ACLMessage(ACLMessage.PROPOSE);

                            res.addReceiver(msg.getSender());
                            if (results == null) {
                                res.setPerformative(ACLMessage.FAILURE);
                                res.setContent(C.CONTENT_ACL + "none");
                            } else {
                                res.setContent(C.CONTENT_ACL + results.toString().replaceAll("^\\[|]$", "")); // Trimming the brackets out, so Array.asList can be used later on
                            }
                            send(res);
                        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
                            e.printStackTrace();
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.addReceiver(msg.getSender());
                            res.setContent(C.CONTENT_ACL + "unknown");
                            send(res);
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    /**
     * Disconnect from the remote source on deletion.
     */
    @Override
    protected void takeDown() {
        super.takeDown();
        if (mr != null) {
            mr.disconnect();
        }
    }

    /**
     * Send a FAILURE ACL message to the PlayerAgent
     * with the description of the error.
     *
     * @param what Description of the error.
     * @see PlayerAgent
     */
    private void informPlayerOfFailure(String what) {
        ACLMessage msg = new ACLMessage(ACLMessage.FAILURE);

        msg.addReceiver(new AID(C.PLAYER_AID, AID.ISLOCALNAME));
        msg.setContent(C.MANAGEMENT_FAILURE_ACL + what);
        send(msg);
    }
}
