package com.team16.swipeinvite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasCloudMessagingService;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;


public class LogoutActivity extends ActionBarActivity {
    private static final String LOG_TAG = "LOGOUT_ACTIVITY";

    //region Local instance of the model
    private Model model;
    private static final String MODEL_KEY = "model_d";
    //endregion


    //region Private variables for view
    //Form view
    private View formLogout;
    private TextView text;
    private Button logoutButton;
    //Status view
    private View statusLogout;
    private TextView statusMessage;
    //endregion


    //region Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        setTitle("Logout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Check for resumed states and retrieve model from somewhere
        if (savedInstanceState != null) {
            //model = savedInstanceState.getParcelable(MODEL_KEY);
            modelRT = savedInstanceState.getParcelable(MODEL_TOKEN_KEY);
            cloudRT = savedInstanceState.getParcelable(CLOUD_TOKEN_KEY);
            logoutRT = savedInstanceState.getParcelable(LOGOUT_TOKEN_KEY);
        } /*else {
            model = getIntent().getParcelableExtra("model_data");
        } */
        model = Model.getInstance(this);
        Log.d(LOG_TAG, "Model active group size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());

        //Setup the form views
        formLogout = findViewById(R.id.form_logout);
        text = (TextView) findViewById(R.id.logout_text_prompt);
        logoutButton = (Button) findViewById(R.id.logout_button);

        //Setup the status view
        statusLogout = findViewById(R.id.status_logout);
        statusMessage = (TextView) findViewById(R.id.status_message_logout);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (modelRT != null) {
            showProgress(true);
            modelRT.resume(onModelSave);
        } else if (cloudRT != null) {
            showProgress(true);
            cloudRT.resume(onCloudComplete);
        } else if (logoutRT != null) {
            showProgress(true);
            logoutRT.resume(onLogout);
        }
        if (model == null) {
            model = Model.getInstance(this);
            Log.d(LOG_TAG, "Model active group size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (modelRT != null) {
            showProgress(false);
            modelRT.suspend();
        } else if (cloudRT != null) {
            showProgress(false);
            cloudRT.suspend();
        } else if (logoutRT != null) {
            showProgress(false);
            logoutRT.suspend();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelable(MODEL_KEY, model);
        if (modelRT != null) {
            outState.putParcelable(MODEL_TOKEN_KEY, modelRT);
        } else if (cloudRT != null) {
            outState.putParcelable(CLOUD_TOKEN_KEY, cloudRT);
        } else if (logoutRT != null) {
            outState.putParcelable(LOGOUT_TOKEN_KEY, logoutRT);
        }
    }
    //endregion


    //region Method to respond to logout button
    public void logoutButtonResponder(View v) {
        //Show the progress bar
        showProgress(true);
        //Try to save the model to server
        BaasDocument m = model.toServerVersion();
        modelRT = m.save(SaveMode.IGNORE_VERSION, onModelSave);
    }
    //endregion


    //region Variables and methods to deal with ansync model save request
    private static final String MODEL_TOKEN_KEY = "model";
    private RequestToken modelRT;
    private final BaasHandler<BaasDocument> onModelSave = new BaasHandler<BaasDocument>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<BaasDocument> result) {
            modelRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server save error: " + result.error());
                showProgress(false);
                return;
            } else if (result.isSuccess()) {
                //logoutRT = BaasUser.current().logout(onLogout);
                //Disable current user for push notifications
                BaasCloudMessagingService box = BaasBox.messagingService();
                cloudRT = box.disable(onCloudComplete);
                return;
            }
            Log.d(LOG_TAG, "Server save weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
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
                //Continue to Logout
                logoutRT = BaasUser.current().logout(onLogout);
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Variables and methods to deal with ansync logout request
    private static final String LOGOUT_TOKEN_KEY = "logout";
    private RequestToken logoutRT;
    private final BaasHandler<Void> onLogout = new BaasHandler<Void>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<Void> result) {
            logoutRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server logout error: " + result.error());
                showProgress(false);
                return;
            } else if (result.isSuccess()) {
                launchLoginActivity();
                return;
            }
            Log.d(LOG_TAG, "Server logout weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Method to return to the login activity
    private void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Model.dumpInstance();   //Delete the model from singleton
        startActivity(intent);
        finish();
    }
    //endregion


    //region Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.logout, menu);
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


    //region Method to show the progress bar instead of the form view
    //This method just loads a different view with a transition of fading
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            statusLogout.setVisibility(View.VISIBLE);
            statusLogout.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            statusLogout.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            formLogout.setVisibility(View.VISIBLE);
            formLogout.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            formLogout.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            statusLogout.setVisibility(show ? View.VISIBLE : View.GONE);
            formLogout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    //endregion

}
