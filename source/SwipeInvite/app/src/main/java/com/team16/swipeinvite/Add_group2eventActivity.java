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

/**
 * Created by Zening on 11/26/2014.
 */
public class Add_group2eventActivity extends ActionBarActivity implements View.OnClickListener {
    private  ListView ListView_search_group;
    private ArrayAdapter<String> ListAdapter;
    private static final String LOG_TAG = "ADD_P2G";
    private ImageButton bt_search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group2event);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Button bt_join = (Button) findViewById(R.id.button_searchgroup_add);
        bt_search = (ImageButton) findViewById(R.id.add_group_searchBtn);
        bt_search.setOnClickListener(this);
        ListView_search_group = (ListView) findViewById(R.id.add_group_listView);
        ListAdapter = new ArrayAdapter<String>(Add_group2eventActivity.this,R.layout.list_item_add_group2event, R.id.list_add_group_tv,new ArrayList<String>());
        ListView_search_group.setAdapter(ListAdapter);

        ListView_search_group.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                // String username = ListAdapter.getItem(position);
                if (position == 1) {
                    Intent intent = new Intent(Add_group2eventActivity.this, SearchSpeMemberActivity.class);
                    startActivity(intent);
                }


            }
        });

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

    public void onClick(View v) {
        switch(v.getId())
        {

            case R.id.add_group_searchBtn:

                String[] data = {
                        "Engl 101 + Description: Fall 2014",
                        "Engl 101 + Description:"
                };

                ArrayList<String> GroupList = new ArrayList<String>();
                GroupList.addAll(Arrays.asList(data));

                ListAdapter = new ArrayAdapter<String>(Add_group2eventActivity.this,R.layout.list_item_add_group2event, R.id.list_add_group_tv, GroupList);
                ListView_search_group.setAdapter(ListAdapter);

                break;
        }

    }






}
