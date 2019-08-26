package com.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Path;
import java.util.Objects;

public class ChatClientSettingsWindow {
    private transient ObjectOutputStream    outObjSettings;
    private transient JFrame                frame;
    private transient JTextField            tfNameClient;
    private transient JCheckBox             cbEnter;
    private transient JCheckBox             cbAltEnter;
    private transient JCheckBox             cbCtrlEnter;
    private ChatClientSettings              settingsGeneral;

    {
        tfNameClient    = new JTextField();
    }

    public ChatClientSettingsWindow(ChatClientSettings sg) {
        settingsGeneral = sg;

        frame   = new JFrame("Settings");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(300, 300));

        //Tab
        JTabbedPane tpSettings      = new JTabbedPane();

        //Central pane
        JPanel panelCentral         = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral,BoxLayout.Y_AXIS));

        //Control pane
        JPanel paneKeys             = new JPanel();
        paneKeys.setLayout(new BoxLayout(paneKeys,BoxLayout.Y_AXIS));

        //Buttons panel
        JPanel panelButton          = new JPanel();
        panelButton.setLayout(new FlowLayout());

        //Fielfs
        tfNameClient.setPreferredSize(new Dimension(60, 10));
        panelCentral.add(tfNameClient);

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
        }
    }

    private boolean saveSettings(){
        boolean res = false;

        //Gather fielfs
        ChatClientSettings settings = new ChatClientSettings();
        settings.setNameClient(tfNameClient.getText().trim());
        settings.setSendMassageEnter(settingsGeneral.isSendMassageEnter());
        settings.setSendMassageAltEnter(settingsGeneral.isSendMassageAltEnter());
        settings.setSendMassageCtrlEnter(settingsGeneral.isSendMassageCtrlEnter());

        Path pathFileSettings = settingsGeneral.getPathFileSettings();
        try {
            outObjSettings = new ObjectOutputStream(new FileOutputStream(pathFileSettings.toFile()));
            outObjSettings.writeObject(settings);
            outObjSettings.close();
            res             = true;
            settingsGeneral = settings;

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
            }else if (actionCommand.equals("SendEnter")){
                settingsGeneral.setSendMassageEnter(cbEnter.isSelected());
            }else if (actionCommand.equals("SendAltEnter")){
                settingsGeneral.setSendMassageAltEnter(cbAltEnter.isSelected());
            }else if (actionCommand.equals("SendCtrlEnter")){
                settingsGeneral.setSendMassageCtrlEnter(cbCtrlEnter.isSelected());
            }
        }
    }
}
