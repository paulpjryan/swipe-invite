package com.team16.swipeinvite;

import android.app.Application;
import android.util.Log;
import com.baasbox.android.*;

import java.util.ArrayList;
import java.util.List;

/* The purpose of this class is to handle any global variables that the application must manage
* while it is active. The BaasBox static object is managed by the application. Also, probably the
* model object.
* */
public class StartUp extends Application {
    /* -------------------- LOG TAG CONSTANTS --------------------------- */
    private final static String LOG_TAG = "APPLICATION";
    /* -------------------- END LOG TAG CONSTANTS ----------------------- */



    /* ------------------ OVERRIDE METHODS FOR APPLICATION CLASS ---------------------- */
    //The onCreate method is where startup procedures should be run.
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate called");

        //Create a new BaasBox builder object to set the configs
        BaasBox.Builder b = new BaasBox.Builder(this);
        //set your configurations
        b.setAuthentication(BaasBox.Config.AuthType.SESSION_TOKEN); //Standard handshake protocol
        b.setApiDomain("128.46.66.236");  //IP of server holding DB
        b.setAppCode("1234567890");  //App code to verify client (personalize later)
        b.setPort(9000);  //Server port
        box = b.init();  //Initialize the BaasBox object through a builder method
    }
    /* ------------------------------ END OVERRIDE METHODS ---------------------------- */



    /* ----------------------- BAASBOX SETUP STUFF ---------------------------- */
    //This holds the BaasBox android object with the connection settings.
    private BaasBox box;

    //A method to return the BaasBox object
    public BaasBox getBaasBox(){
        return box;
    }
    /* ----------------------- END BAASBOX SETUP ------------------------------ */

}
