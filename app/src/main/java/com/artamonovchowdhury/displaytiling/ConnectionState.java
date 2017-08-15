package com.artamonovchowdhury.displaytiling;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class for storing current connection state
 */
public class ConnectionState {
    private static ConnectionState instance;
    List<Updatable> listeners = new ArrayList<>();
    // Global variable
    private boolean connected;
    private List<InetAddress> clients;

    // Restrict the constructor from being instantiated
    private ConnectionState() {
    }

    public void setConnected(boolean b) {
        this.connected = b;

        for (Updatable listener : listeners) {
            try {
                listener.connectionStateChanged(b);
            } catch (Throwable e) {
            }
        }
    }

    public void setClients(List<InetAddress> clients) {
        this.clients = clients;
    }

    public static synchronized ConnectionState getInstance() {
        if (instance == null) {

            instance = new ConnectionState();
        }
        return instance;
    }

    public void registerListener(Updatable listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(Updatable listener) {
        if (listener != null && listeners.contains(listener)) {
            listeners.remove(listeners.indexOf(listener));
        }
    }
}
