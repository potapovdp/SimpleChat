package com.chat.server;

import com.chat.client.ChatClient;
import com.chat.client.ChatMassage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServerService {
    private ServerSocket            serverSocket;
    private JTextArea               taCommon;
    private CopyOnWriteArrayList<ChatUnits> arrUnits;

    private JList<ChatUnits>              listClients;
    private DefaultListModel<ChatUnits>   listModelClients;

    ChatServerService(){
        arrUnits = new CopyOnWriteArrayList<>();
        listModelClients    = new DefaultListModel<>();

        //update the data ??
        Thread refreshData  = new Thread(new UpdateData());
        refreshData.start();
    }

    public static void main(String[] args) {
       new ChatServerService().startServerService();
    }

    private void startServerService(){
        setupGUI();
        try {
            serverSocket            = new ServerSocket(7171);

            while (true){
                Socket socket           = serverSocket.accept();

                Thread inputListener    = new Thread(new InputListener(socket));
                inputListener.start();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void setupGUI(){
        JFrame frame        = new JFrame("Server for the chat app");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowsCloseListener());

        JPanel panel        = new JPanel();
        BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxLayout);

        taCommon            = new JTextArea();
        taCommon.setEnabled(false);
        taCommon.setLineWrap(true);

        JScrollPane spCommon= new JScrollPane(taCommon);
        spCommon.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        spCommon.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(spCommon);
        frame.add(BorderLayout.CENTER, panel);

        listClients             = new JList<>(listModelClients);
        JScrollPane spClients   = new JScrollPane(listClients);
        spClients.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        spClients.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.add(BorderLayout.EAST ,spClients);

        JButton bRefreshClients = new JButton("Send clients to clients");
        bRefreshClients.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ChatUnits client : arrUnits) {
                    try {
                        client.getOutObj().writeObject(new ChatMassage(null,"$$ServiceCode$$ClientsList", "", getCllientsArray()));
                    }catch (IOException ioe) { ioe.printStackTrace(); }
                }
            }
        });
        frame.add(BorderLayout.SOUTH, bRefreshClients);

        frame.setSize(new Dimension(450, 600));
        frame.setVisible(true);
    }

    private void refreshClientsList(){

        for (ChatUnits client : arrUnits) {
            if (!listModelClients.contains(client))
                listModelClients.addElement(client);
        }

        //collect disconnected clients
        ArrayList<ChatUnits> arrTmp = new ArrayList<>();
        for (int i = 0; i < listModelClients.size(); i++) {
            if (!arrUnits.contains(listModelClients.get(i))){
                arrTmp.add(listModelClients.get(i));
            }
        }

        //remove disconnected clients
        for (ChatUnits unit : arrTmp) {
            listModelClients.removeElement(unit);
        }
    }

    private ArrayList<ChatClient> getCllientsArray(){
        ArrayList<ChatClient> clients = new ArrayList<>();

        for (ChatUnits unit : arrUnits) {
            clients.add(unit.getClient());
        }

        return clients;
    }

    class InputListener implements Runnable{
        ObjectInputStream   inObj;
        ObjectOutputStream  outObj;

        Socket              socket;
        ChatClient          client;
        boolean             isRunning;

        InputListener(Socket s){
            socket      = s;
            isRunning   = true;
            try {
                outObj      = new ObjectOutputStream(socket.getOutputStream());
                inObj       = new ObjectInputStream(socket.getInputStream());

            }catch (IOException e){ e.printStackTrace(); }
        }

        @Override
        public void run() {
            try {
                while (isRunning) {
                    ChatMassage massageIn = (ChatMassage) inObj.readObject();
                    String newMassage = massageIn.getClient().getName() + ": " + massageIn.getString();

                    //to write to the common window
                    taCommon.append("\n" + newMassage);

                    //to process an input massage
                    if (massageIn.getServiseString().contains("$$ServiceCode$$SimpleMassage")) {
                        ChatMassage massageOut = new ChatMassage(massageIn.getClient(), "$$ServiceCode$$SimpleMassage", massageIn.getString(), null);
                        outObj.writeObject(massageOut);
                    } else {
                        applyServiceCode(massageIn);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        //Apply service codes for clients
        private void applyServiceCode(ChatMassage massage){
            String scLine   = massage.getServiseString();
            client          = massage.getClient();
            if (scLine.contains("$$ServiceCode$$NameOfClient")){
                arrUnits.add(new ChatUnits(client, outObj, inObj,socket));
            }else if (scLine.contains("$$ServiceCode$$CloseConnection")){
                isRunning = false;
                for (ChatUnits unit : arrUnits) {
                    if (unit.getClient() == client){
                        try {
                            isRunning = false;
                            ChatMassage massageBuy = new ChatMassage(null, "$$ServiceCode$$CloseConnection", "Buy buy!", null);
                            outObj.writeObject(massageBuy);
                        }catch (IOException ioe) {ioe.printStackTrace();
                        }finally { arrUnits.remove(unit); }

                    }
                }
            }
        }
    }

    class WindowsCloseListener implements WindowListener{
        @Override
        public void windowOpened(WindowEvent e) { }

        @Override
        public void windowClosing(WindowEvent e) {
            for (ChatUnits unit : arrUnits) {
                try {
                    unit.getSocket().close();
                }catch (IOException ioe) { ioe.printStackTrace();}
            }
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

    class UpdateData implements Runnable{
        @Override
        public void run() {

            while (true){
                refreshClientsList();

                //send the list of clients to each clent
                for (ChatUnits client : arrUnits) {

                }

                try {
                    Thread.sleep(500);
                }catch (InterruptedException e) {e.printStackTrace();}
            }
        }
    }
}
