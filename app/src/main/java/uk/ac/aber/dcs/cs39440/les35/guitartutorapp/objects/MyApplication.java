package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static Context mContext;

    public void onCreate(){
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getAppContext(){
        return mContext;
    }
}
