package com.team16.swipeinvite;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

/**
 * Created by Zening on 11/13/2014.
 */
public class SearchSpeMemberActivity extends Activity {
    private ArrayAdapter<String> ListAdapter;
    public ListView ListView_search_group;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_specfic_member);

        String[] data = {
                "Joseph",
                "Tel"
        };

        ListView_search_group = (ListView) findViewById(R.id.listView_group_member);

        ArrayList<String> GroupList = new ArrayList<String>();
        GroupList.addAll(Arrays.asList(data));

        ListAdapter = new ArrayAdapter<String>(SearchSpeMemberActivity.this,R.layout.list_item_search_spec,GroupList);
        ListView_search_group.setAdapter(ListAdapter);
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
