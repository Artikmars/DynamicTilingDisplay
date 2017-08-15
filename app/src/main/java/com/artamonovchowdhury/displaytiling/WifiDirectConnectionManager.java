package com.artamonovchowdhury.displaytiling;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;

public class WifiDirectConnectionManager {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private DTBroadcastReceiver receiver;
    private MainActivity mainActivity;
    private boolean shouldConnect;
    private boolean isMaster;

    public WifiDirectConnectionManager(WifiP2pManager wifiP2pManager, final MainActivity mainActivity) {
        this.wifiP2pManager = wifiP2pManager;
        this.mainActivity = mainActivity;
        this.channel = wifiP2pManager.initialize(mainActivity, Looper.getMainLooper(), null);


        this.receiver = new DTBroadcastReceiver(wifiP2pManager, channel, mainActivity, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(final WifiP2pDeviceList peers) {

                if (isMaster && shouldConnect) {
                    if (peers.getDeviceList().size() >= 1) {
                        // Log.i("Peers Available", "Yes");
                        Log.i("myLogs", "WifiDirectConnectionManager: Peers Available: Yes");

                        final String[] peerNamesArray = new String[peers.getDeviceList().size()];
                        final String[] peerAddressesArray = new String[peers.getDeviceList().size()];
                        int i = 0;
                        for (WifiP2pDevice peer : peers.getDeviceList()) {
                            peerNamesArray[i] = peer.deviceName;
                            peerAddressesArray[i++] = peer.deviceAddress;
                        }


                        mainActivity.openPeerListWindow(peers, peerNamesArray, peerAddressesArray);
                        shouldConnect = false;

                    } else {
                        Log.i("myLogs", "WifiDirectConnectionManager: Peers Available: No");
                    }
                }
            }
        });
    }

    public DTBroadcastReceiver getReceiver() {
        return receiver;
    }

    public void discoverPeers(final boolean isMaster) {

        Log.i("myLogs", "WifiDirectConnectionManager: Discovering Pears...");
        this.isMaster = isMaster;
        shouldConnect = true;
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                Log.i("myLogs", "WifiDirectConnectionManager: Discovering Pears: Success");

            }

            @Override
            public void onFailure(int reasonCode) {

                Log.i("myLogs", "WifiDirectConnectionManager: Discovering Pears: Failure" + reasonCode);

            }
        });
    }

    public void connectToPeer(WifiP2pDevice device) {


        Log.i("myLogs", "WifiDirectConnectionManager: Connecting to Pear...");


        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.groupOwnerIntent = 15;
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                Log.i("myLogs", "WifiDirectConnectionManager: Connecting to Pear: Success");
            }

            @Override
            public void onFailure(int reason) {

                Log.i("myLogs", "WifiDirectConnectionManager: Connecting to Pear: Failure" + reason);
            }
        });
    }
}
