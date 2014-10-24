package com.team16.swipeinvite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;

import org.w3c.dom.Text;


public class LogoutActivity extends Activity {
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

        //Check for resumed states and retrieve model from somewhere
        if (savedInstanceState != null) {
            model = savedInstanceState.getParcelable(MODEL_KEY);
            modelRT = savedInstanceState.getParcelable(MODEL_TOKEN_KEY);
        } else {
            model = getIntent().getParcelableExtra("model_data");
        }

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
            modelRT.resume(onModelSave);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (modelRT != null) {
            modelRT.suspend();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MODEL_KEY, model);
        if (modelRT != null) {
            outState.putParcelable(MODEL_TOKEN_KEY, modelRT);
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
                //MOVE ON TO LOGOUT
                return;
            }
            Log.d(LOG_TAG, "Server save weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout, menu);
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