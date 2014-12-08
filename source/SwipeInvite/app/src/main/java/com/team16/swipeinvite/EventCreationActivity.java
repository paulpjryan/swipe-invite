package com.team16.swipeinvite;

/**
 * Created by Tej on 11/6/2014.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

public class EventCreationActivity extends ActionBarActivity implements Observer {

    /* -------------------- LOG TAG CONSTANTS --------------------------- */
    private final static String LOG_TAG = "EVENT_CREATE_ACT";
    /* -------------------- END LOG TAG CONSTANTS ----------------------- */

    //region Local variables for views
    private View mEventCreateView;
    private TextView mEventNameField;
    private TextView mEventLocationField;
    private TextView mEventDescriptionField;
    private Button mAddGroupButton;
    private Calendar mCurrentCalendar;
    private EditText mStartDateText;
    private EditText mStartTimeText;
    private EditText mEndDateText;
    private EditText mEndTimeText;
    private View mEventStatusView;
    //endregion


    //region Picker setup
    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        public int hour, minute;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
            setTextFromPickers();
        }
    }

    private TimePickerFragment mStartTimePicker;
    private TimePickerFragment mEndTimePicker;

    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public int year, month, day;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.year = year;
            setTextFromPickers();
        }
    }

    private DatePickerFragment mStartDatePicker;
    private DatePickerFragment mEndDatePicker;
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
        mCurrentCalendar = Calendar.getInstance();
        int currentYear = mCurrentCalendar.get(Calendar.YEAR);
        int currentMonth = mCurrentCalendar.get(Calendar.MONTH);
        int currentDay = mCurrentCalendar.get(Calendar.DATE);
        int currentHour = mCurrentCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = mCurrentCalendar.get(Calendar.MINUTE);

        //Setting up the views
        mEventCreateView = (View) findViewById(R.id.event_create_form);
        mEventStatusView = (View) findViewById(R.id.event_create_status);

        mStartDateText = (EditText) findViewById(R.id.start_date_text);
        mStartTimeText = (EditText) findViewById(R.id.start_time_text);
        mEndDateText = (EditText) findViewById(R.id.end_date_text);
        mEndTimeText = (EditText) findViewById(R.id.end_time_text);

        mEventNameField = (TextView) findViewById(R.id.text_event_name);
        mEventLocationField = (TextView) findViewById(R.id.text_event_location);
        mEventDescriptionField = (TextView) findViewById(R.id.text_event_description);
        mAddGroupButton = (Button) findViewById(R.id.bt_add_group2event);

        // Setup pickers
        mStartDatePicker = new DatePickerFragment();
        mStartDatePicker.year = currentYear;
        mStartDatePicker.month = currentMonth;
        mStartDatePicker.day = currentDay;

        mStartTimePicker = new TimePickerFragment();
        mStartTimePicker.hour = currentHour;
        mStartTimePicker.minute = currentMinute;

        mEndDatePicker = new DatePickerFragment();
        mEndDatePicker.year = currentYear;
        mEndDatePicker.month = currentMonth;
        mEndDatePicker.day = currentDay; // FIXME

        mEndTimePicker = new TimePickerFragment();
        mEndTimePicker.hour = currentHour;
        mEndTimePicker.minute = currentMinute;

        setTextFromPickers();

        //Listener for the add group button
        mAddGroupButton.setOnClickListener(new View.OnClickListener() {

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
        });
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

    private void setTextFromPickers() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mStartDatePicker.year, mStartDatePicker.month, mStartDatePicker.day, mStartTimePicker.hour, mStartTimePicker.minute);
        mStartDateText.setText(new SimpleDateFormat("MM/dd/yyyy").format(calendar.getTime()));
        mStartTimeText.setText(new SimpleDateFormat("hh:mm").format(calendar.getTime()));

        calendar.set(mEndDatePicker.year, mEndDatePicker.month, mEndDatePicker.day, mEndTimePicker.hour, mEndTimePicker.minute);
        mEndDateText.setText(new SimpleDateFormat("MM/dd/yyyy").format(calendar.getTime()));
        mEndTimeText.setText(new SimpleDateFormat("hh:mm").format(calendar.getTime()));
    }

    //region Method called when the add group returns
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
    //endregion


    //region Methods for menus and options
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event_creation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.submit_event_creation:
                onEventSubmit(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion


    //region Methods for Date and Time showing
    public void showStartDatePickerDialog(View v) {
        mStartDatePicker.show(getFragmentManager(), "startDatePicker");
    }

    public void showStartTimePickerDialog(View v) {
        mStartTimePicker.show(getFragmentManager(), "startTimePicker");
    }

    public void showEndDatePickerDialog(View v) {
        mEndDatePicker.show(getFragmentManager(), "endDatePicker");
    }

    public void showEndTimePickerDialog(View v) {
        mEndTimePicker.show(getFragmentManager(), "endTimePicker");
    }
    //endregion


    //region Method for submit button
    public void onEventSubmit(View v) {
        //Prevent button spam
        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing button spam.");
            makeToast("Server busy");
            return;
        }

        showProgress(true);

        String name = mEventNameField.getText().toString();
        String location = mEventLocationField.getText().toString();
        String description = mEventDescriptionField.getText().toString();
        int startYear = mStartDatePicker.year;
        int startMonth = mStartDatePicker.month;
        int startDay = mStartDatePicker.day;
        int startHour = mStartTimePicker.hour;
        int startMinute = mStartTimePicker.minute;
        int endYear = mEndDatePicker.year;
        int endMonth = mEndDatePicker.month;
        int endDay = mEndDatePicker.day;
        int endHour = mEndTimePicker.hour;
        int endMinute = mEndTimePicker.minute;

        Calendar startDate, endDate, currentDate;
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        currentDate = Calendar.getInstance();
        startDate.clear();
        endDate.clear();

        startDate.set(startYear, startMonth, startDay, startHour, startMinute);
        endDate.set(endYear, endMonth, endDay, endHour, endMinute);

        if (TextUtils.isEmpty(name)) {
            Log.d(LOG_TAG, "Name field cannot be blank.");
            showProgress(false);
            mEventNameField.setError("Cannot be left blank");
            mEventNameField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(description)) {
            Log.d(LOG_TAG, "Description field cannot be blank.");
            showProgress(false);
            mEventDescriptionField.setError("Cannot be left blank");
            mEventDescriptionField.requestFocus();
            return;
        } else if (TextUtils.isEmpty(location)) {
            Log.d(LOG_TAG, "location field cannot be blank.");
            showProgress(false);
            mEventLocationField.setError("Cannot be left blank");
            mEventLocationField.requestFocus();
            return;
        } else if (name.length() > 30) {
            Log.d(LOG_TAG, "Name field cannot be longer than 20 characters.");
            showProgress(false);
            mEventNameField.setError("Cannot be longer than 20 characters");
            mEventNameField.requestFocus();
            return;
        } else if (description.length() > 100 || description.length() <= 3) {
            Log.d(LOG_TAG, "Description field not within range.");
            showProgress(false);
            mEventDescriptionField.setError("Must be between 4 and 100 characters");
            mEventDescriptionField.requestFocus();
            return;
        } else if (endDate.before(startDate)) {
            Log.d(LOG_TAG, "End date must be after start date");
            showProgress(false);
            Toast bread = Toast.makeText(EventCreationActivity.this, "End date must be after start date", Toast.LENGTH_LONG);
            bread.show();
            mEndDateText.requestFocus();
            return;
        } else if (startDate.before(currentDate)) {
            Log.d(LOG_TAG, "start date must be after current date");
            showProgress(false);
            Toast bread = Toast.makeText(EventCreationActivity.this, "Start date must be after current date", Toast.LENGTH_LONG);
            bread.show();
            mStartDateText.requestFocus();
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
        Event e = new Event(name, description, location, startDate, endDate, groups);

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

            mEventStatusView.setVisibility(View.VISIBLE);
            mEventStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mEventStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mEventCreateView.setVisibility(View.VISIBLE);
            mEventCreateView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mEventCreateView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mEventStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mEventCreateView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    //endregion


    //region Helper method to make toast
    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    //endregion

}
