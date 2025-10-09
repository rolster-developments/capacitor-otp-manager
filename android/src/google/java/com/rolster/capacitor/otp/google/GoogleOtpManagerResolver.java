package com.rolster.capacitor.otp.google;

import android.app.Activity;
import android.content.IntentFilter;

import com.getcapacitor.PluginCall;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.rolster.capacitor.otp.OtpManagerResolver;
import com.rolster.capacitor.otp.OtpReceiveListener;

public class GoogleOtpManagerResolver implements OtpManagerResolver {
    @Override()
    public void execute(OtpReceiveListener listener, Activity activity, PluginCall call) {
        String senderCode = call.getString("senderCode");

        SmsRetriever.getClient(activity)
            .startSmsUserConsent(senderCode)
            .addOnSuccessListener(command -> {
                IntentFilter intent = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
                listener.registerReceiverSms(new GoogleBroadcastReceiver(listener), intent);

                call.resolve();
            })
            .addOnFailureListener(error -> {
                call.reject(error.getMessage());
            });
    }
}