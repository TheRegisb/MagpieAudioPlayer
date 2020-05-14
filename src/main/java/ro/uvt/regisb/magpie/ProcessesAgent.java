package ro.uvt.regisb.magpie;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import ro.uvt.regisb.magpie.utils.ProcessAttributes;

import java.util.ArrayList;
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
                System.out.println("tick");
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
        // TODO send serialized processAttributes
        /*
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        StringBuilder sb = new StringBuilder();


        sb.append("process:bpm:").append(processAttributes.getBpmTweak());
        if (!processAttributes.getFeel().isEmpty()) {
            sb.append(":");
            for (Pair<String, Integer> attr : processAttributes.getFeel()) {
                sb.append(attr.getKey()).append(":").append(attr.getValue() * (deleted ? -1 : 1)); // Negate previously added tags at process exit
            }
        }
        if (!processAttributes.getGenre().isEmpty()) {
            sb.append(":");
            for (Pair<String, Integer> attr : processAttributes.getFeel()) {
                sb.append(attr.getKey()).append(":").append(attr.getValue() * (deleted ? -1 : 1)); // Negate previously added tags at process exit
            }
        }
        msg.addReceiver(new AID("magpie_playlist", AID.ISLOCALNAME));
        msg.setContent(sb.toString());
        System.out.println(msg.getContent());
        send(msg);
         */
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
