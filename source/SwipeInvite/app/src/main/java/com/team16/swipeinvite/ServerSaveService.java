package com.team16.swipeinvite;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasResult;
import com.baasbox.android.SaveMode;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class ServerSaveService extends IntentService {
    private static final String LOG_TAG = "ServerSaveService";

    public ServerSaveService() {
        super("ServerSaveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the model instance
        Model model = Model.getInstance(this);

        //Convert the Local model object to the server version and save
        BaasResult<BaasDocument> result = model.toServerVersion().saveSync(SaveMode.IGNORE_VERSION);

        //Check result
        if (result.isFailed()) {
            Log.d(LOG_TAG, "Failed to save model to server.");
            return;
        }

        //Otherwise overwrite the current model object with server return
        Log.d(LOG_TAG, "Finished model save to server");
        model.setServerVersion(result.value());
    }

}
