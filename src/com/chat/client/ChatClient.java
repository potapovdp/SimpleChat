package com.chat.client;

import com.chat.server.ChatUnits;
import com.chat.server.ChatServiceCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClient implements Serializable {
    private transient Socket                                socket;
    private transient ObjectInputStream                     inObj;
    private transient ObjectOutputStream                    outObj;

    //Settings
    private transient ObjectInputStream                     inObjSettings;
    private ChatClientSettings                              settingsGeneral;

    private transient DefaultListModel<ChatUnits>           listModel;
    private transient CopyOnWriteArrayList<ChatUnits>       listUnits;
    private transient JList<ChatUnits>                      list;
    private transient JFrame                                frame;
    private transient ChatClientPannellOnline               panelInf;
    private transient InputListener                         threadListener;

    private String                                          idClient;
    private String                                          version;
    private Boolean                                         isRunning;
    private LocalDateTime                                   timeOfLastServerMassage;
    private Boolean                                         isConnected;

    private CopyOnWriteArrayList<ChatClientWindow>          listDialogWindows;

    public ChatClient() {
        isRunning   = true;
        version     = "b 0.0.4";

        listDialogWindows   = new CopyOnWriteArrayList<>();

        //Settings
        settingsGeneral     = new ChatClientSettings();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient().go());
    }

    private void go(){
        setupGUI();
        setupSettings();
        setupNet();

        //Timer and Shedulers
        Timer timer         = new Timer("Update net-status");
        TimerTask timerTaskUpdateNetStatus = new TimerTask() {
            @Override
            public void run() {
                updateNetStatus(false);
            }
        };

        TimerTask timerTaskReconnectToServer = new TimerTask() {
            @Override
            public void run() {
                if ( ( Objects.nonNull(socket) && (socket.isClosed()) ) || !isConnected ){
                    reconnect();
                }
            }
        };

        timer.schedule(timerTaskUpdateNetStatus,0, 500);
        timer.schedule(timerTaskReconnectToServer,0, 3000);
    }

    private void setupGUI(){
        frame                = new JFrame("Chat client " + settingsGeneral.getNameClient());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowCloseListener());
        frame.setSize(new Dimension(350, 500));

        listModel                               = new DefaultListModel();
        list                                    = new JList(listModel);
        list.addMouseListener(new MyMouseListener());

        //menu - settings
        ButtonListener buttonListener = new ButtonListener();

        JMenuBar        menuBar            = new JMenuBar();
        JMenu           menuSettings       = new JMenu("Settings");
        JMenuItem       itemSettingsSet    = new JMenuItem("Set settings");
        itemSettingsSet.setActionCommand("SettingsSet");
        itemSettingsSet.addActionListener(buttonListener);
        menuSettings.add(itemSettingsSet);

        //menu - file
        JMenu           menuFile           = new JMenu("File");
        JMenuItem       itemExit           = new JMenuItem("Exit");
        itemExit.setActionCommand("Exit");
        itemExit.addActionListener(buttonListener);
        menuFile.add(itemExit);

        //menu - about
        JMenu           menuHelp          = new JMenu("Help");
        JMenuItem       itemAbout         = new JMenuItem("About");
        itemAbout.setActionCommand("About");
        itemAbout.addActionListener(buttonListener);
        menuHelp.add(itemAbout);

        //menu bar
        menuBar.add(menuFile);
        menuBar.add(menuSettings);
        menuBar.add(menuHelp);

        //Panel for an information
        panelInf       = new ChatClientPannellOnline();

        //labelNet.setIcon();

        frame.setJMenuBar(menuBar);

        frame.add(BorderLayout.CENTER, list);
        frame.add(BorderLayout.SOUTH, panelInf);

        frame.setVisible(true);

    }

    private void setupNet(){
        try {
            String  serverIp    = settingsGeneral.getServerIp();
            int     serverSocet = settingsGeneral.getServerSocket();

            socket  = new Socket(serverIp, serverSocet);

            inObj   = new ObjectInputStream(socket.getInputStream());
            outObj  = new ObjectOutputStream(socket.getOutputStream());

            sendMassage(ChatServiceCode.NameOfClient, settingsGeneral.getNameClient(), null);

            threadListener = new InputListener();
            threadListener.start();

        }catch (IOException ioe) {
            try {
                if ( !Objects.isNull(socket) && socket.isConnected() )
                    socket.close();
            }catch (IOException e) {}

            //ioe.printStackTrace();
        }
    }

    private void setupSettings(){

        settingsGeneral.fillByDefault();
        try {
            inObjSettings = new ObjectInputStream(new FileInputStream(settingsGeneral.getPathFileSettings().toFile()));

            //reading setting object
            Object o = inObjSettings.readObject();
            if (o instanceof ChatClientSettings){
                settingsGeneral = (ChatClientSettings) o;
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Settings-file not found");
        }
        catch (ClassNotFoundException | InvalidClassException e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,"Settings file not found"));
        }
        catch (IOException e) {e.printStackTrace();}
        finally {
            applySettings();
        }
    }

    void sendMassage(ChatServiceCode serviceCode, String msg, CopyOnWriteArrayList<ChatUnits> units){
        if ( !Objects.isNull(socket)  &&  socket.isConnected() && !socket.isClosed()){
            ChatServiceCode shippedServiseStr = ( Objects.isNull(serviceCode) ) ? ChatServiceCode.SimpleMassage : serviceCode;

            ChatUnits unit = new ChatUnits(this, getIdClient(), null, null, null );
            ChatMassage massage = new ChatMassage(unit,serviceCode, msg, units);
            try {
                outObj.writeObject(massage);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void removeDialogWindow(ChatClientWindow window){
        listDialogWindows.remove(window);
    }

    private void refreshUnitList(){
        if ( Objects.nonNull(listUnits)){
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
    }

    private ChatClientWindow getChoosenConnection(ChatUnits choosenClient){
        ChatClientWindow res = null;
        CopyOnWriteArrayList<ChatUnits> arrUnits = new CopyOnWriteArrayList<>();
        arrUnits.add(choosenClient);

        boolean windowAlreadyRunning = false;
        for (ChatClientWindow window : listDialogWindows) {
            if (arrUnits.equals(window.getListUnits())){
                windowAlreadyRunning = true;
                res = window;
                if (!window.getFrame().isVisible())
                    window.getFrame().setVisible(true);
            }
        }

        if (!windowAlreadyRunning){
            ChatClientWindow clientWindow = new ChatClientWindow(this, arrUnits, settingsGeneral);
            clientWindow.setWindow(clientWindow);
            listDialogWindows.add(clientWindow);
            res = clientWindow;
            SwingUtilities.invokeLater(clientWindow::startCllient);
        }

        return res;
    }

    String getVersion() {
        return version;
    }

    String getIdClient() {
        return idClient;
    }

    private void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    private void setNameClient(String name) {
        if (!Objects.isNull(frame)){
            frame.setTitle("Chat client " + name);
        }
    }

    @Override
    public String toString() {
        return settingsGeneral.getNameClient();
    }

    private boolean equalListUnits(CopyOnWriteArrayList<ChatUnits> list1, CopyOnWriteArrayList<ChatUnits> list2){
        boolean res = true;

        if (  !Objects.isNull(list1) &  !Objects.isNull(list2)  ) {
            ArrayList<String> l1 = new ArrayList<>();
            for (ChatUnits unit : list1) {
                l1.add(unit.getIDClient());
            }

            ArrayList<String> l2 = new ArrayList<>();
            for (ChatUnits unit : list2) {
                l2.add(unit.getIDClient());
            }

            Collections.sort(l1);
            Collections.sort(l2);

            if (l1.size() != l2.size()) {
                res = false;
            } else {
                for (int i = 0; i < l1.size(); i++) {
                    if (!l1.get(i). equals(l2.get(i))) {
                        res = false;
                    }
                }
            }

        }else
            res = false;

        return res;
    }

    private void beforeClose(){
        sendMassage(ChatServiceCode.CloseConnection, "Buy buy!", null);
    }

    private void applySettings(){
        if ( !Objects.isNull(settingsGeneral) ){

            String tmpFieldName = settingsGeneral.getNameClient();
            if ( !Objects.equals(tmpFieldName, settingsGeneral.getNameClient()) ){
                setNameClient(tmpFieldName);
            }
        }
    }

    void cancelNetConnrction(){
        beforeClose();

        try {
            if (Objects.nonNull(inObj))  { inObj.close();   inObj = null;}
            if (Objects.nonNull(outObj)) { outObj.close();  outObj = null;}
            if (Objects.nonNull(socket)) { socket.close();  socket = null;}

            isRunning = false;

            //Stop previous thread listener
            try {
                Thread.sleep(60);
            }catch (InterruptedException e) {}

            if ( threadListener.isAlive() ){
                threadListener.interrupt();
            }
        }catch (IOException e) {}
    }

    public String getNameClient(){
        return settingsGeneral.getNameClient();
    }

    ChatClientSettings getSettingsGeneral() {
        return settingsGeneral;
    }

    void setSettingsGeneral(ChatClientSettings sg) {
        ChatClientSettings oldSettings  = this.settingsGeneral;
        this.settingsGeneral            = sg;

        if ( !Objects.equals(oldSettings.getServerIp(), settingsGeneral.getServerIp())
                || !Objects.equals(oldSettings.getServerSocket(), settingsGeneral.getServerSocket())
                || Objects.isNull(socket) || socket.isClosed()){
            reconnect();
        }
    }

    public void setTimeOfLastServerMassage(LocalDateTime timeOfLastServerMassage) {
        this.timeOfLastServerMassage = timeOfLastServerMassage;
    }

    public LocalDateTime getTimeOfLastServerMassage() {
        Optional<LocalDateTime> optional = Optional.ofNullable(timeOfLastServerMassage);
        return  optional.orElseGet(() -> LocalDateTime.now().minusSeconds(100));
    }

    //haveServerMassage - for indicate incoming massage of server
    private void updateNetStatus(boolean haveServerMassage){
        LocalDateTime now       = LocalDateTime.now();
        LocalDateTime last      = getTimeOfLastServerMassage();
        LocalDateTime lastPlus2 = last.plusSeconds(2);
        if ( lastPlus2.compareTo(now) < 0  ){
            isConnected = false;
            panelInf.setColorOnline(Color.RED);
            panelInf.setConnectState(ChatConnectState.offline);
            panelInf.setStrServer("");
        }else{
            isConnected = true;
            panelInf.setColorOnline(Color.BLUE);
            panelInf.setConnectState(ChatConnectState.online);
            panelInf.setStrServer(settingsGeneral.getServerIp() + ": " + settingsGeneral.getServerSocket());
        }
        panelInf.repaint();

        if (haveServerMassage){
            setTimeOfLastServerMassage(LocalDateTime.now());
        }
    }

    void showMassage(String s){
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, s));
    }

    private void reconnect(){
        cancelNetConnrction();
        setupNet();
        refreshUnitList();
        for (ChatClientWindow window : listDialogWindows) {
            window.getFrame().dispatchEvent( new WindowEvent(window.getFrame(), WindowEvent.WINDOW_CLOSING) );
            removeDialogWindow(window);
        }
    }

    class InputListener extends Thread {
        private int         counter = 0;
        private Thread      currenrThred;
        private LocalDate   timeOfTheLastMassage;

        public InputListener() {
            super();
            isRunning               = true;
            currenrThred            = Thread.currentThread();
            timeOfTheLastMassage    = LocalDate.now();
        }

        @Override
        public void run() {


            while (Objects.nonNull(socket) && socket.isConnected() && isRunning){

                try {
                    ChatMassage massage = (ChatMassage) inObj.readObject();
                    processMassage(massage);
                }
                catch (SocketException e){}
                catch (EOFException e){
                    reconnect();}
                catch (StreamCorruptedException e){
                    reconnect();}
                catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processMassage(ChatMassage massage){
            if (massage.getServiseCode() == ChatServiceCode.SimpleMassage){
                boolean windowIsFound = false;
                for (ChatClientWindow window : listDialogWindows) {
                    if (window.getListUnits().contains( massage.getUnit() )  ){
                        window.recievMassage(massage);
                        windowIsFound = true;
                        if (!window.getFrame().isVisible())
                            window.getFrame().setVisible(true);
                    }

                    if ( (massage.getUnit().getIDClient().equals(getIdClient())) & (equalListUnits(window.getListUnits(), massage.getListUnits())) ){
                        window.recievMassage(massage);
                        windowIsFound = true;
                    }

                }
                if (!windowIsFound){
                    ChatClientWindow window = getChoosenConnection(massage.getUnit());
                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException e){ e.printStackTrace(); }
                    window.recievMassage(massage);

                }

            }else if (massage.getServiseCode() == ChatServiceCode.ClientsList){
                listUnits = massage.getListUnits();
                refreshUnitList();
                updateNetStatus(true);

            }else if (massage.getServiseCode() == ChatServiceCode.CloseConnection){
                isRunning = false;
                try {
                    Thread.sleep(100);
                    socket.close();
                }
                catch (IOException | InterruptedException e) { e.printStackTrace(); }

            }else if (massage.getServiseCode() == ChatServiceCode.NameOfClient){
                setIdClient(massage.getString());
                frame.setTitle("Chat client " + settingsGeneral.getNameClient() +  " [" + idClient + "]");
            }
        }
    }

    class WindowCloseListener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {}

        @Override
        public void windowClosing(WindowEvent e) {
            beforeClose();
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

    class ButtonListener implements ActionListener{
        String actionCommand = "";
        @Override
        public void actionPerformed(ActionEvent e) {

            actionCommand = e.getActionCommand();
            if (actionCommand.equals("Exit")){
                beforeClose();
                System.exit(0);
            }else{

                SwingUtilities.invokeLater(() -> {
                        switch (actionCommand){
                            case "About":
                                new ChatClientAbout(ChatClient.this);
                                break;

                            case "SettingsSet":
                                 new ChatClientSettingsWindow(ChatClient.this);
                                break;
                        }
                });

            }
        }
    }
}
