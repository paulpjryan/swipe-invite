package com.team16.swipeinvite;

/**
 * Created by Tej on 11/6/2014.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
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

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

public class EventCreationActivity extends ActionBarActivity implements Observer {

    /* -------------------- LOG TAG CONSTANTS --------------------------- */
    private final static String LOG_TAG = "EVENT_CREATE_ACT";
    /* -------------------- END LOG TAG CONSTANTS ----------------------- */

    //region Local variables for views
    private View eventCreateView;
    private TextView eventnameField;
    private TextView locationField;
    private TextView descriptionField;
    private Button submitButton;
    private Button AddGroupButton;
    private Calendar c;
    private DatePicker startdateField;
    private TimePicker starttimeField;
    private DatePicker enddateField;
    private TimePicker endtimeField;

    private View eventStatusView;
    //endregion


    //region Local variables for data
    private Model model;
    private ArrayList<String> groups;
    private static final String GROUPS_KEY = "parentGroups";
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
        setContentView(R.layout.activity_event_creation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the model instance
        model = Model.getInstance(this);

        //Get the bundle stuff
        if (savedInstanceState != null) {
            groups = savedInstanceState.getStringArrayList(GROUPS_KEY);
            saveRT = savedInstanceState.getParcelable(SAVE_TOKEN_KEY);
        }

        //Setting up the calendar
        c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        //Setting up the views
        eventCreateView = (View) findViewById(R.id.event_create_form);
        eventStatusView = (View) findViewById(R.id.event_create_status);


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
        AddGroupButton = (Button) findViewById(R.id.bt_add_group2event);

        //Listener for the add group button
        AddGroupButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Cancel previous save if user clicks button
                if (saveRT != null) {
                    saveRT.cancel();
                }
                //Launch group add activity
                Intent intent = new Intent(EventCreationActivity.this, Add_group2eventActivity.class);
                //intent.putStringArrayListExtra(GROUPS_KEY, groups);
                startActivityForResult(intent, GROUP_ADD_REQUEST_CODE);
            }
        } );
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        if (model == null) {
            model = Model.getInstance(this);
        }
        model.addObserver(this);
        if (saveRT != null) {
            saveRT.resume(onSaveComplete);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        if (saveRT != null) {
            saveRT.suspend();
        }
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
        if (groups != null) {
            outState.putStringArrayList(GROUPS_KEY, groups);
        }
        if (saveRT != null) {
            outState.putParcelable(SAVE_TOKEN_KEY, saveRT);
        }
    }
    //endregion

    private static final int GROUP_ADD_REQUEST_CODE = 1;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GROUP_ADD_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "Got OK from group add.");
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(LOG_TAG, "Got CANCEL from group add.");
                }
                //Get the group list from the intent
                groups = data.getStringArrayListExtra(GROUPS_KEY);
                Log.d(LOG_TAG, "Got groups: " + groups.toString());
                break;
        }
    }


    //region Methods for menus and options
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.event_creation, menu);
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
    //endregion


    //region Method for submit button
    public void onEventSubmit(View v)
    {
        //Prevent button spam
        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing button spam.");
            makeToast("Server busy");
            return;
        }

        showProgress(true);

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
            showProgress(false);
            eventnameField.setError("Cannot be left blank");
            eventnameField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(description)) {
            Log.d(LOG_TAG, "Description field cannot be blank.");
            showProgress(false);
            descriptionField.setError("Cannot be left blank");
            descriptionField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(location)) {
            Log.d(LOG_TAG, "location field cannot be blank.");
            showProgress(false);
            locationField.setError("Cannot be left blank");
            locationField.requestFocus();
            return;
        } else if (eventname.length() > 30 ) {
            Log.d(LOG_TAG, "Name field cannot be longer than 20 characters.");
            showProgress(false);
            eventnameField.setError("Cannot be longer than 20 characters");
            eventnameField.requestFocus();
            return;
        } else if (description.length() > 100 || description.length() <= 3) {
            Log.d(LOG_TAG, "Description field not within range.");
            showProgress(false);
            descriptionField.setError("Must be between 4 and 100 characters");
            descriptionField.requestFocus();
            return;
        } else if(endDate.before(startDate)) {
            Log.d(LOG_TAG, "End date must be after start date");
            showProgress(false);
            Toast bread = Toast.makeText(EventCreationActivity.this, "End date must be after start date", Toast.LENGTH_LONG);
            bread.show();
            enddateField.requestFocus();
            return;
        } else if(startDate.before(currentDate)) {
            Log.d(LOG_TAG, "start date must be after current date");
            showProgress(false);
            Toast bread = Toast.makeText(EventCreationActivity.this, "Start date must be after current date", Toast.LENGTH_LONG);
            bread.show();
            startdateField.requestFocus();
            return;
        }

        //Check if the group array is empty or null
        if (groups == null || groups.size() <= 0) {
            Log.d(LOG_TAG, "Event must have a group.");
            showProgress(false);
            makeToast("No groups set for event");
            return;
        }

        //Create the event
        Event e = new Event(eventname, description, location, startDate, endDate, groups);

        //Save the event to the server
        saveRT = e.getBaasDocument().save(SaveMode.IGNORE_VERSION, onSaveComplete);


        /*Toast bread = Toast.makeText(EventCreationActivity.this, "Submitted", Toast.LENGTH_LONG);
        bread.show(); */
    }
    //endregion


    //region Methods for async save request
    private static final String SAVE_TOKEN_KEY = "save";
    private RequestToken saveRT;
    private final BaasHandler<BaasDocument> onSaveComplete = new BaasHandler<BaasDocument>() {
        @Override
        public void handle(BaasResult<BaasDocument> result) {
            saveRT = null;
            if (result.isFailed()) {
                Log.d(LOG_TAG, "Server error: " + result.error());
                showProgress(false);
                makeToast("Unable to create event");
                return;
            } else if (result.isSuccess()) {
                Log.d(LOG_TAG, "Save success.");
                completeSave(result.value());
            } else if (result.isCanceled()) {
                Log.d(LOG_TAG, "Save cancelled.");
                showProgress(false);
                return;
            }
        }
    };
    //endregion


    //region Method to deal with save result
    private void completeSave(BaasDocument d) {
        //Add the event to the local model
        model.getAcceptedEvents().add(new Event(d));
        Model.saveModel(this);

        //Send intent to finish up event stuff
        Log.d(LOG_TAG, "Sending intent to finish event creation.");
        Intent intent = new Intent(this, EventCreateService.class);
        intent.putExtra("event", d.getId());
        intent.putStringArrayListExtra(GROUPS_KEY, groups);
        startService(intent);

        //Finish this activity
        finish();
        return;
    }
    //endregion


    //region Method to show the progress wheel
    //This method just loads a different view with a transition of fading
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            eventStatusView.setVisibility(View.VISIBLE);
            eventStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            eventStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            eventCreateView.setVisibility(View.VISIBLE);
            eventCreateView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            eventCreateView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            eventStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            eventCreateView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    //endregion


    //region Helper method to make toast
    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    //endregion

}
