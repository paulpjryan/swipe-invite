package com.team16.swipeinvite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;


public class ForgotPasswordActivity extends Activity {
    private static final String LOG_TAG = "ForgotPass";

    //region Local variables for views
    private View formView;
    private EditText nameText;

    private View statusView;
    //endregion

    //region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (savedInstanceState != null) {
            recoverRT = savedInstanceState.getParcelable(RECOVER_TOKEN_KEY);
        }

        formView = findViewById(R.id.form_recovery);
        nameText = (EditText) findViewById(R.id.username_recover);
        statusView = findViewById(R.id.status_recovery);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        if (recoverRT != null) {
            recoverRT.resume(onRecoverComplete);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        if (recoverRT != null) {
            recoverRT.suspend();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        if (recoverRT != null) {
            outState.putParcelable(RECOVER_TOKEN_KEY, recoverRT);
        }

    }
    //endregion


    //region Methods for menus and options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forgot_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
    //endregion


    //region Method for submitting a password recovery
    public void onSubmitRecovery(View view) {
        //Show the progress bar
        showProgress(true);
        //Check to see if any inputs are invalid
        String username = nameText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username cannot be empty");
            showProgress(false);
            nameText.setError("Cannot be left blank");
            nameText.requestFocus();
            return;
        }

        if (recoverRT != null) {
            Log.d(LOG_TAG, "Preventing spam.");
            showProgress(false);
            makeToast("Server busy", Toast.LENGTH_SHORT);
            return;
        }

        //Send the request to recover password
        recoverRT = BaasUser.requestPaswordReset(username, onRecoverComplete);

    }
    //endregion


    private static final String RECOVER_TOKEN_KEY = "recoverRT";
    private RequestToken recoverRT;
    private final BaasHandler<Void> onRecoverComplete = new BaasHandler<Void>() {
        @Override
        public void handle(BaasResult<Void> result) {
            recoverRT = null;
            if (result.isFailed()) {
                Log.d(LOG_TAG, "Request failed: " + result.error());
                showProgress(false);
                makeToast("Failed to send email", Toast.LENGTH_LONG);
                return;
            } else if (result.isSuccess()) {
                showProgress(false);
                makeToast("Recovery email sent", Toast.LENGTH_LONG);
                finish();
                return;
            }
        }
    };


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


    //region Method to make toast
    private void makeToast(String message, int length) {
        Toast.makeText(this, message, length).show();
    }
    //endregion


}
