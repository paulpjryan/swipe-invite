package com.team16.swipeinvite;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.security.acl.Group;
import java.util.Observable;
import java.util.Observer;




public class Add_person2group extends Activity implements Observer, OnClickListener{

    private Group group;
    private Button ivButton;
    private EditText nameText;
    private ImageButton scButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person2group);


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
                //todo

                break;

        }




    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub

    }
}
