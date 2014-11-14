package com.team16.swipeinvite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
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

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.Grant;
import com.baasbox.android.RequestToken;
import com.baasbox.android.Role;
import com.baasbox.android.SaveMode;

public class GroupCreationActivity extends ActionBarActivity {
    /* -------------------- LOG TAG CONSTANTS --------------------------- */
    private final static String LOG_TAG = "GROUP_CREATE_ACT";
    /* -------------------- END LOG TAG CONSTANTS ----------------------- */


    //region Local instance variables for view elements
    //Variables for form view
    private View groupCreateView;  //View with form
    private EditText nameview;
    private EditText descview;
    private RadioGroup radgroup;
    private Button submit;
    //Variables for status bar view
    private View groupStatusView;  //View with status bar
    private TextView statusMessage;
    //endregion


    //region Local variable for the model
    private Model model;
    private static final String MODEL_KEY = "model_d";
    private static final String MODEL_INTENT_KEY = "model_data";
    private Group2 newGroup;
    private static final String GROUP_KEY = "new_group";
    //endregion


    //region Lifecycle methods
    @Override
    //onCreate is called when the activity is first started
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate called");

        //DO NOT ADD ANY VIEW ACTIVITY CODE ABOVE HERE
        //Loading any previous server requests if the activity was closed
        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "Got model from saved instance.");
            saveRT = savedInstanceState.getParcelable(SAVE_TOKEN_KEY);
            readRT = savedInstanceState.getParcelable(READ_TOKEN_KEY);
            updateRT = savedInstanceState.getParcelable(UPDATE_TOKEN_KEY);
            //model = savedInstanceState.getParcelable(MODEL_KEY);
            newGroup = savedInstanceState.getParcelable(GROUP_KEY);
        }

        model = Model.getInstance(this);
        Log.d(LOG_TAG, "Got model, size: " + /*model.activeGroups.size()*/ model.getActiveGroups().size());

        //Setting the content view and support action bar
        setContentView(R.layout.activity_group_creation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Form view creation to local variables
        this.groupCreateView = (View) findViewById(R.id.group_create_form);
        this.nameview = (EditText) findViewById(R.id.textView_group_name);
		this.descview = (EditText) findViewById(R.id.textView_group_description);
        this.radgroup = (RadioGroup) findViewById(R.id.radioGroup);
        this.submit = (Button) findViewById(R.id.button_submit_groupCreation);

        //Status View creation to local variables
        this.groupStatusView = (View) findViewById(R.id.group_create_status);
        this.statusMessage = (TextView) findViewById(R.id.group_create_status_message);
	}


	@Override
    //Method to add options to the options menu for the activity
	public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu called");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_creation, menu);
		return true;
	}

	@Override
    //Method to determine which option menu item was selected
	public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected called");
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
        else if(id == android.R.id.home)
        {
            Log.d(LOG_TAG, "Navigating away from group creation, return CANCEL.");
            showProgress(false);
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
		return super.onOptionsItemSelected(item);
	}

    @Override
    //onPause is called when the activity leaves the user's view
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause called");
        if (saveRT != null) {
            showProgress(false);
            saveRT.suspend();
        }
        if (readRT != null) {
            showProgress(false);
            readRT.suspend();
        }
        if (updateRT != null) {
            showProgress(false);
            updateRT.suspend();
        }
    }

    @Override
    //onResume is called when the activity comes back to the user's view
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume called");
        if (saveRT != null) {
            showProgress(true);
            saveRT.resume(onSaveComplete);
        }
        if (readRT != null) {
            showProgress(true);
            readRT.resume(onReadComplete);
        }
        if (updateRT != null) {
            showProgress(true);
            updateRT.resume(onUpdateComplete);
        }
    }

    @Override
    //Method called when the system asks the activity to save any simple data to a bundle state
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState called");
        if (saveRT != null) {
            outState.putParcelable(SAVE_TOKEN_KEY, saveRT);
        }
        if (readRT != null) {
            outState.putParcelable(READ_TOKEN_KEY, readRT);
        }
        if (updateRT != null) {
            outState.putParcelable(UPDATE_TOKEN_KEY, updateRT);
        }
        if (model != null) {
            outState.putParcelable(MODEL_KEY, model);
        }
        if (newGroup != null) {
            outState.putParcelable(GROUP_KEY, newGroup);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop called");
    }
    //endregion


    //region Radio Group responder
    //Method to respond to the radio button clicked
    //Listener set in the xml
    private int ispriv = -1;  //start at -1 to represent nothing checked
    public void onRadioButtonClicked(View v) {
        // Is the button now checked?
        boolean checked = ((RadioButton) v).isChecked();

        // Check which radio button was clicked
        switch(v.getId()) {
            case R.id.text_private:
                if (checked)
                    ispriv = 1;
                    break;
            case R.id.text_public:
                if (checked)
                    ispriv = 0;
                    break;
        }
        return;
    }
    //endregion


    //region Submit button responder
    //Method to respond to the actual button click
    //Listener is set in the xml
    public void submitResponder(View v) {
        //Change the views
        showProgress(true);

        //Getting user inputs from views
        String name = nameview.getText().toString();
        String description  = descview.getText().toString();

        //Check for null entries
        if (TextUtils.isEmpty(name)) {
            Log.d(LOG_TAG, "Name field cannot be blank.");
            showProgress(false);
            nameview.setError("Cannot be left blank");
            nameview.requestFocus();
            return;
        } else if (TextUtils.isEmpty(description)) {
            Log.d(LOG_TAG, "Description field cannot be blank.");
            showProgress(false);
            descview.setError("Cannot be left blank");
            descview.requestFocus();
            return;
        } else if (ispriv == -1) {
            Log.d(LOG_TAG, "Privacy field cannot be blank.");
            showProgress(false);
            radgroup.requestFocus();
            Toast.makeText(this, "Must specifiy a privacy level.", Toast.LENGTH_SHORT).show();
            return;
        } else if (name.length() > 20 ) {
            Log.d(LOG_TAG, "Name field cannot be longer than 20 characters.");
            showProgress(false);
            nameview.setError("Cannot be longer than 20 characters");
            nameview.requestFocus();
            return;
        } else if (description.length() > 100 || description.length() <= 3) {
            Log.d(LOG_TAG, "Description field not within range.");
            showProgress(false);
            descview.setError("Must be between 4 and 100 characters");
            descview.requestFocus();
            return;
        }

        //Proceed to create new group with information
        boolean priv = true;
        if (ispriv == 0) {
            priv = false;
        }
        Log.d(LOG_TAG, "Saving group to server.");
        Group2 g = new Group2(name, description, priv);
        saveRT = g.getBaasDocument().save(SaveMode.IGNORE_VERSION, onSaveComplete);

        return;
    }
    public void textFieldResponder(View v) {
        //Reset the error messages if you click the text views
        nameview.setError(null);
        descview.setError(null);
    }
    //endregion


    //region Variables and methods to deal with ansync creation request
    private static final String SAVE_TOKEN_KEY = "save";
    private RequestToken saveRT;
    private final BaasHandler<BaasDocument> onSaveComplete = new BaasHandler<BaasDocument>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<BaasDocument> result) {
            saveRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                showProgress(false);
                //CREATION FAIL
                failedSave();
                return;
            } else if (result.isSuccess()) {
                //MOVE ON TO EDITING PERMISSIONS IF NEEDED
                completeSave(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Methods that handle a server response for a save
    private void completeSave(BaasDocument d) {
        Log.d(LOG_TAG, "Recieved successful group creation response.");
        //Create group from BaasDocument
        newGroup = new Group2(d);

        //Decide whether or not to make public
        if (newGroup.isPrivate()) {
            Log.d(LOG_TAG, "Created private group.");
            //Update model, return to main activity
            //model.activeGroups.add(newGroup);      //OLD way
            model.getActiveGroups().add(newGroup);    //Synchronized meth. 1
            GroupsAdapter.updateData(model.getActiveGroups());
            returnToMainSuccess();
            return;
        }
        //Else, grant read and update access to all users
        Log.d(LOG_TAG, "Creating public group, sending grants.");
        readRT = newGroup.getBaasDocument().grantAll(Grant.READ, Role.REGISTERED, onReadComplete);
        updateRT = newGroup.getBaasDocument().grantAll(Grant.UPDATE, Role.REGISTERED, onUpdateComplete);
    }
    private void failedSave() {
        showProgress(false);
        //Notify user of error
        Toast.makeText(getApplicationContext(), "Group could not be created", Toast.LENGTH_SHORT).show();
        return;
    }
    //endregion


    //region Variables and methods to deal with ansync read grant
    private static final String READ_TOKEN_KEY = "read";
    private RequestToken readRT;
    private static final int READ_ID = 0;
    private final BaasHandler<Void> onReadComplete = new BaasHandler<Void>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<Void> result) {
            readRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                //GRANT FAIL
                failedGrant(READ_ID);
                return;
            } else if (result.isSuccess()) {
                //MOVE ON TO COMPLETION
                completeGrant();
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Variables and methods to deal with ansync update grant
    private static final String UPDATE_TOKEN_KEY = "update";
    private RequestToken updateRT;
    private static final int UPDATE_ID = 1;
    private final BaasHandler<Void> onUpdateComplete = new BaasHandler<Void>() {
        @Override
        //This is the method that will receive the server return
        public void handle(BaasResult<Void> result) {
            updateRT = null;
            if (result.isFailed()) {
                //NOTIFY USER OF ERROR
                Log.d(LOG_TAG, "Server request error: " + result.error());
                //GRANT FAIL
                failedGrant(UPDATE_ID);
                return;
            } else if (result.isSuccess()) {
                //MOVE ON TO COMPLETION
                completeGrant();
                return;
            }
            Log.d(LOG_TAG, "Server request weird: " + result.toString());
            showProgress(false);
            return;
        }
    };
    //endregion


    //region Methods to handle grant responses
    private void completeGrant() {
        //Check if both grants have finished
        if (readRT == null && updateRT == null) {
            Log.d(LOG_TAG, "Created public group.");
            //model.activeGroups.add(newGroup);    //OLD Methodology
            model.getActiveGroups().add(newGroup);     //Synchronized methodology 1
            GroupsAdapter.updateData(model.getActiveGroups());
            returnToMainSuccess();
            return;
        }
    }

    //FIX THIS METHOD LATER, ON SERVER FAIL IT WILL INFINITE LOOP
    private void failedGrant(int id) {
        showProgress(false);
        //Figure out which grant failed
        switch (id) {
            case READ_ID:    //Try to resend proper grants
                Log.d(LOG_TAG, "Retrying read grant.");
                readRT = newGroup.getBaasDocument().grantAll(Grant.READ, Role.REGISTERED, onReadComplete);
                break;
            case UPDATE_ID:
                Log.d(LOG_TAG, "Retrying update grant.");
                updateRT = newGroup.getBaasDocument().grantAll(Grant.UPDATE, Role.REGISTERED, onUpdateComplete);
                break;
            default:
                break;
        }
    }
    //endregion


    //region Methods to return to main
    private void returnToMainSuccess() {
        showProgress(false);
        Intent returnIntent = new Intent();
        //returnIntent.putExtra(MODEL_INTENT_KEY, model);
        Model.saveModel(this);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
    //endregion


    //region Method to show the progress wheel
    //This method just loads a different view with a transition of fading
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            groupStatusView.setVisibility(View.VISIBLE);
            groupStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            groupStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            groupCreateView.setVisibility(View.VISIBLE);
            groupCreateView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            groupCreateView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            groupStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            groupCreateView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    //endregion

}
