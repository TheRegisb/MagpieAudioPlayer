package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.IOUtil;
import ro.uvt.regisb.magpie.utils.TimeInterval;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimeAgent extends Agent {
    private List<TimeInterval> timeSlots = new ArrayList<>();

    @Override
    protected void setup() {
        System.out.println("Time Agent online.");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith("timeslot:add:")) {
                        try {
                            timeSlots.add((TimeInterval) IOUtil.deserializeFromBase64(msg.getContent().split(":")[2]));
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.addReceiver(msg.getSender());
                            res.setContent("timeslot:add:unknown");
                            send(res);
                        }
                    } else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith("timeslot:remove:")) {
                        String slotName = msg.getContent().substring(16);

                        for (int i = 0; i != timeSlots.size(); i++) {
                            TimeInterval ti = timeSlots.get(i);

                            if (ti.equals(slotName)) {
                                if (ti.isActive()) {
                                    notifyPlaylistAgent(ti, true);
                                }
                                timeSlots.remove(ti);
                                return;
                            }
                        }
                        ACLMessage res = new ACLMessage(ACLMessage.REFUSE);

                        res.addReceiver(msg.getSender());
                        res.setContent("timeslot:remove:unregistered");
                        send(res);
                    }
                } else {
                    block();
                }
            }
        });
        addBehaviour(new TickerBehaviour(this, 10000) { // 1 minute
            @Override
            protected void onTick() {
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    Date currentTime = new SimpleDateFormat("HH:mm").parse(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

                    for (TimeInterval ti : timeSlots) {
                        if (ti.isTimeInInterval(currentTime) && !ti.isActive()) { // In time interval but effect not already applied
                            ti.setActive(true);
                            notifyPlaylistAgent(ti, false);
                        } else if (!ti.isTimeInInterval(currentTime) && ti.isActive()) { // Out of time interval and effect not already reverted
                            ti.setActive(false);
                            notifyPlaylistAgent(ti, true);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void notifyPlaylistAgent(TimeInterval time, boolean deleted) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

            msg.addReceiver(new AID("magpie_playlist", AID.ISLOCALNAME));
            msg.setContent("timeslot:" + IOUtil.serializeToBase64(time));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
