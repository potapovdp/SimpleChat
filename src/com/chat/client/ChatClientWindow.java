package com.chat.client;
/*
It's old version for text massage only
 */

import com.chat.server.ChatUnits;
import com.chat.server.ChatServiceCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClientWindow implements Serializable {
    private transient JFrame                            frame;
    private transient JTextArea                         taCommon;
    private transient JTextArea                         taPrivate;
    private transient JToggleButton                     tbBold;
    private transient JToggleButton                     tbItalic;

    private ChatClient                                  client;     //host of this dialog window
    private CopyOnWriteArrayList<ChatUnits>             listUnits;  //list of units for a conversation
    private ChatClientWindow window;     //tis object for identify it in host
    private ChatClientSettings                          settingsGeneral;

    private final int                                   FRAME_WIDTH = 500;

    ChatClientWindow(){}

    ChatClientWindow(ChatClient client, CopyOnWriteArrayList<ChatUnits> listUnits, ChatClientSettings settings) {
        this.client             = client;
        this.listUnits          = listUnits;
        this.settingsGeneral    = settings;
    }

    void startCllient() {
        setupGUI();
        setWindowActive();
    }

    private void setWindowActive(){
        frame.setExtendedState(JFrame.ICONIFIED);
        frame.setExtendedState(JFrame.NORMAL);
        frame.toFront();
        frame.requestFocus();
        taPrivate.requestFocus();
    }

    private void setupGUI() {

        String strUnits = "("+client.getNameClient() + " [" + client.getIdClient() + "]) ";
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
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                 if ( (e.isAltDown()) & (Objects.equals(e.getKeyCode(), KeyEvent.VK_ENTER)) & settingsGeneral.isSendMassageAltEnter() ){
                    sendMassage(null,"");
                }else if ( (e.isControlDown()) & (Objects.equals(e.getKeyCode(), KeyEvent.VK_ENTER)) & settingsGeneral.isSendMassageCtrlEnter() ){
                    sendMassage(null,"");
                }else if ( Objects.equals(e.getKeyCode(), KeyEvent.VK_ENTER) & !e.isControlDown() & !e.isAltDown() & settingsGeneral.isSendMassageEnter() ){
                    sendMassage(null,"");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        //Font settings *******************************
        //Icons for font buttons
        //ImageIcon iFontItalic   = new ImageIcon(new File("").getAbsolutePath().concat("\\FontI.png") );
        ImageIcon iFontBold     = new ImageIcon("resources\\FontB.png");
        ImageIcon iFontItalic   = new ImageIcon("resources\\FontI.png");
        //Image imFontItalic       = new BufferedImage();

        //Listener for buttons
        ButtonListener buttonListener = new ButtonListener();

        //Buttons for font
        tbBold   = new JToggleButton();
        tbBold.setIcon(iFontBold);
        tbBold.setPreferredSize(new Dimension(30, 30));
        tbBold.setMaximumSize(new Dimension(30, 30));
        tbBold.setActionCommand("FontBold");
        tbBold.addActionListener(buttonListener);

        tbItalic  = new JToggleButton();
        tbItalic.setPreferredSize(new Dimension(30, 30));
        tbItalic.setMaximumSize(new Dimension(30, 30));
        tbItalic.setIcon(iFontItalic);
        tbItalic.setActionCommand("FontItalic");
        tbItalic.addActionListener(buttonListener);

        //Panel for font settings
        JPanel panelFont = new JPanel();
        panelFont.setLayout(new BoxLayout(panelFont, BoxLayout.LINE_AXIS));
        panelFont.setMaximumSize(new Dimension(FRAME_WIDTH, 50));
        panelFont.add(tbBold);
        panelFont.add(tbItalic);

        //General panel
        JPanel panel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxLayout);
        panel.add(spCommon);
        panel.add(panelFont);
        panel.add(spPrivate);
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        JButton bSend = new JButton("Send massage");
        bSend.addActionListener((e)  ->  sendMassage(null,"") );

        frame.add(bSend, BorderLayout.SOUTH);
        frame.setSize(new Dimension(FRAME_WIDTH, 550));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //frame.pack();
        frame.setVisible(true);
    }

    private void sendMassage(ChatServiceCode serviseCode, String msg){
        ChatServiceCode shippedServiseCode    = Objects.isNull(serviseCode)     ? ChatServiceCode.SimpleMassage : serviseCode;
        String shippedMsg                 = (msg.equals(""))                ? taPrivate.getText()       :  msg;

        client.sendMassage(shippedServiseCode, shippedMsg, listUnits);

        if (msg.equals(""))
            taPrivate.setText("");

    }

    protected void recievMassage(ChatMassage massage){
        Calendar calendar = Calendar.getInstance();
        ChatServiceCode sc = massage.getServiseCode();
        if (sc == ChatServiceCode.SimpleMassage){
            taCommon.append("" + massage.getUnit() + " (" + calendar.getTime() + "): \n");
            taCommon.append(massage.getString() + "\n");
        }
        setWindowActive();
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

    class ButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Font curFont = taPrivate.getFont();

            if (tbBold.isSelected() && tbItalic.isSelected())
                taPrivate.setFont(curFont.deriveFont(Font.BOLD | Font.ITALIC));
            else if(tbBold.isSelected() && !tbItalic.isSelected())
                taPrivate.setFont(curFont.deriveFont(Font.BOLD));
            else if(!tbBold.isSelected() && tbItalic.isSelected())
                taPrivate.setFont(curFont.deriveFont(Font.ITALIC));
            else
                taPrivate.setFont(curFont.deriveFont(Font.PLAIN));
        }
    }

}
