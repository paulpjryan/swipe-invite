package com.team16.swipeinvite;

/**
 * Created by Tej on 11/13/2014.
 */

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.RequestToken;
import com.baasbox.android.SaveMode;

import java.text.SimpleDateFormat;
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
    private View mEventEditView;
    private TextView mEventNameField;
    private TextView mEventLocationField;
    private TextView mEventDescriptionField;
    private EditText mStartDateText;
    private EditText mStartTimeText;
    private EditText mEndDateText;
    private EditText mEndTimeText;
    private TextView mCountText;
    private ProgressBar progressSpinner;
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
            this.day = day;
            setTextFromPickers();
        }
    }

    private DatePickerFragment mStartDatePicker;
    private DatePickerFragment mEndDatePicker;
    //endregion


    //region Local model and data instances
    private Model model;
    private String eventID;
    private static final String EVENT_KEY = "eventID";
    private boolean permission = false;
    private boolean deleted = false;
    private int typeOfToken;
    //endregion


    //region Implementation of observer
    public void update(Observable ob, Object o) {
        //NEEDS TO RUN ON UI THREAD
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!deleted) {
                    populateViews();
                }
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
            saveRT = savedInstanceState.getParcelable(SAVE_TOKEN_KEY);
            typeOfToken = savedInstanceState.getInt("typeOfTok");
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

        //Setting up the views
        mStartDateText = (EditText) findViewById(R.id.edit_start_date_text);
        mStartTimeText = (EditText) findViewById(R.id.edit_start_time_text);
        mEndDateText = (EditText) findViewById(R.id.edit_end_date_text);
        mEndTimeText = (EditText) findViewById(R.id.edit_end_time_text);
        mCountText = (TextView) findViewById(R.id.event_edit_count);
        progressSpinner = (ProgressBar) findViewById(R.id.progressBar_event_edit);
        showProgress(false);

       // mEventNameField = (TextView) findViewById(R.id.edit_text_event_name);
        mEventLocationField = (TextView) findViewById(R.id.edit_text_event_location);
        mEventDescriptionField = (TextView) findViewById(R.id.edit_text_event_description);

        //Lock them
       // mEventNameField.setEnabled(false);
       // mEventNameField.setFocusable(false);
        mEventLocationField.setEnabled(false);
        mEventLocationField.setFocusable(false);
        mEventDescriptionField.setEnabled(false);
        mEventDescriptionField.setFocusable(false);
        mCountText.setEnabled(false);
        mCountText.setFocusable(false);

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
        if (saveRT != null) {
            showProgress(true);
            switch (typeOfToken) {
                case 1:
                    saveRT.resume(onSaveComplete);
                    break;
                case 2:
                    saveRT.resume(onDeleteComplete);
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        if (saveRT != null) {
            showProgress(false);
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
        outState.putString(EVENT_KEY, eventID);
        if (saveRT != null) {
            outState.putParcelable(SAVE_TOKEN_KEY, saveRT);
            outState.putInt("typeOfTok", typeOfToken);
        }
    }
    //endregion


    //region Method to populate the views
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
       // mEventNameField.setText(event.getName());
        setTitle(event.getName());
        mEventLocationField.setText(event.getLocation());
        mEventDescriptionField.setText(event.getDescription());
        mCountText.setText(Integer.toString(event.getCount()));

        //Populate the date and time
        Calendar startDate = event.getBeginDate();
        Calendar endDate = event.getEndDate();

        mStartDateText.setText(new SimpleDateFormat("MM/dd/yyyy").format(startDate.getTime()));
        mEndDateText.setText(new SimpleDateFormat("MM/dd/yyyy").format(endDate.getTime()));
        mStartTimeText.setText(new SimpleDateFormat("hh:mm").format(startDate.getTime()));
        mEndTimeText.setText(new SimpleDateFormat("hh:mm").format(endDate.getTime()));

        // Setup pickers
        mStartDatePicker = new DatePickerFragment();
        mStartDatePicker.year = startDate.get(Calendar.YEAR);
        mStartDatePicker.month = startDate.get(Calendar.MONTH);
        mStartDatePicker.day = startDate.get(Calendar.DAY_OF_MONTH);

        mStartTimePicker = new TimePickerFragment();
        mStartTimePicker.hour = startDate.get(Calendar.HOUR_OF_DAY);
        mStartTimePicker.minute = startDate.get(Calendar.MINUTE);

        mEndDatePicker = new DatePickerFragment();
        mEndDatePicker.year = endDate.get(Calendar.YEAR);
        mEndDatePicker.month = endDate.get(Calendar.MONTH);
        mEndDatePicker.day = endDate.get(Calendar.DAY_OF_MONTH);

        mEndTimePicker = new TimePickerFragment();
        mEndTimePicker.hour = endDate.get(Calendar.HOUR_OF_DAY);
        mEndTimePicker.minute = endDate.get(Calendar.MINUTE);


        //Check permissions
        //Decide whether or not the user has access to edit
        boolean perm = false;
        if (event.hasPermission()) {
            Log.d(LOG_TAG, "The user has detail permission on group or it is open group.");
            perm = true;
        }
        mEventLocationField.setEnabled(perm);
        mEventLocationField.setFocusable(perm);
        mEventDescriptionField.setEnabled(perm);
        mEventDescriptionField.setFocusable(perm);

        permission = perm;

    }
    //endregion


    //region Methods for menus and options
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.event_edit_submit_d:
                onEventEdit();
                return true;
            case R.id.event_edit_submit_nod:
                onEventEdit();
                return true;
            case R.id.event_edit_delete:
                deleteEvent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.event_edit, menu);
        if (permission) {
            getMenuInflater().inflate(R.menu.event_edit_delete, menu);
        } else {
            getMenuInflater().inflate(R.menu.event_edit_nodelete, menu);
        }

        return true;
    }
    //endregion


    //region Methods for Date and Time showing
    public void showStartDatePickerDialog(View v) {
        if (!permission) {
            makeToast("You cannot edit this event");
            return;
        }
        mStartDatePicker.show(getFragmentManager(), "startDatePicker");
    }

    public void showStartTimePickerDialog(View v) {
        if (!permission) {
            makeToast("You cannot edit this event");
            return;
        }
        mStartTimePicker.show(getFragmentManager(), "startTimePicker");
    }

    public void showEndDatePickerDialog(View v) {
        if (!permission) {
            makeToast("You cannot edit this event");
            return;
        }
        mEndDatePicker.show(getFragmentManager(), "endDatePicker");
    }

    public void showEndTimePickerDialog(View v) {
        if (!permission) {
            makeToast("You cannot edit this event");
            return;
        }
        mEndTimePicker.show(getFragmentManager(), "endTimePicker");
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

    //region Method for submitting changes to event
    private void onEventEdit() {

        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing spam of button.");
            makeToast("Server busy...");
            return;
        }

        showProgress(true);

        //String name = mEventNameField.getText().toString();
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

        if (TextUtils.isEmpty(description)) {
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
        } else if (description.length() > 100 || description.length() <= 3) {
            Log.d(LOG_TAG, "Description field not within range.");
            showProgress(false);
            mEventDescriptionField.setError("Must be between 4 and 100 characters");
            mEventDescriptionField.requestFocus();
            return;
        } else if (endDate.before(startDate)) {
            Log.d(LOG_TAG, "End date must be after start date");
            showProgress(false);
            Toast bread = Toast.makeText(EventEditActivity.this, "End date must be after start date", Toast.LENGTH_LONG);
            bread.show();
            mEndDateText.requestFocus();
            return;
        } else if (startDate.before(currentDate)) {
            Log.d(LOG_TAG, "start date must be after current date");
            showProgress(false);
            Toast bread = Toast.makeText(EventEditActivity.this, "Start date must be after current date", Toast.LENGTH_LONG);
            bread.show();
            mStartDateText.requestFocus();
            return;
        }

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
            showProgress(false);
            makeToast("Event no longer available");
            finish();
            return;
        }

        //Check permissions
        if (!event.hasPermission()) {
            showProgress(false);
            makeToast("You cannot edit this event");
            return;
        }

        //Update the event
        Event updateEvent = new Event(event.toJson());
        updateEvent.setDescription(description);
        updateEvent.setLocation(location);
        updateEvent.setBeginDate(startDate);
        updateEvent.setEndDate(endDate);

        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing spam of button.");
            showProgress(false);
            makeToast("Server busy...");
            return;
        }

        //Save event to server
        saveRT = updateEvent.getBaasDocument().save(SaveMode.IGNORE_VERSION, onSaveComplete);
        typeOfToken = 1;
    }
    //endregion


    //region Methods to handle updating the event async
    private static final String SAVE_TOKEN_KEY = "saveRT";
    private RequestToken saveRT;
    private final BaasHandler<BaasDocument> onSaveComplete = new BaasHandler<BaasDocument>() {
        @Override
        public void handle(BaasResult<BaasDocument> result) {
            saveRT = null;
            if (result.isFailed()) {
                Log.d(LOG_TAG, "Save failed: " + result.error());
                showProgress(false);
                populateViews();
                return;
            } else if (result.isSuccess()) {
                Log.d(LOG_TAG, "Save success.");
                completeSave(result.value());
                return;
            }
            Log.d(LOG_TAG, "Server save weird.");
        }
    };


    private void completeSave(BaasDocument d) {
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
            showProgress(false);
            makeToast("Event no longer available");
            finish();
            return;
        }

        event.setBaasDocument(d);

        Model.saveModel(this);
        showProgress(false);
        makeToast("Event updated");
    }
    //endregion


    //region Methods to delete an event
    private void deleteEvent() {
        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing spam of button.");
            makeToast("Server busy...");
            return;
        }

        showProgress(true);

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

        //Check permissions
        if (!event.hasPermission()) {
            showProgress(false);
            makeToast("You cannot delete this event");
            return;
        }

        if (saveRT != null) {
            Log.d(LOG_TAG, "Preventing spam of button.");
            showProgress(false);
            makeToast("Server busy...");
            return;
        }

        saveRT = event.getBaasDocument().delete(onDeleteComplete);
        typeOfToken = 2;

    }


    private final BaasHandler<Void> onDeleteComplete = new BaasHandler<Void>() {
        @Override
        public void handle(BaasResult<Void> result) {
            saveRT = null;
            if (result.isFailed()) {
                Log.d(LOG_TAG, "Delete failed.");
                makeToast("Unable to delete event");
                return;
            } else if (result.isSuccess()) {
                Log.d(LOG_TAG, "Delete success.");
                completeDelete();
                return;
            }
            Log.d(LOG_TAG, "Delete weird.");
        }
    };

    private void completeDelete() {
        //Need to get the event instance
        List<Event> acceptedEvents = model.getAcceptedEvents();
        List<Event> waitingEvents = model.getWaitingEvents();
        List<Event> rejectedEvents = model.getRejectedEvents();
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
                    if (current.equals(eventID) || !current.isOnServer()) {
                        Log.d(LOG_TAG, "Event deleted: " + current.getName());
                        i.remove();
                    }
                }
            }
        }

        deleted = true;

        Model.saveModel(this);

        showProgress(false);
        makeToast("Event deleted");
        finish();
    }
    //endregion


    //region Helper method to make toast and change progress spinner
    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void showProgress(boolean show) {
        if (show) {
            progressSpinner.setVisibility(View.VISIBLE);
        } else {
            progressSpinner.setVisibility(View.GONE);
        }
    }
    //endregion

}
