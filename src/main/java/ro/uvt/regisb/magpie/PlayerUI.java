package ro.uvt.regisb.magpie;


import javax.swing.*;
import java.awt.*;

public class PlayerUI extends JFrame {
    BorderLayout borderLayout = new BorderLayout();
    JPanel pnlMain = new JPanel();
    JButton btnExit = new JButton("Exit");
    Component component;

    protected PlayerAgent owner;

    public PlayerUI(PlayerAgent owner) {
        jbInit();
        this.owner = owner;
    }

    private void jbInit() {

    }
}
