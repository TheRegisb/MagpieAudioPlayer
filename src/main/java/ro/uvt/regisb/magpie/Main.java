package ro.uvt.regisb.magpie;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import ro.uvt.regisb.magpie.agent.PlayerAgent;
import ro.uvt.regisb.magpie.utils.C;

public class Main {
    public static void main(String[] argv) throws StaleProxyException {
        if (argv == null || (argv.length > 1 && argv[0].contains("-help"))) {
            System.out.println("Usage: magpie (source) (adapter)" + System.lineSeparator()
                    + "Default: audiosample.sqlite.db local");
            return;
        }
        String[] playerArgs = new String[2];

        playerArgs[0] = (argv.length >= 1 ? argv[0] : "audiosample.sqlite.db");
        playerArgs[1] = (argv.length >= 2 ? argv[1] : "local");

        System.out.println(String.format("Info: Trying to start Magpie Audio Player using %s (%s)",
                playerArgs[0],
                playerArgs[1]));

        Runtime rt = Runtime.instance(); // Creating a JADE platform instance
        rt.setCloseVM(true);

        // Creating an agent container with a default profile, then adding a PlayerAgent to it
        AgentContainer mainContainer = rt.createMainContainer(new ProfileImpl(null, 1200, null));
        mainContainer.createNewAgent(C.PLAYER_AID, PlayerAgent.class.getCanonicalName(), playerArgs).start();
    }
}
