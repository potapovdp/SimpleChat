package com.chat.client;

import com.chat.server.ChatUnits;
import com.chat.server.ServiceCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClientWindow {
    private transient JFrame                            frame;
    private transient JPanel                            panel;
    private transient JTextArea                         taCommon;
    private transient JTextArea                         taPrivate;
    private transient JButton                           bSend;

    private boolean                                     isRunning;
    private ChatClient                                  client;
    private CopyOnWriteArrayList<ChatUnits>             listUnits;

    public ChatClientWindow(ChatClient client, CopyOnWriteArrayList<ChatUnits> listUnits) {
        this.client = client;
        this.listUnits = listUnits;
    }

    public void startCllient() {
        setupGUI();
    }

    private void setupGUI() {

        String strUnits = "Simple chat with ";
        for (int i = 0; i < listUnits.size(); i++) {
            if (i > 0)
                strUnits += ", ";

            strUnits += listUnits.get(i);
        }

        frame = new JFrame(strUnits);
        frame.addWindowListener(new WindowCloseListener());

        //Common text area
        taCommon = new JTextArea();
        taCommon.setLineWrap(true);
        taCommon.setEnabled(false);
        JScrollPane spCommon = new JScrollPane(taCommon);
        spCommon.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spCommon.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        //Private text area
        taPrivate = new JTextArea();
        taPrivate.setLineWrap(true);
        taPrivate.setFocusable(true);
        taPrivate.requestFocus();
        JScrollPane spPrivate = new JScrollPane(taPrivate);
        spPrivate.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        spPrivate.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxLayout);
        panel.add(spCommon);
        panel.add(spPrivate);
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        bSend = new JButton("Send massage");
        bSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMassage(null,"");
            }
        });

        frame.add(bSend, BorderLayout.SOUTH);
        frame.setSize(new Dimension(450, 550));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void sendMassage(ServiceCode serviseCode, String msg){
        ServiceCode shippedServiseCode    = (serviseCode.equals("")) ? ServiceCode.SimpleMassage : serviseCode;
        String shippedMsg           = (msg.equals(""))           ? taPrivate.getText()            :  msg;

        ChatUnits   unit    = new ChatUnits(client, client.getIdClient(), null, null, null );
        ChatMassage massage = new ChatMassage(unit, shippedServiseCode, shippedMsg, listUnits);

//        try {
//            client.writeObject(massage);
//        }catch (IOException e) {
//            //e.printStackTrace();
//        }

        if (msg.equals(""))
            taPrivate.setText("");

    }

    class WindowCloseListener implements WindowListener{
        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {

        }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowIconified(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }
    }
}
