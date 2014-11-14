package com.team16.swipeinvite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasCloudMessagingService;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;


public class NewUserLoginActivity extends Activity {
    private static final String LOG_TAG = "NEWUSERLOGIN";

    //region Instance variable for the new model
    private Model model;
    private static final String MODEL_KEY = "model_d";
    //endregion


    //region Private instance variables for the view
    //The form view
    private View formView;
    private EditText usernameField;
    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private Button submitButton;
    private RadioGroup radGroup;
    //The status display
    private View statusView;
    private TextView statusMessage;
    //endregion


    //region Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate called.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_login);

        //Load in any old signin tokens
        if (savedInstanceState != null) {
            signInRT = savedInstanceState.getParcelable(SIGN_IN_TOKEN_KEY);
            groupRT = savedInstanceState.getParcelable(GROUP_TOKEN_KEY);
            modelRT = savedInstanceState.getParcelable(MODEL_TOKEN_KEY);
            cloudRT = savedInstanceState.getParcelable(CLOUD_TOKEN_KEY);
            try {
                model = Model.getInstance(this);
            } catch (Model.ModelException e) {
                Log.d(LOG_TAG, "Caught a model exception.");
                model = null;
            }
        }

        //Check for google play services
        if (!checkPlayServices()) {
            return;
        }

        //Setup the form view variables
        formView = findViewById(R.id.form_new_login);
        usernameField = (EditText) findViewById(R.id.username_new_login);
        nameField = (EditText) findViewById(R.id.name_new_login);
        emailField = (EditText) findViewById(R.id.email_new_login);
        passwordField = (EditText) findViewById(R.id.password_new_login);
        submitButton = (Button) findViewById(R.id.create_user_button);
        radGroup = (RadioGroup) findViewById(R.id.radioGroup_male_female);

