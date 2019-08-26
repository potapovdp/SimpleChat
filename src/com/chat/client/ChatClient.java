package com.chat.client;

import com.chat.server.ChatUnits;
import com.chat.server.ChatServiceCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClient extends ChatClientSettings implements Serializable {
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

    private Boolean                                         isRunning;
    private String                                          idClient;
    private String                                          version;

    private CopyOnWriteArrayList<ChatClientWindow>          listDialogWindows;

    public ChatClient() {
        isRunning   = true;
        version     = "b 0.0.3";

        try {
            setNameClient(InetAddress.getLocalHost().getHostName());
        }catch (UnknownHostException e) { e.printStackTrace(); }

        listDialogWindows   = new CopyOnWriteArrayList<>();

        //Settings
        settingsGeneral     = new ChatClientSettings();
        settingsGeneral.setNameClient(getNameClient());
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
        setupSettings();
    }

    private void setupGUI(){
        frame                = new JFrame("Chat client " + getNameClient());
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

        frame.setJMenuBar(menuBar);

        frame.add(BorderLayout.CENTER, list);

        frame.setVisible(true);

    }

    private void setupNet(){
        try {
            socket  = new Socket("192.168.1.110", 7171);
            //socket  = new Socket("192.168.1.63", 7171);
            inObj   = new ObjectInputStream(socket.getInputStream());
            outObj  = new ObjectOutputStream(socket.getOutputStream());

            sendMassage(ChatServiceCode.NameOfClient, getNameClient(), null);

            Thread threadListener = new Thread(new InputListener());
            threadListener.start();

        }catch (IOException ioe) {ioe.printStackTrace();}
    }

    private void setupSettings(){

        Path pathFileSettings = Path.of(System.getProperty("user.home") + "\\settings");
        try {
            //pathFileSettings.toFile().is
            inObjSettings                       = new ObjectInputStream(new FileInputStream(pathFileSettings.toFile()));

            //reading setting object
            Object o = inObjSettings.readObject();
            if (o instanceof ChatClientSettings){
                settingsGeneral = (ChatClientSettings) o;
                applySettings();
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
            settingsGeneral.setPathFileSettings(pathFileSettings);
            setSendMethod();
        }
    }

    private void setSendMethod(){
        if (!isSendMassageAltEnter() & !isSendMassageCtrlEnter() & !isSendMassageEnter()){
            setSendMassageAltEnter(true);
        }
        applySettings();
    }

    void sendMassage(ChatServiceCode serviceCode, String msg, CopyOnWriteArrayList<ChatUnits> units){
        if ( !Objects.isNull(socket)  &&  socket.isConnected()){
            ChatServiceCode shippedServiseStr = ( Objects.isNull(serviceCode) ) ? ChatServiceCode.SimpleMassage : serviceCode;

            ChatUnits unit = new ChatUnits(this, getIdClient(), null, null, null );
            ChatMassage massage = new ChatMassage(unit,serviceCode, msg, units);
            try {
                outObj.writeObject(massage);
            }catch (IOException e) { e.printStackTrace();}
        }
    }

    void removeDialogWindow(ChatClientWindow window){
        listDialogWindows.remove(window);
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

    @Override
    public void setNameClient(String name) {
        super.setNameClient(name);
        if (!Objects.isNull(frame)){
            frame.setTitle("Chat client " + getNameClient());
        }
    }

    @Override
    public String toString() {
        return getNameClient();
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
            if ( !Objects.equals(tmpFieldName, getNameClient()) ){
                setNameClient(tmpFieldName);
            }

            boolean tmpFieldBoolean = settingsGeneral.isSendMassageEnter();
            if ( !Objects.equals(tmpFieldBoolean, isSendMassageEnter()) ){
                setSendMassageEnter(tmpFieldBoolean);
            }

            tmpFieldBoolean         = settingsGeneral.isSendMassageAltEnter();
            if ( !Objects.equals(tmpFieldBoolean, isSendMassageAltEnter()) ){
                setSendMassageAltEnter(tmpFieldBoolean);
            }

            tmpFieldBoolean         = settingsGeneral.isSendMassageCtrlEnter();
            if ( !Objects.equals(tmpFieldBoolean, isSendMassageCtrlEnter()) ){
                setSendMassageCtrlEnter(tmpFieldBoolean);
            }
        }
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

            }else if (massage.getServiseCode() == ChatServiceCode.CloseConnection){
                isRunning = false;
                try {
                    Thread.sleep(100);
                    socket.close();
                }
                catch (IOException | InterruptedException e) { e.printStackTrace(); }

            }else if (massage.getServiseCode() == ChatServiceCode.NameOfClient){
                setIdClient(massage.getString());
                frame.setTitle("Chat client " + getNameClient() +  " [" + idClient + "]");
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
                                new ChatClientSettingsWindow(settingsGeneral);
                                break;
                        }
                });

            }
        }
    }
}
