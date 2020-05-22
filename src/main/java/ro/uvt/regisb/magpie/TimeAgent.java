package ro.uvt.regisb.magpie;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.utils.C;
import ro.uvt.regisb.magpie.utils.Configuration;
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
        // ACL messages handler
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    // Reacting to interval monitoring request
                    if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().startsWith(C.TIMESLOT_ADD_ACL)) {
                        try {
                            timeSlots.add((TimeInterval) IOUtil.deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[2]));
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.addReceiver(msg.getSender());
                            res.setContent(C.TIMESLOT_ADD_ACL + "unknown");
                            send(res);
                        }
                    }
                    // Reacting to interval un-monitoring request
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^"
                            + C.TIMESLOT_REMOVE_ACL
                            + "\\[[01]\\d:[0-5]\\d [01]\\d:[0-5]\\d]$")) {
                        String slotName = msg.getContent().substring(C.TIMESLOT_REMOVE_ACL.length());

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
                        res.setContent(C.TIMESLOT_ADD_ACL + "unregistered");
                        send(res);
                    }
                    // Reacting to a configuration proposal
                    else if (msg.getPerformative() == ACLMessage.INFORM
                            && msg.getContent().matches("^" + C.CONFIGURATION_RESTORE_ACL + ".*$")) {
                        try {
                            applyConfiguration((Configuration) IOUtil.deserializeFromBase64(msg.getContent().split(C.SEPARATOR)[1]));
                        } catch (IOException | ClassNotFoundException e) {
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.setContent(C.CONFIGURATION_RESTORE_ACL + "unknown");
                            res.addReceiver(msg.getSender());
                            send(msg);
                            e.printStackTrace();
                        }
                    }

                } else {
                    block();
                }
            }
        });
        // Apply or remove effects of a time interval based on current system time
        addBehaviour(new TickerBehaviour(this, 10000) { // 1 minute
            @Override
            protected void onTick() {
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    // "Normalizing" the time by resetting the rest of the date to 1970-01-01
                    Date currentTime = new SimpleDateFormat("HH:mm").parse(String.format("%02d:%02d",
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE)));

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
        // Requesting the stored configuration
        ACLMessage confRequest = new ACLMessage(ACLMessage.REQUEST);

        confRequest.setContent(C.CONFIGURATION_ACL);
        confRequest.addReceiver(new AID(C.PREFERENCES_AID, AID.ISLOCALNAME));
        send(confRequest);
    }

    private void applyConfiguration(Configuration conf) {
        timeSlots.addAll(conf.getTimeIntervals());
    }

    private void notifyPlaylistAgent(TimeInterval time, boolean deleted) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);

            msg.addReceiver(new AID(C.PLAYLIST_AID, AID.ISLOCALNAME));
            msg.setContent(C.TIMESLOT_OBJ_ACL + IOUtil.serializeToBase64((deleted ? time.invert() : time)));
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
