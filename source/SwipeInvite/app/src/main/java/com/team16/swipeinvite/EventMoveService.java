package com.team16.swipeinvite;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasResult;
import com.baasbox.android.SaveMode;

import java.util.List;


/**
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class EventMoveService extends IntentService {
    private static final String LOG_TAG = "EventMoveService";

    public static final String EVENT_ID = "EVENT_ID";
    public static final String EVENT_DESTINATION = "EVENT_DESTINATION";

    public static final int EVENT_ACCEPTED = 0;
    public static final int EVENT_PENDING = 1;
    public static final int EVENT_REJECTED = 2;

    public EventMoveService() {
        super("EventMoveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Got intent to move event.");
        //Get the model
        Model model = Model.getInstance(this);

        //Get info from intent
        String eventID = intent.getStringExtra(EVENT_ID);
        int destination = intent.getIntExtra(EVENT_DESTINATION, -1);
        if (eventID == null || destination == -1) {
            Log.d(LOG_TAG, "Incorrect intent format");
            return;
        }

        // Find the event to move
        List<Event>[] EventLists = new List[3];
        EventLists[EVENT_ACCEPTED] = model.getAcceptedEvents();
        EventLists[EVENT_PENDING] = model.getWaitingEvents();
        EventLists[EVENT_REJECTED] = model.getRejectedEvents();

        // Find what list the event is in
        int from = -1;
        Event eventToMove = null;
        outerLoop: // dirty hack
        for (int a = 0; a < 3; a++) {
            for (Event current : EventLists[a]) {
                if (current.equals(eventID)) {
                    from = a;
                    eventToMove = current;
                    break outerLoop;
                }
            }
        }

        // Decide what to do
        if (from == destination) //Places are the same
            Log.d(LOG_TAG, "No move to be made.");
        else if (destination == EVENT_PENDING)
            Log.d(LOG_TAG, "Cannot move event back to pending");
        else {
            //Decrement counter and save
            Event updateEvent = new Event(eventToMove.toJson());
            updateEvent.removeUser();
            BaasDocument d = saveToServer(updateEvent.getBaasDocument());
            if (d == null) {
                eventToMove.setBaasDocument(updateEvent.getBaasDocument());
            } else {
                eventToMove.setBaasDocument(d);
                //Move the event
                EventLists[from].remove(eventToMove);
                EventLists[destination].add(eventToMove);
            }
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
