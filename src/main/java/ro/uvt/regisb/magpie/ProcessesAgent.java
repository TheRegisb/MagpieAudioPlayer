package ro.uvt.regisb.magpie;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.ProcessAttributes;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProcessesAgent extends Agent {
    private List<ProcessAttributes> watchlist = new ArrayList<>();
    private List<String> monitoredProcesses = new ArrayList<>();

    @Override
    protected void setup() {
        System.out.println("Processes monitoring agent online.");

        addBehaviour(new TickerBehaviour(this, 5000) { // 5 seconds
            @Override
            protected void onTick() {
                ProcessHandle.allProcesses().filter(ProcessHandle::isAlive).forEach(ph -> {
                    if (ph.info().command().isPresent() // If under watchlist and not already monitored
                            && !processActivelyMonitored(ph.info().command().get())
                            && processInWatchlist(ph.info().command().get())) {
                        ProcessAttributes pa = getProcessAttributesByName(ph.info().command().get());

                        if (pa != null) {
                            monitoredProcesses.add(pa.getName());
                            notifyPlaylistAgent(pa, false);
                            ph.onExit().thenRun(() -> {
                                notifyPlaylistAgent(pa, true);
                                monitoredProcesses.remove(pa.getName());
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
                            String serializedProcAttrs = msg.getContent().split(":")[2];
                            byte[] b = Base64.getDecoder().decode(serializedProcAttrs.getBytes());
                            ByteArrayInputStream bi = new ByteArrayInputStream(b);
                            ObjectInputStream si = new ObjectInputStream(bi);

                            watchlist.add((ProcessAttributes) si.readObject());
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
                                    if (processActivelyMonitored(processName)) {
                                        notifyPlaylistAgent(watchlist.get(i), true); // Remove process effect if currently running
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
                } else {
                    block();
                }
            }
        });
    }
    // TODO receive order to monitor a named process

    private boolean processInWatchlist(String processName) {
        for (ProcessAttributes pa : watchlist) {
            if (processName.contains(pa.getName())) { // TODO less naive checking
                return true;
            }
        }
        return false;
    }

    private boolean processActivelyMonitored(String processName) {
        for (String pa : monitoredProcesses) {
            if (processName.contains(pa)) { // TODO less naive checking
                return true;
            }
        }
        return false;
    }

    private void notifyPlaylistAgent(ProcessAttributes processAttributes, boolean deleted) {
        if (deleted && !processInWatchlist(processAttributes.getName())) { // Process was unmonitored while the process was running.
            return;
        }
        System.out.println("PA: Proc: " + (deleted ? processAttributes.invert() : processAttributes) + ": deleted: " + deleted);
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);

            so.writeObject((deleted ? processAttributes.invert() : processAttributes));
            so.flush();
            msg.setContent("process:" + new String(Base64.getEncoder().encode(bo.toByteArray())));
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
