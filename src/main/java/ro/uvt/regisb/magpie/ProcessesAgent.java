package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
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
        System.out.println("Processes monitoring agent online.");

        addBehaviour(new TickerBehaviour(this, 5000) { // 5 seconds
            @Override
            protected void onTick() {
                ProcessHandle.allProcesses().filter(ProcessHandle::isAlive).forEach(ph -> {
                    if (ph.info().command().isPresent() // If under watchlist and not already monitored
                            && processInWatchlist(ph.info().command().get())) {

                        ProcessAttributes pa = getProcessAttributesByName(ph.info().command().get());

                        if (pa != null && !pa.isActive()) {
                            notifyPlaylistAgent(pa, false);
                            pa.setActive(true);
                            ph.onExit().thenRun(() -> {
                                notifyPlaylistAgent(pa, true);
                                pa.setActive(false);
                            });
                        }
                    }
                });
            }
        });
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith("process:add")) {
                        try {
                            watchlist.add((ProcessAttributes) IOUtil.deserializeFromBase64(msg.getContent().split(":")[2]));
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.addReceiver(msg.getSender());
                            res.setContent("process:add:unknown");
                            send(res);
                        }
                    } else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith("process:remove:")) {
                        String processName = msg.getContent().split(":")[2];

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
                            res.setContent("process:remove:unregistered");
                            send(res);
                        }
                    }
                    // Receiving configuration proposal
                    else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("conf:")) {
                        try {
                            applyConfiguration((Configuration) IOUtil.deserializeFromBase64(msg.getContent().split(":")[1]));
                        } catch (IOException | ClassNotFoundException e) {
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.addReceiver(msg.getSender());
                            res.setContent("configuration:unknown");
                            send(res);
                            e.printStackTrace();
                        }
                    }
                } else {
                    block();
                }
            }
        });

        ACLMessage confRequest = new ACLMessage(ACLMessage.REQUEST);

        confRequest.addReceiver(new AID("magpie_preferences", AID.ISLOCALNAME));
        confRequest.setContent("configuration");
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

            msg.addReceiver(new AID("magpie_playlist", AID.ISLOCALNAME));
            msg.setContent("process:" + IOUtil.serializeToBase64((deleted ? processAttributes.invert() : processAttributes)));
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
