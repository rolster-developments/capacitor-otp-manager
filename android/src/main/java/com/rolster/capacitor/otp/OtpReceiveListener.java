package com.rolster.capacitor.otp;

import android.content.Intent;

public interface OtpReceiveListener {
    void onSmsReceivedSuccess(String sms);
    void onSmsReceivedSuccess(Intent intent, String activityCallback);
    void onSmsReceivedCancel();
    void onSmsReceivedTimeOut();
    void onSmsReceivedError(String msgError);
}
