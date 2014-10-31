package com.team16.swipeinvite;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonArray;
import com.baasbox.android.json.JsonObject;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by kylekrynski on 10/23/14.
 */
class Model implements Parcelable {
    private static final String LOG_TAG = "MODEL";

    //region Constants to wrap the model data into a BaasDocument for later pulldown
    private static final String COLLECTION_NAME = "model";
    private static final String ACTIVE_GROUPS_KEY = "active_groups";
    private static final String ACCEPTED_EVENTS_KEY = "accepted_events";
    private static final String WAITING_EVENTS_KEY = "waiting_events";
    private static final String REJECTED_EVENTS_KEY = "rejected_events";
    private static final String ACQUAINTENCE_KEY = "acquaintences";
    //endregion


    //region The arrays representing all data for a logged in user
    //All data can be manipulated from classes in the app, this is just use to encapsulate
    protected ArrayList<Group2> activeGroups;    //List of groups that the user is either creator of or has accepted an invite to
    protected ArrayList<Event> acceptedEvents;   //List of events that the user has accepted
    protected ArrayList<Event> waitingEvents;    //List of events that the user has waited for a decision on
    protected ArrayList<Event> rejectedEvents;   //List of events that the user has rejected
    protected ArrayList<Acquaintence> friends;   //List of users that the user has invited or been in a group with
    protected CurrentUser currentUser;    //The current user object
    private ArrayList<BaasDocument> modelList;    //This is just for easy passing of model object
    //endregion


    //region Instance variable to store the a server model instance (primarily for its ID)
    private BaasDocument model;
    //endregion


    //region Load/Save Model functions
    void saveModel()
    {
        Gson gson = new Gson();
        String gsonString = gson.toJson(this);
        SharedPreferences sprefs = StartUp.getAppContext()
                .getSharedPreferences(BaasUser.current().getName(), Context.MODE_PRIVATE);
        sprefs.edit().putString("model", gsonString);
        sprefs.edit().commit();
    }

    static void loadModel( Model model )
    {
        Gson gson = new Gson();
        SharedPreferences sprefs = StartUp.getAppContext()
                .getSharedPreferences(BaasUser.current().getName(), Context.MODE_PRIVATE);
        String gsonString = sprefs.getString("model", null);
        model = gson.fromJson(gsonString, Model.class);
    }
    //endregion

    //region Methods for Parcelable interface
    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(activeGroups);
        out.writeTypedList(acceptedEvents);
        out.writeTypedList(waitingEvents);
        out.writeTypedList(rejectedEvents);
        out.writeTypedList(friends);
        //out.writeValue(currentUser);
        modelList = new ArrayList<BaasDocument>();
        modelList.add(model);
        out.writeTypedList(modelList);
        //out.writeParcelable(model, flags);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Model> CREATOR
            = new Parcelable.Creator<Model>() {
        public Model createFromParcel(Parcel in) {
            return new Model(in);
        }

        public Model[] newArray(int size) {
            return new Model[size];
        }
    };

    private Model(Parcel in) {   //Have to check for null object references
        activeGroups = new ArrayList<Group2>();
        acceptedEvents = new ArrayList<Event>();
        waitingEvents = new ArrayList<Event>();
        rejectedEvents = new ArrayList<Event>();
        friends = new ArrayList<Acquaintence>();
        modelList = new ArrayList<BaasDocument>();
        try {
            in.readTypedList(activeGroups, Group2.CREATOR);
        } catch (NullPointerException e) {
            activeGroups = new ArrayList<Group2>();
            Log.d(LOG_TAG, "New active group array created.");
        }
        try {
            in.readTypedList(acceptedEvents, Event.CREATOR);
        } catch (NullPointerException e) {
            acceptedEvents = new ArrayList<Event>();
            Log.d(LOG_TAG, "New accepted event array created.");
        }
        try {
            in.readTypedList(waitingEvents, Event.CREATOR);
        } catch (NullPointerException e) {
            waitingEvents = new ArrayList<Event>();
            Log.d(LOG_TAG, "New waiting event array created.");
        }
        try {
            in.readTypedList(rejectedEvents, Event.CREATOR);
        } catch (NullPointerException e) {
            rejectedEvents = new ArrayList<Event>();
            Log.d(LOG_TAG, "New rejected event array created.");
        }
        try {
            in.readTypedList(friends, Acquaintence.CREATOR);
        } catch (NullPointerException e) {
            friends = new ArrayList<Acquaintence>();
            Log.d(LOG_TAG, "New friend array created.");
        }
        try {
            in.readTypedList(modelList, BaasDocument.CREATOR);
        } catch (NullPointerException e) {
            modelList = new ArrayList<BaasDocument>();
            Log.d(LOG_TAG, "New model created, this is bad.");
        }
        currentUser = new CurrentUser(BaasUser.current());
        model = modelList.get(0);
    }

