package com.team16.swipeinvite;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonArray;
import com.baasbox.android.json.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kylekrynski on 10/22/14.
 * DO NOT MAKE PUBLIC CLASS
 * MOST DATA METHODS SHOULD BE PROTECTED
 */
class Event implements Parcelable {
    //region Constant keys for Json data contained in the BaasDocument
    private static final String COLLECTION_NAME = "event";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String LOCATION_KEY = "location";
    private static final String BEGIN_DATE_KEY = "begin_date";
    private static final String END_DATE_KEY = "end_date";
    private static final String ADMIN_ARRAY_KEY = "admins";
    private static final String PARENT_GROUP_ARRAY_KEY = "groups";
    private static final String ATTENDEE_KEY = "users";
    //endregion


    //region Instance variables for an event
    private BaasDocument event;
    //endregion


    //region Methods for Parcelable interface
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(event, flags);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Event> CREATOR
            = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    private Event(Parcel in) {
        event = in.readParcelable(BaasDocument.class.getClassLoader());
    }
    //endregion


    //region Constructors for the event class
    //Create a brand new event object from scratch
    protected Event(String name, String description, String location, Calendar begdate, Calendar enddate, List<?> groups) {
        this.event = new BaasDocument(COLLECTION_NAME);
        initializeAdminArray();
        this.event.put(NAME_KEY, name);
        this.event.put(DESCRIPTION_KEY, description);
        this.event.put(LOCATION_KEY, location);
        this.setBeginDate(begdate);
        this.setEndDate(enddate);
        this.event.put(ATTENDEE_KEY, 1);
        setParentGroups(groups);
    }

    //Create a brand new group object from an existing BaasDocument
    protected Event(BaasDocument d) throws EventException {
        setBaasDocument(d);
    }

    //Create a brand new group object from a Json object
    protected Event(JsonObject j) throws EventException {
        setFromJson(j);
    }
    //endregion


    //region Methods to initialize arrays upon creation of an event
    private void initializeAdminArray() {
        JsonArray ja = new JsonArray();
        ja.add(BaasUser.current().getName());
        this.event.put(ADMIN_ARRAY_KEY, ja);
    }
    //endregion


    //region Getter and setter for the BaasDocument
    protected synchronized void setBaasDocument(BaasDocument d) throws EventException {
        if (!(d.getCollection().equals(COLLECTION_NAME))) {   //If the json object was not a group
            throw new EventException("BaasDocument was not an event: " + d.toString());
        }
        if (isOnServer() && !d.getId().equals(getId())) {    //must be the same document to overwrite
            throw new EventException("BaasDocument did not match id: " + d.toString());
        }
        this.event = d;
    }

    protected synchronized BaasDocument getBaasDocument() {
        return this.event;
    }
    //endregion


    //region Getter and setter with JsonObjects
    protected synchronized void setFromJson(JsonObject j) throws EventException {
        if ((j.getString("@class") != null) && !(j.getString("@class").equals(COLLECTION_NAME))) {   //If the json object was not a group
            throw new EventException("Json object was not an event: " + j.toString());
        }
        if (isOnServer() && !j.getString("id").equals(getId())) {    //must be the same document to overwrite
            throw new EventException("Json object did not match id: " + j.toString());
        }
        this.event = BaasDocument.from(j);
    }

    protected synchronized JsonObject toJson() {
        return this.event.toJson();
    }
    //endregion


    //region Getter and setter for event name
    protected synchronized void setName(String name) throws EventException {
        if (!hasPermission())
            throw new EventException("User does not have permission to edit event.");
        this.event.put(NAME_KEY, name);
    }

    protected synchronized String getName() {
        return this.event.getString(NAME_KEY);
    }
    //endregion


    //region Getter and setter for event description
    protected synchronized void setDescription(String description) throws EventException {
        if (!hasPermission())
            throw new EventException("User does not have permission to edit event.");
        this.event.put(DESCRIPTION_KEY, description);
    }

    protected synchronized String getDescription() {
        return this.event.getString(DESCRIPTION_KEY);
    }
    //endregion


    //region Methods to keep track of attendees
    protected void addUser() {
        int count = this.event.getInt(ATTENDEE_KEY, 0);
        count++;
        this.event.put(ATTENDEE_KEY, count);
    }

    protected void removeUser() {
        int count = this.event.getInt(ATTENDEE_KEY, 0);
        if (count == 0) {
            return;
        }
        count--;
        this.event.put(ATTENDEE_KEY, count);
    }
    //endregion


    //region Getter and setter for location
    protected synchronized void setLocation(String location) throws EventException {
        if (!hasPermission())
            throw new EventException("User does not have permission to edit event.");
        this.event.put(LOCATION_KEY, location);
    }

    protected synchronized String getLocation() {
        return this.event.getString(LOCATION_KEY);
    }
    //endregion


    //region Getter and setter for begin date
    protected synchronized void setBeginDate(Calendar startDate) throws EventException {
        if (!hasPermission())
            throw new EventException("User does not have permission to edit event.");
        SimpleDateFormat df = new SimpleDateFormat();
        String date = df.format(startDate.getTime());
        this.event.put(BEGIN_DATE_KEY, date);
    }

