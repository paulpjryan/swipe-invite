package com.team16.swipeinvite;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasUser;
import com.baasbox.android.Grant;
import com.baasbox.android.SaveMode;
import com.google.android.gms.games.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Zening on 11/26/2014.
 */
public class Add_group2eventActivity extends ActionBarActivity implements View.OnClickListener, Observer {
    private static final String LOG_TAG = "ADD_G2E";

    //region Local variables for views
    private  ListView ListView_search_group;
    private GroupsAdapter ListAdapter;
    //endregion


    //region Local variables for model and data
    private Model model;
    private ArrayList<String> groups;
    private static final String GROUPS_KEY = "parentGroups";
    //endregion


    //region Implementation of observer
    public void update(Observable ob, Object o) {
        //NEEDS TO RUN ON UI THREAD
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Refresh anything
                ListAdapter.updateData(model.getActiveGroups());
            }
        });
        return;
    }
    //endregion


    //region Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group2event);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the model
        model = Model.getInstance(this);

        //Get the groups list
        if (savedInstanceState != null) {
            groups = savedInstanceState.getStringArrayList(GROUPS_KEY);
        } else {
            groups = new ArrayList<String>();
        }

        ListView_search_group = (ListView) findViewById(R.id.add_group_listView);
        ListAdapter = new GroupsAdapter(this, model.getActiveGroups());
        ListView_search_group.setAdapter(ListAdapter);

        ListView_search_group.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                Group2 g = (Group2) ListAdapter.getItem(position);
                if (!g.isOpen()) {
                    if (!g.hasEventPermission()) {
                        makeToast("Not an event admin for group");
                        return;
                    }
                }
                if (groups.contains(g.getId())) {
                    groups.remove(g.getId());
                    view.setBackgroundColor(Color.TRANSPARENT);
                    makeToast("Group removed");
                }
                else {
                    groups.add(g.getId());
                    view.setBackgroundColor(Color.LTGRAY);
                    makeToast("Group added");
                }
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
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
        Log.d(LOG_TAG, "onSaveInstanceState");
        if (groups != null) {
            outState.putStringArrayList(GROUPS_KEY, groups);
        }
    }
    //endregion


    //region Methods for menus and buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_group, menu);
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
        } else if (id == android.R.id.home) {
            returnToEventCreate();
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        returnToEventCreate();
        super.onBackPressed();
    }
    //endregion


    private void returnToEventCreate() {
        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra(GROUPS_KEY, groups);
        setResult(RESULT_OK, returnIntent);
    }


    //region Method for search button click
    public void onClick(View v) {
        switch(v.getId())
        {
        }

    }
    //endregion


    //region Helper method to make toast
    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    //endregion

}
