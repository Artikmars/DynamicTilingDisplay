package com.artamonovchowdhury.displaytiling.statemachine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.artamonovchowdhury.displaytiling.CommunicationService;
import com.artamonovchowdhury.displaytiling.Direction;
import com.artamonovchowdhury.displaytiling.SwipeInfo;

public abstract class StateMachine {

    protected CommunicationService comService;
    protected Handler handler;

    public StateMachine(CommunicationService comIntentService){
        this.comService = comIntentService;
    }
    public void handleSwipe(boolean isSwipeOut, String direction, float angle){
        if(isSwipeOut){
            handleSwipeOut(new SwipeInfo(Direction.valueOf(direction), angle));
        } else {
            handleSwipeIn(new SwipeInfo(Direction.valueOf(direction), angle));
        }
    }
    public abstract void handleSwipeIn(SwipeInfo swipeInfo);
    public abstract void handleSwipeOut(SwipeInfo swipeInfo);
    public abstract void handleMessage(Object message);
    public abstract void setIdle();
    public abstract void setIn(SwipeInfo swipeInfo);
    public abstract void setOut(SwipeInfo swipeInfo);
    public abstract void setAwaitingIn();
    public abstract void setStitching();

    protected void startWaiting(){
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                setIdle();
            }
        }, 5000);
    }

    protected void stopWaiting() {
        handler = new Handler(Looper.getMainLooper());
        handler.removeCallbacksAndMessages(null);
        setIdle();
    }

    protected double[] getScreenDimensions(){
        double[] screenDims = new double[6];

        WindowManager wm = (WindowManager) comService.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        //wm.getDefaultDisplay().getMetrics(dm);

        int width=dm.widthPixels;
        int height=dm.heightPixels;
        float dens=dm.density;
        int dpi=dm.densityDpi;
        double wi=(double)width/(double)dens;
        double hi=(double)height/(double)dens;
        screenDims[0]=width;
        screenDims[1]=height;
        screenDims[2]=wi;
        screenDims[3]=hi;
        screenDims[4]=dens;
        screenDims[5]=dpi;

        return screenDims;
    }
}