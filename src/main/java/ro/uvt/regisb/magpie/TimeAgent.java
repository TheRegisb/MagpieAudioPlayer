package ro.uvt.regisb.magpie;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.TimeInterval;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
                            String serializedTimeInterval = msg.getContent().split(":")[2];
                            byte[] b = Base64.getDecoder().decode(serializedTimeInterval.getBytes());
                            ByteArrayInputStream bi = new ByteArrayInputStream(b);
                            ObjectInputStream si = new ObjectInputStream(bi);

                            timeSlots.add((TimeInterval) si.readObject());
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
                        System.out.println("TA: for: " + ti
                                + ": active: " + ti.isActive()
                                + ": include " + currentTime + ": " + ti.isTimeInInterval(currentTime));
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
        System.out.println("TA: npa: " + (deleted ? time.invert() : time) + "; deleted: " + deleted);
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);

            so.writeObject((deleted ? time.invert() : time));
            so.flush();
            msg.setContent("timeslot:" + new String(Base64.getEncoder().encode(bo.toByteArray())));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
