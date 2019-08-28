package com.chat.client;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Objects;

public class ChatClientSettingsWindow {

    //Interface
    private transient ObjectOutputStream    outObjSettings;
    private transient JFrame                frame;
    private transient JTextField            tfNameClient;
    private transient JCheckBox             cbEnter;
    private transient JCheckBox             cbAltEnter;
    private transient JCheckBox             cbCtrlEnter;
    private transient JFormattedTextField   tfServerIP;
    private transient JFormattedTextField   tfServerSocket;

    //Settings
    private ChatClientSettings              settingsGeneral;
    private transient ChatClient            client;

    {
        tfNameClient    = new JTextField();
    }

    public ChatClientSettingsWindow(ChatClient client) {
        this.client     = client;
        settingsGeneral = client.getSettingsGeneral();

        frame   = new JFrame("Settings");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(300, 300));
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if ( Objects.equals(e.getKeyCode(), KeyEvent.VK_ESCAPE) ){
                    frame.setExtendedState(JFrame.ICONIFIED);
                    frame.setVisible(false);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        //Tab
        JTabbedPane tpSettings      = new JTabbedPane();

        //Central pane
        JPanel panelCentral         = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral,BoxLayout.Y_AXIS));

        //Control pane
        JPanel paneKeys             = new JPanel();
        paneKeys.setLayout(new BoxLayout(paneKeys,BoxLayout.Y_AXIS));

        //Net pane
        JPanel panelNet             = new JPanel();
        panelNet.setLayout(new BoxLayout(panelNet, BoxLayout.Y_AXIS));

        //Buttons panel
        JPanel panelButton          = new JPanel();
        panelButton.setLayout(new FlowLayout());

        //Fielfs
        tfNameClient.setPreferredSize(new Dimension(60, 10));
        panelCentral.add(new JLabel("Client name:"));
        panelCentral.add(tfNameClient);

        MaskFormatter mfip  = new MaskFormatter();
        MaskFormatter mfsoc = new MaskFormatter();
        try {
            mfip.setMask("###.###.###.###");
            mfsoc.setMask("#####");
        }catch (ParseException e){
            e.printStackTrace();
        }
//        mfip.setPlaceholder("0");
//        mfsoc.setPlaceholder("0");

        tfServerIP          = new JFormattedTextField(mfip);
        tfServerSocket      = new JFormattedTextField();
        panelNet.add(new JLabel("Server IP:"));
        panelNet.add(tfServerIP);
        panelNet.add(new JLabel("Server Socket:"));
        panelNet.add(tfServerSocket);

        //Buttons
        ButtonListener bListener    = new ButtonListener();
        JButton bOk                 = new JButton("OK");
        JButton bClose              = new JButton("Close");
        bOk.addActionListener(bListener);
        bClose.addActionListener(bListener);
        panelButton.add(bOk);
        panelButton.add(bClose);

        //Check buttons
        cbEnter           = new JCheckBox("Send by key \"Enter\"");
        cbAltEnter        = new JCheckBox("Send by key \"Alt\" + \"Enter\"");
        cbCtrlEnter       = new JCheckBox("Send by key \"Ctrl\" + \"Enter\"");
        cbAltEnter.setEnabled(false);
        cbEnter.setActionCommand("SendEnter");
        cbAltEnter.setActionCommand("SendAltEnter");
        cbCtrlEnter.setActionCommand("SendCtrlEnter");
        cbEnter.addActionListener(bListener);
        cbAltEnter.addActionListener(bListener);
        cbCtrlEnter.addActionListener(bListener);
        paneKeys.add(cbEnter);
        paneKeys.add(cbAltEnter);
        paneKeys.add(cbCtrlEnter);

        tpSettings.addTab("Visualization", panelCentral);
        tpSettings.addTab("Net", panelNet);
        tpSettings.addTab("Keys", paneKeys);

        frame.add(BorderLayout.CENTER, tpSettings);
        frame.add(BorderLayout.SOUTH, panelButton);
        frame.setVisible(true);

        fillSettings();
    }

    private void fillSettings(){
        if ( !Objects.isNull(settingsGeneral) ){

            if (tfNameClient.getText().equals("")){
                tfNameClient.setText(settingsGeneral.getNameClient());
            }

            cbEnter.setSelected(settingsGeneral.isSendMassageEnter());
            cbAltEnter.setSelected(settingsGeneral.isSendMassageAltEnter());
            cbCtrlEnter.setSelected(settingsGeneral.isSendMassageCtrlEnter());

            tfServerIP.setText(settingsGeneral.getServerIp().replace(".",""));
            tfServerSocket.setText(""+ settingsGeneral.getServerSocket());
        }
    }

    private boolean saveSettings(){
        boolean res = false;

        //Gather fielfs
        ChatClientSettings settings = new ChatClientSettings();
        settings.setNameClient(tfNameClient.getText().trim());
        settings.setSendMassageEnter(cbEnter.isSelected());
        settings.setSendMassageAltEnter(cbAltEnter.isSelected());
        settings.setSendMassageCtrlEnter(cbCtrlEnter.isSelected());

        settings.setServerIp(tfServerIP.getText());
        settings.setServerSocket(Integer.parseInt(tfServerSocket.getText()));

        //Gather invisible fields
        Path pathFileSettings = settingsGeneral.getPathFileSettings();
        if ( Objects.isNull(pathFileSettings) || Objects.equals(pathFileSettings, "") ){
            settings.fillPathFileByDefault();
            pathFileSettings = settings.getPathFileSettings();
        }

        settings.setPathFileSettings(pathFileSettings.toString());

        try {
            outObjSettings = new ObjectOutputStream(new FileOutputStream(pathFileSettings.toFile()));
            outObjSettings.writeObject(settings);
            outObjSettings.close();
            res             = true;
            client.setSettingsGeneral(settings);

        }catch (IOException e){
            e.printStackTrace();
        }

        return res;
    }

    class ButtonListener implements ActionListener{
        String actionCommand = "";

        @Override
        public void actionPerformed(ActionEvent e) {
            actionCommand = e.getActionCommand();
            if (actionCommand.equals("Close")){
                frame.setExtendedState(JFrame.ICONIFIED);
                frame.setVisible(false);

            }else if (actionCommand.equals("OK")){
                if (!saveSettings()){
                    JOptionPane.showMessageDialog(null, "Settings hasn't saved");
                }
                frame.setExtendedState(JFrame.ICONIFIED);
                frame.setVisible(false);
            }
        }
    }
}
