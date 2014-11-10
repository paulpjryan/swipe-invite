package com.team16.swipeinvite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasCloudMessagingService;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;


public class LoginActivity2 extends ActionBarActivity {
    private static final String LOG_TAG = "LOGIN_ACTIVITY";

    //region Instance of the model class
    private Model model;
    private static final String MODEL_KEY = "model_d";
    private static final String MODEL_INTENT_KEY = "model_data";
    //endregion


    //region Private variables for the views
    //Form views
    private View formView;
    private EditText usernameField;
    private EditText passwordField;
    private Button signinButton;
    private Button newuserButton;
    //Status views
    private View statusView;
    private TextView statusMessage;
    //endregion


    //region Methods for handling the lifecycle of the activity
    @Override
    //Create any view instances here and try to recover a Request token
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate called.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity2);

        //Load a previous request for onResume to take care of if one exists
        if (savedInstanceState != null) {
            signInRT = savedInstanceState.getParcelable(SIGN_IN_TOKEN_KEY);
            modelRT = savedInstanceState.getParcelable(MODEL_TOKEN_KEY);
            friendRT = savedInstanceState.getParcelable(FRIEND_TOKEN_KEY);
            groupRT = savedInstanceState.getParcelable(GROUP_TOKEN_KEY);
            eventRT = savedInstanceState.getParcelable(EVENT_TOKEN_KEY);
            cloudRT = savedInstanceState.getParcelable(CLOUD_TOKEN_KEY);
            //model = savedInstanceState.getParcelable(MODEL_KEY);
            model = Model.getInstance(getSharedPreferences(BaasUser.current().getName(), Context.MODE_PRIVATE));
        }

        //Check for google play services
        if (!checkPlayServices()) {
            return;
        }

        //Instantiate the local view variables correctly
        formView = (View) findViewById(R.id.form_login);
        usernameField = (EditText) findViewById(R.id.username);
        passwordField = (EditText) findViewById(R.id.password2);
        signinButton = (Button) findViewById(R.id.login_button);
        newuserButton = (Button) findViewById(R.id.new_user_button);

        //Instantiate the views to be loaded later
        statusView = (View) findViewById(R.id.status_login);
        statusMessage = (TextView) findViewById(R.id.status_message_login);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();   //Need to check for valid google play services
        if (signInRT != null) {
            showProgress(true);
            signInRT.resume(onComplete);
        } else if (modelRT != null) {
            showProgress(true);
            modelRT.resume(onModelComplete);
            return;     //If this resumes, don't resume any further requests
        }
        if (groupRT != null) {
            showProgress(true);
            groupRT.resume(onGroupComplete);
        }
        if (eventRT != null) {
            showProgress(true);
            eventRT.resume(onEventComplete);
        }
        if (friendRT != null) {
            showProgress(true);
            friendRT.resume(onFriendComplete);
        }
        if (cloudRT != null) {
            showProgress(true);
            cloudRT.resume(onCloudComplete);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (signInRT != null) {
            showProgress(false);
            signInRT.suspend();
        } else if (modelRT != null) {
            showProgress(false);
            modelRT.suspend();
            return;    //if this pauses, don't execute any further requests
        }
        if (groupRT != null) {
            showProgress(false);
            groupRT.suspend();
        }
        if (eventRT != null) {
            showProgress(false);
            eventRT.suspend();
        }
        if (friendRT != null) {
            showProgress(false);
            friendRT.suspend();
        }
        if (cloudRT != null) {
            showProgress(false);
            cloudRT.suspend();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState called.");
        if (signInRT != null) {
            outState.putParcelable(SIGN_IN_TOKEN_KEY, signInRT);
        } else if (modelRT != null) {
            outState.putParcelable(MODEL_TOKEN_KEY, modelRT);
            return;     //If this pauses, don't save any further requests
        }
        if (groupRT != null) {
            outState.putParcelable(GROUP_TOKEN_KEY, groupRT);
        }
        if (eventRT != null) {
            outState.putParcelable(EVENT_TOKEN_KEY, eventRT);
        }
        if (friendRT != null) {
            outState.putParcelable(FRIEND_TOKEN_KEY, friendRT);
        }
        if (model != null) {
            //outState.putParcelable(MODEL_KEY, model);
            Model.saveModel(getSharedPreferences(BaasUser.current().getName(), Context.MODE_PRIVATE));
        }
        if (cloudRT != null) {
            outState.putParcelable(CLOUD_TOKEN_KEY, cloudRT);
        }
    }
    //endregion


    //region Methods for handling the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);    //adds a menu layout to the menu bar
        return true;
    }
    @Override
    public void onBackPressed() {
        return;
    }
    //endregion


    //region Responders for buttons
    public void loginResponder(View v) {
        //Show the progress bar
        showProgress(true);
        //Check to see if any inputs are invalid
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(username)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username cannot be empty");
            showProgress(false);
            usernameField.setError("Cannot be left blank");
            usernameField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(password)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Password cannot be empty");
            showProgress(false);
            passwordField.setError("Cannot be left blank");
            passwordField.requestFocus();
            return;
        } else if (username.length() > 30 || username.length() < 4) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username must be between 4 and 30 characters.");
            showProgress(false);
            usernameField.setError("Must be between 4 and 30 characters");
            usernameField.requestFocus();
            return;
        } else if (password.length() < 6) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Password must be more than 6 characters.");
            showProgress(false);
            passwordField.setError("Must be greater than 6 characters");
            passwordField.requestFocus();
            return;
        }
        //Create a current user object - not synced with server yet
        CurrentUser u = new CurrentUser(username, password);

        //Attempt to login the user
        signInRT = u.getBaasUser().login(onComplete);
    }

    public void newUserResponder(View v) {
        //Launch a new activity to deal with a new user registration
        Intent intent = new Intent(this, NewUserLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    public void textFieldResponder(View v) {
        //Reset the error messages if you click the text views
        usernameField.setError(null);
        passwordField.setError(null);
    }
    //endregion


    //region Variables and methods to deal with ansync signin request
    private static final String SIGN_IN_TOKEN_KEY = "sign_in";
    private RequestToken signInRT;
    private final BaasHandler<BaasUser> onComplete = new BaasHandler<BaasUser>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<BaasUser> result) {
            signInRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                failedLogin();
                return;
            } else if (result.isSuccess()) {
                completeLogin(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Method called after login request returns success
    private void completeLogin(BaasUser u) {
        //Create a new instance of the model, this will auto create the current user correctly
        //model = new Model();
        model = Model.getInstance(getSharedPreferences(BaasUser.current().getName(), Context.MODE_PRIVATE));   //Switch to singleton methodology

        //Attempt to retrieve the current user's model from the server
        modelRT = BaasDocument.fetchAll("model", onModelComplete);
    }

    private void failedLogin() {
        //TELL THE USER THAT THE LOGIN FAILED BECAUSE THE PASSWORD WAS INCORRECT
        passwordField.setError("Incorrect username or password");
        passwordField.requestFocus();
    }
    //endregion


    //region Variables and methods to deal with ansync model pulldown
    private static final String MODEL_TOKEN_KEY = "model";
    private RequestToken modelRT;
    private final BaasHandler<List<BaasDocument>> onModelComplete = new BaasHandler<List<BaasDocument>>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<List<BaasDocument>> result) {
            modelRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                return;
            } else if (result.isSuccess()) {
                completeModel(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Method called after model request returns success
    private void completeModel(List<BaasDocument> r) {
        if (r == null) {
            //NO DATA TO RETRIEVE
            //THIS CASE SHOULD NEVER HAPPEN
            Log.d(LOG_TAG, "Model object from server was null!");
            showProgress(false);
            return;
        } else if (r.size() == 0 || r.size() > 1) {
            //NO DATA TO RETRIEVE
            //THIS CASE SHOULD NEVER HAPPEN
            Log.d(LOG_TAG, "Model object from server was not the correct size!: " + r.size());
            Toast.makeText(this, "Your data is corrupted, contact server admin.", Toast.LENGTH_SHORT).show();
            showProgress(false);
            //launchMainActivity();
            return;
        }
        //MODEL EXISTS, set the server object in the model for later use of getting id
        //This call also extracts the id list to an instance variable
        model.setServerVersion(r.remove(0));

        //Check to see if there is any data to retrieve
        if (model.idListEmpty()) {
            //NO DATA HAS EVER BEEN WRITTEN TO THE SERVER FOR THIS USER
            //JUST LAUNCH THE MAIN ACTIVITY WITH THE CURRENT MODEL OBJECT
            launchMainActivity();
            return;
        }
        if (model.getIdList().get(4).size() != 0) {    //Check if the friend array is empty
            //There is friend profiles, retrieve them
            BaasQuery fquery = BaasQuery.builder().build();
            for (String s : model.getIdList().get(5)) {
                fquery = fquery.buildUpon().or("name=" + "'" + s + "'").build();
            }
            Log.d(LOG_TAG, "Friend list request sent.");
            friendRT = BaasUser.fetchAll(fquery.buildUpon().criteria(), onFriendComplete);
        }

        if (model.getIdList().get(0).size() != 0) {   //Check if the group array is empty
            BaasQuery queryG = BaasQuery.builder().build();
            for (String y : model.getIdList().get(0)) {
                Log.d(LOG_TAG, "Group added to query params: " + y);
                queryG = queryG.buildUpon().or("id=" + "'" + y + "'").build();
            }
            Log.d(LOG_TAG, "Group list request sent.");
            groupRT = BaasDocument.fetchAll("group", queryG.buildUpon().criteria(), onGroupComplete);
        }
        //Check if the event arrays are empty
        if (model.getIdList().get(1).size() != 0 || model.getIdList().get(2).size() != 0 || model.getIdList().get(3).size() != 0) {
            BaasQuery queryE = BaasQuery.builder().build();
            for (int i = 1; i < (model.getIdList().size()-1); i++) {
                for (String y : model.getIdList().get(i)) {
                    Log.d(LOG_TAG, "Event added to query params: " + y);
                    queryE = queryE.buildUpon().or("id=" + "'" + y + "'").build();
                }
            }
            Log.d(LOG_TAG, "Event list request sent.");
            eventRT = BaasDocument.fetchAll("group", queryE.buildUpon().criteria(), onEventComplete);
        }

        //Signup current user for push notifications
        BaasCloudMessagingService box = BaasBox.messagingService();
        cloudRT = box.enable(onCloudComplete);

    }
    //endregion


    //region Variables and methods to deal with cloud signup
    private static final String CLOUD_TOKEN_KEY = "cloud";
    private RequestToken cloudRT;
    private final BaasHandler<Void> onCloudComplete = new BaasHandler<Void>() {
        @Override
        public void handle(BaasResult<Void> result) {
            cloudRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                return;
            } else if (result.isSuccess()) {
                //COMPLETE THE  CLOUD SIGNUP
                Log.d(LOG_TAG, "Cloud request received.");
                //Launch main activity if the all pulldowns are done
                if (friendRT == null && eventRT == null && groupRT == null && cloudRT == null) {
                    launchMainActivity();
                }
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Variables and methods to deal with ansync event pulldown
    private static final String EVENT_TOKEN_KEY = "events";
    private RequestToken eventRT;
    private final BaasHandler<List<BaasDocument>> onEventComplete = new BaasHandler<List<BaasDocument>>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<List<BaasDocument>> result) {
            eventRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                return;
            } else if (result.isSuccess()) {
                //COMPLETE THE EVENT PULLDOWN
                Log.d(LOG_TAG, "Event list request received.");
                completeData(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Variables and methods to deal with ansync group pulldown
    private static final String GROUP_TOKEN_KEY = "groups";
    private RequestToken groupRT;
    private final BaasHandler<List<BaasDocument>> onGroupComplete = new BaasHandler<List<BaasDocument>>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<List<BaasDocument>> result) {
            groupRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                return;
            } else if (result.isSuccess()) {
                //COMPLETE THE GROUP PULLDOWN
                Log.d(LOG_TAG, "Group list request received.");
                completeData(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Method called after data request returns success
    private void completeData(List<BaasDocument> data) {
        Log.d(LOG_TAG, "Received list size: " + data.size());
        for (BaasDocument d : data) {      //Figure out what to do with returned data objects
            Log.d(LOG_TAG, "Iterating through received document list.");
            //BaasDocument d = BaasDocument.from(x);    //Convert to BaasDocument
            if (d.getCollection().equals("group")) {
                Group2 g = new Group2(d);     //Create group instance with BaasDocument
                model.activeGroups.add(g);
                Log.d(LOG_TAG, "Added group to active group list.");
            } else if (d.getCollection().equals("event")) {
                Event e = new Event(d);     //Create event instance with BaasDocument
                for (int i = 1; i < (model.getIdList().size()-1); i++) {     //Iterate through all event lists
                    if (model.getIdList().get(i).contains(e.getId())) {      //If it contains the current event ID, add event
                        switch (i) {
                            case 1:
                                model.acceptedEvents.add(e);
                                Log.d(LOG_TAG, "Added event to accepted.");
                                break;
                            case 2:
                                model.waitingEvents.add(e);
                                Log.d(LOG_TAG, "Added event to waiting.");
                                break;
                            case 3:
                                model.rejectedEvents.add(e);
                                Log.d(LOG_TAG, "Added event to rejected.");
                                break;
                            default:
                                Log.d(LOG_TAG, "Event not placed: " + e.toString());
                        }
                    }
                }
            } else {
                Log.d(LOG_TAG, "Data object was not group or event: " + d.toString());
            }
        }
        //Launch main activity if the friend pulldowns are done
        if (friendRT == null && eventRT == null && groupRT == null && cloudRT == null) {
            launchMainActivity();
        }
    }
    //endregion


    //region Variables and methods to deal with ansync friend pulldown
    private static final String FRIEND_TOKEN_KEY = "friend";
    private RequestToken friendRT;
    private final BaasHandler<List<BaasUser>> onFriendComplete = new BaasHandler<List<BaasUser>>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<List<BaasUser>> result) {
            friendRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                return;
            } else if (result.isSuccess()) {
                //COMPLETE FRIEND SETTING
                completeFriends(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Method called after friend request returns success
    private void completeFriends(List<BaasUser> f) {
        //Convert all received user profiles to local objects and store them
        for (BaasUser u : f) {
            Acquaintence a = new Acquaintence(u);
            model.friends.add(a);
            Log.d(LOG_TAG, "Added friends to list.");
        }
        if (friendRT == null && eventRT == null && groupRT == null && cloudRT == null) {
            launchMainActivity();
        }
    }
    //endregion


    //region Method to launch the main activity with the current local model object
    private void launchMainActivity() {
        Log.d(LOG_TAG, "Launching main activity.");
        showProgress(false);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.putExtra(MODEL_INTENT_KEY, model);
        Model.saveModel(getSharedPreferences(BaasUser.current().getName(), Context.MODE_PRIVATE));
        startActivity(intent);
        finish();
    }
    //endregion


    //region Method and variables to check if a valid Google play services is found
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    //endregion


    //region Method to show the progress bar instead of the form view
    //This method just loads a different view with a transition of fading
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            statusView.setVisibility(View.VISIBLE);
            statusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            statusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            formView.setVisibility(View.VISIBLE);
            formView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            formView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            statusView.setVisibility(show ? View.VISIBLE : View.GONE);
            formView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    //endregion

}
