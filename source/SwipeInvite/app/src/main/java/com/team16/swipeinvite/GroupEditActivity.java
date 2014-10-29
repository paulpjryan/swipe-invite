package com.team16.swipeinvite;

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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;


public class GroupEditActivity extends ActionBarActivity {

    private static final String LOG_TAG = "GROUPEDIT";

    private ListView mainListView;
    private ListView mainListView2;
    private ArrayAdapter<String> ListAdapter;

    private TextView groupnameField;
    private TextView descriptionField;

    private ArrayAdapter<String> ListAdapter2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        groupnameField = (TextView) findViewById(R.id.textView_new_group);
        descriptionField = (TextView) findViewById(R.id.et_edit_group_des);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Submit button

       /* Button submit_bt = (Button)findViewById(R.id.group_edit_submit);
        submit_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast words = Toast.makeText(GroupEditActivity.this,"Sucessful",Toast.LENGTH_LONG);
                words.show();
            }
        });*/

        //Add member

        /*Button add_bt = (Button)findViewById(R.id.bt_add_member);
        add_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent_add_person2group = new Intent(GroupEditActivity.this, Add_person2group.class);
                startActivity(intent_add_person2group);
            }
        }); */




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
        EventList2.addAll(Arrays.asList(data));


        ListAdapter2 = new ArrayAdapter<String>(this,R.layout.list_item_group_member,R.id.list_add_person_tv,data2);

        mainListView2.setAdapter(ListAdapter2);
        //endregion








    }


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
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
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

    public void submitListener(View v)
    {
        //showprogress here ***************************************
        Log.d(LOG_TAG, "Button Pressed.");
        String groupname = groupnameField.getText().toString();
        String description = descriptionField.getText().toString();

       if (TextUtils.isEmpty(groupname)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username cannot be empty");
            //showProgress(false); *******************************
            groupnameField.setError("Cannot be left blank");
            groupnameField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(description)) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Password cannot be empty");
            //showProgress(false); *******************************
            descriptionField.setError("Cannot be left blank");
            descriptionField.requestFocus();
            return;
        } else if (groupname.length() > 20) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Username must be between 4 and 30 characters.");
            //showProgress(false); *******************************
            groupnameField.setError("Must be between 4 and 30 characters");
            groupnameField.requestFocus();
            return;
        } else if (description.length() > 100 || description.length() <= 3) {
            //NOTIFY USER OF EMPTY FIELD
            Log.d(LOG_TAG, "Password must be more than 6 characters.");
            //showProgress(false); *******************************
            descriptionField.setError("Must be greater than 6 characters");
            descriptionField.requestFocus();
            return;
        }



    }

}
