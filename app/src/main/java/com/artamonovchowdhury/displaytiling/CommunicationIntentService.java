package com.artamonovchowdhury.displaytiling;

import android.app.Service;

public abstract class CommunicationIntentService extends Service {

    public CommunicationIntentService() {
        super();
    }

    public abstract void writeOut(Object o);
}
