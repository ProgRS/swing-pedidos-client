package com.luis.swingclient;

import com.luis.swingclient.ui.PedidoFrame;

import javax.swing.SwingUtilities;

public class SwingClientApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PedidoFrame frame = new PedidoFrame();
            frame.setVisible(true);
        });
    }
}
