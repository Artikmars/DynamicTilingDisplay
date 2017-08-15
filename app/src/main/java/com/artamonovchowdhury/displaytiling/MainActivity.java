package com.artamonovchowdhury.displaytiling;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.software.shell.fab.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener, Updatable {

    ImageView mainImageView;
    Bitmap mainImageViewBitmap = null;
    FloatingActionButton fabAddImage;
    FloatingActionButton crossIcon;
    boolean isFullScreen, isConnected;
    float crossIconX, crossIconY = 0.0f;
    float crossIconXCentered, crossIconYCentered;
    boolean crossIconMoving;
    boolean togglingFullscreenFirstTime = true;

    private Swipe swipe;

    private WifiDirectConnectionManager wifiDirectConnectionManager;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mainImageView = (ImageView) findViewById(R.id.mainImageView);

        buildAddButton();
        buildCrossIcon();
        Log.i("myLogs", "Main Activity: onCreate");
        mainImageView.setOnTouchListener(this);

        wifiDirectConnectionManager = new WifiDirectConnectionManager((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE), this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        swipe = new Swipe();

        wifiDirectConnectionManager.discoverPeers(false);
        ConnectionState.getInstance().registerListener(this);

        //Register receiver to listen for stitch
        intentFilterStitch = new IntentFilter("com.artamonovchowdhury.displaytiling.ACTION_STITCH_HAPPENED");
        broadcastReceiverStitch = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("myLogs", "Main Activity: Action Stitch Happened");
                sendBitmap();
            }
        };

        intentFilterBitmap = new IntentFilter("com.artamonovchowdhury.displaytiling.ACTION_SHOW_BITMAP");
        broadcastReceiverBitmap = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("myLogs", "Main Activity: Action Show Bitmap");
                if (intent.hasExtra("bitmap")) {
                    byte[] byteArray = intent.getByteArrayExtra("bitmap");
                    Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

                    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

                    Log.i("myLogs", "Main Activity: Bitmap: " + bmp.getWidth() + " " + bmp.getHeight());
                    Log.i("myLogs", "Main Activity: BitmapDP: " + bmp.getWidth() / displayMetrics.density + " " + bmp.getHeight() / displayMetrics.density);

                    float dpHeightScreen = displayMetrics.heightPixels / displayMetrics.density;
                    float dpWidthScreen = displayMetrics.widthPixels / displayMetrics.density;

                    Log.i("myLogs", "Main Activity: Screen Size displayWidth: " + dpWidthScreen + " displayHeight: " + dpHeightScreen);

                    //mainImageView.setBackgroundColor(Color.RED);

                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics dm = new DisplayMetrics();
                    wm.getDefaultDisplay().getMetrics(dm);
                    int widthScreen = dm.widthPixels;
                    int heightScreen = dm.heightPixels;

                    int newHeight = widthScreen * bmp.getHeight() / bmp.getWidth();
                    Log.i("myLogs", "Main Activity: Bmp Height: " + widthScreen + "*" + bmp.getHeight() + "/" + bmp.getWidth() + "=" + newHeight);

                    Bitmap otherBitmap = Bitmap.createScaledBitmap(bmp, widthScreen, heightScreen, false);
                    mainImageView.setImageBitmap(otherBitmap);
                    Log.i("myLogs", "Main Activity: imgView: " + mainImageView.getWidth() + " " + mainImageView.getHeight());
                }
            }
        };
    }

    BroadcastReceiver broadcastReceiverStitch, broadcastReceiverBitmap;
    IntentFilter intentFilterStitch, intentFilterBitmap;

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiDirectConnectionManager.getReceiver(), intentFilter);
        registerReceiver(broadcastReceiverStitch, intentFilterStitch);
        registerReceiver(broadcastReceiverBitmap, intentFilterBitmap);
        ConnectionState.getInstance().registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiDirectConnectionManager.getReceiver());
        unregisterReceiver(broadcastReceiverStitch);
        unregisterReceiver(broadcastReceiverBitmap);
        ConnectionState.getInstance().unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == RESULT_OK) {
            if (reqCode == 1) {
                Uri imageUri = data.getData();
                try {
                    mainImageViewBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mainImageView.setImageURI(data.getData());
            }
        }
    }

    private void toggleFullScreen() {
        if (!isFullScreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            fabAddImage.hide();
            crossIcon.show();

            if (togglingFullscreenFirstTime) {
                crossIconXCentered = crossIcon.getX();
                crossIconYCentered = crossIcon.getY();
                togglingFullscreenFirstTime = false;
            } else {
                ObjectAnimator animationX = ObjectAnimator.ofFloat(crossIcon, "x", crossIcon.getX(), crossIconXCentered);
                ObjectAnimator animationY = ObjectAnimator.ofFloat(crossIcon, "y", crossIcon.getY(), crossIconYCentered);
                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(animationX, animationY);
                animSetXY.setDuration(1000);
                animSetXY.setInterpolator(new OvershootInterpolator());
                animSetXY.start();
            }
            isFullScreen = true;

        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            fabAddImage.show();
            crossIcon.hide();
            isFullScreen = false;
            swipe = new Swipe();
        }

        mainImageView.requestLayout();
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            toggleFullScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v.equals(crossIcon)) {//Movement of the crossIcon

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    crossIconMoving = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (crossIconMoving && !swipe.isOffScreen()) {
                        //this prevents the crossicon from jumping when dragged
                        crossIconX = event.getRawX() - crossIcon.getWidth() / 2;
                        crossIconY = event.getRawY() - crossIcon.getHeight() / 2;
                        crossIcon.setX(crossIconX);
                        crossIcon.setY(crossIconY);

                        //dragging the crossIcon off the screen
                        if (event.getRawX() >= mainImageView.getWidth() - 30) {
                            startTilingProcess(event.getRawX(), event.getRawY(), Direction.RIGHT);
                        }
                        if (event.getRawX() <= 30) {
                            startTilingProcess(event.getRawX(), event.getRawY(), Direction.LEFT);
                        }
                        if (event.getRawY() <= 30) {
                            startTilingProcess(event.getRawX(), event.getRawY(), Direction.UP);
                        }
                        if (event.getRawY() >= mainImageView.getHeight() - 30) {
                            startTilingProcess(event.getRawX(), event.getRawY(), Direction.DOWN);
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    //animation stuff
                    if (!swipe.isOffScreen()) {
                        ObjectAnimator animationX = ObjectAnimator.ofFloat(crossIcon, "x", crossIcon.getX(), crossIconXCentered);
                        ObjectAnimator animationY = ObjectAnimator.ofFloat(crossIcon, "y", crossIcon.getY(), crossIconYCentered);
                        AnimatorSet animSetXY = new AnimatorSet();
                        animSetXY.playTogether(animationX, animationY);
                        animSetXY.setDuration(1000);
                        animSetXY.setInterpolator(new OvershootInterpolator());
                        animSetXY.start();
                    }

                    crossIconMoving = false;
                    break;
            }
        } else if (v.equals(mainImageView)) {//Swipe onto screen

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Vector2d swipeStartPoint;
                    if (event.getRawX() <= 100) {
                        //touch on left side
                        swipeStartPoint = new Vector2d(event.getRawX(), event.getRawY());
                        swipe = new Swipe(this, false, Direction.RIGHT, swipeStartPoint);
                    } else if (event.getRawX() >= mainImageView.getWidth() - 100) {
                        //touch on right side
                        swipeStartPoint = new Vector2d(event.getRawX(), event.getRawY());
                        swipe = new Swipe(this, false, Direction.LEFT, swipeStartPoint);
                    } else if (event.getRawY() <= 100) {
                        //touch on top of screen
                        swipeStartPoint = new Vector2d(event.getRawX(), event.getRawY());
                        swipe = new Swipe(this, false, Direction.DOWN, swipeStartPoint);
                    } else if (event.getRawY() >= mainImageView.getHeight() - 100) {
                        //touch on bottom of screen
                        swipeStartPoint = new Vector2d(event.getRawX(), event.getRawY());
                        swipe = new Swipe(this, false, Direction.UP, swipeStartPoint);
                    } else {
                        //touch on center of screen
                        toggleFullScreen();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (swipe != null) {
                        if (swipe.inProgress()) {
                            swipe.setSwipeEndPoint(new Vector2d(event.getRawX(), event.getRawY()));
                            Log.i("myLogs", "Main Activity: MotionEvent.ACTION_UP" + swipe.toString());
                            //wifiDirectConnectionManager.discoverPeers(false);
                        }
                    }
                    break;
            }
        }

        return true;
    }

    private void startTilingProcess(float x, float y, Direction dir) {
        Vector2d centerPoint = new Vector2d(mainImageView.getWidth() / 2, mainImageView.getHeight() / 2);
        Vector2d edgePoint = new Vector2d(x, y);
        swipe = new Swipe(this, true, dir, centerPoint, edgePoint);
        Log.i("myLogs", "Main Activity: startTilingProcess: swipe to string: " + swipe.toString());
    }

    private void buildAddButton() {
        fabAddImage = (FloatingActionButton) findViewById(R.id.action_button);
        fabAddImage.setButtonColor(getResources().getColor(R.color.purple));
        fabAddImage.setButtonColorPressed(getResources().getColor(R.color.purple));
        fabAddImage.setImageDrawable(getResources().getDrawable(R.drawable.fab_plus_icon));
        fabAddImage.setAnimationOnShow(FloatingActionButton.Animations.FADE_IN);
        fabAddImage.setAnimationOnHide(FloatingActionButton.Animations.FADE_OUT);
        fabAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Select an Image",
                        Toast.LENGTH_SHORT).show();

                //select image from phone
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);

                toggleFullScreen();
            }
        });
    }

    private void buildCrossIcon() {
        crossIcon = (FloatingActionButton) findViewById(R.id.cross_icon);
        crossIcon.setButtonColor(getResources().getColor(R.color.purple));
        crossIcon.setButtonColorPressed(getResources().getColor(R.color.purple));
        crossIcon.setImageDrawable(getResources().getDrawable(R.drawable.crossarrowbuttonpurple));
        crossIcon.setAnimationOnShow(FloatingActionButton.Animations.FADE_IN);
        crossIcon.setAnimationOnHide(FloatingActionButton.Animations.FADE_OUT);
        crossIcon.setOnTouchListener(this);
        crossIcon.hide();
    }

    public static boolean isWindowOpen = false;
    private MaterialDialog window;

    public void openPeerListWindow(final WifiP2pDeviceList peers, String[] names, final String[] adresses) {
        if (isWindowOpen) {
            window.dismiss();
        }
        isWindowOpen = true;

        window = new MaterialDialog.Builder(this)
                .title(R.string.p2pConnectionDialogueTitle)
                .items(names)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.i("myLogs", "Main Activity: Device Selected, Name: " + text);
                        wifiDirectConnectionManager.connectToPeer(peers.get(adresses[which]));
                        isWindowOpen = false;
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        isWindowOpen = false;
                    }
                })
                .autoDismiss(true)
                .show();

    }

    public void openConnectionWindow(boolean shouldConnect) {

        if (shouldConnect) {
            if (!isWindowOpen) {
                window = new MaterialDialog.Builder(this)
                        .title(R.string.connection_window_not_connected_title)
                        .content(R.string.connection_widow_not_connected_content)
                        .positiveText(R.string.agree)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                wifiDirectConnectionManager.discoverPeers(true);
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                isWindowOpen = false;
                            }
                        })
                        .show();
                isWindowOpen = true;
            }
        } else {
            if (isWindowOpen) {
                window.dismiss();
            }
            new MaterialDialog.Builder(this)
                    .title(R.string.connection_window_connected_title)
                    .content(R.string.connection_window_connected_content)
                    .positiveText(R.string.cool)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            isWindowOpen = false;
                        }
                    })
                    .show();
            isWindowOpen = true;
        }

    }

    @Override
    public void connectionStateChanged(boolean isConnected) {

        Log.i("myLogs", "Main Activity: Update. Connection: " + isConnected);

        //Not Connected - Initiate Connection Process
        if (!this.isConnected && !isConnected) {
            openConnectionWindow(true);

        }

        //Connection Lost - Reestablish Connection
        if (this.isConnected && !isConnected) {
            wifiDirectConnectionManager.discoverPeers(false);
            openConnectionWindow(true);
        }

        //Connection Established - Confirm
        if (!this.isConnected && isConnected) {
            openConnectionWindow(false);
        }

        this.isConnected = isConnected;
    }

    private void sendBitmap() {
        //mainImageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        mainImageView.getLayoutParams();
        mainImageView.buildDrawingCache();
        Bitmap bmp = mainImageView.getDrawingCache();
        //mainImageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
        //Convert to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        if (mainImageViewBitmap != null) {
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            mainImageViewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
            byteArray = stream2.toByteArray();
        }

        Intent i = new Intent("com.artamonovchowdhury.displaytiling.ACTION_BITMAP");
        i.putExtra("image", byteArray);
        sendBroadcast(i);
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}