    //endregion


    //region Constructor methods
    //Constructor for a model object for a user logging in, under any circumstance
    protected Model() throws ModelException {
        if (BaasUser.current() == null) throw new ModelException("No logged in user.");
        activeGroups = new ArrayList<Group2>();
        acceptedEvents = new ArrayList<Event>();
        waitingEvents = new ArrayList<Event>();
        rejectedEvents = new ArrayList<Event>();
        friends = new ArrayList<Acquaintence>();
        currentUser = new CurrentUser(BaasUser.current());
    }
    //endregion


    //region Methods for conversion of a local model object to a server model object
    protected BaasDocument toServerVersion() {
        if (model == null) {
            model = new BaasDocument(COLLECTION_NAME);
        }
        model.put(ACTIVE_GROUPS_KEY, getJAofGroups(activeGroups));
        model.put(ACCEPTED_EVENTS_KEY, getJAofEvents(acceptedEvents));
        model.put(WAITING_EVENTS_KEY, getJAofEvents(waitingEvents));
        model.put(REJECTED_EVENTS_KEY, getJAofEvents(rejectedEvents));
        model.put(ACQUAINTENCE_KEY, getJAofFriends(friends));
        return model;
    }

    private static JsonArray getJAofGroups(ArrayList<Group2> g) {
        JsonArray ja = new JsonArray();
        for (Group2 x : g) {
            ja.add(x.getId());
        }
        return ja;
    }

    private static JsonArray getJAofEvents(ArrayList<Event> e) {
        JsonArray ja = new JsonArray();
        for (Event x : e) {
            ja.add(x.getId());
        }
        return ja;
    }

    private static JsonArray getJAofFriends(ArrayList<Acquaintence> a) {
        JsonArray ja = new JsonArray();
        for (Acquaintence x : a) {
            ja.add(x.getUsername());
        }
        return ja;
    }
    //endregion


    //region Methods for setting and getting the raw server model object  -- LOGIN ONLY
    //ONLY USE THESE METHODS AT LOGIN
    protected void setServerVersion(BaasDocument d) {
        if(!d.getCollection().equals(COLLECTION_NAME)) throw new ModelException("Document is not a model: " + d.toString());
        model = d;
        extractIDs();
    }

    //region Method and instance variable used to hold a list of IDS for retrieval  -- LOGIN ONLY
    private ArrayList<ArrayList<String>> idList;
    protected ArrayList<ArrayList<String>> getIdList() {
        return idList;
    }
    protected boolean idListEmpty() {
        if (idList == null) return true;
        for (int i = 0; i < (idList.size()); i++) {
            if (!idList.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    protected boolean dataIsEmpty() {
        if (idList == null) return true;
        for (int i = 0; i < (idList.size()-1); i++) {     //account for only document id's
            if (!idList.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    //should only be used upon login and setting of a server version
    private void extractIDs() {
        idList = new ArrayList<ArrayList<String>>(5);
        ArrayList<String> grps = new ArrayList<String>();
        for (Object x : model.getArray(ACTIVE_GROUPS_KEY)) {
            grps.add((String) x);
        }
        ArrayList<String> accpdEvents = new ArrayList<String>();
        for (Object x : model.getArray(ACCEPTED_EVENTS_KEY)) {
            accpdEvents.add((String) x);
        }
        ArrayList<String> wtgEvents = new ArrayList<String>();
        for (Object x : model.getArray(WAITING_EVENTS_KEY)) {
            wtgEvents.add((String) x);
        }
        ArrayList<String> rjdEvents = new ArrayList<String>();
        for (Object x : model.getArray(REJECTED_EVENTS_KEY)) {
            rjdEvents.add((String) x);
        }
        ArrayList<String> frnds = new ArrayList<String>();
        for (Object x : model.getArray(ACQUAINTENCE_KEY)) {
            frnds.add((String) x);
        }
        idList.add(0, grps);
        idList.add(1, accpdEvents);
        idList.add(2, wtgEvents);
        idList.add(3, rjdEvents);
        idList.add(4, frnds);
    }
    //endregion

    protected BaasDocument getServerVersion() {
        return model;
    }
    //endregion


    //region Nested class for model exceptions
    protected class ModelException extends RuntimeException {
        public ModelException(String message) {
            super(message);
        }
    }
    //endregion

}
