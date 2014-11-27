package com.team16.swipeinvite;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PushSender extends IntentService {
    private static final String LOG_TAG = "PushSender";

    private int iterations = 0;

    public PushSender() {
        super("PushSender");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Handling push.");
        //Get the model instance
        Model model = Model.getInstance(this);

        //Pull the extras out of the intent
        ArrayList<String> users = intent.getStringArrayListExtra("users");
        JsonObject message = intent.getParcelableExtra("message");
        String notMessage = intent.getStringExtra("notification");
        if (users == null || message == null || notMessage == null) {
            Log.d(LOG_TAG, "Intent messed up.");
            return;
        }

        //Create a server query for users with these names
        BaasQuery queryU = BaasQuery.builder().users().where("user.name='a'").build();
        for (String u : users) {
            if (BaasUser.current().getName().equals(u)) {
                Log.d(LOG_TAG, "Not sending push to current user.");
                continue;
            }
            queryU = queryU.buildUpon().or("user.name=" + "'" + u + "'").build();
        }

        //Fetch the user list
        BaasResult<List<BaasUser>> result = BaasUser.fetchAllSync(queryU.buildUpon().criteria());
        if (result.isFailed()) {
            //Try another time
            Log.d(LOG_TAG, "Query trying again.");
            tryAgain(users, message, notMessage);
            iterations++;
            return;
        }
        iterations = 0;
        Log.d(LOG_TAG, "Query success.");

        //Get the list of users
        List<BaasUser> groupUsers = result.value();
        if (groupUsers.size() <= 0) {
            Log.d(LOG_TAG, "No users to push to.");
            return;
        }
        Log.d(LOG_TAG, "Pushing to: " + groupUsers.toString());

        //Launch the push notification
        try {
            BaasResult<Void> resultPush = BaasBox.messagingService().newMessage().extra(message)
                    .to(groupUsers.toArray(new BaasUser[groupUsers.size()]))
                    .text(notMessage).sendSync();
            if (resultPush.isFailed()) {
                //Try another time
                Log.d(LOG_TAG, "Push trying again.");
                tryAgain(users, message, notMessage);
                iterations++;
                return;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Push service not available: " + e.getMessage());
            iterations = 0;
            return;
        }

        Log.d(LOG_TAG, "Push success.");
        iterations = 0;
        return;
    }

    //region Method to start another try if a failure occurs
    private void tryAgain(ArrayList<String> users, JsonObject message, String notMessage) {
        if (iterations >= 3) {
            return;
        }
        //Create intent
        Intent intent1 = new Intent(this, PushSender.class);
        intent1.putStringArrayListExtra("users", users);
        intent1.putExtra("message", message);
        intent1.putExtra("notification", notMessage);
        PendingIntent pending = PendingIntent.getService(this, 0, intent1, 0);

        //Set alarm for 1 min
        Log.d(LOG_TAG, "Set alarm.");
        AlarmManager m = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        m.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1 * 1000, pending);
        return;
    }
    //endregion

}
