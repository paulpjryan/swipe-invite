package com.team16.swipeinvite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasResult;
import com.baasbox.android.RequestToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SearchGroupActivity extends ActionBarActivity implements View.OnClickListener, Observer {
    private static final String LOG_TAG = "SearchGroup";


    //region Local variables for views
    private ListView ListView_search_group;
    private GroupsAdapter ListAdapter;
    private ImageButton bt_search;
    private EditText nameText;
    private ProgressBar progressSpinner;
    //endregion


    //region Local model variable
    private Model model;
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
        setContentView(R.layout.activity_search_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the model instance
        model = Model.getInstance(this);

        //Progress bar
        progressSpinner = (ProgressBar) findViewById(R.id.progressBar_group_search);
        loaderSpin(false);

        // Button bt_join = (Button) findViewById(R.id.button_searchgroup_add);
        nameText = (EditText) findViewById(R.id.editText_searchgroup);
        bt_search = (ImageButton) findViewById(R.id.button_searchgroup_search);
        bt_search.setOnClickListener(this);
        ListView_search_group = (ListView) findViewById(R.id.lv_search_group);

        ListView_search_group.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Get the correct group
                Group2 group = (Group2) ListAdapter.getItem(position);

                //Create the intent to start the join activity
                Intent intent = new Intent(SearchGroupActivity.this, SearchSpeMemberActivity.class);
                intent.putExtra("group", group);

                //Launch the intent
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        //Make sure model exists
        if (model == null) {
            model = Model.getInstance(this);
        }
        //Clear the list of searched groups
        ListAdapter = new GroupsAdapter(this, new ArrayList<Group2>());
        ListView_search_group.setAdapter(ListAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        model.addObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        model.deleteObserver(this);
        if (searchRT != null) {
            searchRT.cancel();
        }
    }
    //endregion


    //region Methods for menus and options
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
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion


    //region Methods for the clicking of a button
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_searchgroup_search:

                //Need to prevent button spamming
                if (searchRT != null) {
                    Log.d(LOG_TAG, "Preventing spam of button.");
                    makeToast("Server busy.");
                    return;
                }

                loaderSpin(true);

                //Check the input
                BaasQuery queryG = BaasQuery.builder().where("privacy=false").build();
                String groupName = nameText.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Log.d(LOG_TAG, "Correcting blank group name.");
                } else {
                    queryG = queryG.buildUpon().and("name like '" + groupName + "%'").build();
                }

                //Create the query
                //Filter out any active groups
                List<Group2> activeGroups = model.getActiveGroups();
                synchronized (activeGroups) {
                    for (Group2 x : activeGroups) {
                        if (!x.isPrivate()) {
                            queryG = queryG.buildUpon().and("id<>'" + x.getId() + "'").build();
                        }
                    }
                }
                //Launch query
                searchRT = BaasDocument.fetchAll("group", queryG.buildUpon().criteria(), onSearchComplete);

                break;

            case R.id.editText_searchgroup:
                nameText.setError(null);
                break;
        }

    }
    //endregion


    //region Methods for async search request
    private static final String SEARCH_TOKEN_KEY = "search";
    private RequestToken searchRT;
    private final BaasHandler<List<BaasDocument>> onSearchComplete = new BaasHandler<List<BaasDocument>>() {
        @Override
        public void handle(BaasResult<List<BaasDocument>> result) {
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
            loaderSpin(false);
            Log.d(LOG_TAG, "Server weird.");
        }
    };
    //endregion


    //region Methods to deal with search result
    private void failedSearch() {
        loaderSpin(false);
        makeToast("Server unavailable");
        ListAdapter = new GroupsAdapter(this, new ArrayList<Group2>());
        ListView_search_group.setAdapter(ListAdapter);
        return;
    }

    private void completeSearch(List<BaasDocument> groupList) {
        loaderSpin(false);
        if (groupList == null || groupList.size() == 0) {
            makeToast("No inactive public groups available with that name");
            ListAdapter = new GroupsAdapter(this, new ArrayList<Group2>());
            ListView_search_group.setAdapter(ListAdapter);
            return;
        }
        //Convert to list of groups
        ArrayList<Group2> gList = new ArrayList<Group2>();
        for (BaasDocument x : groupList) {
            gList.add(new Group2(x));
        }
        //Create Group Adapter for list
        ListAdapter = new GroupsAdapter(this, gList);
        ListView_search_group.setAdapter(ListAdapter);
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
