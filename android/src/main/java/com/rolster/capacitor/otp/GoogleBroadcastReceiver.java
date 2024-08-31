package com.rolster.capacitor.otp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class GoogleBroadcastReceiver extends BroadcastReceiver {
    private final OtpReceiveListener listener;

    public GoogleBroadcastReceiver(OtpReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            switch (smsRetrieverStatus.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    listener.onSMSReceivedSuccess(consentIntent, "handlerGoogleSMS");
                    break;
                case CommonStatusCodes.TIMEOUT:
                    listener.onSMSReceivedTimeOut();
                    break;
                default:
                    listener.onSMSReceivedError("Unknown error");
            }
        }
    }
}
