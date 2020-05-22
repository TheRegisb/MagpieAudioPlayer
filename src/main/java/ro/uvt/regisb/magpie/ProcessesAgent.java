package ro.uvt.regisb.magpie;

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

public class ProcessesAgent extends Agent {
    private List<ProcessAttributes> watchlist = new ArrayList<>();

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

    private void applyConfiguration(Configuration conf) {
        watchlist.addAll(conf.getProcessesAttributes());
    }

    private boolean processInWatchlist(String processName) {
        for (ProcessAttributes pa : watchlist) {
            if (processName.contains(pa.getName())) { // TODO less naive checking
                return true;
            }
        }
        return false;
    }

    private void notifyPlaylistAgent(ProcessAttributes processAttributes, boolean deleted) {
        if (deleted && !processInWatchlist(processAttributes.getName())) { // Process was unmonitored while the process was running.
            return;
        }
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

            msg.addReceiver(new AID(C.PLAYLIST_AID, AID.ISLOCALNAME));
            msg.setContent(C.PROCESS_OBJ_ACL + IOUtil.serializeToBase64((deleted ? processAttributes.invert() : processAttributes)));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ProcessAttributes getProcessAttributesByName(String name) {
        for (ProcessAttributes pa : watchlist) {
            if (name.contains(pa.getName())) { // TODO less naive checking
                return pa;
            }
        }
        return null;
    }
}
