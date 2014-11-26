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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.widget.EditText;


import android.widget.Button;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;
import com.baasbox.android.json.JsonObject;

import org.apache.http.HttpRequest;
import org.apache.http.protocol.HTTP;


public class GroupEditActivity extends ActionBarActivity implements Observer {
    private static final String LOG_TAG = "GROUPEDIT";

    //region Local variables for views
    private ListView mainListView;
    private ListView mainListView2;
    private ArrayAdapter<String> ListAdapter;

    private EditText groupnameField;
    private EditText descriptionField;
    private EditText groupTypeField;
    private EditText groupOpenField;

    private ArrayAdapter<String> ListAdapter2;
    private ProgressBar progressSpinner;
    //endregion


    //region Local variable for model and group reference
    private Model model;
    private String groupToEdit;
    private static final String ID_KEY = "id";
    //endregion


    //region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_group_edit);

        //Loading any saved states
        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "Loading bundle.");
            groupToEdit = savedInstanceState.getString(ID_KEY);
            saveRT = savedInstanceState.getParcelable(SAVE_TOKEN_KEY);
        } else {
            Log.d(LOG_TAG, "Getting group id from intent.");
            groupToEdit = getIntent().getStringExtra("id");
        }

        //Setting up views
        groupnameField = (EditText) findViewById(R.id.textView_new_group);
        descriptionField = (EditText) findViewById(R.id.et_edit_group_des);
        groupTypeField = (EditText) findViewById(R.id.textView_new_group_type);
        groupTypeField.setEnabled(false);
        groupTypeField.setFocusable(false);
        groupOpenField = (EditText) findViewById(R.id.textView_new_group_typeOpen);
        groupOpenField.setEnabled(false);
        groupOpenField.setFocusable(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressSpinner = (ProgressBar) findViewById(R.id.progressBar_group_edit);
        progressSpinner.setVisibility(View.GONE);
        mainListView = (ListView) findViewById(R.id.listView_events_group);
        mainListView2 = (ListView) findViewById(R.id.listView_group_member);

        //Load the model
        model = Model.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        if (saveRT != null) {
            progressSpinner.setVisibility(View.VISIBLE);
            saveRT.resume(onSaveComplete);
        }
        if (model == null) {
            model = Model.getInstance(this);
        }
        model.addObserver(this);

        //Populate the Text views
        populateTextViews();

        //Populate the list views
        populateEventList();
        populateMemberList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        if (saveRT != null) {
            progressSpinner.setVisibility(View.GONE);
            saveRT.suspend();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putString(ID_KEY, groupToEdit);     //cannot lose the id that we were passed
        if (saveRT != null) {
            outState.putParcelable(SAVE_TOKEN_KEY, saveRT);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        model.deleteObserver(this);
    }
    //endregion


    //region Implementation of observer
    public void update(Observable ob, Object o) {
        //NEED TO RUN ON UI THREAD
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Update all views
                Log.d(LOG_TAG, "Updating from notification.");
                populateTextViews();
                populateEventList();
                populateMemberList();
            }
        });
    }
    //endregion


    //region Method to populate the text views
    private void populateTextViews() {
        Log.d(LOG_TAG, "Repopulating text views.");
        //Get the current group to read from
        List<Group2> lG = model.getActiveGroups();
        Group2 g = null;
        synchronized (lG) {
            for (Group2 x : lG) {
                if (x.equals(groupToEdit)) {
                    g = x;
                }
            }
        }
        if (g == null) {
            Toast.makeText(this, "This group is no longer available.", Toast.LENGTH_SHORT).show();
            returnCancelled();
            finish();
            return;
        }
        //Set the text of the views
        groupnameField.setText(g.getName());
        descriptionField.setText(g.getDescription());
        if (g.isPrivate()) {
            groupTypeField.setText("private");
        } else {
            groupTypeField.setText("public");
        }
        if (g.isOpen()) {
            groupOpenField.setText("open");
        } else {
            groupOpenField.setText("closed");
        }

        //Decide whether or not the user has access to edit
        boolean perm = false;
        if (g.hasDetailPermission() || g.isOpen()) {
            Log.d(LOG_TAG, "The user has detail permission on group or it is open group.");
            perm = true;
        }
        groupnameField.setEnabled(perm);
        groupnameField.setFocusable(perm);
        descriptionField.setEnabled(perm);
        descriptionField.setFocusable(perm);
        Log.d(LOG_TAG, "Json string of group: " + g.getBaasDocument().toJson().encode());
    }
    //endregion


    //region Methods to repopulate the list views
    private void populateEventList() {
        Log.d(LOG_TAG, "Populating event list");

        //Pull the proper list from the model
        List<Group2> lG = model.getActiveGroups();
        Group2 g = null;
        synchronized (lG) {
            for (Group2 x : lG) {
                if (x.equals(groupToEdit)) {
                    g = x;
                }
            }
        }
        if (g == null) {
            Toast.makeText(this, "This group is no longer available.", Toast.LENGTH_SHORT).show();
            returnCancelled();
            finish();
            return;
        }

        ArrayList<String> EventList = new ArrayList<String>();
        EventList.addAll(g.getEventList());

        ListAdapter = new ArrayAdapter<String>(this,R.layout.list_item_event,EventList);

        mainListView.setAdapter(ListAdapter);
    }
    private void populateMemberList() {
        Log.d(LOG_TAG, "Populating member list");

        //Pull the proper list from the model
        List<Group2> lG = model.getActiveGroups();
        Group2 g = null;
        synchronized (lG) {
            for (Group2 x : lG) {
                if (x.equals(groupToEdit)) {
                    g = x;
                }
            }
        }
        if (g == null) {
            Toast.makeText(this, "This group is no longer available.", Toast.LENGTH_SHORT).show();
            returnCancelled();
            finish();
            return;
        }

        ArrayList<String> MemberList = new ArrayList<String>();
        MemberList.addAll(g.getUserList());

        ListAdapter2 = new ArrayAdapter<String>(this,R.layout.list_item_group_member,R.id.list_add_person_tv,MemberList);

        mainListView2.setAdapter(ListAdapter2);
    }
    //endregion


    //region Methods to return to the main activity
    private void returnOk() {
        Log.d(LOG_TAG, "Navigating away from profile edit, return OK.");
        progressSpinner.setVisibility(View.GONE);
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
    }
    private void returnCancelled() {
        Log.d(LOG_TAG, "Navigating away from profile edit, return CANCEL.");
        progressSpinner.setVisibility(View.GONE);
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
    }
    //endregion


    //region Methods for menus
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu called");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                if (saveRT != null) {
                    returnCancelled();
                } else {
                    returnOk();
                }
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.bt_add_member:
                addMemberListener();
                return true;
            case R.id.group_edit_submit:
                submitListener();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (saveRT != null) {
            returnCancelled();
        } else {
            returnOk();
        }
        super.onBackPressed();
    }
    //endregion


    //region Methods for add member button
    public void addMemberListener() {
        //Check to make sure the button is not spammed
        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing spam of submit button.");
            Toast.makeText(this, "Please wait request to finish...", Toast.LENGTH_SHORT).show();
            return;
        }

        //Get the current group from the model
        List<Group2> lG = model.getActiveGroups();
        Group2 g = null;
        synchronized (lG) {
            for (Group2 x : lG) {
                if (x.equals(groupToEdit)) {
                    g = x;
                }
            }
        }
        if (g == null) {
            Toast.makeText(this, "This group is no longer available.", Toast.LENGTH_SHORT).show();
            returnCancelled();
            finish();
            return;
        }
        if (!g.isOpen()) {
            if (!g.hasMemberPermission()) {   //If the user does not have permission, don't submit
                Log.d(LOG_TAG, "User does not have permission to add members.");
                Toast.makeText(this, "You are not a member admin for this group.", Toast.LENGTH_SHORT).show();
                //RELOAD the Member list view
                return;
            }
        }

        //Start intent to add a person to a group
        Intent intent = new Intent(this, Add_person2group.class);
        intent.putExtra("id", groupToEdit);
        startActivityForResult(intent, ADD_MEMBER_REQUEST_CODE);
    }

    //Method to handle the returning new member activity
    private static final int ADD_MEMBER_REQUEST_CODE = 1;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Figure out which activity is returning a result
        switch (requestCode) {
            case ADD_MEMBER_REQUEST_CODE:    //Group Create activity result
                Log.d(LOG_TAG, "Got activity return.");
                if(resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "Got ok result from add memeber.");
                    //RELOAD the list views
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(LOG_TAG, "Got canceled result from group creation.");
                    //RELOAD the list views
                }
                break;
            default:
                Log.d(LOG_TAG, "Request code not set.");
                break;
        }
        Log.d(LOG_TAG, "Model active group size: " + model.getActiveGroups().size());
    }
    //endregion


    //region Methods for the submit button
    public void submitListener()
    {
        //Check to make sure the button is not spammed
        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing spam of submit button.");
            Toast.makeText(this, "Already contacting server...", Toast.LENGTH_SHORT).show();
            return;
        }

        //showprogress here ***************************************
        progressSpinner.setVisibility(View.VISIBLE);
        Log.d(LOG_TAG, "Submit button Pressed.");
        String groupname = groupnameField.getText().toString();
        String description = descriptionField.getText().toString();

       if (TextUtils.isEmpty(groupname)) {
           //NOTIFY USER OF EMPTY FIELD
           Log.d(LOG_TAG, "Username cannot be empty");
           //showProgress(false); *******************************
           progressSpinner.setVisibility(View.GONE);
           groupnameField.setError("Cannot be left blank");
           groupnameField.requestFocus();
           return;
       } else if (TextUtils.isEmpty(description)) {
           //NOTIFY USER OF EMPTY FIELD
           Log.d(LOG_TAG, "Password cannot be empty");
           //showProgress(false); *******************************
           progressSpinner.setVisibility(View.GONE);
           descriptionField.setError("Cannot be left blank");
           descriptionField.requestFocus();
           return;
       } else if (groupname.length() > 20) {
           //NOTIFY USER OF EMPTY FIELD
           Log.d(LOG_TAG, "Username must be between 4 and 30 characters.");
           //showProgress(false); *******************************
           progressSpinner.setVisibility(View.GONE);
           groupnameField.setError("Must be between 4 and 30 characters");
           groupnameField.requestFocus();
           return;
       } else if (description.length() > 100 || description.length() <= 3) {
           //NOTIFY USER OF EMPTY FIELD
           Log.d(LOG_TAG, "Password must be more than 6 characters.");
           //showProgress(false); *******************************
           progressSpinner.setVisibility(View.GONE);
           descriptionField.setError("Must be greater than 6 characters");
           descriptionField.requestFocus();
           return;
       }

       //Get the current group from the model
       List<Group2> lG = model.getActiveGroups();
       Group2 g = null;
       synchronized (lG) {
           for (Group2 x : lG) {
               if (x.equals(groupToEdit)) {
                   g = x;
               }
           }
       }
       if (g == null) {
           Toast.makeText(this, "This group is no longer available.", Toast.LENGTH_SHORT).show();
           returnCancelled();
           finish();
           return;
       }
       if (!g.isOpen()) {
           if (!g.hasDetailPermission()) {   //If the user does not have permission, don't submit
               Log.d(LOG_TAG, "User does not have permission to submit change.");
               Toast.makeText(this, "You are not a detail admin for this group.", Toast.LENGTH_SHORT).show();
               populateTextViews();
               progressSpinner.setVisibility(View.GONE);
               return;
           }
       }

       //THIS WILL NEED TO BE CHANGED ONCE THE BAASBOX API INCLUDES UPDATE METHOD
       //Make changes by using a new group object
       Group2 updateG = new Group2(g.getBaasDocument().toJson());    //this will not alter the model object itself
       updateG.setName(groupname);
       updateG.setDescription(description);
        Log.d(LOG_TAG, "Group in model: " + g.getName());  //Just checking to make sure the model is not changed yet

       //Save the group to the server, but do not affect the model until a response is received
       saveRT = updateG.getBaasDocument().save(SaveMode.IGNORE_VERSION, onSaveComplete);
    }

    //region Variables and methods to deal with ansync save request
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
                failedSave();
                return;
            } else if (result.isSuccess()) {
                //REPORT SAVE
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
    private void completeSave(BaasDocument u) {
        Log.d(LOG_TAG, "Save sucessful.");
        progressSpinner.setVisibility(View.GONE);
        //Update the model
        //Get the current group from the model
        List<Group2> lG = model.getActiveGroups();
        Group2 g = null;
        synchronized (lG) {
            for (Group2 x : lG) {
                if (x.equals(groupToEdit)) {
                    g = x;
                }
            }
        }
        if (g == null) {
            Toast.makeText(this, "This group is no longer available.", Toast.LENGTH_SHORT).show();
            returnCancelled();
            finish();
            return;
        }
        g.setBaasDocument(u);    //Actually put the change into the model

        //Save the Model
        Model.saveModel(this);

        //Reload the views
        //populateTextViews();  SHOULD NO LONGER BE NECESSARY AFTER SAVE MODEL

        //Send the push notifications to members
        Log.d(LOG_TAG, "Sending push.");
        //Create the json object
        JsonObject message = new JsonObject();
        message.put("type", "group");
        message.put("id", groupToEdit);
        String notificationMessage = g.getName() + " has been updated.";

        //Start the intent service to send the push
        Intent intent = new Intent(this, PushSender.class);
        intent.putStringArrayListExtra("users", g.getUserList());
        intent.putExtra("message", message);
        intent.putExtra("notification", notificationMessage);
        startService(intent);

        //Toast user success
        Toast.makeText(getApplicationContext(), "Group updated.", Toast.LENGTH_SHORT).show();
    }

    private void failedSave() {
        progressSpinner.setVisibility(View.GONE);

        //Reload the views
        populateTextViews();

        //Toast the user fail
        Toast.makeText(getApplicationContext(), "Update failed.", Toast.LENGTH_SHORT).show();

    }
    //endregion

    //endregion


}
