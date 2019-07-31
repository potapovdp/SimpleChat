package com.chat.client;

import com.chat.server.ChatUnits;
import com.chat.server.ServiceCode;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatMassage implements Serializable {
    private String                              string;
    private ChatUnits                           unit;
    private ServiceCode serviseCode;
    private CopyOnWriteArrayList<ChatUnits>     listUnits;

    public ChatMassage(ChatUnits unit, ServiceCode serviseCode, String string, CopyOnWriteArrayList<ChatUnits> listUnits) {
        this.unit           = unit;
        this.string         = string;
        this.serviseCode    = serviseCode;
        this.listUnits      = listUnits;
    }

    public void setServiseCode(ServiceCode serviseCode) {
        this.serviseCode = serviseCode;
    }

    public void setUnit(ChatClient client) {
        this.unit = unit;
    }

    public void setString(String string) {
        this.string = string;
    }

    public void setChatUnits(CopyOnWriteArrayList<ChatClient> listClients) {
        this.listUnits = listUnits;
    }

    public ServiceCode getServiseCode() {
        return serviseCode;
    }

    public ChatUnits getUnit() {
        return unit;
    }

    public String getString() {
        return string;
    }

    public CopyOnWriteArrayList<ChatUnits> getListUnits() {
        return listUnits;
    }
}
