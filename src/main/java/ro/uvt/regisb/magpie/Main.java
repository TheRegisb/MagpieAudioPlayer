/*
 * Copyright 2020 Régis BERTHELOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.uvt.regisb.magpie;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import ro.uvt.regisb.magpie.agent.PlayerAgent;
import ro.uvt.regisb.magpie.utils.C;

/**
 * Standalone Magpie launcher.
 * Can be used to automatically start the JADE platform and populate its container
 * with Magpie's agents.
 */
public class Main {
    /**
     * Main method.
     * Start the JADE platform and populate it with Magpie's agents
     * using the provided arguments or their default fallback.
     *
     * @param argv Command line arguments. Expected: [source, adapter]. Default: ["audiosample.sqlite.db", "local"].
     * @throws StaleProxyException Unable to start the JADE platform or create containers.
     */
    public static void main(String[] argv) throws StaleProxyException {
        if (argv == null || (argv.length > 1 && argv[0].contains("-help"))) {
            System.out.println("Usage: magpie (source) (adapter)" + System.lineSeparator()
                    + "Default: audiosample.sqlite.db local");
            return;
        }
        // Arguments normalization
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
