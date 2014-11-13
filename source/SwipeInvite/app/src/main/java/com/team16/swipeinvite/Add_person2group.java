package com.team16.swipeinvite;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;




public class Add_person2group extends ActionBarActivity implements Observer, OnClickListener{
    private static final String LOG_TAG = "ADD_P2G";


    //region Local variables for group
    private Model model;
    private String groupToEdit;
    private static final String ID_KEY = "id";
    //endregion


    //region Local variables for view elements
    private Group group;
    private Button ivButton;
    private EditText nameText;
    private ImageButton scButton;
    private ListView ListView_add_people;
    private ArrayAdapter<String> ListAdapter;
    //endregion






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person2group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        //Problem for model
        //group = new Group();

        ivButton = (Button)findViewById(R.id.add_person_button);
        nameText = (EditText)findViewById(R.id.add_person);
        scButton = (ImageButton)findViewById(R.id.add_person_searchBtn);
        ivButton.setOnClickListener(this);
        nameText.setOnClickListener(this);
        scButton.setOnClickListener(this);
    }

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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.add_person_button:
                //to do


                break;
            case R.id.add_person_searchBtn:
                //to do

                // Add sql server search data here
                String[] data = {
                        "Name: Joseph",
                };

                ListView_add_people = (ListView) findViewById(R.id.add_person_listView);
                ArrayList<String> GroupList = new ArrayList<String>();
                GroupList.addAll(Arrays.asList(data));
                ListAdapter = new ArrayAdapter<String>(Add_person2group.this,R.layout.list_add_person2group_withouticon,GroupList);
                ListView_add_people.setAdapter(ListAdapter);
                break;

        }




    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub

    }
}
