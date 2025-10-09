package com.rolster.capacitor.otp.huawei;

import android.app.Activity;
import android.content.IntentFilter;

import com.getcapacitor.PluginCall;
import com.huawei.hms.support.sms.ReadSmsManager;
import com.huawei.hms.support.sms.common.ReadSmsConstant;
import com.rolster.capacitor.otp.OtpManagerResolver;
import com.rolster.capacitor.otp.OtpReceiveListener;

public class HuaweiOtpManagerResolver implements OtpManagerResolver {
    @Override()
    public void execute(OtpReceiveListener listener, Activity activity, PluginCall call) {
        String senderCode = call.getString("senderCode");

        ReadSmsManager.startConsent(activity, senderCode)
            .addOnSuccessListener(command -> {
                IntentFilter intent = new IntentFilter(ReadSmsConstant.READ_SMS_BROADCAST_ACTION);
                listener.registerReceiverSms(new HuaweiBroadcastReceiver(listener), intent);

                call.resolve();
            })
            .addOnFailureListener(error -> {
                call.reject(error.getMessage());
            });
    }
}