    protected synchronized Calendar getBeginDate() {
        String date = this.event.getString(BEGIN_DATE_KEY);
        SimpleDateFormat df = new SimpleDateFormat();
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(df.parse(date));
        } catch (ParseException p) {
            Log.d("EVENT", "****DATE PARSE FAIL: " + p.getMessage());
        }
        return c;
    }
    //endregion


    //region Getter and setter for end date
    protected synchronized void setEndDate(Calendar endDate) throws EventException {
        if (!hasPermission())
            throw new EventException("User does not have permission to edit event.");
        SimpleDateFormat df = new SimpleDateFormat();
        String date = df.format(endDate.getTime());
        this.event.put(END_DATE_KEY, date);
    }

    protected synchronized Calendar getEndDate() {
        String date = this.event.getString(END_DATE_KEY);
        SimpleDateFormat df = new SimpleDateFormat();
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(df.parse(date));
        } catch (ParseException p) {
            Log.d("EVENT", "****DATE PARSE FAIL: " + p.getMessage());
        }
        return c;
    }
    //endregion


    //region Getter for author
    protected synchronized String getCreator() {
        return event.getAuthor();
    }
    //endregion

    //region toString for date and time
    protected synchronized String dateToString() {
        /*
        String formatDate = "MM/dd/yyyy";
        String formatTime = "hh:mma";
        SimpleDateFormat sdfDate = new SimpleDateFormat(formatDate);
        SimpleDateFormat sdfTime = new SimpleDateFormat(formatTime);

        String beginDate = sdfDate.format(getBeginDate().getTime());
        String endDate = sdfDate.format(getEndDate().getTime());
        String beginTime = sdfTime.format(getBeginDate().getTime());
        String endTime = sdfTime.format(getEndDate().getTime());

        String s = "";

        //Event is on the same day
        if(beginDate.equals(endDate))
        {
            s += beginDate;
            s += " ";
            s += beginTime;
            s += "-";
            s += endTime;
        }

        else
        {
            s += beginDate;
            s += " ";
            s += beginTime;
            s += "-";
            s += endDate;
            s += " ";
            s += endTime;
        }
*/
        String formatDate = "MM/dd/yyyy hh:mma";
        SimpleDateFormat sdfDate = new SimpleDateFormat(formatDate);

        String s = sdfDate.format(getBeginDate().getTime());

        return s;
    }


    //region Methods for setting and getting parent groups
    private synchronized void setParentGroups(List<?> groupList) throws EventException {
        if (groupList.size() <= 0) throw new EventException("Parentless event not accepatable.");
        //Create new json array
        JsonArray ja = new JsonArray();
        //Convert groups to json array of id's
        for (Object x : groupList) {
            if (x instanceof Group2) {
                ja.add(((Group2) x).getId());
            } else if (x instanceof String) {
                ja.add(((String) x));
            } else {
                throw new EventException("Illegal group type in list.");
            }
        }
        //Put json array into event
        this.event.put(PARENT_GROUP_ARRAY_KEY, ja);
    }

    protected synchronized ArrayList<String> getParentGroups() {
        //Get json array from event doc
        JsonArray ja = this.event.getArray(PARENT_GROUP_ARRAY_KEY);
        //Convert to arraylist
        ArrayList<String> pgList = new ArrayList<String>();
        for (Object x : ja) {
            pgList.add(((String) x));
        }
        return pgList;
    }
    //endregion


    //region Methods for altering the admins of the event
    protected synchronized void addAdmin(String username) throws EventException {
        if (!hasPermission())
            throw new EventException("User does not have permission to edit event.");
        if (containsAdmin(username)) return;
        JsonArray ja = this.event.getArray(ADMIN_ARRAY_KEY);
        ja.add(username);
        this.event.put(ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized void removeAdmin(String username) throws EventException {
        if (!hasPermission())
            throw new EventException("User does not have permission to edit event.");
        if (username.equals(getCreator())) throw new EventException("Cannot remove the creator.");
        JsonArray ja = this.event.getArray(ADMIN_ARRAY_KEY);
        if (!(ja.contains(username))) {
            return;
        }
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            if (username.equals(ja.getString(i))) {
                ja.remove(i);
            }
        }
        this.event.put(ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized int getAdminCount() {
        return this.event.getArray(ADMIN_ARRAY_KEY).size();
    }

    protected synchronized boolean containsAdmin(String username) {
        return this.event.getArray(ADMIN_ARRAY_KEY).contains(username);
    }
    //endregion


    //region Methods for dealing with the server ID for a specific group
    //Method to check if the current group instance is on the server
    protected synchronized boolean isOnServer() {
        if (this.event != null && this.event.getId() != null) {
            return true;
        }
        return false;
    }

    protected synchronized String getId() {
        if (isOnServer()) {
            return this.event.getId();
        }
        return null;
    }
    //endregion


    //region Method to check if the current user has permission to edit this event
    protected synchronized boolean hasPermission() {
        return (containsAdmin(BaasUser.current().getName()));
    }
    //endregion


    //region Methods for checking equality
    protected synchronized boolean equals(Event other) {
        return equals(other.getId());
    }

    protected synchronized boolean equals(BaasDocument other) {
        if (!other.getCollection().equals(COLLECTION_NAME)) {
            return false;
        }
        return equals(other.getId());
    }

    protected synchronized boolean equals(String id) {
        try {
            return this.getId().equals(id);
        } catch (NullPointerException e) {
            return false;
        }
    }
    //endregion


    //region Nested class for exceptions with events
    protected class EventException extends RuntimeException {

        public EventException(String message) {
            super(message);
        }

    }
    //endregion

}
