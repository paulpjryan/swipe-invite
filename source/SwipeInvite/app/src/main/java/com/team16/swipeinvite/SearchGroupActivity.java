package com.team16.swipeinvite;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
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

import com.google.android.gms.games.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static com.team16.swipeinvite.R.id.list_add_person_tv;

public class SearchGroupActivity extends ActionBarActivity {
    public static ListView ListView_search_group;
    private ArrayAdapter<String> ListAdapter;
    private static final String LOG_TAG = "ADD_P2G";



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       // Button bt_join = (Button) findViewById(R.id.button_searchgroup_add);
        ImageButton bt_search = (ImageButton) findViewById(R.id.button_searchgroup_search);




        //region Codes for join group button
/*
        bt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast msg = Toast.makeText(SearchGroupActivity.this,"Successfully send the invitation",Toast.LENGTH_LONG);
                msg.show();
            }
        });*/
        //endregion


/*
        String[] data = {
                "Engl 101 + Description: Fall 2014",
                "Engl 101i + Description: Spring 2014",

        };
        */

        //region codes for group listview Make listview blank first before the user click search Button

        //endregion



        //region Codes for searching button

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //To-do
                //Add search code in the server here

                // Dummy data
                String[] data = {
                        "Engl 101 + Description: Fall 2014",
                        "Engl 101 + Description:"
                };

                ListView_search_group = (ListView) findViewById(R.id.lv_search_group);

                ArrayList<String> GroupList = new ArrayList<String>();
                GroupList.addAll(Arrays.asList(data));

                ListAdapter = new ArrayAdapter<String>(SearchGroupActivity.this,R.layout.list_item_group_search,GroupList);
                ListView_search_group.setAdapter(ListAdapter);


            }



        });

        // !!!!!!!!!!!!!!!!!   Things needed to be fixed here !!!!!!!!!!!!!!!!
        //  LISTVIEW IS ALWAYS NULL WHEN TRY TO LOAD IT !!!!!!!!!!!!!

        if (ListView_search_group != null ) {
            ListView_search_group.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 1) {
                        Intent intent = new Intent(SearchGroupActivity.this, SearchSpeMemberActivity.class);
                        startActivity(intent);
                    }
                }
            });
            return;
        }
        else {
            Log.d(LOG_TAG, "Still Null");

        };

        //endregion
	}

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






}
