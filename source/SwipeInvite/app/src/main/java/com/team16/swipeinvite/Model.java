package com.team16.swipeinvite;

import android.app.Activity;
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
import java.util.Collections;
import java.util.List;
import java.util.Observable;

/**
 * Created by kylekrynski on 10/23/14.
 */
class Model extends Observable {
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
    private ArrayList<Group2> activeGroups;    //List of groups that the user is either creator of or has accepted an invite to
    private ArrayList<Event> acceptedEvents;   //List of events that the user has accepted
    private ArrayList<Event> waitingEvents;    //List of events that the user has waited for a decision on
    private ArrayList<Event> rejectedEvents;   //List of events that the user has rejected
    private ArrayList<Acquaintence> friends;   //List of users that the user has invited or been in a group with
    protected CurrentUser currentUser;    //The current user object
    private ArrayList<BaasDocument> modelList;    //This is just for easy passing of model object
    //endregion


    //region Instance variable to store the a server model instance (primarily for its ID)
    private BaasDocument model;
    //endregion


    //region Load/Save Model functions
    protected static synchronized void saveModel(Context context)
    {
        if (BaasUser.current() == null) {    //Need to make sure there is a valid user
            Log.d(LOG_TAG, "No current user, cannot save model.");
            return;
        }
        //context = context.getApplicationContext();
        Gson gson = new Gson();
        String gsonString = gson.toJson(theModel);
        Log.d(LOG_TAG, "Model saved: " + gsonString);
        //String gsonString = gson.toJson(this);
        //Log.d(LOG_TAG, "Context: " + context.toString());
        SharedPreferences sprefs = context.getApplicationContext().getSharedPreferences(BaasUser.current().getName(), Context.MODE_PRIVATE);
        Log.d(LOG_TAG, "Shared Prefs: " + sprefs.toString());
        SharedPreferences.Editor sprefEdit = sprefs.edit();
        sprefEdit.clear();
        sprefEdit.putString("model", gsonString);
        sprefEdit.apply();
        Log.d(LOG_TAG, "Saved model for user: " + BaasUser.current().getName());
        //Notify the observers
        theModel.setChanged();
        theModel.notifyObservers();
        //Save model to server
    }

    private static Model loadModel(Context context)
    {
        if (BaasUser.current() == null) {    //Need to make sure there is a valid user
            Log.d(LOG_TAG, "No current user, cannot load model.");
            return null;
        }
        //context = context.getApplicationContext();
        Gson gson = new Gson();
        //Log.d(LOG_TAG, "Context: " + context.toString());
        SharedPreferences sprefs = context.getApplicationContext().getSharedPreferences(BaasUser.current().getName(), Context.MODE_PRIVATE);
        Log.d(LOG_TAG, "Shared Prefs: " + sprefs.toString());
        String gsonString = sprefs.getString("model", null);
        if (gsonString == null) {
            Log.d(LOG_TAG, "No local data for current user: " + BaasUser.current().getName());
            return null;
        }
        Log.d(LOG_TAG, "Model loaded: " + gsonString);
        Model model = gson.fromJson(gsonString, Model.class);
        Log.d(LOG_TAG, "Loaded model for user: " + BaasUser.current().getName());
        return model;
    }
    //endregion


    //region Constructor methods
    //Constructor for a model object for a user logging in, under any circumstance
    private Model() throws ModelException {
        if (BaasUser.current() == null) throw new ModelException("No logged in user.");
        activeGroups = new ArrayList<Group2>();
        acceptedEvents = new ArrayList<Event>();
        waitingEvents = new ArrayList<Event>();
        rejectedEvents = new ArrayList<Event>();
        friends = new ArrayList<Acquaintence>();
        currentUser = new CurrentUser(BaasUser.current());
    }
    //endregion


    //region Singleton methods and motifs
    private static Model theModel;
    protected static synchronized Model getInstance(Context context) {
        if (theModel == null) {
            Log.d(LOG_TAG, "The singeton model object was null.");
            theModel = Model.loadModel(context);
            Log.d(LOG_TAG, "The singeton model repopulated from shared prefs.");
            if (theModel == null) {
                Log.d(LOG_TAG, "Loading from shared prefs failed, creating new model instance");
                theModel = new Model();
            }
        }
        return theModel;
    }
    protected static void dumpInstance() {
        theModel = null;
    }
    protected static Model resetInstance() {
        theModel = new Model();
        return theModel;
    }
    //endregion


    //region Methods for conversion of a local model object to a server model object
    protected BaasDocument toServerVersion() {
        if (model == null) {
            model = new BaasDocument(COLLECTION_NAME);
        }
        model.put(ACTIVE_GROUPS_KEY, getJAofGroups(getActiveGroups()));
        model.put(ACCEPTED_EVENTS_KEY, getJAofEvents(acceptedEvents));
        model.put(WAITING_EVENTS_KEY, getJAofEvents(waitingEvents));
        model.put(REJECTED_EVENTS_KEY, getJAofEvents(rejectedEvents));
        model.put(ACQUAINTENCE_KEY, getJAofFriends(friends));
        return model;
    }

    private static JsonArray getJAofGroups(List<Group2> g) {
        JsonArray ja = new JsonArray();
        synchronized (g) {
            for (Group2 x : g) {
                ja.add(x.getId());
            }
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


    //region Methods for synchronized access to the active groups list
    protected synchronized List<Group2> getActiveGroups() {
        return Collections.synchronizedList(activeGroups);
    }
    //endregion


    //region Methods for synchronized access to the event lists
    protected synchronized List<Event> getAcceptedEvents() {
        return Collections.synchronizedList(acceptedEvents);
    }
    protected synchronized List<Event> getWaitingEvents() {
        return Collections.synchronizedList(waitingEvents);
    }
    protected synchronized List<Event> getRejectedEvents() {
        return Collections.synchronizedList(rejectedEvents);
    }
    //endregion


    //region Methods for synchronized access to the active groups list
    protected synchronized List<Acquaintence> getFriends() {
        return Collections.synchronizedList(friends);
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
