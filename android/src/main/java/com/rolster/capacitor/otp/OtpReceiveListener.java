package com.rolster.capacitor.otp;

import android.content.Intent;

public interface OtpReceiveListener {
    void onSMSReceivedSuccess(String sms);
    void onSMSReceivedSuccess(Intent intent, String activityCallback);
    void onSMSReceivedCancel();
    void onSMSReceivedTimeOut();
    void onSMSReceivedError(String errorMsg);
}
