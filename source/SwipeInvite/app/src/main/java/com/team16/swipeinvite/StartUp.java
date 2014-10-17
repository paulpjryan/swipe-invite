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
    private static final int MAX_CAPACITY = 10;

    //This holds the BaasBox android object with the connection settings.
    private BaasBox box;
    private User activeUser;
    private ArrayList<User> previousLogins;

    //The onCreate method is where startup procedures should be run.
    @Override
    public void onCreate() {
        super.onCreate();

        //Create a new BaasBox builder object to set the configs
        BaasBox.Builder b = new BaasBox.Builder(this);
        //set your configurations
        b.setAuthentication(BaasBox.Config.AuthType.SESSION_TOKEN); //Standard handshake protocol
        b.setApiDomain("128.46.66.236");  //IP of server holding DB
        b.setAppCode("1234567890");  //App code to verify client (personalize later)
        b.setPort(9000);  //Server port
        box = b.init();  //Initialize the BaasBox object through a builder method

        //OLD CODE, DO NOT USE
//        BaasBox.builder(this).setAuthentication(BaasBox.Config.AuthType.SESSION_TOKEN)
//                .setApiDomain("10.0.2.0")
//                .setPort(9000)
//                .setAppCode("1234567890")
//                .init();
//        BaasBox.Config config = new BaasBox.Config();
//        config.authenticationType = BaasBox.Config.AuthType.SESSION_TOKEN;
//        config.apiDomain = "192.168.56.1"; // the host address
//        config.httpPort = 9000;
//        box = BaasBox.initDefault(this,config);

        activeUser = new User();
        previousLogins = new ArrayList<User>();
    }

    //A method to return the BaasBox object, not really necessary as it is already global
    public BaasBox getBaasBox(){
        return box;
    }

    //Method to return the active user object
    public User getActiveUser() { return activeUser; }

    //Method to set active user object
    public void setActiveUser(BaasUser u) {
        //Checking if the user is in the previous user list
        int n = isPrev(u);
        if (n != -1) {
            activeUser = previousLogins.remove(n);  //pull prev user from list if present
        }

        //Unpack BaasUser profile data into user object
        activeUser.unWrapUser(u);
        return;
    }

    //Method to completely reset the active user to its original, blank state
    public void resetActiveUser(boolean remember) {
        if (remember) {
            //Check to see if going over capacity for local user data
            if(localDataAtMax()) {
                previousLogins.remove(0);
            }

            previousLogins.add(activeUser);   //adding logged out user to previous list
        }
        activeUser = new User();
    }

    //Method to remove previous profile to the User login history
    //Note: adding is handled in this class, removal can be an option later
    public void removePrevUser(User u) {
        previousLogins.remove(u);
        return;
    }

    //Method to decide if previous user is logging in again
    private int isPrev(BaasUser u) {
        //Check for null prev list
        if (previousLogins.size() == 0) {
            return -1;
        }

        //Check for matching username in list
        for (User x: previousLogins) {
            if (x.getUserName().equals(u.getName())) {
                Log.d("LOG", "Old user found: " + x.getUserName());
                return previousLogins.indexOf(x);  //present result: index
            }
        }

        return -1; //not present result
    }

    //Method for checking how many user profiles are stored in database
    private boolean localDataAtMax() {
        return (previousLogins.size() >= MAX_CAPACITY);
    }

}
