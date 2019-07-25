package com.chat.client;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatMassage implements Serializable {
    private String                  string;
    private ChatClientWindow client;
    private String                  serviseString;
    private ArrayList<ChatClientWindow>   listClients;

    public ChatMassage(ChatClientWindow client, String serviseString, String string, ArrayList<ChatClientWindow> listClients) {
        this.client         = client;
        this.string         = string;
        this.serviseString  = serviseString;
        this.listClients    = listClients;
    }

    public void setServiseString(String serviseString) {
        this.serviseString = serviseString;
    }

    public void setClient(ChatClientWindow client) {
        this.client = client;
    }

    public void setString(String string) {
        this.string = string;
    }

    public void setListClients(ArrayList<ChatClientWindow> listClients) {
        this.listClients = listClients;
    }

    public String getServiseString() {
        return serviseString;
    }

    public ChatClientWindow getClient() {
        return client;
    }

    public String getString() {
        return string;
    }

    public ArrayList<ChatClientWindow> getListClients() {
        return listClients;
    }
}
