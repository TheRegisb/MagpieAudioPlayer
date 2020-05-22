package ro.uvt.regisb.magpie.agent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.commons.lang3.NotImplementedException;
import ro.uvt.regisb.magpie.content.LocalSqliteAdapter;
import ro.uvt.regisb.magpie.content.MediaRetriever;
import ro.uvt.regisb.magpie.utils.C;
import ro.uvt.regisb.magpie.utils.IOUtil;
import ro.uvt.regisb.magpie.utils.WeightedMediaFilter;

import java.io.IOException;
import java.util.List;

public class ContentManagerAgent extends Agent {
    private MediaRetriever mr = null;

    @Override
    protected void setup() {
        String[] args = (String[]) getArguments(); // Expect arguments of style ["local", address_or_path]

        if (args.length != 2 || args[0] == null || args[1] == null) {
            doDelete();
        }

        if (args[0].equals("local")) {
            mr = new LocalSqliteAdapter();
            if (!mr.connect("jdbc:sqlite:" + args[1])) {
                System.err.println("Unable to connect to the database");
                doDelete();
            }
        } else {
            throw new NotImplementedException("Unknown adapter format");
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

    @Override
    protected void takeDown() {
        super.takeDown();
        if (mr != null) {
            mr.disconnect();
        }
    }
}
