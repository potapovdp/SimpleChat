package com.chat.client;

import com.chat.server.ChatUnits;
import com.chat.server.ServiceCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClientWindowOld extends ChatClient implements Serializable {
    private transient JFrame                            frame;
    private transient JPanel                            panel;
    private transient JTextArea                         taCommon;
    private transient JTextArea                         taPrivate;
    private transient JButton                           bSend;

    private transient Socket                            socket;

    private transient ObjectInputStream                 inObj;
    private transient ObjectOutputStream                outObj;

    private transient CopyOnWriteArrayList<ChatClientWindowOld>  istClients;

    private String                                      name;
    private boolean                                     isRunning;

    ChatClientWindowOld() {
        super();

        //name of the current computer
        name = "" + hashCode();
        isRunning = true;
        try {
            name = InetAddress.getLocalHost().getHostName();//"" + hashCode();
        }catch (UnknownHostException e) {e.printStackTrace();}
    }

    public static void main(String[] args) {
        new ChatClientWindowOld().startCllient();
    }

    private void startCllient() {
        setupGUI();

        try {
            socket  = new Socket("192.168.1.63", 7171);
            outObj  = new ObjectOutputStream(socket.getOutputStream());
            inObj   = new ObjectInputStream(socket.getInputStream());

            sendNameToServer();

            Thread inputListener = new Thread(new InputListener());
            inputListener.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNameToServer(){
        sendMassage(ServiceCode.NameOfClient, getName());
    }

    private void setupGUI() {

        frame = new JFrame("Simple chat [" + getName() + "]");
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

    private void sendMassage(ServiceCode serviceCode, String msg){
        String shippedMsg = (msg.equals("")) ? taPrivate.getText() :  msg;
        ServiceCode shippedServiseCode = (serviceCode.equals("")) ? ServiceCode.SimpleMassage : serviceCode;

        if (!shippedMsg.equals("")){
            ChatUnits unit = new ChatUnits(this,getIdClient(), null, null, null );
            ChatMassage massage = new ChatMassage(unit, shippedServiseCode, shippedMsg, new CopyOnWriteArrayList<ChatUnits>());

            try {
                outObj.writeObject(massage);
            }catch (IOException e) {
                //e.printStackTrace();
            }

            if (msg.equals(""))
                taPrivate.setText("");
        }
    }

    public String getName() {
        return name;
    }

    private void refreshClientsList(ChatMassage massage){

    }

    @Override
    public String toString() {
        return getName();
    }

    class InputListener implements Runnable {
        @Override
        public void run() {
            try {
                while (isRunning) {
                    if (!socket.isClosed()) {
                        ChatMassage massage = (ChatMassage) inObj.readObject();

                        if (massage.getServiseCode().equals("$$ServiceCode$$SimpleMassage")) {
                            taCommon.append("\n" + massage.getUnit().getClient().getName() + ": " + massage.getString());
                        }else if (massage.getServiseCode().equals("$$ServiceCode$$ClientsList")){
                            refreshClientsList(massage);
                        }else if (massage.getServiseCode().equals("$$ServiceCode$$CloseConnection")){
                            isRunning = false;

                            try {
                                Thread.sleep(100);
                                socket.close();
                            }
                            catch (IOException ioe) { ioe.printStackTrace(); }
                            catch (InterruptedException ie) { ie.printStackTrace(); }
                        }

                        try {
                            Thread.sleep(50);
                        }catch (InterruptedException e) { e.printStackTrace(); }
                    }

                }
            }
            catch (IOException e) { e.printStackTrace(); }
            catch (ClassNotFoundException e) { e.printStackTrace(); }
        }
    }

    class WindowCloseListener implements WindowListener{
        @Override
        public void windowOpened(WindowEvent e) {}

        @Override
        public void windowClosing(WindowEvent e) {
            sendMassage(ServiceCode.CloseConnection, "Buy buy!");
        }

        @Override
        public void windowClosed(WindowEvent e) {}

        @Override
        public void windowIconified(WindowEvent e) {}

        @Override
        public void windowDeiconified(WindowEvent e) {}

        @Override
        public void windowActivated(WindowEvent e) {}

        @Override
        public void windowDeactivated(WindowEvent e) {}
    }
}