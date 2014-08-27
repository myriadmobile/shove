package com.myriadmobile.library.shove.example;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.myriadmobile.library.shove.ShoveClient;

public class ShoveActivity extends Activity {

    private TextView tvRegistrationId;
    private Button btCopy;
    private Button btSend;
    private TextView tvNotificationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shove);

        tvRegistrationId = (TextView) findViewById(R.id.tvRegistrationId);
        btCopy = (Button) findViewById(R.id.btCopy);
        btSend = (Button) findViewById(R.id.btSend);
        tvNotificationData = (TextView) findViewById(R.id.tvNotificationData);

        parseIntent(getIntent());

        btCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("registration id", tvRegistrationId.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ShoveActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, tvRegistrationId.getText());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String registrationId = intent.getStringExtra("registrationId");
                tvRegistrationId.setText(registrationId);
                tvRegistrationId.setTextColor(getResources().getColor(android.R.color.black));
            }
        }, new IntentFilter("ShoveClient.initialize.onSuccess"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Throwable error = (Throwable) intent.getSerializableExtra("error");
                tvRegistrationId.setText(error.getMessage());
                tvRegistrationId.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
        }, new IntentFilter("ShoveClient.initialize.onError"));

    }

    @Override
    protected void onNewIntent(Intent intent) {
        parseIntent(intent);
    }

    private void parseIntent(Intent intent) {
        if (intent.hasExtra("shove")) {
            Bundle data = intent.getBundleExtra("shove");
            data.isEmpty(); // unparcel
            tvNotificationData.setText(data.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        tvRegistrationId.setText(ShoveClient.getRegistrationId());
    }
}
