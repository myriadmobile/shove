package com.myriadmobile.push;

public class PushException extends Exception {

    public PushException(String message) {
        super(message);
    }

    public PushException(Throwable t) {
        super(t);
    }

}