        //Setup the status view variables
        statusView = findViewById(R.id.status_new_login);
        statusMessage = (TextView) findViewById(R.id.status_message_new_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();   //Need to check for valid google play services
        if (signInRT != null) {
            showProgress(true);
            signInRT.resume(onComplete);
        } else if (groupRT != null) {
            showProgress(true);
            groupRT.resume(onGroupComplete);
        } else if (cloudRT != null) {
            showProgress(true);
            cloudRT.resume(onCloudComplete);
        } else if (modelRT != null) {
            showProgress(true);
            modelRT.resume(onModelComplete);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (signInRT != null) {
            showProgress(false);
            signInRT.suspend();
        } else if (groupRT != null) {
            showProgress(false);
            groupRT.suspend();
        } else if (cloudRT != null) {
            showProgress(true);
            cloudRT.suspend();
        } else if (modelRT != null) {
            showProgress(false);
            modelRT.suspend();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState called.");
        if (signInRT != null) {
            outState.putParcelable(SIGN_IN_TOKEN_KEY, signInRT);
        } else if (groupRT != null) {
            outState.putParcelable(GROUP_TOKEN_KEY, groupRT);
        } else if (cloudRT != null) {
            outState.putParcelable(CLOUD_TOKEN_KEY, cloudRT);
        } else if (modelRT != null) {
            outState.putParcelable(MODEL_TOKEN_KEY, modelRT);
        }
        if (model != null) {
            Model.saveModel(this);
        }
    }
    //endregion


    //region Responders and variables for button clicks
    private int male = -1;
    public void onRadioButtonClicked(View v) {
        // Is the button now checked?
        boolean checked = ((RadioButton) v).isChecked();

        // Check which radio button was clicked
        switch(v.getId()) {
            case R.id.radiobutton_male:
                if (checked)
                    male = 1;
                break;
            case R.id.radiobutton_female:
                if (checked)
                    male = 0;
                break;
        }
        return;
    }

    public void createResponder(View v) {
        //Show the progress bar
        showProgress(true);
        String username = usernameField.getText().toString();
        String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        //Catch faulty inputs
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
        } else if (TextUtils.isEmpty(name)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Name cannot be empty");
            showProgress(false);
            nameField.setError("Cannot be left blank");
            nameField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(email)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Email cannot be empty");
            showProgress(false);
            emailField.setError("Cannot be left blank");
            emailField.requestFocus();
            return;
        } else if (male == -1) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Gender cannot be empty");
            showProgress(false);
            radGroup.requestFocus();
            return;
        }
        //Create new user based on fields
        CurrentUser user = new CurrentUser(username, password);
        user.setCommonName(name);
        user.setEmail(email);
        if (male == 1) {
            user.setMale(true);
        } else {
            user.setMale(false);
        }

        //Attempt to sign up the user with BaasBox
        signInRT = user.getBaasUser().signup(onComplete);

    }
    public void textFieldResponder(View v) {
        //Reset the error messages if you click the text views
        usernameField.setError(null);
        passwordField.setError(null);
        emailField.setError(null);
        nameField.setError(null);
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
                //SAY ERROR MESSAGE TO USER
                Toast.makeText(getApplicationContext() , "Server unavailable.", Toast.LENGTH_SHORT).show();
                return;
            } else if (result.isSuccess()) {

                completeSignup(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Method that is called after the signup is complete
    private void completeSignup(BaasUser u) {
        //Create the model, this also stores the current user into it
        //model = new Model();
        model = Model.getInstance(this);

        //Create the user's personal group
        Group2 g = new Group2("Personal", "A group just for you to push your own events to.", true);

        //Store the group in the local model object
        //model.activeGroups.add(g);
        model.getActiveGroups().add(g);

        //Attempt to send the group to the server
        groupRT = g.getBaasDocument().save(SaveMode.IGNORE_VERSION, onGroupComplete);
    }
    //endregion


    //region Variables and methods to deal with ansync group creation
    private static final String GROUP_TOKEN_KEY = "group";
    private RequestToken groupRT;
    private final BaasHandler<BaasDocument> onGroupComplete = new BaasHandler<BaasDocument>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<BaasDocument> result) {
            groupRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                //SAY ERROR MESSAGE TO USER
                Toast.makeText(getApplicationContext() , "Server unavailable.", Toast.LENGTH_SHORT).show();
                return;
            } else if (result.isSuccess()) {
                //MOVE ON TO MODEL SAVE
                completeGroup(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Method called after group creation
    private void completeGroup(BaasDocument g) {
        //Check if the local model has a group in it (which it should)
        if (/*model.activeGroups.size()*/ model.getActiveGroups().size() != 1) {
            Log.d(LOG_TAG, "Model is not the correct size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());
            showProgress(false);
            return;
        }
        model.getActiveGroups().get(0).setBaasDocument(g);

        //Send the cloud setup
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
                //Save model to server
                //Convert the model object to a server version for storage on server
                modelRT = model.toServerVersion().save(SaveMode.IGNORE_VERSION, onModelComplete);

                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Variables and methods to deal with ansync model creation
    private static final String MODEL_TOKEN_KEY = "model";
    private RequestToken modelRT;
    private final BaasHandler<BaasDocument> onModelComplete = new BaasHandler<BaasDocument>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<BaasDocument> result) {
            modelRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                //SAY ERROR MESSAGE TO USER
                Toast.makeText(getApplicationContext() , "Server unavailable.", Toast.LENGTH_SHORT).show();
                return;
            } else if (result.isSuccess()) {
                //LAUNCH THE MAIN ACTIVITY
                launchMainActivity(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Method to launch the main activity when everything is done
    private void launchMainActivity(BaasDocument m) {
        //Store the server version received, mainly for its ID for later
        model.setServerVersion(m);

        //Stop the progress wheel
        showProgress(false);

        //Start the main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Model.saveModel(this);
        startActivity(intent);
        finish();
    }
    //endregion


    //region Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_user_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
