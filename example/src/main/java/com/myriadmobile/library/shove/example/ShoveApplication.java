package com.myriadmobile.library.shove.example;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.myriadmobile.library.shove.ShoveClient;

public class ShoveApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ShoveClient.initialize(this, "YOUR_GMC_APPLICATION_ID_HERE", new ShoveClient.InitializeCallback() {
            @Override
            public void onSuccess(String registrationId, boolean updated) {
                Intent intent = new Intent("ShoveClient.initialize.onSuccess");
                intent.putExtra("registrationId", registrationId);
                LocalBroadcastManager.getInstance(ShoveApplication.this).sendBroadcast(intent);
            }

            @Override
            public void onError(Throwable error) {
                Intent intent = new Intent("ShoveClient.initialize.onError");
                intent.putExtra("error", error);
                LocalBroadcastManager.getInstance(ShoveApplication.this).sendBroadcast(intent);
            }
        });
    }
}
