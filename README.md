![Shove](https://github.com/myriadmobile/shove/raw/master/res/shove_banner.png)
=====

Convenient wrapper for push notifications on top of GCM

Install
--------

```groovy
compile 'com.myriadmobile.library:shove:0.9.0'
```

Introduction
------------

Push notifications are becoming increasingly common in mobile applications as the expectations
of near-realtime data grows. Unfortunately, implementing them often requires masses of
boilerplate and can be quite a hassle. This is why we made Shove!

Features
--------

### Simple Asynchronous Registration

```java
ShoveClient.initialize(this, "YOUR_GCM_APPLICATION_ID", new ShoveClient.InitializeCallback() {
    @Override
    public void onSuccess(String registrationId, boolean updated) {
        // registered with gcm
    }

    @Override
    public void onError(Throwable error) {
        // failed to register with gcm
    }
});
```

Note: ShoveClient.initialize can only be called once per application start. For this reason,
it is recommended to call initializes in the `onCreate()` of a custom application class.

### Drawer Notifications

When a push notification is received, it is often desirable to display a drawer notification
to notify the user, unfortunately GCM does not do this for you... but we did!

By default, a notification will be shown if the pushed data includes a `message`.
Optionally, you can also supply a `title`.

### Intents

Sometimes simply displaying a drawer notification isn't enough. You might want to do 
something when it is clicked. Luckily, we thought of that too!

There are two different ways to do such:

If the activity is not opened
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    Intent intent = getIntent();
    if (intent.hasExtra("shove")) {
        Bundle data = intent.getBundleExtra("shove");
        // do something with the data
    }
}
```

If the activity is already open and launchMode="singleTask"
```java
@Override
protected void onNewIntent(Intent intent) {
    if (intent.hasExtra("shove")) {
        Bundle data = intent.getBundleExtra("shove");
        // do something with the data
    }
}
```

### Configurable

Shove was designed to be highly configurable in order to handle many of the different
scenarios we often encounter. To do this, we created the `ShoveDelegate`. The `ShoveDelegate`
contains a single method:

```java
public void onReceive(Context context, Intent notification)
```

Yep. One method. This allows you to define exactly what you want to happen when a push
notification is received.

#### SimpleNotificationDelegate

By default, Shove uses the `SimpleNotificationDelegate` which provides the intent and drawer
notification features listed above. `SimpleNotificationDelegate` can be configured or extended
to meet many of your needs.

##### Configure

```java
SimpleNotificationDelegate delegate = new SimpleNotificationDelegate();
// set the draw notification gcm data title key, default: title
// the title is not provided, the launcher activity title is used
delegate.setTitleKey("customTileKey");
// set the draw notification gcm data message key, default: message
delegate.setMessageKey("customMessageKey");
// set the draw notification icon, default: application launch icon
delegate.setDefaultIcon(R.drawable.ic_launcher);
// the activity to open when the drawer notification is clicked, default: launch activity
delegate.setDefaultActivity(CustomActivity.class);
```
 
Initialize with the delegate:
```java
ShoveClient.initialize(this, "YOUR_GCM_APPLICATION_ID", delegate);
```

You can also extend `SimpleNotificationDelegate`.

#### Custom Delegate

If `SimpleNotificationDelegate` doesn't provide the functionality you need, it's easy to make your
own delegate. Simply create a class that implements `com.myriadmobile.library.shove.ShoveDelegate`.

License
-------

The MIT License (MIT)

Copyright (c) 2014 Myriad Mobile

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

![Shove](https://github.com/myriadmobile/shove/raw/master/res/shove.png)
