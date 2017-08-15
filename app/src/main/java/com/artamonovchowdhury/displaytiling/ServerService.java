package com.artamonovchowdhury.displaytiling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.artamonovchowdhury.displaytiling.statemachine.StateMachineIdle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerService extends CommunicationService {

    public static String ACTION_SWIPE = "com.artamonovchowdhury.displaytiling.ACTION_SWIPE";
    private static final String EXTRA_IS_SWIPE_OUT = "com.artamonovchowdhury.displaytiling.extra.ISSWIPEOUT";
    private static final String EXTRA_DIRECTION = "com.artamonovchowdhury.displaytiling.extra.DIRECTION";
    private static final String EXTRA_ANGLE = "com.artamonovchowdhury.displaytiling.extra.ANGLE";
    private static final int PORT = 8888;
    private final IBinder mBinder = new ServerServiceBinder();

    private List<InetAddress> clientAddressList = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver;
    public static ServerSocket serverSocket;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;

    public static String handshake;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("myLogs", "Server Service: onCreate");
        this.stateMachine = new StateMachineIdle(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SWIPE);
        this.broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final boolean isSwipeOut = intent.getBooleanExtra(EXTRA_IS_SWIPE_OUT, true);
                final String direction = intent.getStringExtra(EXTRA_DIRECTION);
                final float angle = intent.getFloatExtra(EXTRA_ANGLE, 0.0f);
                stateMachine.handleSwipe(isSwipeOut, direction, angle);
            }
        };

        this.registerReceiver(this.broadcastReceiver, intentFilter);
    }

    @Override
    public void establishSockets(String hostAddress) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    if (serverSocket == null) {
                        serverSocket = new ServerSocket();
                        serverSocket.setReuseAddress(true);
                        serverSocket.bind(new InetSocketAddress(PORT));
                        Log.i("myLogs", "Server Service: New serverSocket built");
                    }

                    Log.i("myLogs", "Server Service: Waiting for a handshake. Run the app on another device");

                    Socket clientSocket = serverSocket.accept();
                    Log.i("myLogs", "Server Service: clientSocket established");

                    inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    Log.i("myLogs", "Server Service: Input Stream built");
                    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    Log.i("myLogs", "Server Service: Output Stream built");

                    clientAddressList.add(clientSocket.getInetAddress());

                    for (InetAddress clientAddress : clientAddressList) {
                        Log.i("myLogs", "Server Service: Client Address: " + clientAddress);
                    }
                    ConnectionState.getInstance().setClients(clientAddressList);

                    handshake = (String) inputStream.readObject();

                    //Start listening to the Input Stream once handshake received

                    if (handshake.equals("Hi")) {
                        Log.i("myLogs", "Server Service: Handshake received: " + handshake);

                        //stateMachine = new StateMachineIdle(ServerService.this);

                        Runnable socketLoop = new Runnable() {
                            public void run() {
                                while (true) {
                                    try {
                                        Object message = inputStream.readObject();
                                        //if(message!=null && swipeHandler instanceof SwipeHandlerStitching == false){
                                        Log.i("myLogs", "Server Service: Message Received: " + message);
                                        stateMachine.handleMessage(message);
                                        //}
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                        performOnBackgroundThread(socketLoop);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.broadcastReceiver);
    }

    @Override
    public void writeOut(Object o) {
        try {
            Log.i("myLogs", "WRITEOUT " + this.toString());
            outputStream.writeObject(o);
            Log.i("myLogs", "MESSAGE SENT: " + o);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
            Log.i("myLogs", "Server Service: NullPointerException (client service is not created) ");
            ne.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServerServiceBinder extends Binder {
        ServerService getService() {
            // Return this instance of ServerService so clients can call public methods
            return ServerService.this;
        }
    }
}