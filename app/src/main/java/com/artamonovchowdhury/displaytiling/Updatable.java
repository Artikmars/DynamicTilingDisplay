package com.artamonovchowdhury.displaytiling;

/**
 * Classes which implement Updatable can listen to changes in the ConnectionState Singleton
 */
public interface Updatable {
    void connectionStateChanged(boolean isConnected);
}
