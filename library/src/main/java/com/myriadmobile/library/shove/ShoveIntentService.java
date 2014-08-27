package com.myriadmobile.library.shove;

import android.app.IntentService;
import android.content.Intent;

/**
 * Handles GCM push notifications
 */
public class ShoveIntentService extends IntentService {
    public static final String TAG = ShoveIntentService.class.getSimpleName();

    public ShoveIntentService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        ShoveClient.INSTANCE.getDelegate().onReceive(this, intent);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        ShoveBroadcastReceiver.completeWakefulIntent(intent);
    }

}