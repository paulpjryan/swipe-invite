package com.team16.swipeinvite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
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

public class GroupCreationActivity extends ActionBarActivity {
    /* ------------------ BAAS-SERVER TOKEN SECTION ------------------------------ */
    private String rememberId;
    private final static String REMEMBER_ID_KEY = "id";
    private RequestToken savingRequestToken;
    private final static String SAVING_TOKEN_KEY = "saving";
    private RequestToken grantingRequestToken;
    private final static String GRANTING_TOKEN_KEY = "granting";
    /* ------------------- END TOKEN SECTION -------------------------------------- */



    /* ------------------- LOCAL VARIALBES FOR VIEW ELEMENTS -------------------------- */
    //Variables for form view
    private View groupCreateView;  //View with form
    private int ispriv = -1;  //start at -1 to represent nothing checked
    private EditText nameview;
    private EditText descview;
    private RadioGroup radgroup;
    private Button submit;

    //Variables for status bar view
    private View groupStatusView;  //View with status bar
    private TextView statusMessage;
    /* -------------------- END VARIABLES FOR VIEW ELEMENTS SECTION --------------------- */



    /* ------------------ OVERRIDE METHODS FOR ANDROID ACTIVITY ------------------------ */
	@Override
    //onCreate is called when the activity is first started
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //DO NOT ADD ANY VIEW ACTIVITY CODE ABOVE HERE
        //Loading any previous server requests if the activity was closed
        if (savedInstanceState != null) {
            savingRequestToken = RequestToken.loadAndResume(savedInstanceState, SAVING_TOKEN_KEY, onComplete);
            grantingRequestToken = RequestToken.loadAndResume(savedInstanceState, GRANTING_TOKEN_KEY, onGrantComplete);
            rememberId = savedInstanceState.getString(REMEMBER_ID_KEY);
        }

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

