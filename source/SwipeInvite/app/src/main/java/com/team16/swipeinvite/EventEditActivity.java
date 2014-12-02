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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

public class EventEditActivity extends ActionBarActivity implements Observer {

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


    //region Local model and data instances
    private Model model;
    private String eventID;
    private static final String EVENT_KEY = "eventID";
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
    }
    //endregion


    //region Lifecycle methods
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the event ID
        if (savedInstanceState != null) {
            eventID = savedInstanceState.getString(EVENT_KEY);
        } else {
            eventID = getIntent().getStringExtra(EVENT_KEY);
        }
        if (eventID == null) {
            Log.d(LOG_TAG, "Event ID has been lost.");
            makeToast("Event no longer available");
            finish();
            return;
        }

        //Get the model instance
        model = Model.getInstance(this);

        //Setting up views
        startdateField = (DatePicker) findViewById(R.id.event_start_date);
        starttimeField = (TimePicker) findViewById(R.id.event_start_time);
        enddateField = (DatePicker) findViewById(R.id.event_end_date);
        endtimeField = (TimePicker) findViewById(R.id.event_end_time);

        eventnameField = (TextView) findViewById(R.id.textView_new_event);
        locationField = (TextView) findViewById(R.id.textView_event_location);
        descriptionField = (TextView) findViewById(R.id.et_edit_event_description);

        //Lock them
        eventnameField.setEnabled(false);
        eventnameField.setFocusable(false);
        locationField.setEnabled(false);
        locationField.setFocusable(false);
        descriptionField.setEnabled(false);
        descriptionField.setFocusable(false);

        //Populate views
        populateViews();

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        if (model == null) {
            model = Model.getInstance(this);
        }
        model.addObserver(this);

    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");

    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        model.deleteObserver(this);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putString(EVENT_KEY, eventID);
    }
    //endregion


    private void populateViews() {
        //Need to get the event instance
        List<Event> acceptedEvents = model.getAcceptedEvents();
        List<Event> waitingEvents = model.getWaitingEvents();
        List<Event> rejectedEvents = model.getRejectedEvents();
        Event event = null;
        //Iterate through all possible event lists
        for (int a = 0; a < 3; a++) {
            List<Event> currentList = acceptedEvents;
            switch (a) {
                case 1:
                    currentList = waitingEvents;
                    break;
                case 2:
                    currentList = rejectedEvents;
                    break;
            }
            synchronized (currentList) {
                for (final ListIterator<Event> i = currentList.listIterator(); i.hasNext(); ) {
                    Event current = i.next();
                    if (current.equals(eventID)) {
                        event = current;
                    }
                }
            }
        }
        if (event == null) {
            Log.d(LOG_TAG, "Event disappeared.");
            makeToast("Event no longer available");
            finish();
            return;
        }

        //Populate the views
        eventnameField.setText(event.getName());
        locationField.setText(event.getLocation());
        descriptionField.setText(event.getDescription());

        //Populate the date and time
        Calendar startDate = event.getBeginDate();
        Calendar endDate = event.getEndDate();
        //TODO ANDREW NEEDS TO MAKE THE FRAGMENTS AND VIEWS PROPERLY


    }


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


    //region Method for submitting changes to event
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
    //endregion


    //region Helper method to make toast
    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    //endregion

}
