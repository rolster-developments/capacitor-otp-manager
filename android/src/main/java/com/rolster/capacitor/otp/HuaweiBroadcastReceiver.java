package com.rolster.capacitor.otp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.huawei.hms.common.api.CommonStatusCodes;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.sms.common.ReadSmsConstant;

public class HuaweiBroadcastReceiver extends BroadcastReceiver {
    private final OtpReceiveListener listener;

    public HuaweiBroadcastReceiver(OtpReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ReadSmsConstant.READ_SMS_BROADCAST_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status smsBroadcastStatus = (Status) extras.getParcelable(ReadSmsConstant.EXTRA_STATUS);

            switch (smsBroadcastStatus.getStatusCode()) {
                case CommonStatusCodes.SUCCESS -> {
                    if (extras.containsKey(ReadSmsConstant.EXTRA_SMS_MESSAGE)) {
                        listener.onSmsReceivedSuccess(extras.getString(ReadSmsConstant.EXTRA_SMS_MESSAGE));
                    } else {
                        listener.onSmsReceivedCancel();
                    }
                }
                case CommonStatusCodes.TIMEOUT -> {
                    listener.onSmsReceivedTimeOut();
                }
                default -> {
                    listener.onSmsReceivedError("Unknown error");
                }
            }
        }
    }
}