        //Submit button
        Button submit_bt = (Button)findViewById(R.id.button_submit_groupCreation);
        submit_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast words = Toast.makeText(GroupCreationActivity.this,"Sucessfully create a group",Toast.LENGTH_LONG);
                words.show();
                Intent intent_sg = new Intent(GroupCreationActivity.this,MainActivity.class);
                startActivity(intent_sg);
            }
        });

	}


	@Override
    //Method to add options to the options menu for the activity
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_creation, menu);
		return true;
	}

	@Override
    //Method to determine which option menu item was selected
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
        else if(id == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
		return super.onOptionsItemSelected(item);
	}

    @Override
    //onPause is called when the activity leaves the user's view
    public void onPause() {
        super.onPause();
    }

    @Override
    //onResume is called when the activity comes back to the user's view
    public void onResume() {
        super.onResume();
        /*OBSOLETE
        if (savingRequestToken != null) {
            //Need to resume token, only instance variable needed
            showProgress(true);
            savingRequestToken.resume(onComplete);
        } */
    }

    @Override
    //Method called when the system asks the activity to save any simple data to a bundle state
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (savingRequestToken != null) {
            //Need to suspend and save this token in the bundle
            savingRequestToken.suspendAndSave(outState, SAVING_TOKEN_KEY);
        }
        if (grantingRequestToken != null) {
            //Need to suspend and save this token in the bundle
            grantingRequestToken.suspendAndSave(outState, GRANTING_TOKEN_KEY);
            outState.putString(REMEMBER_ID_KEY, rememberId);
        }
    }
    /* ------------------------ END OVERRIDE METHOD SECTION ---------------------------------- */



    /* ----------------------- METHODS FOR BAASHANDLERS --------------------------------- */
    //handler for sending a request to the server for Group
    private final BaasHandler<BaasDocument> onComplete = new BaasHandler<BaasDocument>() {
        @Override
        public void handle(BaasResult<BaasDocument> result) {
            savingRequestToken = null;
            if (result.isFailed()) {
                Log.d("ERROR","ERROR",result.error());
            }
            completeCreate(result);
        }
    };

    //handler for sending a grant to the server for Group
    private final BaasHandler<Void> onGrantComplete = new BaasHandler<Void>() {
        @Override
        public void handle(BaasResult<Void> result) {
            grantingRequestToken = null;
            if (result.isFailed()) {
                Log.d("ERROR","ERROR",result.error());
            }
            completeGrant(result);
        }
    };

    /* ------------------------ END BAASHANDLER SECTION ------------------------------ */



    /* --------------------- RADIO GROUP RESPONDER SECTION ----------------------------- */

    //Method to respond to the radio button clicked
    //Listener set in the xml
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

    /* ------------------------ END RADIO GROUP SECTION --------------------------------- */



    /* ------------------------ SUBMIT BUTTON RESPONDER SECTION ---------------------------- */

    //Method to respond to the actual button click
    //Listener is set in the xml
    public void submitResponder(View v) {
        //Change the views
        showProgress(true);

        //Getting user inputs from views and Check their null conditions
        Editable n = this.nameview.getText();
        Editable d = this.descview.getText();

        //Check for null entries
        if ((ispriv == -1) || (n == null) || (d == null)) {
            //ACTUALLY NEED TO NOTIFY USER OF ISSUE
            Log.d("Log", "Cannot submit, fields are required.");
            showProgress(false);
            return;
        }

        //Switch to strings and bool
        String name = n.toString();
        String descrip = d.toString();
        boolean priv = false;
        if (ispriv == 1) {
            priv = true;
        }

        //Check for unsatisfactory entries
        if (n.charAt(0) == ' ') {
            //ACTUALLY NEED TO NOTIFY USER OF ISSUE
            Log.d("Log", "Cannot submit, name cannot begin with space.");
            showProgress(false);
            return;
        }
        else if (n.length() < 2) {
            //ACTUALLY NEED TO NOTIFY USER OF ISSUE
            Log.d("Log", "Cannot submit, name to short.");
            showProgress(false);
            return;
        } else if ((d.length() < 2) || (d.length() > 100)) {
            //ACTUALLY NEED TO NOTIFY USER OF ISSUE
            Log.d("Log", "Cannot submit, description invalid.");
            showProgress(false);
            return;
        }

        //Proceed to create new group with information
        createNewGroup(name, descrip, priv);

        return;
    }
    /* ---------------------- END SUBMIT BUTTON SECTION ----------------------- */



    /* ---------------------- GROUP CREATION SECTION ------------------------------- */
    private void createNewGroup(String n, String d, boolean p) {
        //Creating local instance of the group
        Group g = new Group(((StartUp) this.getApplication()).getActiveUser(), n, p);
        g.setDescription(d);
        g.addUser(((StartUp) this.getApplication()).getActiveUser());

        //Try to send group to the server
        //Log.d("LOG", "So far so good.");  JUST A CHECK
        //showProgress(false);   JUST A CHECK

        //Package new group object into BaasDocument
        BaasDocument newDoc = Group.getBaasGroup(g);

        //Try to send Group Document object to server
        savingRequestToken = newDoc.save(onComplete);

    }

    //Method called after a server request token finishes
    private void completeCreate(final BaasResult<BaasDocument> result) {
        //Reset token to null
        savingRequestToken = null;
        saveGroup(result.value());
        //Check if successful result
        if (result.isSuccess()) {
            //Try to grant permission if not private
            if(!(result.value().getBoolean("isprivate"))){
                //Launch grant request and put result group id in global variable
                rememberId = result.value().getId();
                result.value().grantAll(Grant.READ, Role.REGISTERED, onGrantComplete);
            } else {
                //TRY TO FINISH ACTIVITY, remove progress wheel
                //save group locally to active user
                showProgress(false);
                finish();
            }
        } else {
            Log.d("LOG", "**ERROR**", result.error());
            showProgress(false);
        }
        return;
    }

    /* -------------------------- END GROUP CREATION SECTION ----------------------------- */



    /* -------------------------- GRANT COMPLETION SECTION ------------------------------- */
    private void completeGrant(BaasResult<Void> result) {
        grantingRequestToken = null;
        //Checking to see if grant went through
        if (result.isSuccess()) {
            //Nothing to fix, just exit
            rememberId = null;
            showProgress(false);
            finish();
        } else {
            //Fix unsync
            for (Group x : ((StartUp) this.getApplication()).getActiveUser().groups) {
                if (x.getGroupDoc().getId().equals(rememberId)) {
                    ((StartUp) this.getApplication()).getActiveUser().removeGroup(x);
                    Log.d("LOG", "FIXED SYNC ISSUE BY DELETING GROUP");
                }
            }
            showProgress(false);
        }
        rememberId = null;
    }
    /* --------------------------- END GRANT SECTION ---------------------------------- */


    
    /* -------------------------- LOCAL SAVING SECTION -------------------------------- */
    private void saveGroup(BaasDocument result) {
        Group n = new Group(result);
        ((StartUp) this.getApplication()).getActiveUser().addGroup(n);

    }
    /* -------------------------- END LOCAL SAVING SECTION ---------------------------- */



    /* --------------------------- SHOW SEND PROGRESS SECTION ----------------------------- */
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
    /* ------------------------ END SHOWING PROGRESS SECTION ----------------------------- */

}
