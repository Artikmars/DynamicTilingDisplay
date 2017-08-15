package com.artamonovchowdhury.displaytiling;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import static com.artamonovchowdhury.displaytiling.ServerService.handshake;


public class ServiceManager implements Updatable {

    private Context context;
    private boolean bound = false, isServer = false;
    private CommunicationService mService;
    private String hostAddress;
    private ImageProcessor imageProcessor;

    private final static String EXTRA_BITMAP = "com.artamonovchowdhury.displaytiling.extra.BITMAP";
    private final static String EXTRA_DIRECTION = "com.artamonovchowdhury.displaytiling.extra.DIRECTION";
    private final static String EXTRA_ISMASTER = "com.artamonovchowdhury.displaytiling.extra.ISMASTER";
    private String dirString;

    public ServiceManager() {
    }

    public void startServerService() {
        //mService = new ServerService();
        Log.i("myLogs", "Service Manager: Starting Client Service... " + this.toString());
        isServer = true;
        Intent intent = new Intent(context, ServerService.class);
        context.bindService(intent, serverConnection, Context.BIND_AUTO_CREATE);

        //mService.establishSockets(null);
    }

    public void startClientService(String hostAddress) {
        this.hostAddress = hostAddress;
        Intent intent = new Intent(context, ClientService.class);
        //intent.setAction("com.artamonovchowdhury.displaytiling.action.ESTABLISH_SOCKETS");
        intent.putExtra("com.artamonovchowdhury.displaytiling.extra.HOSTADDRESS", hostAddress);

        context.bindService(intent, clientConnection, Context.BIND_AUTO_CREATE);
        //mService.establishSockets(hostAddress);

        Log.i("myLogs", "Starting Client Service...");
        // mService.startActionEstablishSockets(context, hostAddress);
        //}
    }

    private ServiceConnection serverConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to ServerIntentService, cast the IBinder and get ServerIntentService instance
            ServerService.ServerServiceBinder binder = (ServerService.ServerServiceBinder) service;
            mService = binder.getService();
            mService.establishSockets(hostAddress);
            bound = true;
            Log.i("myLogs", "Service Manager: Server Connection: onServiceConnected: " + handshake);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            Log.i("myLogs", "Service Manager: Server Connection: onServiceDisconnected");

        }
    };

    private ServiceConnection clientConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to ServerIntentService, cast the IBinder and get ServerIntentService instance
            ClientService.ClientServiceBinder binder = (ClientService.ClientServiceBinder) service;
            mService = binder.getService();
            mService.establishSockets(hostAddress);
            bound = true;
            Log.i("myLogs", "Service Manager: Client Connection: onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            Log.i("myLogs", "Service Manager: Client Connection: onServiceDisconnected");
        }
    };

    @Override
    public void connectionStateChanged(boolean isConnected) {
        Log.i("myLogs", "Service Manager: isConnected: " + isConnected + " Bound?: " + bound);
        if (!isConnected && bound) {
            if (isServer) {
                Log.i("myLogs", "Service Manager: Connection Lost. Unbinding Service");
                context.unbindService(serverConnection);
            } else {
                context.unbindService(clientConnection);
            }
        }
    }

    private void startImageProcessing(Bitmap bmp) {

        Bitmap[] cutBitmaps = imageProcessor.processImage(bmp);
        Bitmap myBmp = cutBitmaps[0], otherBmp = cutBitmaps[1];

        //show myBmp
        double[] imgViewDims = imageProcessor.getImageViewDims();
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        myBmp.compress(Bitmap.CompressFormat.PNG, 100, stream1);
        byte[] byteArray1 = stream1.toByteArray();


        Intent intent = new Intent(context, FullscreenActivity.class);
        intent.putExtra(EXTRA_BITMAP, byteArray1);
        intent.putExtra("imgViewDims", imgViewDims);
        intent.putExtra(EXTRA_DIRECTION, dirString);
        intent.putExtra(EXTRA_ISMASTER, true);
        context.startActivity(intent);

        //send otherBmp
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        otherBmp.compress(Bitmap.CompressFormat.PNG, 100, stream2);
        byte[] byteArray2 = stream2.toByteArray();
        mService.writeOut(byteArray2);
    }

    public void registerReceivers(Context context) {
        this.context = context;

        ConnectionState.getInstance().registerListener(this);


        //Register receiver to listen for stitch
        IntentFilter filterStitch = new IntentFilter("com.artamonovchowdhury.displaytiling.ACTION_STITCH");
        BroadcastReceiver receiverStitch = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                double[] myDims = intent.getDoubleArrayExtra("MY_DIMS");
                double[] otherDims = intent.getDoubleArrayExtra("OTHER_DIMS");
                dirString = intent.getExtras().getString("DIRECTION");
                imageProcessor = new ImageProcessor(myDims, otherDims, dirString, context);
                Intent i = new Intent("com.artamonovchowdhury.displaytiling.ACTION_STITCH_HAPPENED");
                context.sendBroadcast(i);

            }
        };
        context.registerReceiver(receiverStitch, filterStitch);

        //Register receiver to listen for Bitmap
        IntentFilter filter = new IntentFilter("com.artamonovchowdhury.displaytiling.ACTION_BITMAP");
        BroadcastReceiver receiverBitmap = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                byte[] byteArray = intent.getByteArrayExtra("image");
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                Log.i("myLogs", "Service Manager: onReceive BitMap: startImageProcessing:");
                startImageProcessing(bmp);
            }
        };
        context.registerReceiver(receiverBitmap, filter);
    }


}