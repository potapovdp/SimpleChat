package com.chat.client;

import com.chat.server.ChatUnits;
import com.chat.server.ServiceCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClient implements Serializable {
    private transient Socket                                socket;
    private transient ObjectInputStream                     inObj;
    private transient ObjectOutputStream                    outObj;

    private transient DefaultListModel<ChatUnits>           listModel;
    private transient CopyOnWriteArrayList<ChatUnits>       listUnits;
    private transient JList<ChatUnits>                      list;
    private transient JFrame                                frame;

    private Boolean                                         isRunning;
    private String                                          name;
    private String                                          idClient;

    private CopyOnWriteArrayList<ChatClientWindow>          listDialogWindows;

    public ChatClient() {
        isRunning   = true;

        try {
            name    = InetAddress.getLocalHost().getHostName();
        }catch (UnknownHostException e) { e.printStackTrace(); }

        listDialogWindows = new CopyOnWriteArrayList<>();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient().go();
            }
        });
    }

    private void go(){
        setupGUI();
        setupNet();
    }

    private void setupGUI(){
        frame                = new JFrame("Chat client " + name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowCloseListener());
        frame.setSize(new Dimension(180, 500));

        listModel                               = new DefaultListModel();
        list                                    = new JList(listModel);
        list.addMouseListener(new MyMouseListener());

        frame.add(BorderLayout.CENTER, list);

        frame.setVisible(true);

    }

    private void setupNet(){
        try {
            socket  = new Socket("192.168.1.63", 7171);
            inObj   = new ObjectInputStream(socket.getInputStream());
            outObj  = new ObjectOutputStream(socket.getOutputStream());

            sendMassage(ServiceCode.NameOfClient, getName(), null);

            Thread threadListener = new Thread(new InputListener());
            threadListener.start();

        }catch (IOException ioe) {ioe.printStackTrace();}
    }

    private void sendMassage(ServiceCode serviceCode, String msg, CopyOnWriteArrayList<ChatUnits> units){
        if ((socket != null) && socket.isConnected()){
            ServiceCode shippedServiseStr = (serviceCode == null) ? ServiceCode.SimpleMassage : serviceCode;

            ChatUnits unit = new ChatUnits(this, getIdClient(), null, null, null );
            ChatMassage massage = new ChatMassage(unit,serviceCode, msg, units);
            try {
                outObj.writeObject(massage);
            }catch (IOException e) { e.printStackTrace();}
        }
    }

    private void refreshUnitList(){
        for (ChatUnits unit : listUnits) {
            if (!listModel.contains(unit))
                listModel.addElement(unit);
        }

        //collect disconnected clients
        ArrayList<ChatUnits> arrTmp = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            if (!listUnits.contains(listModel.get(i))){
                arrTmp.add(listModel.get(i));
            }
        }

        //remove disconnected clients
        for (ChatUnits unit : arrTmp) {
            listModel.removeElement(unit);
        }
    }

    private void getChoosenConnection(ChatUnits choosenClient){
        CopyOnWriteArrayList<ChatUnits> arrUnits = new CopyOnWriteArrayList<>();
        arrUnits.add(choosenClient);

        ChatClientWindow clientWindow = new ChatClientWindow(this, arrUnits);
        listDialogWindows.add(clientWindow);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                clientWindow.startCllient();
            }
        });
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    class InputListener implements Runnable{
        @Override
        public void run() {
            while (socket.isConnected() && isRunning){

                try {
                    ChatMassage massage = (ChatMassage) inObj.readObject();
                    processMassage(massage);
                }
                catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
            }
        }

        private void processMassage(ChatMassage massage){
            if (massage.getServiseCode() == ServiceCode.SimpleMassage){
            }else if (massage.getServiseCode() == ServiceCode.ClientsList){
                listUnits = massage.getListUnits();
                refreshUnitList();
            }else if (massage.getServiseCode() == ServiceCode.CloseConnection){
                isRunning = false;
                try {
                    Thread.sleep(100);
                    socket.close();
                }
                catch (IOException | InterruptedException e) { e.printStackTrace(); }
            }else if (massage.getServiseCode() == ServiceCode.NameOfClient){
                setIdClient(massage.getString());
                frame.setTitle("Chat client " + name +  " [" + idClient + "]");
            }
        }
    }

    class WindowCloseListener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {}

        @Override
        public void windowClosing(WindowEvent e) {
            sendMassage(ServiceCode.CloseConnection, "Buy buy!", null);
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

    class MyMouseListener implements MouseListener{
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2){
                getChoosenConnection(list.getSelectedValue());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
    }
}