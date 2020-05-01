package ro.uvt.regisb.magpie;

import jade.core.Agent;

import javax.swing.*;

public class PlayerAgent extends Agent {
    protected JFrame gui = null;

    @Override
    protected void setup() {
        System.out.println("Player online.");
        setupUi();
    }

    private void setupUi() {
        gui = new PlayerGui();//JFrame("App");

        gui.setVisible(true);
        gui.validate();
    }
}
