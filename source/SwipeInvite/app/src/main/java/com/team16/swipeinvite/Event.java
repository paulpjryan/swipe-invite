package com.team16.swipeinvite;


import android.os.Parcel;
import android.os.Parcelable;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonArray;
import com.baasbox.android.json.JsonObject;

import java.util.Date;

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
    protected Event(String name, String description, String location, String begdate, String enddate) {
        this.event = new BaasDocument(COLLECTION_NAME);
        initializeAdminArray();
        this.event.put(NAME_KEY, name);
        this.event.put(DESCRIPTION_KEY, description);
        this.event.put(LOCATION_KEY, location);
        this.event.put(BEGIN_DATE_KEY, begdate);
        this.event.put(END_DATE_KEY, enddate);
    }

    //Create a brand new group object from an existing BaasDocument
    protected Event(BaasDocument d) throws EventException {
        setBaasDocument(d);
    }

    //Create a brand new group object from a Json object
    protected Event(JsonObject j) throws EventException  {
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
        if (!hasPermission()) throw new EventException("User does not have permission to edit event.");
        this.event.put(NAME_KEY, name);
    }

    protected synchronized String getName() {
        return this.event.getString(NAME_KEY);
    }
    //endregion


    //region Getter and setter for event description
    protected synchronized void setDescription(String description) throws EventException {
        if (!hasPermission()) throw new EventException("User does not have permission to edit event.");
        this.event.put(DESCRIPTION_KEY, description);
    }

    protected synchronized String getDescription() {
        return this.event.getString(DESCRIPTION_KEY);
    }
    //endregion


    //region Getter and setter for location
    protected synchronized void setLocation(String location) throws EventException {
        if (!hasPermission()) throw new EventException("User does not have permission to edit event.");
        this.event.put(LOCATION_KEY, location);
    }

    protected synchronized String getLocation() {
        return this.event.getString(LOCATION_KEY);
    }
    //endregion


    //region Getter and setter for begin date
    protected synchronized void setBeginDate(String date) throws EventException {
        if (!hasPermission()) throw new EventException("User does not have permission to edit event.");
        this.event.put(BEGIN_DATE_KEY, date);
    }

    protected synchronized String getBeginDate() {
        return this.event.getString(BEGIN_DATE_KEY);
    }
    //endregion


    //region Getter and setter for end date
    protected synchronized void setEndDate(String date) throws EventException {
        if (!hasPermission()) throw new EventException("User does not have permission to edit event.");
        this.event.put(END_DATE_KEY, date);
    }

    protected synchronized String getEndDate() {
        return this.event.getString(END_DATE_KEY);
    }
    //endregion


    //region Getter for author
    protected synchronized String getCreator() {
        return event.getAuthor();
    }
    //endregion


    //region Methods for altering the admins of the event
    protected synchronized void addAdmin(String username) throws EventException {
        if (!hasPermission()) throw new EventException("User does not have permission to edit event.");
        if (containsAdmin(username)) return;
        JsonArray ja = this.event.getArray(ADMIN_ARRAY_KEY);
        ja.add(username);
        this.event.put(ADMIN_ARRAY_KEY, ja);
    }

    protected synchronized void removeAdmin(String username) throws EventException {
        if (!hasPermission()) throw new EventException("User does not have permission to edit event.");
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
