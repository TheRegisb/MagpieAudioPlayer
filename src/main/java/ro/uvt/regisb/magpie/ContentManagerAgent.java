package ro.uvt.regisb.magpie;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ro.uvt.regisb.magpie.content.LocalSqliteAdapter;
import ro.uvt.regisb.magpie.content.MediaRetriever;
import ro.uvt.regisb.magpie.utils.WeightedMediaFilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.List;

public class ContentManagerAgent extends Agent {
    private MediaRetriever mr = null;

    @Override
    protected void setup() {
        System.out.println("Content Manager online.");
        // TODO setup correct adapter on given arguments
        // getArguments(); // [local/http];<addr>
        mr = new LocalSqliteAdapter();


        if (!mr.connect("jdbc:sqlite:audiosample.sqlite.db")) {
            System.err.println("Unable to connect to the database");
        }

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST
                            && msg.getContent().matches("^content:\\d+:.*$")) { // Regex of form 'content:[NUMBER]:[Serialized Object]'
                        try {
                            String[] parts = msg.getContent().split(":");
                            int count = Integer.parseInt(parts[1]);
                            byte[] b = Base64.getDecoder().decode(parts[2].getBytes());
                            ByteArrayInputStream bi = new ByteArrayInputStream(b);
                            ObjectInputStream si = new ObjectInputStream(bi);
                            WeightedMediaFilter mf = (WeightedMediaFilter) si.readObject();

                            ACLMessage res = new ACLMessage(ACLMessage.PROPOSE);
                            List<String> results = mr.download(count, mf);
                            res.addReceiver(msg.getSender());
                            if (results == null) {
                                res.setPerformative(ACLMessage.FAILURE);
                                res.setContent("content:none");

                            } else {
                                res.setContent("content:" + results.toString().replaceAll("^\\[|]$", "")); // Trimming the brackets out, so Array.asList can be used later on
                            }
                            send(res);
                        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
                            e.printStackTrace();
                            ACLMessage res = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);

                            res.addReceiver(msg.getSender());
                            res.setContent("content:unknown");
                            send(res);
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        if (mr != null) {
            mr.disconnect();
        }
    }
}
