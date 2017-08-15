package com.artamonovchowdhury.displaytiling.statemachine;

import android.util.Log;

import com.artamonovchowdhury.displaytiling.CommunicationService;
import com.artamonovchowdhury.displaytiling.SwipeInfo;

import static com.artamonovchowdhury.displaytiling.ServerService.handshake;

public class StateMachineIdle extends StateMachine {


    public StateMachineIdle(CommunicationService comService) {
        super(comService);

        Log.i("myLogs", "Swipe Handler: Set to Idle: " + handshake);
    }


    @Override
    public void handleSwipeIn(SwipeInfo swipeInfo) {

        Log.i("myLogs", "Swipe Handler Idle: Swipe In" + handshake);
        setIn(swipeInfo);
    }

    @Override
    public void handleSwipeOut(SwipeInfo swipeInfo) {

        Log.i("myLogs", "Swipe Handler Idle: Swipe Out" + handshake);
        //Send message to client
        String message= "SwipeOut";
        comService.writeOut(message);

        setOut(swipeInfo);
    }

    @Override
    public void handleMessage(Object message) {

        Log.i("myLogs", "Swipe Handler Idle: Message Received");

        if(message instanceof byte[]){
            comService.fireBitmapIntent ((byte[])message);
        } else {
            String messageString = (String) message;
            if (messageString.equals("SwipeOut")) {
                setAwaitingIn();
            } else if (messageString.equals("Stitch")) {
                comService.writeOut(getScreenDimensions());
            }
        }
    }

    @Override
    public void setIdle() {}

    @Override
    public void setIn(SwipeInfo swipeInfo) { comService.stateMachine = new StateMachineIn(comService,swipeInfo);}

    @Override
    public void setOut(SwipeInfo swipeInfo) { comService.stateMachine = new StateMachineOut(comService,swipeInfo);}

    @Override
    public void setAwaitingIn() { comService.stateMachine = new StateMachineAwaitingIn(comService);}

    @Override
    public void setStitching() {

    }
}
