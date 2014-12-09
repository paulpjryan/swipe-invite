package com.team16.swipeinvite;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasUser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocalSaveService extends IntentService {
    private static final String LOG_TAG = "LocalSaveService";

    public LocalSaveService() {
        super("MyIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the application
        StartUp application = (StartUp) getApplication();

        //Get the model
        Model model = Model.getInstance(this);

        //Delete user data files if necessary
        String[] files = fileList();
        if (files.length > 5) {
            Log.d(LOG_TAG, "Need to delete a user file.");
            ArrayList<String> filesArray = new ArrayList<String>(Arrays.asList(files));
            for (String y : filesArray) {  //Iterate to find a non current user file
                if (!y.equals(BaasUser.current().getName())) {
                    deleteFile(y);   //Delete first possible file
                    Log.d(LOG_TAG, "Deleted user data: " + y);
                    break;
                }
            }
        }

        //Get all of the arraylists and the BaasDocument model
        List<Group2> activeGroups = model.getActiveGroups();
        List<Event> acceptedEvents = model.getAcceptedEvents();
        List<Event> waitingEvents = model.getWaitingEvents();
        List<Event> rejectedEvents = model.getRejectedEvents();
        BaasDocument serverModel = model.getServerVersion();

        //Write the server model to a string
        String serverModelString = serverModel.toJson().encode();

        //Write the active groups to a string
        String activeGroupsString = "";
        synchronized (activeGroups) {
            for (Group2 x : activeGroups) {
                activeGroupsString = activeGroupsString + x.getBaasDocument().toJson().encode() + application.getDelim1();
            }
        }

        //Write accepted events to a string
        String acceptedEventsString = "";
        synchronized (acceptedEvents) {
            for (Event x : acceptedEvents) {
                acceptedEventsString = acceptedEventsString + x.getBaasDocument().toJson().encode() + application.getDelim1();
            }
        }

        //Write waiting events to a string
        String waitingEventsString = "";
        synchronized (waitingEvents) {
            for (Event x : waitingEvents) {
                waitingEventsString = waitingEventsString + x.getBaasDocument().toJson().encode() + application.getDelim1();
            }
        }

        //Write rejected events to a string
        String rejectedEventsString = "";
        synchronized (rejectedEvents) {
            for (Event x : rejectedEvents) {
                rejectedEventsString = rejectedEventsString + x.getBaasDocument().toJson().encode() + application.getDelim1();
            }
        }

        //Generate the overall string
        String total = serverModelString + application.getDelim2() + activeGroupsString + application.getDelim2()
                + acceptedEventsString + application.getDelim2() + waitingEventsString + application.getDelim2()
                + rejectedEventsString + application.getDelim2();
        Log.d(LOG_TAG, "Total string: " + total);

        //Open the file
        FileOutputStream fos;
        try {
            fos = openFileOutput(BaasUser.current().getName(), Context.MODE_PRIVATE);
        } catch (FileNotFoundException f) {
            Log.d(LOG_TAG, "File not found: " + f.getMessage());
            return;
        } catch (NullPointerException e) {
            Log.d(LOG_TAG, "No current user: " + e.getMessage());
            return;
        }

        //Write and close the file
        try {
            fos.write(total.getBytes(Charset.defaultCharset()));
            fos.close();
        } catch (IOException i) {
            Log.d(LOG_TAG, "IO exception: " + i.getMessage());
            return;
        }

        Log.d(LOG_TAG, "File saved successfully.");
    }

}
