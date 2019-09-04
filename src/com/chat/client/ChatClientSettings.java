package com.chat.client;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Objects;

public class ChatClientSettings implements Serializable {
    private String      nameClient;
    private boolean     sendMassageEnter;
    private boolean     sendMassageAltEnter;
    private boolean     sendMassageCtrlEnter;
    private String      pathFileSettings;

    //Net
    private String      serverIp;
    private int         serverSocket;

    //Setters *********************************************
    public void setNameClient(String nameClient) {
        this.nameClient = nameClient;
    }

    public void setSendMassageEnter(boolean sendMassageEnter) {
        this.sendMassageEnter = sendMassageEnter;
    }

    public void setSendMassageAltEnter(boolean sendMassageAltEnter) {
        this.sendMassageAltEnter = sendMassageAltEnter;
    }

    public void setPathFileSettings(String pathFileSettings) {
        this.pathFileSettings = pathFileSettings;
    }

    public void setSendMassageCtrlEnter(boolean sendMassageCtrlEnter) {
        this.sendMassageCtrlEnter = sendMassageCtrlEnter;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setServerSocket(int serverSocket) {
        this.serverSocket = serverSocket;
    }

    //Getters *********************************************
    public String getNameClient() {
        return nameClient;
    }

    public boolean isSendMassageEnter() {
        return sendMassageEnter;
    }

    public boolean isSendMassageAltEnter() {
        return sendMassageAltEnter;
    }

    public String getPathString(){
        return pathFileSettings;
    }

    public Path getPathFileSettings() {
        return Path.of(getPathString());
    }

    public boolean isSendMassageCtrlEnter() {
        return sendMassageCtrlEnter;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerSocket() {
        return serverSocket;
    }

    //Fill fields
    void fillPathFileByDefault(){
        String path = Path.of(System.getProperty("user.home") + "\\settings").toString();
        setPathFileSettings(path);
    }

    void fillByDefault(){
        fillPathFileByDefault();

        //Set a send method
        if (!isSendMassageAltEnter() & !isSendMassageCtrlEnter() & !isSendMassageEnter()){
            setSendMassageAltEnter(true);
        }

        //Set Net and General settings
        InetAddress inetAddress = null;
        String      strIp       = "192.168.1.110";
        int         intSocket   = 7171;
        try {
            inetAddress = InetAddress.getLocalHost();
        }catch (UnknownHostException e) {e.printStackTrace();}
        if ( !Objects.isNull(inetAddress) ){
            strIp       = getFullIp(inetAddress.getHostAddress().split("\\."));
            intSocket   = intSocket;
            setNameClient(inetAddress.getHostName());
        }

        setServerIp(strIp);
        setServerSocket(intSocket);

    }

    public String getFullIp(String[] sip){
        String res = "";
        for (String s : sip) {
            res += ("000" + s).substring(s.length());
        }
        return res;
    }

    public String getFullSocket(String socket){
        String res = "";
        res += ("00000" + socket).substring(socket.length());

        return res;
    }
}
