package com.chat.server;

import com.chat.client.ChatClient;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatUnits {
    private ChatClient          client;
    private ObjectOutputStream  outObj;
    private ObjectInputStream   inObj;
    private Socket              socket;

    public ChatUnits(ChatClient client, ObjectOutputStream outObj, ObjectInputStream inObj, Socket socket) {
        this.client = client;
        this.outObj = outObj;
        this.inObj = inObj;
        this.socket = socket;
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

    @Override
    public String toString() {
        return getClient().getName();
    }
}
