package com.team16.swipeinvite;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.Grant;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;
import com.baasbox.android.json.JsonObject;
import com.google.android.gms.plus.model.people.Person;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;




public class Add_person2group extends ActionBarActivity implements Observer, OnClickListener{
    private static final String LOG_TAG = "ADD_P2G";


    //region Local variables for group
    private Model model;
    private String groupToEdit;
    private static final String ID_KEY = "id";
    private String userToInvite = null;
    private static final String USER_KEY = "userToInvite";
    //endregion


    //region Local variables for view elements
    private Group group;
    private Button ivButton;
    private EditText nameText;
    private ImageButton scButton;
    private ListView ListView_add_people;
    private ArrayAdapter<String> ListAdapter;
    private ProgressBar progressSpinner;
    //endregion


    //region Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_add_person2group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the model
        model = Model.getInstance(this);

        //Get the group that is being edited
        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "Got group from saved instance.");
            groupToEdit = savedInstanceState.getString(ID_KEY);
            userToInvite = savedInstanceState.getString(USER_KEY);
            saveRT = savedInstanceState.getParcelable(SAVE_TOKEN_KEY);
            grantRT = savedInstanceState.getParcelable(GRANT_TOKEN_KEY);
            pushRT = savedInstanceState.getParcelable(PUSH_TOKEN_KEY);
        } else {
            Log.d(LOG_TAG, "Got group from intent.");
            groupToEdit = getIntent().getStringExtra("id");
        }

        ivButton = (Button)findViewById(R.id.add_person_button);
        nameText = (EditText)findViewById(R.id.add_person);
        scButton = (ImageButton)findViewById(R.id.add_person_searchBtn);
        ivButton.setOnClickListener(this);
        nameText.setOnClickListener(this);
        scButton.setOnClickListener(this);
        progressSpinner = (ProgressBar) findViewById(R.id.progressBar_add_member);
        progressSpinner.setVisibility(View.GONE);
        ListView_add_people = (ListView) findViewById(R.id.add_person_listView);
        ListAdapter = new ArrayAdapter<String>(Add_person2group.this,R.layout.list_add_person2group_withouticon, new ArrayList<String>());
        ListView_add_people.setAdapter(ListAdapter);

        //region Method for the clicking of a name in the list view
        ListView_add_people.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                //Make sure to avoid spamming the search button
                if (saveRT != null || grantRT != null || searchRT != null) {
                    Log.d(LOG_TAG, "Preventing list change while adding member.");
                    makeToast("Server busy.");
                    return;
                }

                progressSpinner.setVisibility(View.VISIBLE);
                //Figure out which name is being clicked
                String username = ListAdapter.getItem(position);

                //Get the current group to edit
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
                    makeToast("This group is no longer available.");
                    returnCancelled();
                    finish();
                    return;
                }
                //Check to make sure the user is not already in the group
                if (g.containsUser(username)) {
                    Log.d(LOG_TAG, "User is already in group.");
                    makeToast("User is already a member.");
                    progressSpinner.setVisibility(View.GONE);
                    return;
                }

                //Add the user to the group
                g.addUser(username);
                saveRT = g.getBaasDocument().save(SaveMode.IGNORE_VERSION, onSaveComplete);

                //Grant the permission for the user
                grantRT = g.getBaasDocument().grant(Grant.ALL, username, onGrantComplete);

                //Set the variable to remember the username
                userToInvite = username;

            }
        });
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
        if (grantRT != null) {
            progressSpinner.setVisibility(View.VISIBLE);
            grantRT.resume(onGrantComplete);
        }
        if (pushRT != null) {
            progressSpinner.setVisibility(View.VISIBLE);
            pushRT.resume(onPushComplete);
        }
        if (model == null) {
            model = Model.getInstance(this);
        }
        model.addObserver(this);
        checkForGone();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        if (searchRT != null) {
            searchRT.cancel();   //Just cancel the query if the user leaves the page, dont save it
        }

        if (saveRT != null) {
            progressSpinner.setVisibility(View.GONE);
            saveRT.suspend();
        }
        if (grantRT != null) {
            progressSpinner.setVisibility(View.GONE);
            grantRT.suspend();
        }
        if (pushRT != null) {
            progressSpinner.setVisibility(View.GONE);
            pushRT.suspend();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putString(ID_KEY, groupToEdit);
        if (saveRT != null) {
            outState.putParcelable(SAVE_TOKEN_KEY, saveRT);
        }
        if (grantRT != null) {
            outState.putParcelable(GRANT_TOKEN_KEY, grantRT);
        }
        if (pushRT != null) {
            outState.putParcelable(PUSH_TOKEN_KEY, pushRT);
        }
        if (userToInvite != null) {
            outState.putString(USER_KEY, userToInvite);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        model.deleteObserver(this);
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


    //region Methods for menus and back button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_person2group, menu);
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
            if (grantRT != null || saveRT != null || pushRT != null) {
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
        if (saveRT != null || grantRT != null || pushRT != null) {
            returnCancelled();
        } else {
            returnOk();
        }
        super.onBackPressed();
    }
    //endregion


    //region Method to respond to a click of a button
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.add_person_button:
                //to do


                break;
            case R.id.add_person_searchBtn:
                //Make sure to avoid spamming the search button
                if (saveRT != null || grantRT != null || searchRT != null) {
                    Log.d(LOG_TAG, "Preventing list change while adding member.");
                    Toast.makeText(this, "Server busy.", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressSpinner.setVisibility(View.VISIBLE);
                //Get the text from the text field
                String username = nameText.getText().toString();
                if (TextUtils.isEmpty(username)) {
                    Log.d(LOG_TAG, "Cannot search a blank username.");
                    progressSpinner.setVisibility(View.GONE);
                    nameText.setError("Enter a username");
                    nameText.requestFocus();
                    return;
                }

                // Add sql server search data here
                //where("name like " + "'" + username + "%" + "'")
                //BaasQuery.builder().projection("select * from BaasUser where name like '" + username + "%'")
                BaasQuery queryU = BaasQuery.builder().users().build();
                queryU = queryU.buildUpon().where("user.name like '" + username + "%'").build();
                Log.d(LOG_TAG, "Sending query: " + queryU.toString());
                searchRT = BaasUser.fetchAll(queryU.buildUpon().criteria(), onSearchComplete);
                break;
            case R.id.add_person:
                nameText.setError(null);
                break;
        }

    }
    //endregion


    //region Methods for async search request
    private static final String SEARCH_TOKEN_KEY = "search";
    private RequestToken searchRT;
    private final BaasHandler<List<BaasUser>> onSearchComplete = new BaasHandler<List<BaasUser>>() {
        @Override
        public void handle(BaasResult<List<BaasUser>> result) {
            searchRT = null;
            if (result.isFailed()) {
                Log.d(LOG_TAG, "Server query failed: " + result.error());
                failedSearch();
                return;
            } else if (result.isSuccess()) {
                Log.d(LOG_TAG, "Server query success.");
                completeSearch(result.value());
                return;
            }
            progressSpinner.setVisibility(View.GONE);
            Log.d(LOG_TAG, "Server weird.");
        }
    };
    //endregion


    //region Methods to deal with search result
    private void failedSearch() {
        progressSpinner.setVisibility(View.GONE);
        Toast.makeText(this, "Server unavailable.", Toast.LENGTH_SHORT).show();
        ListAdapter = new ArrayAdapter<String>(Add_person2group.this, R.layout.list_add_person2group_withouticon,
                new ArrayList<String>());
        ListView_add_people.setAdapter(ListAdapter);
        return;
    }
    private void completeSearch(List<BaasUser> userList) {
        progressSpinner.setVisibility(View.GONE);
        if (userList == null || userList.size() == 0) {
            Toast.makeText(this, "No user with that name.", Toast.LENGTH_SHORT).show();
            ListAdapter = new ArrayAdapter<String>(Add_person2group.this, R.layout.list_add_person2group_withouticon,
                    new ArrayList<String>());
            ListView_add_people.setAdapter(ListAdapter);
            return;
        }
        ArrayList<String> uList = new ArrayList<String>();
        for (BaasUser x : userList) {
            Log.d(LOG_TAG, "Person retrieved: " + x.getName());
            uList.add(x.getName());
        }
        ListAdapter = new ArrayAdapter<String>(Add_person2group.this,R.layout.list_add_person2group_withouticon, uList);
        ListView_add_people.setAdapter(ListAdapter);
        return;
    }
    //endregion


    //region Methods to deal with saving the group to the server
    private static final String SAVE_TOKEN_KEY = "save";
    private RequestToken saveRT;
    private final BaasHandler<BaasDocument> onSaveComplete = new BaasHandler<BaasDocument>() {
        @Override
        public void handle(BaasResult<BaasDocument> result) {
            saveRT = null;
            completeSave(result);
        }
    };


    private void completeSave(BaasResult<BaasDocument> result) {
        if (result.isFailed()) {
            Log.d(LOG_TAG, "Server save failed: " + result.error());
            if (grantRT != null) {
                grantRT.cancel();
            }
            Toast.makeText(this, "Unable to complete invite.", Toast.LENGTH_SHORT).show();
            progressSpinner.setVisibility(View.GONE);
            return;
        } else if (result.isSuccess()) {
            Log.d(LOG_TAG, "Server save success.");
            //Get the current group to edit
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
            //Update the local group
            g.setBaasDocument(result.value());

            //Save the model
            Model.saveModel(this);

        } else if (result.isCanceled()) {
            Log.d(LOG_TAG, "Save cancelled.");
            return;
        }
        //Check to see if the push can be sent
        if (grantRT == null) {
            sendPush();
        }
    }
    //endregion


    //region Methods to grant a user permission to read document
    private static final String GRANT_TOKEN_KEY = "grant";
    private RequestToken grantRT;
    private final BaasHandler<Void> onGrantComplete = new BaasHandler<Void>() {
        @Override
        public void handle(BaasResult<Void> result) {
            grantRT = null;
            completeGrant(result);
            return;
        }
    };

    private void completeGrant(BaasResult<Void> result) {
        if (result.isFailed()) {
            Log.d(LOG_TAG, "Grant failed: " + result.error());
            if (saveRT != null) {
                saveRT.cancel();
            }
            Toast.makeText(this, "Unable to complete invite.", Toast.LENGTH_SHORT).show();
            progressSpinner.setVisibility(View.GONE);
            return;
        } else if (result.isSuccess()) {
            Log.d(LOG_TAG, "Grant success.");
        } else if (result.isCanceled()) {
            Log.d(LOG_TAG, "Grant cancelled.");
            return;
        }

        //Check to see if the push can be sent
        if (saveRT == null) {
            sendPush();
        }
    }
    //endregion


    //region Method to send the push notification
    private void sendPush() {
        Log.d(LOG_TAG, "Sending push.");
        //Create the json object
        JsonObject message = new JsonObject();
        message.put("type", "group");
        message.put("id", groupToEdit);

        //Figure out what users to send to
        //Get the current group to edit
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

        String notificationMessage = userToInvite + " joined the group.";

        //Start the intent service to send the push
        Intent intent = new Intent(this, PushSender.class);
        intent.putStringArrayListExtra("users", g.getUserList());
        intent.putExtra("message", message);
        intent.putExtra("notification", notificationMessage);
        startService(intent);


        //Send the push notification
        //pushRT = BaasBox.messagingService().newMessage().extra(message).to(userToInvite)
                //.text(notificationMessage).send(onPushComplete);
        makeToast("Invite sent");
        progressSpinner.setVisibility(View.GONE);
    }
    //endregion


    //region Methods to deal with async push notification
    private static final String PUSH_TOKEN_KEY = "push";
    private RequestToken pushRT;
    private final BaasHandler<Void> onPushComplete = new BaasHandler<Void>() {
        @Override
        public void handle(BaasResult<Void> result) {
            pushRT = null;
            if (result.isFailed()) {
                Log.d(LOG_TAG, "Push error: " + result.error());
                makeToast("Unable to complete invite.");
            } else if (result.isSuccess()) {
                Log.d(LOG_TAG, "Push success.");
                makeToast("Invite sent.");
            }
            userToInvite = null;
            progressSpinner.setVisibility(View.GONE);
        }
    };
    //endregion


    //region Method to make toast
    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    //endregion


    //region Method for observer callback
    public void update(Observable observable, Object data) {
        // MAKE SURE UI THREAD IS USED
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Check for the group deletion
                checkForGone();
            }
        });

    }
    //endregion


    //region Method to check if the current group has been deleted
    private void checkForGone() {
        //Get the current group to edit
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
            makeToast("This group is no longer available.");
            returnCancelled();
            finish();
            return;
        }
    }
    //endregion

}
