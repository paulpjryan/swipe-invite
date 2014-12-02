package com.team16.swipeinvite;

/**
 * Created by Tej on 11/13/2014.
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class EventEditActivity extends ActionBarActivity{

    /* -------------------- LOG TAG CONSTANTS --------------------------- */
    private final static String LOG_TAG = "EVENT_EDIT_ACT";
    /* -------------------- END LOG TAG CONSTANTS ----------------------- */


    //region Local variables for views
    private View EventEditView;
    private TextView eventnameField;
    private TextView locationField;
    private TextView descriptionField;
    private Button submitButton;
    private Calendar c;
    private DatePicker startdateField;
    private TimePicker starttimeField;
    private DatePicker enddateField;
    private TimePicker endtimeField;
    //endregion


    //region Lifecycle methods
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startdateField = (DatePicker) findViewById(R.id.event_start_date);
        starttimeField = (TimePicker) findViewById(R.id.event_start_time);
        enddateField = (DatePicker) findViewById(R.id.event_end_date);
        endtimeField = (TimePicker) findViewById(R.id.event_end_time);

        eventnameField = (TextView) findViewById(R.id.textView_new_event);
        locationField = (TextView) findViewById(R.id.textView_event_location);
        descriptionField = (TextView) findViewById(R.id.et_edit_event_description);
    }
    //endregion


    //region Methods for menus and options
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.event_edit, menu);
        return true;
    }
    //endregion


    public void onEventEdit(View v) {

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

        Calendar startDate, endDate, currentDate;
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        currentDate = Calendar.getInstance();
        startDate.clear();
        endDate.clear();

        startDate.set(startyear, startmonth, startday, starthour, startminute);
        endDate.set(endyear, endmonth, endday, endhour, endminute);

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
        } else if(endDate.before(startDate)) {
            Log.d(LOG_TAG, "End date must be after start date");
            //showProgress(false);
            Toast bread = Toast.makeText(EventEditActivity.this, "End date must be after start date", Toast.LENGTH_LONG);
            bread.show();
            enddateField.requestFocus();
            return;
        } else if(startDate.before(currentDate)) {
            Log.d(LOG_TAG, "start date must be after current date");
            //showProgress(false);
            Toast bread = Toast.makeText(EventEditActivity.this, "Start date must be after current date", Toast.LENGTH_LONG);
            bread.show();
            startdateField.requestFocus();
            return;
        }

        Toast bread = Toast.makeText(EventEditActivity.this, "Submitted", Toast.LENGTH_LONG);
        bread.show();

    }
}
