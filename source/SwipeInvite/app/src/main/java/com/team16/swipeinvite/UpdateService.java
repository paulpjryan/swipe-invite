package com.team16.swipeinvite;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class UpdateService extends IntentService {
    private static final String LOG_TAG = "UpdateService";

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the local model object
        Model model = Model.getInstance(this);


        //region Group updating section
        BaasQuery queryG = BaasQuery.builder().where("privacy=true").build();
        //Fix query to include active public groups
        List<Group2> activeGroups = model.getActiveGroups();
        synchronized (activeGroups) {
            for (Group2 z : activeGroups) {
                if (!z.isPrivate()) {
                    Log.d(LOG_TAG, "Added public group to query: " + z.getName());
                    queryG = queryG.buildUpon().or("id='" + z.getId() + "'").build();
                }
            }
        }
        //Launch query
        BaasResult<List<BaasDocument>> groupResult = BaasDocument.fetchAllSync("group", queryG.buildUpon().criteria());
        if (groupResult.isFailed()) {
            Log.d(LOG_TAG, "Could not fetch groups.");
        } else {
            //For each group in the model, check to see if there is a group that was fetched
            List<BaasDocument> pulledGroups = groupResult.value();
            synchronized (activeGroups) {
                for (final ListIterator<Group2> i = activeGroups.listIterator(); i.hasNext(); ) {  //Setting up iterator
                    final Group2 current = i.next();    //need to get current group
                    //Check for a pulled down equivalent
                    boolean check = true;
                    for (final ListIterator<BaasDocument> j = pulledGroups.listIterator(); j.hasNext(); ) {
                        final BaasDocument currentD = j.next();
                        if (current.equals(currentD)) {
                            current.setBaasDocument(currentD);   //update the group object
                            j.remove();  //remove the group from the pull list
                            Log.d(LOG_TAG, "Updating group: " + current.getName());
                            check = false;
                            break;
                        }
                    }

                    //If no pulldown equivalent was found, the group must have been deleted
                    if (check) {
                        Log.d(LOG_TAG, "Removing group: " + current.getName());
                        i.remove(); //Remove the group from the active groups list
                        //****SEND NOTIFICATION TO THE USER****
                    }
                }
                //If there are any leftover pulldowns, they are new
                for (BaasDocument d : pulledGroups) {
                    Group2 addition = new Group2(d);
                    activeGroups.add(addition);
                    Log.d(LOG_TAG, "Adding group: " + addition.getName());
                    //***SEND NOTIFICATION TO THE USER***
                }
            }
        }
        //endregion


        //region Event updating section
        BaasResult<List<BaasDocument>> eventResult = BaasDocument.fetchAllSync("event");
        if (eventResult.isFailed()) {
            Log.d(LOG_TAG, "Could not fetch events.");
        } else {
            List<Event> acceptedEvents = model.getAcceptedEvents();
            List<Event> waitingEvents = model.getWaitingEvents();
            List<Event> rejectedEvents = model.getRejectedEvents();
            List<BaasDocument> pulledEvents = eventResult.value();
            //Check to see if all active events have pulldown equivalents
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
                        //Check for a pulled down equivalent
                        boolean check = true;
                        for (final ListIterator<BaasDocument> j = pulledEvents.listIterator(); j.hasNext(); ) {
                            final BaasDocument currentD = j.next();
                            if (current.equals(currentD)) {
                                current.setBaasDocument(currentD);   //update the group object
                                j.remove();  //remove the group from the pull list
                                check = false;
                                break;
                            }
                        }

                        //If no pulldown equivalent was found, the group must have been deleted
                        if (check) {
                            i.remove(); //Remove the group from the active groups list
                            //****SEND NOTIFICATION TO THE USER****
                        }
                    }
                }
            }
            //Any leftover pulled events are new
            for (BaasDocument d : pulledEvents) {
                Event addition = new Event(d);
                waitingEvents.add(addition);
                //***NOTIFY USER OF ADDITON***
            }

        }
        //endregion


        //TODO Check for outdated events

        //TODO Pulldown friends from groups

        //Save the model
        model.saveModel(this);

    }

}