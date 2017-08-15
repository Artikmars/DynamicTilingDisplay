package com.artamonovchowdhury.displaytiling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.artamonovchowdhury.displaytiling.statemachine.StateMachineIdle;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientService extends CommunicationService {

    public static String ACTION_SWIPE = "com.artamonovchowdhury.displaytiling.ACTION_SWIPE";
    private static final String EXTRA_ISSWIPEOUT = "com.artamonovchowdhury.displaytiling.extra.ISSWIPEOUT";
    private static final String EXTRA_ANGLE = "com.artamonovchowdhury.displaytiling.extra.ANGLE";
    public final static String EXTRA_DIRECTION = "com.artamonovchowdhury.displaytiling.extra.DIRECTION";
    BroadcastReceiver broadcastReceiver;
    private final IBinder mBinder = new ClientServiceBinder();

    private static final int PORT = 8888;
    Socket clientSocket;
    ObjectInputStream inputstream;
    ObjectOutputStream outputStream;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("myLogs", "Client Service: onCreate");
        this.stateMachine = new StateMachineIdle(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SWIPE);
        this.broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final boolean isSwipeOut = intent.getBooleanExtra(EXTRA_ISSWIPEOUT, true);
                final String direction = intent.getStringExtra(EXTRA_DIRECTION);
                final float angle = intent.getFloatExtra(EXTRA_ANGLE, 0.0f);
                stateMachine.handleSwipe(isSwipeOut, direction, angle);
            }
        };

        this.registerReceiver(this.broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ClientServiceBinder extends Binder {
        ClientService getService() {
            return ClientService.this;
        }
    }

    @Override
    public void writeOut(Object o) {
        try {
            outputStream.writeObject(o);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException n) {
            Log.i("myLogs", "Client Service: NullPointerException (server service is not created)");
        }
    }


    @Override
    public void establishSockets(final String hostAddress) {

        Thread thread = new Thread() {
            @Override
            public void run() {

                try {
                    clientSocket = new Socket();
                    clientSocket.bind(null);
                    Log.i("myLogs", "Client Service: hostAddress: " + hostAddress);
                    clientSocket.setReuseAddress(true);
                    clientSocket.connect((new InetSocketAddress(hostAddress, PORT)), 5000);


                    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    String handshake = "Hi";
                    writeOut(handshake);
                    //outputStream.writeObject(handshake);
                    inputstream = new ObjectInputStream(clientSocket.getInputStream());

                    //stateMachine = new StateMachineIdle(ClientService.this);

                    Runnable socketLoop = new Runnable() {
                        public void run() {
                            while (true) {
                                try {
                                    Object message = inputstream.readObject();
                                    if (message != null) {
                                        Log.i("myLogs", "Client Service: Message Received: " + message);
                                        stateMachine.handleMessage(message);
                                    } else {
                                        Log.i("myLogs", "Client Service: Message is: " + message);
                                    }
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (EOFException e) {

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    performOnBackgroundThread(socketLoop);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

}