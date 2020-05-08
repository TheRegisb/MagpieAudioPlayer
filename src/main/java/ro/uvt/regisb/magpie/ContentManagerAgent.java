package ro.uvt.regisb.magpie;

import jade.core.Agent;
import ro.uvt.regisb.magpie.content.LocalSqliteAdapter;
import ro.uvt.regisb.magpie.content.MediaRetriever;

public class ContentManagerAgent extends Agent {
    private MediaRetriever mr = null;

    @Override
    protected void setup() {
        System.out.println("Content Manager online.");
        // getArguments(); // [local/http];<addr>
        mr = new LocalSqliteAdapter();


        if (!mr.connect("jdbc:sqlite:audiosample.sqlite.db")) {
            System.err.println("Unable to connect to the database");
        }
        mr.download(1, "Electro");
        mr.disconnect();

        // TODO listen for download request
        // TODO get music list from MediaRetriever
        // TODO download music that are not stored locally
        // TODO send list of local file
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        if (mr != null) {
            mr.disconnect();
        }
    }
}
