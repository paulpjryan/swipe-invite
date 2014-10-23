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

        //Restore any user data that may have been saved locally
        if (restoreData()) {  //Check if application has been killed
            //check for data to load, if no data, just setup new application

            //Set up local data
            activeUser = new User();
            previousLogins = new ArrayList<User>();
            remember = false;
            Log.d(LOG_TAG, "Local data reset from scratch.");
        } else {
            //No need to load any local data
            Log.d(LOG_TAG, "Local data not reloaded.");
        }

        //Set the check kill string to a non null value
        checkKill = "alive";
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



    /* ----------------------------- METHODS FOR DATA STORAGE ----------------------------- */
    //Private instance variable to see if the application has be killed
    private static String checkKill;

    //Methods to handle any possible application destruction, need to save data to SQL database

    //Should be called from all activities in onStop()
    //ONLY CALL IF THE ACTIVITY IS NOT LAUNCHING AN INTENT TO A NEW ACTIVITY
    public void possibleKill() {
        //Save all user objects to local disk

    }
    //Should be called from the onCreate() method of the application
    private boolean restoreData() {
        if (checkKill == null) {
            //Restore all user objects from local disk
            return true;
        }
        return false;
    }

    /* ----------------------------- END DATA STORAGE -------------------------------- */



    /* --------- USER LOGIN PROFILE STORAGE AND MANAGEMENT --------------- */
    //Local constants
    private static final int MAX_CAPACITY = 10;

    //Local Variables for use
    private User activeUser;
    private ArrayList<User> previousLogins;
    private boolean remember;


    //Method to set remember checkbox variable from the Login Activity
    public void setRemember(boolean b) {
        this.remember = b;
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
    public void resetActiveUser() {
        if (remember) {
            //Check to see if going over capacity for local user data
            if(localDataAtMax()) {
                previousLogins.remove(0);
            }

            previousLogins.add(activeUser);   //adding logged out user to previous list
        }
        remember = false;  //default state of checkbox in Login activity is false
        activeUser = new User();
    }

    //Method to remove previous profile to the User login history
    //Note: adding is handled in Login Activity, removal can be an option later
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
                Log.d(LOG_TAG, "Old user found: " + x.getUserName());
                return previousLogins.indexOf(x);  //present result: index
            }
        }

        return -1; //not present result
    }

    //Method for checking how many user profiles are stored in database
    private boolean localDataAtMax() {
        return (previousLogins.size() >= MAX_CAPACITY);
    }
    /* ---------------- END USER MANAGEMENT ----------------------------- */



    /* ----------------- BAASBOX INTERACTION METHODS FOR GROUP CREATION ----------------------- */
    //Tokens for saving and resuming requests
    private RequestToken savingRequestToken;
    private final static String SAVING_TOKEN_KEY = "saving";

    //Handlers for receiving request results
    private final BaasHandler<BaasDocument> onComplete = new BaasHandler<BaasDocument>() {
        @Override
        public void handle(BaasResult<BaasDocument> result) {
            savingRequestToken = null;
            if (result.isFailed()) {
                Log.d(LOG_TAG,"ERROR",result.error());
            }
            //completeCreate(result);
        }
    };

    //Method to save the group to users group list
    public void createNewGroup(Group g) {
        //Package new group object into BaasDocument
        BaasDocument newDoc = Group.getBaasGroup(g);

        //Try to send Group Document object to server
        savingRequestToken = newDoc.save(onComplete);
    }


    /* ---------------- END BAASBOX INTERACTION METHODS FOR GROUP CREATION --------------------- */


}
