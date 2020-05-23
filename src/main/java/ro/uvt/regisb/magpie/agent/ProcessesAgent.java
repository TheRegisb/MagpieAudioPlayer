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
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.C;
import ro.uvt.regisb.magpie.utils.Configuration;
import ro.uvt.regisb.magpie.utils.IOUtil;
import ro.uvt.regisb.magpie.utils.ProcessAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * System processes monitor agent.
 * Bind system processes execution to set of tags.
 */
public class ProcessesAgent extends Agent {
    private List<ProcessAttributes> watchlist = new ArrayList<>();

    /**
     * Agent setup.
     * Enable runtime system process binding, ACL message handler and previous configuration restoration.
     */
    @Override
    protected void setup() {
        // Processes monitoring
        addBehaviour(new TickerBehaviour(this, 5000) { // 5 seconds
            @Override
            protected void onTick() {
                // Lookup all active processes
                ProcessHandle.allProcesses().filter(ProcessHandle::isAlive).forEach(ph -> {
                    if (ph.info().command().isPresent() // If under watchlist and not already monitored
                            && processInWatchlist(ph.info().command().get())) {
                        ProcessAttributes pa = getProcessAttributesByName(ph.info().command().get());

                        if (pa != null && !pa.isActive()) { // System process active and effects not applied already
                            notifyPlaylistAgent(pa, false);
                            pa.setActive(true);
                            ph.onExit().thenRun(() -> { // Registering effects removal on system process shutdown
                                notifyPlaylistAgent(pa, true);
                                pa.setActive(false);
                            });
                        }
                    }
                });
            }
        });
        // ACL messages handler
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    // Reacting to process monitoring request
                    if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith(C.PROCESS_ADD_ACL)) {
                        try {
                            watchlist.add((ProcessAttributes) IOUtil.deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[2]));
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.addReceiver(msg.getSender());
                            res.setContent(C.PROCESS_ADD_ACL + "unknown");
                            send(res);
                        }
                    }
                    // Reacting to process removal request
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith(C.PROCESS_REMOVE_ACL)) {
                        String processName = msg.getContent().split(C.SEPARATOR)[2];

                        if (processInWatchlist(processName)) {
                            for (int i = 0; i != watchlist.size(); i++) { // Looking for index of process
                                if (watchlist.get(i).getName().equals(processName)) {
                                    if (watchlist.get(i).isActive()) {
                                        notifyPlaylistAgent(watchlist.get(i), true); // Revert process effect if currently running
                                    }
                                    watchlist.remove(i);
                                    break;
                                }
                            }
                        } else {
                            ACLMessage res = new ACLMessage(ACLMessage.REFUSE);

                            res.addReceiver(msg.getSender());
                            res.setContent(C.PROCESS_REMOVE_ACL + "unregistered");
                            send(res);
                        }
                    }
                    // Receiving configuration proposal
                    else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith(C.CONFIGURATION_RESTORE_ACL)) {
                        try {
                            applyConfiguration((Configuration) IOUtil.deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[1]));
                        } catch (IOException | ClassNotFoundException e) {
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.addReceiver(msg.getSender());
                            res.setContent(C.CONFIGURATION_RESTORE_ACL + "unknown");
                            send(res);
                            e.printStackTrace();
                        }
                    }
                } else {
                    block();
                }
            }
        });
        // Requesting stored configuration
        ACLMessage confRequest = new ACLMessage(ACLMessage.REQUEST);

        confRequest.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
        confRequest.setContent(C.CONFIGURATION_ACL);
        send(confRequest);
    }

    /**
     * Apply the processes attributes of a configuration.
     *
     * @param conf Configuration preset.
     */
    private void applyConfiguration(Configuration conf) {
        watchlist.addAll(conf.getProcessesAttributes());
    }

    /**
     * Check if a process is registered by name.
     *
     * @param processName Name of the process to check.
     * @return The registration (or lack of) of the given process.
     */
    private boolean processInWatchlist(String processName) {
        for (ProcessAttributes pa : watchlist) {
            if (processName.contains(pa.getName())) { // TODO less naive checking
                return true;
            }
        }
        return false;
    }

    /**
     * Notify the PlaylistAgent of the effects of a process going live or down.
     * @param processAttributes Attributes of the bound process.
     * @param deleted Is the process shutting down.
     */
    private void notifyPlaylistAgent(ProcessAttributes processAttributes, boolean deleted) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

            msg.addReceiver(new AID(C.PLAYLIST_AID, AID.ISLOCALNAME));
            msg.setContent(C.PROCESS_OBJ_ACL + IOUtil.serializeToBase64((deleted ? processAttributes.invert() : processAttributes)));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a process attribute for a process name.
     * @param name Name of the process to find its attributes.
     * @return (1) Bound process attributes or (2) null if process is not bound.
     */
    private ProcessAttributes getProcessAttributesByName(String name) {
        for (ProcessAttributes pa : watchlist) {
            if (name.contains(pa.getName())) { // TODO less naive checking
                return pa;
            }
        }
        return null;
    }
}
