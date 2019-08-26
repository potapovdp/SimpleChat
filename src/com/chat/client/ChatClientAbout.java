package com.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatClientAbout implements Runnable {
    private String strAbout;
    private ChatClient client;

    public ChatClientAbout(ChatClient client) {
        this.client = client;

        strAbout =  "Author: Potapov Dmitry \n";
        strAbout += "version: " + client.getVersion()  + "\n";
        strAbout += "e-mail: dmitriyp.potapov@gmail.com";

        JFrame frame = new JFrame("About");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(300, 200));

        JButton button  = new JButton("OK");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setExtendedState(JFrame.ICONIFIED);
                frame.setVisible(false);
            }
        });

        JTextArea taAbout = new JTextArea();
        taAbout.setEnabled(false);
        taAbout.setLineWrap(true);
        //taAbout.setBackground(Color.CYAN);
        //taAbout.setFont(new Font());
        taAbout.setText(strAbout);


        frame.add(BorderLayout.CENTER, taAbout);
        frame.add(BorderLayout.SOUTH ,button);

        //frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void run() {

    }
}
