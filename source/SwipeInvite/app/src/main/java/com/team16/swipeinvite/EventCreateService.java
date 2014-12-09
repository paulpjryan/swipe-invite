package com.team16.swipeinvite;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasResult;
import com.baasbox.android.Grant;
import com.baasbox.android.SaveMode;
import com.baasbox.android.json.JsonObject;

import java.util.ArrayList;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class EventCreateService extends IntentService {
    private static final String LOG_TAG = "EventCreateService";

    public EventCreateService() {
        super("EventCreateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the Model
        Model model = Model.getInstance(this);

        //Get info from intent
        String eventID = intent.getStringExtra("event");
        ArrayList<String> groups = intent.getStringArrayListExtra("parentGroups");

        //Get the event
        Event e = null;
        List<Event> acceptedEvents = model.getAcceptedEvents();
        synchronized (acceptedEvents) {
            for (Event k : acceptedEvents) {
                if (k.equals(eventID)) {
                    e = k;
                }
            }
        }
        if (e == null) {
            Log.d(LOG_TAG, "Error: event no longer exists after creation.");
            return;
        }

        //Add the event to each group and save the group
        ArrayList<String> userList = new ArrayList<String>();
        List<Group2> activeGroups = model.getActiveGroups();
        synchronized (activeGroups) {
            for (Group2 x : activeGroups) {
                for (String y : groups) {
                    if (x.equals(y)) {    //If the group is a parent
                        x.addEvent(eventID);    //add the event to it
                        BaasResult<BaasDocument> result = x.getBaasDocument().saveSync(SaveMode.IGNORE_VERSION);   //save it
                        if (result.isFailed()) {
                            Log.d(LOG_TAG, "Failed to save group: " + x.getName());
                            break; //if failed save stop here
                        }
                        Log.d(LOG_TAG, "Saved group with event: " + x.getName());
                        x.setBaasDocument(result.value());   //set the new baas doc
                        userList.addAll(x.getUserList());    //add to user list
                        groups.remove(y);    //make group list smaller for efficiency
                        break;
                    }
                }
            }
        }

        //Send the grants to users
        for (String username : userList) {
            BaasResult<Void> result = e.getBaasDocument().grantSync(Grant.ALL, username);
            if (result.isFailed()) {
                Log.d(LOG_TAG, "Grant frailed for: " + username);
            }
            Log.d(LOG_TAG, "Granted: " + username);
        }

        //Send the push notifications to members
        Log.d(LOG_TAG, "Sending push.");
        //Create the json object
        JsonObject message = new JsonObject();
        message.put("type", "event");
        message.put("id", eventID);
        String notificationMessage = "You have been invited to an event.";

        //Start the intent service to send the push
        Intent intent2 = new Intent(this, PushSender.class);
        intent2.putStringArrayListExtra("users", userList);
        intent2.putExtra("message", message);
        intent2.putExtra("notification", notificationMessage);
        startService(intent2);

    }

}
