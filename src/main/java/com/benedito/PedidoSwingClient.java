package com.benedito;

import com.benedito.view.PedidoFrame;
import javax.swing.SwingUtilities;

public class PedidoSwingClient {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PedidoFrame pedidoFrame = new PedidoFrame();
            pedidoFrame.setVisible(true);
        });
    }
}
