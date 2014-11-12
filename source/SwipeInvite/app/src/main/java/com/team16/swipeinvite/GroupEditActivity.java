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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.widget.EditText;


import android.widget.Button;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;

import org.apache.http.HttpRequest;
import org.apache.http.protocol.HTTP;


public class GroupEditActivity extends ActionBarActivity {
    private static final String LOG_TAG = "GROUPEDIT";

    //region Local variables for views
    private ListView mainListView;
    private ListView mainListView2;
    private ArrayAdapter<String> ListAdapter;

    private EditText groupnameField;
    private EditText descriptionField;
    private EditText groupTypeField;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressSpinner = (ProgressBar) findViewById(R.id.progressBar_group_edit);
        progressSpinner.setVisibility(View.GONE);

        //Load the model
        model = Model.getInstance(this);

        //Populate the Text views
        populateTextViews();

        //region Submit Button -- DOES NOTHING
        /*

        Button submit_bt = (Button)findViewById(R.id.group_edit_submit);
        submit_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast words = Toast.makeText(GroupEditActivity.this,"Sucessful", Toast.LENGTH_LONG);
                words.show();
            }
        });*/
        //endregion

        //region Add member button  -- DOES NOTHING
        /*
        Button add_bt = (Button)findViewById(R.id.bt_add_member);
        add_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_add_person2group = new Intent(GroupEditActivity.this, Add_person2group.class);
                startActivity(intent_add_person2group);
            }
        }); */
        //endregion

        //region event list view
        mainListView = (ListView) findViewById(R.id.listView_events_group);
        String[] data = {
                "Having fun Halloween - 4:30pm",
                "Party - 6:15pm",
                "CS free toturial - 11:30am",
                "RANDOM DANCING - ALL THE TIME",
                "RANDOM DANCING - ALL THE TIME",
                "RANDOM DANCING - ALL THE TIME",
                "RANDOM DANCING - ALL THE TIME",
                "RANDOM DANCING - ALL THE TIME",
        };
        ArrayList<String> EventList = new ArrayList<String>();
        EventList.addAll(Arrays.asList(data));

        ListAdapter = new ArrayAdapter<String>(this,R.layout.list_item_event,EventList);

        mainListView.setAdapter(ListAdapter);
        //endregion

        //region Group member Listview
        mainListView2 = (ListView) findViewById(R.id.listView_group_member);
        String[] data2 = {
                "Having fun Halloween - 4:30pm",
                "Party - 6:15pm",
                "CS free toturial - 11:30am",
                "RANDOM DANCING - ALL THE TIME",
                "RANDOM DANCING - ALL THE TIME",
                "RANDOM DANCING - ALL THE TIME",
                "RANDOM DANCING - ALL THE TIME",
                "RANDOM DANCING - ALL THE TIME",
        };

        ArrayList<String> EventList2 = new ArrayList<String>();
        EventList2.addAll(Arrays.asList(data2));


        ListAdapter2 = new ArrayAdapter<String>(this,R.layout.list_item_group_member,R.id.list_add_person_tv,EventList2);

        mainListView2.setAdapter(ListAdapter2);
        //endregion

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        if (saveRT != null) {
            progressSpinner.setVisibility(View.VISIBLE);
            saveRT.resume(onSaveComplete);
        }
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
        }
        //Set the text of the views
        groupnameField.setText(g.getName());
        descriptionField.setText(g.getDescription());
        if (g.isPrivate()) {
            groupTypeField.setText("private");
        }
        groupTypeField.setText("public");

        //Decide whether or not the user has access to edit
        boolean perm = false;
        if (g.hasDetailPermission()) {
            Log.d(LOG_TAG, "The user has detail permission on group.");
            perm = true;
        }
        groupnameField.setEnabled(perm);
        groupnameField.setFocusable(perm);
        descriptionField.setEnabled(perm);
        descriptionField.setFocusable(perm);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_edit, menu);
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
        else if(id == android.R.id.home)
        {
            if (saveRT != null) {
                returnCancelled();
            } else {
                returnOk();
            }
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
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


    //region Class for Fragment -- DOES NOT DO ANYTHING
    /*
    public static class PlaceholderFragment extends Fragment {
        private ArrayAdapter<String> mArrayAdapter;
        public PlaceholderFragment() {
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_group_edit,container,false);

            String[] data = {
                    "Having fun Halloween - 4:30pm",
                    "Party - 6:15pm",
                    "CS free toturial - 11:30am",
                    "RANDOM DANCING - ALL THE TIME",
                    "RANDOM DANCING - ALL THE TIME",
                    "RANDOM DANCING - ALL THE TIME",
                    "RANDOM DANCING - ALL THE TIME",
                    "RANDOM DANCING - ALL THE TIME",
            };

            List<String> dummyData = new ArrayList<String>(Arrays.asList(data));
            mArrayAdapter =
                    new ArrayAdapter<String>(
                            getActivity(), // The current context (this activity)
                            R.layout.list_item_event, // The name of the layout ID.
                            R.id.list_item_event_textview, // The ID of the textview to populate.
                            dummyData);
            ListView ListView = (ListView) rootView.findViewById(R.id.listView_events_group);
            ListView.setAdapter(mArrayAdapter);

            return rootView;


        }



    }
    */
    //endregion


    //region Methods for add member button
    public void addMemListener(View v) {
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
        }
        if (!g.hasMemberPermission()) {   //If the user does not have permission, don't submit
            Log.d(LOG_TAG, "User does not have permission to add members.");
            Toast.makeText(this, "You are not a member admin for this group.", Toast.LENGTH_SHORT).show();
            //RELOAD the Member list view
            return;
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
                if(resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "Got ok result from add memeber.");
                    //RELOAD the member list view
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(LOG_TAG, "Got canceled result from group creation.");
                    //DO NOTHING
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
    public void submitListener(View v)
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
       }
       if (!g.hasDetailPermission()) {   //If the user does not have permission, don't submit
           Log.d(LOG_TAG, "User does not have permission to submit change.");
           Toast.makeText(this, "You are not a detail admin for this group.", Toast.LENGTH_SHORT).show();
           populateTextViews();
           progressSpinner.setVisibility(View.GONE);
           return;
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
        }
        g.setBaasDocument(u);    //Actually put the change into the model

        //Notify the GroupFragment that a change has occurred
        GroupsAdapter.updateData(lG);

        //Save the Model
        Model.saveModel(this);

        //Reload the views
        populateTextViews();

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
