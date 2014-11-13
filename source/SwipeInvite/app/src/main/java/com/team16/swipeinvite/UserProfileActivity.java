package com.team16.swipeinvite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;

public class UserProfileActivity extends ActionBarActivity {
    private static final String LOG_TAG = "USERPROFILE";

    //region Local variables for views
    private TextView fullnameField;
    private TextView emailField;
    private RadioGroup genderGroup;
    private RadioButton genderButton;
    private RadioButton maleButton;
    private RadioButton femaleButton;
    private EditText usernameField;
    private EditText passwordField;
    private ProgressBar progressSpinner;
    //endregion


    //region Local variable for the model
    private Model model;
    private static final String MODEL_KEY = "model_d";
    private static final String MODEL_INTENT_KEY = "model_data";
    //endregion


    //region Lifecycle methods
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Retrieve model from somewhere
        if (savedInstanceState != null) {
            saveRT = savedInstanceState.getParcelable(SAVE_TOKEN_KEY);
            //model = savedInstanceState.getParcelable(MODEL_KEY);
        }
        model = Model.getInstance(this);
        Log.d(LOG_TAG, "Model active group size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());

        //Setup local variables for the views
        usernameField = (EditText) findViewById(R.id.editText_user_username);
        passwordField = (EditText) findViewById(R.id.editText_user_password);
		fullnameField = (TextView) findViewById(R.id.editText_user_name);
        emailField = (TextView) findViewById(R.id.editText_user_email);
        genderGroup = (RadioGroup) findViewById(R.id.RadioGroup_gender);
        maleButton = (RadioButton) findViewById(R.id.Radiobutton_user_male);
        femaleButton = (RadioButton) findViewById(R.id.Radiobutton_user_female);
        progressSpinner = (ProgressBar) findViewById(R.id.progressBar_user_profile);
        progressSpinner.setVisibility(View.GONE);

        //Populate the views with data from the model
        populateViews();
	}
    @Override
    protected void onPause() {
        super.onPause();
        if (saveRT != null) {
            progressSpinner.setVisibility(View.GONE);
            saveRT.suspend();
        }
    }
    @Override
    protected  void onResume() {
        super.onResume();
        if (saveRT != null) {
            progressSpinner.setVisibility(View.VISIBLE);
            saveRT.resume(onSaveComplete);
        }
        if (model == null) {
            model = Model.getInstance(this);
            Log.d(LOG_TAG, "Model active group size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (model != null) {
            //outState.putParcelable(MODEL_KEY, model);
        }
        if (saveRT != null) {
            outState.putParcelable(SAVE_TOKEN_KEY, saveRT);
        }
    }
    //endregion


    //region Method to populate the views with model data
    private void populateViews() {
        usernameField.setText(model.currentUser.getUsername());
        fullnameField.setText(model.currentUser.getCommonName());
        emailField.setText(model.currentUser.getEmail());
        if (model.currentUser.isMale()) {
            maleButton.setChecked(true);
        } else {
            femaleButton.setChecked(true);
        }
    }
    //endregion


    //region Methods for menu creation and selection
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_profile, menu);
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
        else if(id == android.R.id.home) {
            navigateToMain();
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
		return super.onOptionsItemSelected(item);
	}
    @Override
    public void onBackPressed() {
        navigateToMain();
        super.onBackPressed();
    }
    //endregion


    //region Method to figure out how to go back to main
    private void navigateToMain() {
        if (saveRT == null) {
            Log.d(LOG_TAG, "Navigating away from profile edit, return OK.");
            progressSpinner.setVisibility(View.GONE);
            Intent returnIntent = new Intent();
            //returnIntent.putExtra(MODEL_INTENT_KEY, model);
            setResult(RESULT_OK, returnIntent);
        } else {
            Log.d(LOG_TAG, "Navigating away from profile edit, return CANCEL.");
            progressSpinner.setVisibility(View.GONE);
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
        }
        finish();
    }
    //endregion


    //region Method for clicking the submit button
    public void onClickListener(View v)
    {
        progressSpinner.setVisibility(View.VISIBLE);
        //show progress here **************************************

        String fullname = fullnameField.getText().toString();
        String email = emailField.getText().toString();
        int selectedid = genderGroup.getCheckedRadioButtonId();
        boolean ismale = false;
        // need to fix if none of them are checked**********************************
        genderButton = (RadioButton) findViewById(selectedid);
        String gender = genderButton.getText().toString();

        //Catch faulty inputs
        if (TextUtils.isEmpty(fullname)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username cannot be empty");
            progressSpinner.setVisibility(View.GONE);
            fullnameField.setError("Cannot be left blank");
            fullnameField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(email)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Password cannot be empty");
            progressSpinner.setVisibility(View.GONE);
            emailField.setError("Cannot be left blank");
            emailField.requestFocus();
            return;
        } else if (fullname.length() > 30 || fullname.length() < 4) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username must be between 4 and 30 characters.");
            progressSpinner.setVisibility(View.GONE);
            fullnameField.setError("Must be between 4 and 30 characters");
            fullnameField.requestFocus();
            return;
        } else if (email.length() < 6) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Password must be more than 6 characters.");
            progressSpinner.setVisibility(View.GONE);
            emailField.setError("Must be greater than 6 characters");
            emailField.requestFocus();
            return;
        }

        if((gender).equals("Male"))
            ismale=true;
        else if((gender).equals("Female"))
            ismale = false;
        else
        {
            Log.d(LOG_TAG, "Gender cannot be empty");
            progressSpinner.setVisibility(View.GONE);
            genderGroup.requestFocus();
            return;

        }

        //Get create a Baasuser from the current user
        CurrentUser updateUser = new CurrentUser(BaasUser.current());
        updateUser.setCommonName(fullname);
        updateUser.setEmail(email);
        updateUser.setMale(ismale);

        //Save the user to BaasBox
        saveRT = updateUser.getBaasUser().save(onSaveComplete);

    }
    //endregion


    //region Variables and methods to deal with ansync save request
    private static final String SAVE_TOKEN_KEY = "save";
    private RequestToken saveRT;
    private final BaasHandler<BaasUser> onSaveComplete = new BaasHandler<BaasUser>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<BaasUser> result) {
            saveRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                failedSave();
                return;
            } else if (result.isSuccess()) {
                //MOVE ON TO EDITING PERMISSIONS IF NEEDED
                completeSave(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            progressSpinner.setVisibility(View.GONE);
            return;
        }
    };
    //endregion


    //region Method to deal with saving attempt result
    private void completeSave(BaasUser u) {
        progressSpinner.setVisibility(View.GONE);
        //Put the current user info into the model
        model.currentUser = new CurrentUser(u);
        Model.saveModel(this);

        //Reload the views
        populateViews();

        //Toast user success
        Toast.makeText(getApplicationContext(), "Profile updated.", Toast.LENGTH_SHORT).show();

    }
    private void failedSave() {
        progressSpinner.setVisibility(View.GONE);

        //Reload the views
        populateViews();

        //Toast the user fail
        Toast.makeText(getApplicationContext(), "Update failed.", Toast.LENGTH_SHORT).show();

    }
    //endregion

}
