package com.myriadmobile.push.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.myriadmobile.push.PushClient;


public class PushActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);

        PushClient.initialize(this, "YOUR_GCM_APPLICATION_ID", new PushClient.InitializeCallback() {
            @Override
            public void onSuccess(String registrationId, boolean updated) {
                Log.e("PushActivity", registrationId);
            }

            @Override
            public void onError(Throwable error) {
                Log.e("PushActivity", error.getMessage(), error);
            }
        });
    }

}
