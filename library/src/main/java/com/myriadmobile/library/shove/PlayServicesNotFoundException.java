package com.myriadmobile.library.shove;

public class PlayServicesNotFoundException extends ShoveException {

    public PlayServicesNotFoundException() {
        super("No valid Google Play Services APK found.");
    }

}
