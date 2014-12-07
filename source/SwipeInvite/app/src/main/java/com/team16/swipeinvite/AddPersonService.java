package com.team16.swipeinvite;

import android.app.IntentService;
import android.content.Intent;


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

    }

}
