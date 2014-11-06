package com.team16.swipeinvite;

/**
 * Created by Tej on 11/6/2014.
 */

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class EventCreationActivity extends ActionBarActivity {

    /* -------------------- LOG TAG CONSTANTS --------------------------- */
    private final static String LOG_TAG = "EVENT_CREATE_ACT";
    /* -------------------- END LOG TAG CONSTANTS ----------------------- */

    private View EventCreateView;
    private TextView eventnameField;
    private TextView locationField;
    private TextView descriptionField;
    private Button submitButton;
    private Calendar c;
    private DatePicker startdateField;
    private TimePicker starttimeField;
    private DatePicker enddateField;
    private TimePicker endtimeField;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);


        c = Calendar.getInstance();
        startdateField = (DatePicker) findViewById(R.id.event_start_date);
        starttimeField = (TimePicker) findViewById(R.id.event_start_time);
        enddateField = (DatePicker) findViewById(R.id.event_end_date);
        endtimeField = (TimePicker) findViewById(R.id.event_end_time);

        startdateField.updateDate(year, month, day);
        starttimeField.setCurrentHour(hour);
        starttimeField.setCurrentMinute(minute);
        enddateField.updateDate(year, month, day);
        endtimeField.setCurrentHour(hour);
        endtimeField.setCurrentMinute(minute);

        eventnameField = (TextView) findViewById(R.id.textView_new_event);
        locationField = (TextView) findViewById(R.id.textView_event_location);
        descriptionField = (TextView) findViewById(R.id.et_edit_event_description);
        submitButton = (Button) findViewById(R.id.button_event_submit);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.event_edit, menu);
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

    protected void onEventSubmit(View v)
    {
        //showprogress(true)

        String eventname = eventnameField.getText().toString();
        String location = locationField.getText().toString();
        String description = descriptionField.getText().toString();
        int startyear = startdateField.getYear();
        int startmonth = startdateField.getMonth();
        int startday = startdateField.getDayOfMonth();
        int starthour = starttimeField.getCurrentHour();
        int startminute = starttimeField.getCurrentMinute();
        int endyear = enddateField.getYear();
        int endmonth = enddateField.getMonth();
        int endday = enddateField.getDayOfMonth();
        int endhour = endtimeField.getCurrentHour();
        int endminute = endtimeField.getCurrentMinute();

        if (TextUtils.isEmpty(eventname)) {
            Log.d(LOG_TAG, "Name field cannot be blank.");
            //showProgress(false);
            eventnameField.setError("Cannot be left blank");
            eventnameField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(description)) {
            Log.d(LOG_TAG, "Description field cannot be blank.");
            //showProgress(false);
            descriptionField.setError("Cannot be left blank");
            descriptionField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(location)) {
           // Log.d(LOG_TAG, "location field cannot be blank.");
                //showProgress(false);
            //descriptionField.setError("Cannot be left blank");
            //descriptionField.requestFocus();
            description = "TBD";
            return;
        } else if (eventname.length() > 30 ) {
            Log.d(LOG_TAG, "Name field cannot be longer than 20 characters.");
            //showProgress(false);
            eventnameField.setError("Cannot be longer than 20 characters");
            eventnameField.requestFocus();
            return;
        } else if (description.length() > 100 || description.length() <= 3) {
            Log.d(LOG_TAG, "Description field not within range.");
            //showProgress(false);
            descriptionField.setError("Must be between 4 and 100 characters");
            descriptionField.requestFocus();
            return;
        }


    }

}
