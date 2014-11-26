package com.team16.swipeinvite;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
import java.util.List;
import java.util.Observable;
import java.util.Observer;


import static com.team16.swipeinvite.R.id.list_add_person_tv;

public class SearchGroupActivity extends ActionBarActivity implements View.OnClickListener, Observer {
    private static final String LOG_TAG = "SearchGroup";


    //region Local variables for views
    private  ListView ListView_search_group;
    private ArrayAdapter<String> ListAdapter;
    private ImageButton bt_search;
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

       // Button bt_join = (Button) findViewById(R.id.button_searchgroup_add);
        bt_search = (ImageButton) findViewById(R.id.button_searchgroup_search);
        bt_search.setOnClickListener(this);
        ListView_search_group = (ListView) findViewById(R.id.lv_search_group);
        ListAdapter = new ArrayAdapter<String>(SearchGroupActivity.this,R.layout.list_item_group_search, new ArrayList<String>());
        ListView_search_group.setAdapter(ListAdapter);

        ListView_search_group.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
               // String username = ListAdapter.getItem(position);
                if (position == 1) {
                    Intent intent = new Intent(SearchGroupActivity.this, SearchSpeMemberActivity.class);
                    startActivity(intent);
                }


            }
        });
	}
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        //Make sure model exists
        if (model == null) {
            model = Model.getInstance(this);
        }
        model.addObserver(this);
    }
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        model.deleteObserver(this);
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
        switch(v.getId())
        {

            case R.id.button_searchgroup_search:

                String[] data = {
                        "Engl 101 + Description: Fall 2014",
                        "Engl 101 + Description:"
                };

                ArrayList<String> GroupList = new ArrayList<String>();
                GroupList.addAll(Arrays.asList(data));

                ListAdapter = new ArrayAdapter<String>(SearchGroupActivity.this,R.layout.list_item_group_search, GroupList);
                ListView_search_group.setAdapter(ListAdapter);

                break;
        }

    }
    //endregion






}
