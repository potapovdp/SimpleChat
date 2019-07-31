package com.chat.server;

import com.chat.client.ChatClient;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class ChatUnits implements Serializable {
    private ChatClient                      client;
    private String                          idClient;
    private transient ObjectOutputStream    outObj;
    private transient ObjectInputStream     inObj;
    private transient Socket                socket;

    public ChatUnits(ChatClient client, String id, ObjectOutputStream outObj, ObjectInputStream inObj, Socket socket) {
        this.client     = client;
        this.outObj     = outObj;
        this.inObj      = inObj;
        this.socket     = socket;
        this.idClient   = id;
    }

    public ChatClient getClient() {
        return client;
    }

    public ObjectOutputStream getOutObj() {
        return outObj;
    }

    public ObjectInputStream getInObj() {
        return inObj;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getIDClient() {
        return idClient;
    }

    public void setClient(ChatClient client) {
        this.client = client;
    }

    public void setOutObj(ObjectOutputStream outObj) {
        this.outObj = outObj;
    }

    public void setInObj(ObjectInputStream inObj) {
        this.inObj = inObj;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setIDClient(String idClient) {
        this.idClient = idClient;
    }

    @Override
    public String toString() {
        return getClient().getName() + " [" + getIDClient() + "]";
    }
}
