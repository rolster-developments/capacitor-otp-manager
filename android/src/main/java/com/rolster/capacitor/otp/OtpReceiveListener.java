package com.rolster.capacitor.otp;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

public interface OtpReceiveListener {
    void onSmsReceivedSuccess(String sms);

    void onSmsReceivedSuccess(Intent intent, String activityCallback);

    void onSmsReceivedCancel();

    void onSmsReceivedTimeOut();
    
    void onSmsReceivedError(String msgError);

    void registerReceiverSms(BroadcastReceiver receiver, IntentFilter intent);
}
