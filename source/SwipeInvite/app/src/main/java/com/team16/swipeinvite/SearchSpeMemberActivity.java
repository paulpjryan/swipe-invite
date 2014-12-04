package com.team16.swipeinvite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;
import com.baasbox.android.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Zening on 11/13/2014.
 */
public class SearchSpeMemberActivity extends Activity implements Observer {
    private static final String LOG_TAG = "SearchSpeMemeber";


    //region Local view variables
    private ArrayAdapter<String> ListAdapter;
    public ListView ListView_search_group;
    private ProgressBar progressSpinner;
    private TextView nameField;
   // private TextView typeField;
    private TextView descField;
    //endregion


    //region Local variables for model and group
    private Model model;
    private Group2 group;
    private static final String GROUP_KEY = "group";
    //endregion


    //region Implementation of observer
    public void update(Observable ob, Object o) {
        //NEEDS TO RUN ON UI THREAD
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Refresh anything
            }
        });
        return;
    }
    //endregion


    //region Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_specfic_member);

        //Get the model
        model = Model.getInstance(this);

        //Get the group to display
        group = getIntent().getParcelableExtra("group");
        if (group == null) {
            Log.d(LOG_TAG, "Intent did not have a group");
            if (savedInstanceState != null) {
                Log.d(LOG_TAG, "Getting from saved instance.");
                group = savedInstanceState.getParcelable(GROUP_KEY);
            } else {
                Log.d(LOG_TAG, "Could not find an instance of group, exit.");
                makeToast("This group is no longer available");
                finish();
                return;
            }
        }

        //Instantiate views
        progressSpinner = (ProgressBar) findViewById(R.id.progressBar_group_join);
        loaderSpin(false);
        nameField = (TextView) findViewById(R.id.group_join_name);
        //typeField = (TextView) findViewById(R.id.group_join_type);
        descField = (TextView) findViewById(R.id.group_join_desc);
        ListView_search_group = (ListView) findViewById(R.id.listView_group_member);
        //Populate views
        populateViews();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        //Make sure model is not null
        if (model == null) {
            model = Model.getInstance(this);
        }
        model.addObserver(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        model.deleteObserver(this);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState called");
        outState.putParcelable(GROUP_KEY, group);
    }
    //endregion


    //region Method to populate views from data
    private void populateViews() {
        nameField.setText(group.getName());
        descField.setText(group.getDescription());
        if (group.isPrivate()) {
           // typeField.setText("private");
        } else {
            //typeField.setText("public");
        }
        ListAdapter = new ArrayAdapter<String>(SearchSpeMemberActivity.this,R.layout.list_item_search_spec,group.getUserList());
        ListView_search_group.setAdapter(ListAdapter);
    }
    //endregion


    //region Methods for menus and options
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


    //region Method to respond to joining of group
    public void joinGroupResponder(View v) {
        //prevent spamming of button
        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing spam.");
            makeToast("Server busy");
            return;
        }

        loaderSpin(true);

        //Check to make sure the user is not currently part of the group
        if (group.containsUser(BaasUser.current().getName())) {
            Log.d(LOG_TAG, "User already is in the group");
            makeToast("You already joined this group");
            loaderSpin(false);
            return;
        }

        //Add the user to the group
        group.addUser(BaasUser.current().getName());

        //Save the group to the server
        //TODO Change this when the update function exists
        saveRT = group.getBaasDocument().save(SaveMode.IGNORE_VERSION, onSaveComplete);

    }
    //endregion


    //region Methods for async save request
    private static final String SAVE_TOKEN_KEY = "save";
    private RequestToken saveRT;
    private final BaasHandler<BaasDocument> onSaveComplete = new BaasHandler<BaasDocument>() {
        @Override
        public void handle(BaasResult<BaasDocument> result) {
            saveRT = null;
            if (result.isFailed()) {
                Log.d(LOG_TAG, "Server error: " + result.error());
                failedSave();
                return;
            } else if (result.isSuccess()) {
                Log.d(LOG_TAG, "Save success.");
                completeSave(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server weird.");
            loaderSpin(false);
        }
    };
    //endregion


    //region Methods called after save returns
    private void failedSave() {
        loaderSpin(false);
        makeToast("An error occurred");
        model.deleteObserver(this);
        finish();
        return;
    }
    private void completeSave(BaasDocument d) {
        loaderSpin(false);
        //Save the group locally
        group.setBaasDocument(d);
        model.getActiveGroups().add(new Group2(d));

        //Send the push notifications to members
        Log.d(LOG_TAG, "Sending push.");
        //Create the json object
        JsonObject message = new JsonObject();
        message.put("type", "group");
        message.put("id", group.getId());
        String notificationMessage = BaasUser.current().getName() + " has joined the group.";

        //Start the intent service to send the push
        Intent intent = new Intent(this, PushSender.class);
        intent.putStringArrayListExtra("users", group.getUserList());
        intent.putExtra("message", message);
        intent.putExtra("notification", notificationMessage);
        startService(intent);

        //Finish this activity
        Model.saveModel(this);
        makeToast("Joined the group");
        model.deleteObserver(this);
        finish();

        return;
    }
    //endregion


    //region Helper methods for toasts and loading
    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void loaderSpin(boolean visible) {
        if (visible) {
            progressSpinner.setVisibility(View.VISIBLE);
        } else {
            progressSpinner.setVisibility(View.GONE);
        }
    }
    //endregion


}
