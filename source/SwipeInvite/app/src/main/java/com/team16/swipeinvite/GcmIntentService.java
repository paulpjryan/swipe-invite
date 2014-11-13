package com.team16.swipeinvite;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by kylekrynski on 10/31/14.
 */
public class GcmIntentService extends IntentService {
    private static final String LOG_TAG = "GCM_SERVICE";


    //region Local instance variable for the model
    //private Model model;
    //endregion


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
                String type = extras.getString("type");
                String id = extras.getString("id");
                if (type.equals("group")) {    //Check if a group was received
                    //Attempt to pulldown a group object from the server
                    BaasResult<BaasDocument> result = BaasDocument.fetchSync(type, id);
                    if (result.isFailed()) {
                        Log.d(LOG_TAG, "Group fetch failed: " + result.error());
                    } else if (result.isSuccess()) {
                        completeGroup(result.value());
                    }

                } else if (type.equals("event")) {    //Check if an event was received
                    //Attempt to pulldown an event object from the server
                    BaasResult<BaasDocument> result = BaasDocument.fetchSync(type, id);
                    if (result.isFailed()) {
                        Log.d(LOG_TAG, "Event fetch failed: " + result.error());
                    } else if (result.isSuccess()) {
                        completeEvent(result.value());
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
    private void completeGroup(BaasDocument d) {
        //Put BaasDocument into Group class and handle the model
        Group2 g = new Group2(d);
        Log.d(LOG_TAG, "Received group: " + g.getName());
        Model model = Model.getInstance(this);    //load the model from the shared prefs

        //Go through the model and find the group that was downloaded if there is one
        boolean added = false;
        Log.d(LOG_TAG, "Iterating through group array to find and replace");
        List<Group2> activeGroups = model.getActiveGroups();
        synchronized (activeGroups) {    //Need to synchronize any iteration
            for (final ListIterator<Group2> i = activeGroups.listIterator(); i.hasNext();) {    //Setting up the iterator for each loop
                final Group2 current = i.next();    //need to get current group
                if (current.equals(g)) {
                    i.set(g);   //Replace the old group for the new one
                    added = true;
                    break;
                }
            }
        }
        if (!added) {
            activeGroups.add(g);    //Add the received group if it is new
        }

        //Save the changed model
        Model.saveModel(this);

        //Notify the user of the change
        String title;
        String message;
        if (!added) {
            title = "Group Joined";
            message = "You have been added to " + g.getName() + ".";
        } else {
            title = "Group Updated";
            message = g.getName() + " has been updated.";
        }
        sendNotification(message, title);
    }

    private void completeEvent(BaasDocument d) {

    }
    //endregion


    //region Methods and variables to handle posting a notification
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private void sendNotification(String msg, String title) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    //endregion


}
