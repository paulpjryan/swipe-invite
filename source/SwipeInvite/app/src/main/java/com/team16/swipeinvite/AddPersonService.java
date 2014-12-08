package com.team16.swipeinvite;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasResult;
import com.baasbox.android.Grant;
import com.baasbox.android.SaveMode;
import com.baasbox.android.json.JsonObject;

import java.util.List;
import java.util.ListIterator;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class AddPersonService extends IntentService {
    private static final String LOG_TAG = "ADD_PERSON_SERVICE";

    public AddPersonService() {
        super("AddPersonService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the model
        Model model = Model.getInstance(this);

        //Get intent info
        String groupID = intent.getStringExtra("groupID");
        String username = intent.getStringExtra("username");
        if (groupID == null || username == null) {
            Log.d(LOG_TAG, "Intent not correct");
            return;
        }

        //Get the current group to edit
        List<Group2> lG = model.getActiveGroups();
        Group2 g = null;
        synchronized (lG) {
            for (Group2 x : lG) {
                if (x.equals(groupID)) {
                    g = x;
                }
            }
        }
        if (g == null) {
            Log.d(LOG_TAG, "Could not find group");
            return;
        }

        //Update and save the group
        Group2 updateGroup = new Group2(g.toJson());
        updateGroup.addUser(username);
        BaasResult<BaasDocument> result = updateGroup.getBaasDocument().saveSync(SaveMode.IGNORE_VERSION);
        if (result.isFailed()) {
            Log.d(LOG_TAG, "Could not save group: " + result.error());
            return;
        }
        g.setBaasDocument(result.value());

        //Save the model
        Model.saveModel(this);

        //Send push notification
        Log.d(LOG_TAG, "Sending push.");
        //Create the json object
        JsonObject message = new JsonObject();
        message.put("type", "group");
        message.put("id", g.getId());

        String notificationMessage = username + " joined the group.";

        //Start the intent service to send the push
        Intent intent2 = new Intent(this, PushSender.class);
        intent2.putStringArrayListExtra("users", g.getUserList());
        intent2.putExtra("message", message);
        intent2.putExtra("notification", notificationMessage);
        startService(intent2);

        //Grant permissions to all existing events
        List<Event> acceptedEvents = model.getAcceptedEvents();
        List<Event> waitingEvents = model.getWaitingEvents();
        List<Event> rejectedEvents = model.getRejectedEvents();
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
                    if (g.containsEvent(current.getId())) {
                        Log.d(LOG_TAG, "Granting permission for: " + current.getId());
                        BaasResult<Void> result1 = current.getBaasDocument().grantSync(Grant.ALL, username);
                        if (result1.isFailed()) {
                            Log.d(LOG_TAG, "Grant Failed: " + result1.error());
                        }
                    }
                }
            }
        }


    }

}
