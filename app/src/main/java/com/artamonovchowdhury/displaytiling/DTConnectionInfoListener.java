package com.artamonovchowdhury.displaytiling;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;


public class DTConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {

    Context context;
    private ServiceManager serviceManager;

    public DTConnectionInfoListener(Context context) {
        this.context = context;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        //Internet Address from WifiP2pInfo struct.
        //Internet Address groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
        String hostAddress = info.groupOwnerAddress.getHostAddress();
        // String hostName = info.groupOwnerAddress.getHostName();

        if (serviceManager == null) {
            Log.i("myLogs", "DTConnectionInfoListener: Building New Service Manager... (serviceManager hasn't created yet)");
            serviceManager = new ServiceManager();

            // After the group negotiation, we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a server thread and accepting
                // incoming connections.

                Log.i("myLogs", "DTConnectionInfoListener: I am a Server ");
                Toast.makeText(context, "You are a Server", Toast.LENGTH_LONG).show();
                Log.i("myLogs", "DTConnectionInfoListener: this address: " + hostAddress);

                serviceManager.registerReceivers(context);
                serviceManager.startServerService();


            } else if (info.groupFormed) {
                // The other device acts as the group owner. In this case,
                // you'll want to create a client thread that connects to the group
                // owner.
                Log.i("myLogs", "DTConnectionInfoListener: I am a Client");
                Toast.makeText(context, "You are a Client", Toast.LENGTH_LONG).show();
                serviceManager.registerReceivers(context);
                Log.i("myLogs", "DTConnectionInfoListener: hostAddress: " + hostAddress);
                serviceManager.startClientService(hostAddress);

            }
        }
    }
}
