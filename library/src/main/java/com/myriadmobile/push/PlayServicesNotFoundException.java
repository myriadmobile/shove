package com.myriadmobile.push;

public class PlayServicesNotFoundException extends PushException {

    public PlayServicesNotFoundException() {
        super("No valid Google Play Services APK found.");
    }

}
