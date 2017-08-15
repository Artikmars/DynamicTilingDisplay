package com.artamonovchowdhury.displaytiling.statemachine;

import android.util.Log;

import com.artamonovchowdhury.displaytiling.CommunicationService;
import com.artamonovchowdhury.displaytiling.SwipeInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StateMachineIn extends StateMachine {

    private SwipeInfo swipeInfo;

    public StateMachineIn(CommunicationService comService, SwipeInfo swipeInfo) {
        super(comService);

        Log.i("myLogs", "Swipe Handler: Set to In");
        this.swipeInfo = swipeInfo;
        startWaiting();
    }

    @Override
    public void handleSwipeIn(SwipeInfo swipeInfo) {

        Log.i("myLogs", "Swipe Handler In: Swipe In");
    }

    @Override
    public void handleSwipeOut(SwipeInfo swipeInfo) {

        Log.i("myLogs", "Swipe Handler In: Swipe Out");
    }

    @Override
    public void handleMessage(Object message) {

        Log.i("myLogs", "Swipe Handler In: Message Received");

        stopWaiting();
        String messageString = (String) message;
        if (messageString.equals("SwipeOut")) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            comService.writeOut(gson.toJson(swipeInfo));
        } else if (messageString.equals("Stitch")) {
            comService.writeOut(getScreenDimensions());
        } else {
            setIdle();
        }
    }

    @Override
    public void setIdle() {
        comService.stateMachine = new StateMachineIdle(comService);
    }

    @Override
    public void setIn(SwipeInfo swipeInfo) {

    }

    @Override
    public void setOut(SwipeInfo swipeInfo) {

    }

    @Override
    public void setAwaitingIn() {

    }

    @Override
    public void setStitching() {

    }
}