package com.chat.server;

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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServerService {
    private ServerSocket                    serverSocket;
    private JTextArea                       taCommon;
    private CopyOnWriteArrayList<ChatUnits> arrUnits;

    private JList<ChatUnits>                listClients;
    private DefaultListModel<ChatUnits>     listModelClients;

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

                Thread inputListener    = new Thread(new ClientThread(socket));
                inputListener.start();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void setupGUI(){
        JFrame frame        = new JFrame("Server for the chat");
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
                for (ChatUnits client : arrUnits) {}
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

    private CopyOnWriteArrayList<ChatUnits> getCllientsArray(ChatUnits clientExclude){
        CopyOnWriteArrayList<ChatUnits> units = new CopyOnWriteArrayList<>();

        for (ChatUnits unit : arrUnits) {
            if (unit == clientExclude)
                continue;
            units.add(unit);
        }

        return units;
    }

    class ClientThread implements Runnable{
        private ObjectInputStream   inObj;
        private ObjectOutputStream  outObj;

        private Socket              socket;
        private ChatUnits           unit;
        private boolean             isRunning;
        private Thread              currentThread;

        ClientThread(Socket s){
            socket      = s;
            isRunning   = true;
            currentThread = Thread.currentThread();
            try {
                outObj      = new ObjectOutputStream(socket.getOutputStream());
                inObj       = new ObjectInputStream(socket.getInputStream());

            }catch (IOException e){ e.printStackTrace(); }
        }

        @Override
        public void run() {
            try {
                while (socket.isConnected() && isRunning && !Objects.isNull(inObj)) {
                    ChatMassage massageIn = (ChatMassage) inObj.readObject();

                    String newMassage = massageIn.getUnit().getClient().getNameClient() + ": " + massageIn.getString();

                    //to write to the common window
                    taCommon.append("\n" + newMassage);

                    //to process an input massage
                    if (massageIn.getServiseCode() == ChatServiceCode.SimpleMassage) {
                        ChatMassage massageOut = new ChatMassage(massageIn.getUnit(), ChatServiceCode.SimpleMassage, massageIn.getString(), massageIn.getListUnits());

                        for (ChatUnits unitForMassage : massageIn.getListUnits()) {
                            for (ChatUnits unitOfAll : arrUnits) {
                                if ( (unitForMassage.getIDClient().equals(unitOfAll.getIDClient()) ) || (massageIn.getUnit().getIDClient().equals(unitOfAll.getIDClient()) ) ){
                                    unitOfAll.getOutObj().writeObject(massageOut);
                                }
                            }
                        }

                    } else {
                        applyServiceCode(massageIn);

                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        //Apply service codes for clients
        private void applyServiceCode(ChatMassage massage){
            ChatServiceCode scLine   = massage.getServiseCode();

            if (scLine == ChatServiceCode.NameOfClient){
                unit = new ChatUnits(massage.getUnit().getClient(), ""+massage.getUnit().getClient().hashCode(), outObj, inObj,socket);
                arrUnits.add(unit);

                //send back to unit its id
                ChatMassage massageID = new ChatMassage(null, ChatServiceCode.NameOfClient, ""+massage.getUnit().getClient().hashCode(),null);
                try {
                    outObj.writeObject(massageID);
                }catch (IOException e){ e.printStackTrace(); }

            }else if (scLine == ChatServiceCode.CloseConnection){
                isRunning = false;
                for (ChatUnits chatUnits : arrUnits) {
                    if (chatUnits == unit){
                        try {
                            ChatMassage massageBuy = new ChatMassage(null, ChatServiceCode.CloseConnection, "Buy buy!", null);
                            outObj.writeObject(massageBuy);
                        }catch (IOException ioe) {ioe.printStackTrace();
                        }finally { arrUnits.remove(chatUnits); }
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
                for (ChatUnits unit : arrUnits) {
                    try {
                        unit.getOutObj().writeObject(new ChatMassage(null, ChatServiceCode.ClientsList,"", getCllientsArray(unit)));
                    }catch (SocketException e){
                        arrUnits.remove(unit);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(500);
                }catch (InterruptedException e) {e.printStackTrace();}
            }
        }
    }
}
