package com.team16.swipeinvite;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonObject;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by kylekrynski on 10/31/14.
 */
public class GcmIntentService extends IntentService {
    private static final String LOG_TAG = "GCM_SERVICE";


    //region Constructor for service
    public GcmIntentService() {
        super("GcmIntentService");
    }
    //endregion


    //region Method to handle an incoming intent
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
                Log.d(LOG_TAG, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " +
                // extras.toString());
                Log.d(LOG_TAG, "Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d(LOG_TAG, "Receieved message: " + extras.toString());
                //Parse the type and id of the group / event from the message
                String message1 = extras.getString("custom");
                Log.d(LOG_TAG, "Message 1: " + message1);
                JsonObject message2 = JsonObject.decode(message1);
                Log.d(LOG_TAG, "Message 2: " + message2);
                JsonObject message3 = message2.getObject("custom");
                Log.d(LOG_TAG, "Message 3: " + message3);
                String type = message3.getString("type");
                String id = message3.getString("id");
                String notificationMessage = extras.getString("message", "Something happened.");
                //Check if no current user
                if (BaasUser.current() == null) {
                    Log.d(LOG_TAG, "No current user to receive push");
                    return;
                }

                if (type.equals("group")) {    //Check if a group was received
                    //Attempt to pulldown a group object from the server
                    BaasResult<BaasDocument> result = BaasDocument.fetchSync(type, id);
                    if (result.isFailed()) {
                        Log.d(LOG_TAG, "Group fetch failed: " + result.error());
                    } else if (result.isSuccess()) {
                        completeGroup(result.value(), notificationMessage);
                    }

                } else if (type.equals("event")) {    //Check if an event was received
                    //Attempt to pulldown an event object from the server
                    BaasResult<BaasDocument> result = BaasDocument.fetchSync(type, id);
                    if (result.isFailed()) {
                        Log.d(LOG_TAG, "Event fetch failed: " + result.error());
                    } else if (result.isSuccess()) {
                        completeEvent(result.value(), notificationMessage);
                    }

                } else {   //Something went wrong
                    Log.d(LOG_TAG, "Error unknown type: " + type);
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    //endregion


    //region Methods for dealing with completed document requests
    private void completeGroup(BaasDocument d, String message) {
        //Put BaasDocument into Group class and handle the model
        Group2 g = new Group2(d);
        Log.d(LOG_TAG, "Received group: " + g.getName());
        Model model = Model.getInstance(this);    //load the model from the shared prefs

        //Go through the model and find the group that was downloaded if there is one
        boolean added = false;
        Log.d(LOG_TAG, "Iterating through group array to find and replace");
        List<Group2> activeGroups = model.getActiveGroups();
        synchronized (activeGroups) {    //Need to synchronize any iteration
            for (final ListIterator<Group2> i = activeGroups.listIterator(); i.hasNext(); ) {    //Setting up the iterator for each loop
                final Group2 current = i.next();    //need to get current group
                if (current.equals(g)) {
                    Log.d(LOG_TAG, "Found group to replace: " + g.getName() + ",  " + g.getId());
                    Log.d(LOG_TAG, "Old group replaced: " + current.getName() + ",  " + current.getId());
                    i.set(g);   //Replace the old group for the new one
                    added = true;
                    break;
                }
            }
        }
        if (!added) {
            Log.d(LOG_TAG, "Group added: " + g.getName() + ",  " + g.getId());
            activeGroups.add(g);    //Add the received group if it is new
        }

        //Save the changed model
        Model.saveModel(this);

        //Notify the user of the change
        String title = g.getName();

        sendNotification(message, title);
    }

    private void completeEvent(BaasDocument d, String message) {
        //Get the model instance
        Model model = Model.getInstance(this);
        Event e = new Event(d);
        Log.d(LOG_TAG, "Received event: " + e.getName());

        //Look through the event lists and decide what to do
        List<Event> acceptedEvents = model.getAcceptedEvents();
        List<Event> waitingEvents = model.getWaitingEvents();
        List<Event> rejectedEvents = model.getRejectedEvents();
        boolean added = false;
        //Check to see if all active events have an equivalent
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
                    //check to see if event exists
                    if (current.equals(e)) {
                        Log.d(LOG_TAG, "Found event to replace: " + e.getName() + ",  " + e.getId());
                        Log.d(LOG_TAG, "Old event replaced: " + current.getName() + ",  " + current.getId());
                        i.set(e);
                        added = true;
                        break;
                    }
                }
            }
        }
        //Check to make sure the user actually has a parent group to the event
        List<Group2> activeGroups = model.getActiveGroups();
        boolean hasGroup = false;
        synchronized (activeGroups) {
            for (Group2 x : activeGroups) {
                for (String groupID : e.getParentGroups()) {
                    if (x.equals(groupID)) {
                        hasGroup = true;
                        break;
                    }
                }
                if (hasGroup) {
                    break;
                }
            }
        }
        if (!hasGroup) {
            Log.d(LOG_TAG, "User does not have group for event.");
            return;
        }

        //If the event was not updated, add it
        if (!added) {
            Log.d(LOG_TAG, "Event added: " + e.getName());
            waitingEvents.add(e);
            //POSSIBLY DO A SEPARATE NOTIFICATION TO MAKE A DECISION
        }

        //Update affected groups with the update service
        updateGroups(e.getParentGroups());

        //Save the changed model
        Model.saveModel(this);

        //Notify user of change
        String title = e.getName();

        sendNotification(message, title);

    }

    private void updateGroups(ArrayList<String> groups) {
        Model model = Model.getInstance(this);

        List<Group2> activeGroups = model.getActiveGroups();
        synchronized (activeGroups) {
            for (Group2 x : activeGroups) {
                for (String y : groups) {
                    if (x.equals(y)) {
                        BaasResult<BaasDocument> result = x.getBaasDocument().refreshSync();
                        if (result.isFailed()) {
                            Log.d(LOG_TAG, "Could not refresh group: " + x.getName());
                            break;
                        }
                        Log.d(LOG_TAG, "Refreshed group: " + x.getName());
                        x.setBaasDocument(result.value());
                        break;
                    }
                }
            }
        }

    }
    //endregion


    //region Methods and variables to handle posting a notification
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    private void sendNotification(String msg, String title) {
        Log.d(LOG_TAG, "Sending notification.");
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg).setSmallIcon(R.drawable.ic_launcher);
        Log.d(LOG_TAG, "Built notification: " + mBuilder);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    //endregion


}
