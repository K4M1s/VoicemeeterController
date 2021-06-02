package me.k4m1s.voicemeetercontroller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectionManager {

    private Socket socket;
    private SocketCommunication socketCommunication;

    public ConnectionManager(String IP) throws IOException {
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(IP,22587), 4000);
        this.socketCommunication = new SocketCommunication();
        socketCommunication.start();
    }

    public void destroy() throws IOException {
        socketCommunication.interrupt();
        this.socket.close();
    }

    public Socket getSocket() {
        return socket;
    }

    public SocketCommunication getSocketCommunication() {
        return this.socketCommunication;
    }
}
