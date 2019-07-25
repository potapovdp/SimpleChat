package com.chat.client;

import com.chat.server.ChatUnits;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatMassage implements Serializable {
    private String                  string;
    private ChatClient              client;
    private String                  serviseString;
    private ArrayList<ChatClient>   listClients;

    public ChatMassage(ChatClient client, String serviseString, String string, ArrayList<ChatClient> listClients) {
        this.client         = client;
        this.string         = string;
        this.serviseString  = serviseString;
        this.listClients    = listClients;
    }

    public void setServiseString(String serviseString) {
        this.serviseString = serviseString;
    }

    public void setClient(ChatClient client) {
        this.client = client;
    }

    public void setString(String string) {
        this.string = string;
    }

    public void setListClients(ArrayList<ChatClient> listClients) {
        this.listClients = listClients;
    }

    public String getServiseString() {
        return serviseString;
    }

    public ChatClient getClient() {
        return client;
    }

    public String getString() {
        return string;
    }

    public ArrayList<ChatClient> getListClients() {
        return listClients;
    }
}
