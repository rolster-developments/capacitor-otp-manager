package com.rolster.capacitor.otp;

import android.app.Activity;

import com.getcapacitor.PluginCall;

public interface OtpManagerResolver {
    void execute(OtpReceiveListener listener, Activity activity, PluginCall call);
}
