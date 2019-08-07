package com.chat.client;

import com.chat.server.ChatServerService;
import com.chat.server.ChatUnits;
import com.chat.server.ServiceCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClientWindow {
    private transient JFrame                            frame;
    private transient JPanel                            panel;
    private transient JTextArea                         taCommon;
    private transient JTextArea                         taPrivate;
    private transient JButton                           bSend;

    private ChatClient                                  client;     //host of this dialog window
    private CopyOnWriteArrayList<ChatUnits>             listUnits;  //list of units for a conversation
    private ChatClientWindow                            window;     //tis object for identify it in host

    public ChatClientWindow(ChatClient client, CopyOnWriteArrayList<ChatUnits> listUnits) {
        this.client     = client;
        this.listUnits  = listUnits;
    }

    public void startCllient() {
        setupGUI();
    }

    private void setupGUI() {

        String strUnits = "("+client.getName() + " [" + client.getIdClient() + "]) ";
        strUnits += "Simple chat with: ";
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
        taPrivate.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                //System.out.println(e.getKeyCode());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyCode());
                if ( (e.isAltDown()) & (e.getKeyCode() == KeyEvent.VK_ENTER) ){
                    sendMassage(null,"");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println(e.getKeyCode());
            }
        });

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
        frame.setSize(new Dimension(500, 550));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void sendMassage(ServiceCode serviseCode, String msg){
        ServiceCode shippedServiseCode    = (serviseCode == null)    ? ServiceCode.SimpleMassage : serviseCode;
        String shippedMsg                 = (msg.equals(""))         ? taPrivate.getText()       :  msg;

        client.sendMassage(shippedServiseCode, shippedMsg, listUnits);

        if (msg.equals(""))
            taPrivate.setText("");

    }

    protected void recievMassage(ChatMassage massage){
        Calendar calendar = Calendar.getInstance();
        ServiceCode sc = massage.getServiseCode();
        if (sc == ServiceCode.SimpleMassage){
            taCommon.append("" + massage.getUnit() + " (" + calendar.getTime() + "): \n");
            taCommon.append(massage.getString() + "\n");
        }
    }

    protected void setWindow(ChatClientWindow window) {
        this.window = window;
    }

    public ChatClient getClient() {
        return client;
    }

    public ChatClientWindow getWindow() {
        return window;
    }

    public JFrame getFrame() {
        return frame;
    }

    public CopyOnWriteArrayList<ChatUnits> getListUnits() {
        return listUnits;
    }

    class WindowCloseListener implements WindowListener{
        @Override
        public void windowOpened(WindowEvent e) {}

        @Override
        public void windowClosing(WindowEvent e) {
            //System.out.println("windowClosing");
        }

        @Override
        public void windowClosed(WindowEvent e) {
            getClient().removeDialogWindow(window);
        }

        @Override
        public void windowIconified(WindowEvent e) {}

        @Override
        public void windowDeiconified(WindowEvent e) {}

        @Override
        public void windowActivated(WindowEvent e) {}

        @Override
        public void windowDeactivated(WindowEvent e) {
            //System.out.println("windowDeactivated");
        }
    }

}
