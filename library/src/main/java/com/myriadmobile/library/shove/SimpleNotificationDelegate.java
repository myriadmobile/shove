package com.myriadmobile.library.shove;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public enum SimpleNotificationDelegate implements ShoveDelegate {

    /**
     * The singleton instance
     */
    INSTANCE;

    /**
     * The default push notification callback activity
     */
    private Class<? extends Activity> mDefaultActivity;

    /**
     * The default push notification callback icon
     */
    private int mDefaultIconResId;

    /**
     * The default extra key for the notification title
     */
    private String mTitleKey = "title";

    /**
     * The default extra key for the notification message
     */
    private String mMessageKey = "message";

    /**
     * Sets the activity that is opened by default when a push notification is clicked
     *
     * @param activity The class of the activity
     */
    public static void setDefaultActivity(Class<? extends Activity> activity) {
        INSTANCE.mDefaultActivity = activity;
    }

    /**
     * Sets the default notification icon
     *
     * @param iconResId the icon's resource id
     */
    public static void setDefaultIcon(int iconResId) {
        INSTANCE.mDefaultIconResId = iconResId;
    }

    /**
     * Sets the default bundle extra key for the notification title
     *
     * @param titleKey the bundle extra title key
     */
    public static void setTitleKey(String titleKey) {
        INSTANCE.mTitleKey = titleKey;
    }

    /**
     * Sets the default bundle extra key for the notification message
     *
     * @param messageKey the bundle extra title key
     */
    public static void setMessageKey(String messageKey) {
        INSTANCE.mMessageKey = messageKey;
    }

    /**
     * Called when a notification is received
     *
     * @param context      the service context
     * @param notification the notification intent
     */
    @Override
    public void onReceive(Context context, Intent notification) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(notification);
        Bundle extras = notification.getExtras();

        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            showNotification(context, extras);
        }
    }

    /**
     * Shows a notification for the given push message
     *
     * @param extras the message extras
     */
    public void showNotification(Context context, Bundle extras) {
        // parse the title and message from the extras
        String title = getTitle(context, extras);
        String message = getMessage(context, extras);

        // if the message is empty, bail
        if (message == null)
            return;

        // create the notification
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(getIcon(context))
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(getContentIntent(context, extras, when))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // display the notification
        mNotificationManager.notify((int) when, mBuilder.build());
    }

    /**
     * @param extras the notification extras
     * @return the notification title
     */
    public String getTitle(Context context, Bundle extras) {
        return extras.getString(mTitleKey, context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString());
    }

    /**
     * @param extras the notification extras
     * @return the notification message
     */
    public String getMessage(Context context, Bundle extras) {
        return extras.getString(mMessageKey, null);
    }

    /**
     * @return the content intent's component name
     */
    public ComponentName getComponentName(Context context) {
        Class<? extends Activity> activity = mDefaultActivity;
        if (activity != null) {
            return new ComponentName(context, activity);
        } else {
            PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
            return intent.getComponent();
        }
    }

    /**
     * @return the notification icon
     */
    public int getIcon(Context context) {
        int iconRes = mDefaultIconResId;
        if (iconRes == 0) {
            iconRes = context.getApplicationInfo().icon;
        }
        return iconRes;
    }

    /**
     * @param extras the notification extras
     * @param when   the time the notification was processes
     * @return the notification content extra
     */
    public PendingIntent getContentIntent(Context context, Bundle extras, long when) {
        Intent intent = new Intent();
        intent.setComponent(getComponentName(context));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (extras != null) {
            intent.putExtra("shove", extras);
        }
        return PendingIntent.getActivity(context, (int) when, intent, 0);
    }

}
