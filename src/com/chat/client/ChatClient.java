package com.chat.client;

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
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClient implements Serializable {
    private transient JFrame                            frame;
    private transient JPanel                            panel;
    private transient JTextArea                         taCommon;
    private transient JTextArea                         taPrivate;
    private transient JButton                           bSend;

    private transient Socket                            socket;

    private transient ObjectInputStream                 inObj;
    private transient ObjectOutputStream                outObj;

    private transient CopyOnWriteArrayList<ChatClient>  istClients;

    private String                                      name;
    private boolean                                     isRunning;

    ChatClient() {
        super();

        //name of the current computer
        name = "" + hashCode();
        isRunning = true;
        try {
            name = InetAddress.getLocalHost().getHostName();//"" + hashCode();
        }catch (UnknownHostException e) {e.printStackTrace();}
    }

    public static void main(String[] args) {
        new ChatClient().startCllient();
    }

    private void startCllient() {
        setGUI();

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
        sendMassage("$$ServiceCode$$NameOfClient", getName());
    }

    private void setGUI() {

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
                sendMassage("","");
            }
        });

        frame.add(bSend, BorderLayout.SOUTH);
        frame.setSize(new Dimension(450, 550));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void sendMassage(String serviseString, String msg){
        String shippedMsg = (msg.equals("")) ? taPrivate.getText() :  msg;
        String shippedServiseStr = (serviseString.equals("")) ? "$$ServiceCode$$SimpleMassage" : serviseString;

        if (!shippedMsg.equals("")){
            ChatMassage massage = new ChatMassage(this, shippedServiseStr, shippedMsg, new ArrayList<ChatClient>());

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

                        if (massage.getServiseString().equals("$$ServiceCode$$SimpleMassage")) {
                            taCommon.append("\n" + massage.getClient().getName() + ": " + massage.getString());
                        }else if (massage.getServiseString().equals("$$ServiceCode$$ClientsList")){
                            refreshClientsList(massage);
                        }else if (massage.getServiseString().equals("$$ServiceCode$$CloseConnection")){
                            isRunning = false;

                            //sendMassage("$$ServiceCode$$CloseConnection", "");
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
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
            sendMassage("$$ServiceCode$$CloseConnection", "Buy buy!");
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