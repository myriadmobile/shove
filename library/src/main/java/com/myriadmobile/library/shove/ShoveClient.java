package com.myriadmobile.library.shove;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public enum ShoveClient {

    /**
     * The singleton instance
     */
    INSTANCE;

    /**
     * The logging tag
     */
    private static final String TAG = ShoveClient.class.getSimpleName();

    /**
     * The registration id shared preferences key
     */
    private static final String PREF_REG_ID = "registration_id";

    /**
     * The application version preferences key
     */
    private static final String PREF_APP_VERSION = "app_version";

    /**
     * The Google Cloud Messaging application id preferences key
     */
    private static final String PREF_GCM_ID = "gcm_id";

    /**
     * The application context
     */
    private Context mContext;

    /**
     * The Google Cloud Messaging application id
     */
    private String mGcmId;

    /**
     * The Google Cloud Messaging instance
     */
    private GoogleCloudMessaging mGcm;

    /**
     * The registration id
     */
    private String mRegistrationId;

    /**
     * The callback to invoke when initialization is complete
     */
    private InitializeCallback mInitializeCallback;

    /**
     * The notification delegate
     */
    private ShoveDelegate mShoveDelegate;

    private ShoveClient() {
    }

    /**
     * Initializes the push client
     *
     * @throws RuntimeException if already initialized
     *
     * @param context the application context
     * @param gcmId   the Google Cloud Messaging application id
     */
    public static void initialize(Context context, String gcmId) {
        initialize(context, gcmId, null, null);
    }

    /**
     * Initializes the push client
     *
     * @throws RuntimeException if already initialized
     *
     * @param context the application context
     * @param gcmId   the Google Cloud Messaging application id
     */
    public static void initialize(Context context, String gcmId, ShoveDelegate delegate) {
        initialize(context, gcmId, null, delegate);
    }

    /**
     * Initializes the push client
     *
     * @throws RuntimeException if already initialized
     *
     * @param context  the application context
     * @param gcmId    the Google Cloud Messaging application id
     * @param callback the callback to be invoked when initialization is complete
     */
    public static synchronized void initialize(Context context, String gcmId, InitializeCallback callback) {
        initialize(context, gcmId, callback, null);
    }

    /**
     * Initializes the push client
     *
     * @throws RuntimeException if already initialized
     *
     * @param context  the application context
     * @param gcmId    the Google Cloud Messaging application id
     * @param callback the callback to be invoked when initialization is complete
     */
    public static synchronized void initialize(Context context, String gcmId, InitializeCallback callback, ShoveDelegate delegate) {
        if (INSTANCE.mContext != null) {
            throw new RuntimeException(new ShoveException(TAG + " already initialized"));
        }
        INSTANCE.mContext = context.getApplicationContext();
        INSTANCE.mGcmId = gcmId;
        INSTANCE.mInitializeCallback = callback;
        INSTANCE.mShoveDelegate = delegate;
        INSTANCE.initializeInBackground();
    }

    /**
     * Unregister the client from push notifications
     */
    public static void unregister() {
        if (INSTANCE.mRegistrationId == null) {
            throw new RuntimeException(TAG + " is not registered");
        }
        try {
            INSTANCE.mGcm.unregister();
            INSTANCE.getPushPreferences().edit().clear().apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the registration id or null when the push client is not initialized.
     */
    public static String getRegistrationId() {
        return INSTANCE.mRegistrationId;
    }

    /**
     * @return The shove delegate
     */
    public static synchronized ShoveDelegate getDelegate() {
        if (INSTANCE.mShoveDelegate == null) {
            INSTANCE.mShoveDelegate = new SimpleNotificationDelegate();
        }
        return INSTANCE.mShoveDelegate;
    }

    /**
     * Registers the client for push notifications with Google Cloud Messaging in a background thread
     */
    private void initializeInBackground() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (hasPlayServices()) {
                        mGcm = GoogleCloudMessaging.getInstance(mContext);
                        mRegistrationId = loadRegistrationId();
                        if (TextUtils.isEmpty(mRegistrationId)) {
                            mRegistrationId = mGcm.register(mGcmId);
                            storeRegistrationId(mRegistrationId);
                            onSuccess(mRegistrationId, true);
                        } else {
                            onSuccess(mRegistrationId, false);
                        }
                    } else {
                        throw new PlayServicesNotFoundException();
                    }
                } catch (Exception e) {
                    if (!(e instanceof ShoveException)) {
                        e = new ShoveException(e);
                    }
                    onError(e);
                } finally {
                    // null the callback to prevent memory leaks
                    mInitializeCallback = null;
                }
            }

            /**
             * Posts a success message to the initialization callback on the main thread
             * @param registrationId the registration id
             * @param updated true when the registration id is new or has changed
             */
            private void onSuccess(final String registrationId, final boolean updated) {
                if (mInitializeCallback != null) {
                    final InitializeCallback callback = mInitializeCallback;
                    Handler handler = new Handler(mContext.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(registrationId, updated);
                        }
                    });
                }
            }

            /**
             * Posts an error message to the initialization callback on the main thread
             * @param error the error that occurred
             */
            private void onError(final Throwable error) {
                if (mInitializeCallback != null) {
                    final InitializeCallback callback = mInitializeCallback;
                    Handler handler = new Handler(mContext.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(error);
                        }
                    });
                }
            }

        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    /**
     * @return true when play services is available
     */
    private boolean hasPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        return resultCode == ConnectionResult.SUCCESS;
    }

    /**
     * @return the version code of the application
     */
    private int getAppVersion() {
        try {
            return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return BuildConfig.VERSION_CODE;
        }
    }

    /**
     * @return the shared preferences for the push client
     */
    private SharedPreferences getPushPreferences() {
        return mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    /**
     * Stores a registration id in shared preferences
     *
     * @param registrationId the registration id
     */
    private void storeRegistrationId(String registrationId) {
        SharedPreferences.Editor editor = getPushPreferences().edit();
        editor.putInt(PREF_APP_VERSION, getAppVersion());
        editor.putString(PREF_REG_ID, registrationId);
        editor.putString(PREF_GCM_ID, mGcmId);
        editor.apply();
    }

    /**
     * Load a registration id from shared preferences
     *
     * @return the registration id
     */
    private String loadRegistrationId() {
        SharedPreferences prefs = getPushPreferences();
        String registrationId = prefs.getString(PREF_REG_ID, null);
        if (TextUtils.isEmpty(registrationId)) {
            return null;
        }

        // the app was updated since the last registration, return null
        int registeredVersion = prefs.getInt(PREF_APP_VERSION, -1);
        if (registeredVersion != getAppVersion()) {
            return null;
        }

        // if the gcm id was changed, return null;
        String gcmId = prefs.getString(PREF_GCM_ID, null);
        if (gcmId != null && !gcmId.equals(mGcmId)) {
            return null;
        }

        return registrationId;
    }

    public static interface InitializeCallback {

        /**
         * Callback to be invoked when the push client initializes successfully
         *
         * @param registrationId the registration id
         * @param updated        true when the registration id is new or has changed
         */
        public void onSuccess(String registrationId, boolean updated);

        /**
         * Callback to be invoked when the push client fails to initializes
         *
         * @param error the error that occurred
         */
        public void onError(Throwable error);

    }

}
