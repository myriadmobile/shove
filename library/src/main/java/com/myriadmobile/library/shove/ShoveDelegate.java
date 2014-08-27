package com.myriadmobile.library.shove;

import android.content.Context;
import android.content.Intent;

public interface ShoveDelegate {

    public void onReceive(Context context, Intent notification);

}
