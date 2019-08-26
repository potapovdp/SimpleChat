package com.chat.client;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class ChatClientSettings implements Serializable {
    private String      nameClient;
    private boolean     sendMassageEnter;
    private boolean     sendMassageAltEnter;
    private boolean     sendMassageCtrlEnter;
    private Path        pathFileSettings;

    public void setNameClient(String nameClient) {
        this.nameClient = nameClient;
    }

    public void setSendMassageEnter(boolean sendMassageEnter) {
        this.sendMassageEnter = sendMassageEnter;
    }

    public void setSendMassageAltEnter(boolean sendMassageAltEnter) {
        this.sendMassageAltEnter = sendMassageAltEnter;
    }

    public void setPathFileSettings(Path pathFileSettings) {
        this.pathFileSettings = pathFileSettings;
    }

    public void setSendMassageCtrlEnter(boolean sendMassageCtrlEnter) {
        this.sendMassageCtrlEnter = sendMassageCtrlEnter;
    }

    public String getNameClient() {
        return nameClient;
    }

    public boolean isSendMassageEnter() {
        return sendMassageEnter;
    }

    public boolean isSendMassageAltEnter() {
        return sendMassageAltEnter;
    }

    public Path getPathFileSettings() {
        return pathFileSettings;
    }

    public boolean isSendMassageCtrlEnter() {
        return sendMassageCtrlEnter;
    }
}
