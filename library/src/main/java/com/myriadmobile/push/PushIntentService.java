package com.myriadmobile.push;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Handles GCM push notifications
 */
public class PushIntentService extends IntentService {
    public static final String TAG = PushIntentService.class.getSimpleName();

    public PushIntentService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            showNotification(extras);
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        PushBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Shows a notification for the given push message
     *
     * @param extras the message extras
     */
    public void showNotification(Bundle extras) {
        // parse the title and message from the extras
        String title = getTitle(extras);
        String message = getMessage(extras);

        // if the message is empty, bail
        if (message == null)
            return;

        // create the notification
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getIcon())
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(getContentIntent(extras, when))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // display the notification
        mNotificationManager.notify((int) when, mBuilder.build());
    }

    /**
     * @param extras the notification extras
     * @return the notification title
     */
    public String getTitle(Bundle extras) {
        return extras.getString(PushClient.INSTANCE.getDefaultTitleKey(), getPackageManager().getApplicationLabel(getApplicationInfo()).toString());
    }

    /**
     * @param extras the notification extras
     * @return the notification message
     */
    public String getMessage(Bundle extras) {
        return extras.getString(PushClient.INSTANCE.getDefaultMessageKey(), null);
    }

    /**
     * @return the content intent's component name
     */
    public ComponentName getComponentName() {
        Class<? extends Activity> activity = PushClient.INSTANCE.getDefaultActivity();
        if (activity != null) {
            return new ComponentName(this, activity);
        } else {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(getPackageName());
            return intent.getComponent();
        }
    }

    /**
     * @return the notification icon
     */
    public int getIcon() {
        int iconRes = PushClient.INSTANCE.getDefaultIcon();
        if (iconRes == 0) {
            iconRes = getApplicationInfo().icon;
        }
        return iconRes;
    }

    /**
     * @param extras the notification extras
     * @param when   the time the notification was processes
     * @return the notification content extra
     */
    public PendingIntent getContentIntent(Bundle extras, long when) {
        Intent intent = new Intent();
        intent.setComponent(getComponentName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (extras != null) {
            intent.putExtras(extras);
        }
        return PendingIntent.getActivity(this, (int) when, intent, 0);
    }

}