package com.chat.client;

import javax.swing.*;
import java.awt.*;

public class ChatClientPannellOnline extends JPanel {
    private Color               colorOnline;
    private ChatConnectState    connectState;
    private String              strServer;

    public ChatClientPannellOnline() {
        super();
        colorOnline = Color.RED;
        connectState   = ChatConnectState.offline;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D)g;

        graphics2D.setBackground(Color.WHITE);

        graphics2D.setPaint(colorOnline);
        graphics2D.fillOval(0,0, getHeight(), getHeight());

        graphics2D.setPaint(Color.BLACK);
        graphics2D.setFont(new Font("SansSerif",Font.BOLD, getHeight()));

        String inf = connectState.toString() +strServer ;
        graphics2D.drawString(inf,  getHeight()+15, getHeight());

    }

    public void setColorOnline(Color colorOnline) {
        this.colorOnline = colorOnline;
    }

    public void setConnectState(ChatConnectState connectState) {
        this.connectState = connectState;
    }

    public void setStrServer(String strServer) {
        this.strServer = strServer;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            var pannel = new ChatClientPannellOnline();
            frame.add(BorderLayout.CENTER, pannel);
            frame.setSize(new Dimension(300, 100));
            frame.setVisible(true);
        });
    }
}
