package com.team16.swipeinvite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonArray;
import com.baasbox.android.json.JsonObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by kylekrynski on 10/23/14.
 */
class Model {
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
    private ArrayList<Acquaintance> friends;   //List of users that the user has invited or been in a group with
    protected CurrentUser currentUser;    //The current user object
    //endregion


    //region Instance variable to store the a server model instance (primarily for its ID)
    private BaasDocument model;
    //endregion


    //region Load/Save Model functions
    protected static synchronized void saveModel(Context context) {
        if (BaasUser.current() == null) {    //Need to make sure there is a valid user
            Log.d(LOG_TAG, "No current user, cannot save model.");
            return;
        }
        //Save model locally
        Log.d(LOG_TAG, "Send intent to save local object.");
        Intent intent = new Intent(context, LocalSaveService.class);
        context.startService(intent);
        //Save model to server
        Log.d(LOG_TAG, "Send intent to save model to server.");
        Intent intent2 = new Intent(context, ServerSaveService.class);
        context.startService(intent2);
        //Notify the observers
        theModel.notifyObservers();
    }

    private static void loadModel(Context context) {
        //Check for current user
        if (BaasUser.current() == null) {    //Need to make sure there is a valid user
            Log.d(LOG_TAG, "No current user, cannot load model.");
            return;
        }

        //Get the application
        StartUp application = (StartUp) context.getApplicationContext();

        //Open the file
        FileInputStream fis;
        try {
            fis = context.openFileInput(BaasUser.current().getName());
        } catch (FileNotFoundException f) {
            Log.d(LOG_TAG, "No such file: " + f.getMessage());
            return;
        }
        //Figure out how many bytes are in the file
        int size = 0;
        try {
            while (fis.read() != -1) {
                size++;
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error counting file: " + e.getMessage());
            return;
        }
        //Close the file
        try {
            fis.close();
        } catch (IOException i) {
            Log.d(LOG_TAG, "Error closing file: " + i.getMessage());
            return;
        }

        //Open the file again
        try {
            fis = context.openFileInput(BaasUser.current().getName());
        } catch (FileNotFoundException f) {
            Log.d(LOG_TAG, "Error opening file again: " + f.getMessage());
            return;
        }

        //Read the bytes into an array
        byte[] bytes = new byte[size];
        try {
            int n = fis.read(bytes, 0, bytes.length);
            if (n != bytes.length) {
                Log.d(LOG_TAG, "Read incorrect number of bytes.");
                return;
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error reading all bytes: " + e.getMessage());
        }

        //Convert the byte array to a string
        String total = new String(bytes, Charset.defaultCharset());
        Log.d(LOG_TAG, "Got total: " + total);

        //Parse the string into subsets
        String serverModelString = "";
        String activeGroupString = "";
        String acceptedEventsString = "";
        String waitingEventsString = "";
        String rejectedEventsString = "";
        int delim2pos = total.indexOf(application.getDelim2());
        int n = 0;
        for (int i = 0; i < 5; i++) {
            Log.d(LOG_TAG, "Getting substrings for model lists");
            switch (i) {
                case 0:
                    serverModelString = total.substring(n, delim2pos);
                    break;
                case 1:
                    activeGroupString = total.substring(n, delim2pos);
                    break;
                case 2:
                    acceptedEventsString = total.substring(n, delim2pos);
                    break;
                case 3:
                    waitingEventsString = total.substring(n, delim2pos);
                    break;
                case 4:
                    rejectedEventsString = total.substring(n, delim2pos);
                    break;
            }
            n = delim2pos + application.getDelim2().length();
            delim2pos = total.indexOf(application.getDelim2(), n);
        }

        //Decode the server model string
        Log.d(LOG_TAG, "Decoding server model.");
        theModel.setServerVersion(BaasDocument.from(JsonObject.decode(serverModelString)));

        //Decode the active group string
        Log.d(LOG_TAG, "Decoding active groups.");
        int delim1pos = activeGroupString.indexOf(application.getDelim1());
        int g = 0;
        while (g < activeGroupString.length()) {
            String group = activeGroupString.substring(g, delim1pos);
            theModel.activeGroups.add(new Group2(JsonObject.decode(group)));
            g = delim1pos + application.getDelim1().length();
            delim1pos = activeGroupString.indexOf(application.getDelim1(), g);
        }

        //Decode the accepted events string
        Log.d(LOG_TAG, "Decoding accepted events.");
        delim1pos = acceptedEventsString.indexOf(application.getDelim1());
        g = 0;
        while (g < acceptedEventsString.length()) {
            String group = acceptedEventsString.substring(g, delim1pos);
            theModel.acceptedEvents.add(new Event(JsonObject.decode(group)));
            g = delim1pos + application.getDelim1().length();
            delim1pos = acceptedEventsString.indexOf(application.getDelim1(), g);
        }

        //Decode the waiting events string
        Log.d(LOG_TAG, "Decoding waiting events.");
        delim1pos = waitingEventsString.indexOf(application.getDelim1());
        g = 0;
        while (g < waitingEventsString.length()) {
            String group = waitingEventsString.substring(g, delim1pos);
            theModel.waitingEvents.add(new Event(JsonObject.decode(group)));
            g = delim1pos + application.getDelim1().length();
            delim1pos = waitingEventsString.indexOf(application.getDelim1(), g);
        }

        //Decode the rejected events string
        Log.d(LOG_TAG, "Decoding rejected events.");
        delim1pos = rejectedEventsString.indexOf(application.getDelim1());
        g = 0;
        while (g < rejectedEventsString.length()) {
            String group = rejectedEventsString.substring(g, delim1pos);
            theModel.rejectedEvents.add(new Event(JsonObject.decode(group)));
            g = delim1pos + application.getDelim1().length();
            delim1pos = rejectedEventsString.indexOf(application.getDelim1(), g);
        }

        Log.d(LOG_TAG, "Model repopulated.");

        return;
    }
    //endregion


    //region Old load and save and getInstance
    private void placeHolder() {
        /*
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
        try {
            model.activeGroups.get(0).getUserList();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Model not working: " + e.getMessage());
            Toast.makeText(context, "Data corrupted, please contact admin.", Toast.LENGTH_LONG).show();
            return model;
        }

        Log.d(LOG_TAG, "Loaded model for user: " + BaasUser.current().getName()); */

        /*
        if (theModel == null) {
            Log.d(LOG_TAG, "The singeton model object was null.");
            theModel = Model.loadModel(context);
            Log.d(LOG_TAG, "The singeton model repopulated from shared prefs.");
            if (theModel == null) {
                Log.d(LOG_TAG, "Loading from shared prefs failed, creating new model instance");
                theModel = new Model();
            }
        }
        Log.d(LOG_TAG, "Giving model to something."); */
        /*
        //context = context.getApplicationContext();
        Gson gson = new Gson();
        String gsonString;
        //Remove the observers from the model while saving
        ArrayList<Observer> o = new ArrayList<Observer>();
        o.addAll(theModel.getObservers());
        theModel.deleteObservers();
        try {
            gsonString = gson.toJson(theModel);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Caught something weird: " + e.getMessage());
            Toast.makeText(context, "Data corrupted, please contact admin.", Toast.LENGTH_LONG).show();
            return;
        }
        //Put the observers back
        theModel.setObservers(o);
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
        theModel.notifyObservers(); */
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
        friends = new ArrayList<Acquaintance>();
        currentUser = new CurrentUser(BaasUser.current());
    }
    //endregion


    //region Singleton methods and motifs
    private static Model theModel;

    protected static synchronized Model getInstance(Context context) {
        //Check to make sure not handing off null model
        if (theModel == null) {
            Log.d(LOG_TAG, "Singleton instance was null, creating new.");
            theModel = new Model();

            //Try to load the model
            Log.d(LOG_TAG, "Started loading model.");
            loadModel(context);
        }

        if ((context instanceof Activity) && !((context instanceof LoginActivity2) || (context instanceof NewUserLoginActivity))) {
            //Call the update service at very specific times
            Log.d(LOG_TAG, "Sending intent to update data.");
            Intent intent = new Intent(context, UpdateService.class);
            context.startService(intent);
        }

        Log.d(LOG_TAG, "Model given.");
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


    //region Methods to deal with observers
    private ArrayList<Observer> observers = new ArrayList<Observer>();

    protected synchronized void addObserver(Observer o) {
        Log.d(LOG_TAG, "Added observer to model.");
        this.observers.add(o);
    }

    protected synchronized void deleteObserver(Observer o) {
        Log.d(LOG_TAG, "Removed observer from model.");
        this.observers.remove(o);
    }

    private synchronized void notifyObservers() {
        for (Observer x : observers) {
            try {
                x.update(new Observable(), new Object());
            } catch (Exception e) {
                Log.d(LOG_TAG, "Caught notify observer problem: " + e.getMessage());
            }
        }
    }

    private static ArrayList<Observer> getObservers() {
        return theModel.observers;
    }

    private void deleteObservers() {
        this.observers = null;
    }

    private void setObservers(ArrayList<Observer> o) {
        this.observers = o;
    }
    //endregion


    //region Methods for conversion of a local model object to a server model object
    protected synchronized BaasDocument toServerVersion() {
        if (model == null) {
            model = new BaasDocument(COLLECTION_NAME);
        }
        model.put(ACTIVE_GROUPS_KEY, getJAofGroups(getActiveGroups()));
        model.put(ACCEPTED_EVENTS_KEY, getJAofEvents(getAcceptedEvents()));
        model.put(WAITING_EVENTS_KEY, getJAofEvents(getWaitingEvents()));
        model.put(REJECTED_EVENTS_KEY, getJAofEvents(getRejectedEvents()));
        model.put(ACQUAINTENCE_KEY, getJAofFriends(getFriends()));
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

    private static JsonArray getJAofEvents(List<Event> e) {
        JsonArray ja = new JsonArray();
        synchronized (e) {
            for (Event x : e) {
                ja.add(x.getId());
            }
        }
        return ja;
    }

    private static JsonArray getJAofFriends(List<Acquaintance> a) {
        JsonArray ja = new JsonArray();
        synchronized (a) {
            for (Acquaintance x : a) {
                ja.add(x.getUsername());
            }
        }
        return ja;
    }
    //endregion


    //region Methods for setting and getting the raw server model object  -- LOGIN ONLY
    //ONLY USE THESE METHODS AT LOGIN
    protected synchronized void setServerVersion(BaasDocument d) {
        if (!d.getCollection().equals(COLLECTION_NAME))
            throw new ModelException("Document is not a model: " + d.toString());
        model = d;
        extractIDs();
    }

    //region Method and instance variable used to hold a list of IDS for retrieval  -- LOGIN ONLY
    private ArrayList<ArrayList<String>> idList;

    protected synchronized ArrayList<ArrayList<String>> getIdList() {
        return idList;
    }

    protected synchronized boolean idListEmpty() {
        if (idList == null) return true;
        for (int i = 0; i < (idList.size()); i++) {
            if (!idList.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    protected synchronized boolean dataIsEmpty() {
        if (idList == null) return true;
        for (int i = 0; i < (idList.size() - 1); i++) {     //account for only document id's
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

    protected synchronized BaasDocument getServerVersion() {
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
    protected synchronized List<Acquaintance> getFriends() {
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
