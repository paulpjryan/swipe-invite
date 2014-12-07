package com.team16.swipeinvite;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasResult;
import com.baasbox.android.SaveMode;

import java.util.List;
import java.util.ListIterator;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class EventMoveService extends IntentService {
    private static final String LOG_TAG = "EventMoveService";

    public EventMoveService() {
        super("EventMoveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Got intent to move event.");
        //Get the model
        Model model = Model.getInstance(this);

        //Get info from intent
        String eventID = intent.getStringExtra("eventID");
        String movePlace = intent.getStringExtra("to");
        if (eventID == null || movePlace == null) {
            Log.d(LOG_TAG, "Incorrect intent format");
            return;
        }

        //Convert the move type for easy use
        int moveType = 0;
        if (movePlace.equals("waiting")) {
            moveType = 1;
        } else if (movePlace.equals("rejected")) {
            moveType = 2;
        }

        //Find the event to move
        List<Event> acceptedEvents = model.getAcceptedEvents();
        List<Event> waitingEvents = model.getWaitingEvents();
        List<Event> rejectedEvents = model.getRejectedEvents();
        int fromType = -1;
        Event e = null;
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
                    if (current.equals(eventID)) {
                        Log.d(LOG_TAG, "Found event: " + current.getName());
                        fromType = a;
                        //Decide what to do
                        if (fromType == moveType) {    //Places are the same
                            Log.d(LOG_TAG, "No move to be made.");
                            break;
                        } else if (fromType == 0 && moveType == 2) {    //From accepted to rejected
                            //Decrement counter and save
                            Event updateEvent = new Event(current.toJson());
                            updateEvent.removeUser();
                            BaasDocument d = saveToServer(updateEvent.getBaasDocument());
                            if (d == null) {
                                current.setBaasDocument(updateEvent.getBaasDocument());
                            } else {
                                current.setBaasDocument(d);
                                //Move the event
                                i.remove();
                                rejectedEvents.add(current);
                            }
                            break;
                        } else if ((fromType == 2 || fromType == 1) && (moveType == 0)) {    //From rejected or waiting to accepted
                            //Increment counter and save
                            Event updateEvent = new Event(current.toJson());
                            updateEvent.addUser();
                            BaasDocument d = saveToServer(updateEvent.getBaasDocument());
                            if (d == null) {
                                current.setBaasDocument(updateEvent.getBaasDocument());
                            } else {
                                current.setBaasDocument(d);
                                //Move the event
                                i.remove();
                                acceptedEvents.add(current);
                            }
                            break;
                        } else if (fromType == 1 && moveType == 2) {    //From waiting to rejected
                            //Move the event
                            i.remove();
                            rejectedEvents.add(current);
                            break;
                        } else {
                            Log.d(LOG_TAG, "Unknown case - FT: " + fromType + " ,MT: " + moveType);
                            return;
                        }
                    }
                }
            }
        }

        if (fromType != -1) {
            Log.d(LOG_TAG, "Moved event and saved.");
            Model.saveModel(this);
        }

    }
    private BaasDocument saveToServer(BaasDocument d) {
        BaasResult<BaasDocument> result = d.saveSync(SaveMode.IGNORE_VERSION);
        if (result.isFailed()) {
            Log.d(LOG_TAG, "Save failed.");
            return null;
        }
        return result.value();
    }


}
