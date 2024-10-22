package com.rolster.capacitor.otp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.support.sms.ReadSmsManager;
import com.huawei.hms.support.sms.common.ReadSmsConstant;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CapacitorPlugin(name = "OtpManager")
public class OtpManagerPlugin extends Plugin implements OtpReceiveListener {
    private PluginCall pluginCall;

    private BroadcastReceiver broadcastReceiver;

    @PluginMethod()
    public void activate(PluginCall call) {
        pluginCall = call;

        if (hasHuaweiServicesAvailable()) {
            resolveSmsForHuawei(call);
            return;
        }

        if (hasGoogleServicesAvailable()) {
            resolveSmsForGoogle(call);
            return;
        }

        pluginCall = null;
        call.reject("API services for SMS not available");
    }

    @Override
    public void onSmsReceivedSuccess(String sms) {
        int otpSize = pluginCall.getInt("otpSize");

        resolveOtpFromSMS(sms, otpSize);
        resetBroadcastReceiver();
    }

    @Override
    public void onSmsReceivedSuccess(Intent intent, String activityCallback) {
        startActivityForResult(pluginCall, intent, activityCallback);
    }

    @Override
    public void onSmsReceivedCancel() {
        JSObject result = new JSObject();
        result.put("status", "otpManagerCanceled");
        result.put("message", "User does not accept read permission");

        notifyListeners("otpManagerEvent", result);
    }

    @Override
    public void onSmsReceivedTimeOut() {
        JSObject result = new JSObject();
        result.put("status", "otpManagerTimeout");
        result.put("message", "Sorry, the waiting time of 5 minutes for the arrival of the SMS has expired");

        notifyListeners("otpManagerEvent", result);
    }

    @Override
    public void onSmsReceivedError(String msgError) {
        JSObject result = new JSObject();
        result.put("status", "otpManagerError");
        result.put("message", msgError);

        notifyListeners("otpManagerEvent", result);
    }

    @ActivityCallback
    private void handlerGoogleSMS(PluginCall call, ActivityResult activityResult) {
        if (activityResult.getResultCode() == Activity.RESULT_OK) {
            String sms = activityResult.getData().getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
            int otpSize = call.getInt("otpSize");

            resolveOtpFromSMS(sms, otpSize);
            resetBroadcastReceiver();
        } else {
            JSObject result = new JSObject();
            result.put("status", "otpManagerCanceled");
            result.put("message", "User does not accept read permission");

            notifyListeners("otpManagerEvent", result);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void resolveSmsForGoogle(PluginCall call) {
        String senderCode = call.getString("senderCode");

        resetBroadcastReceiver();

        SmsRetriever.getClient(getActivity())
            .startSmsUserConsent(senderCode)
            .addOnSuccessListener(command -> {
                IntentFilter intent = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
                broadcastReceiver = new GoogleBroadcastReceiver(this);
                getActivity().registerReceiver(broadcastReceiver, intent, Context.RECEIVER_EXPORTED);

                call.resolve();
            })
            .addOnFailureListener(error -> {
                call.reject(error.getMessage());
            });
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void resolveSmsForHuawei(PluginCall call) {
        String senderCode = call.getString("senderCode");

        resetBroadcastReceiver();

        ReadSmsManager.startConsent(getActivity(), senderCode)
            .addOnSuccessListener(command -> {
                IntentFilter intent = new IntentFilter(ReadSmsConstant.READ_SMS_BROADCAST_ACTION);
                broadcastReceiver = new HuaweiBroadcastReceiver(this);
                getActivity().registerReceiver(broadcastReceiver, intent, Context.RECEIVER_EXPORTED);

                call.resolve();
            })
            .addOnFailureListener(error -> {
                call.reject(error.getMessage());
            });
    }

    private String requestOtpFromSMS(String sms, int otpSize) {
        String pattern = MessageFormat.format("(\\d'{'{0}'}')", otpSize);
        Matcher matcher = Pattern.compile(pattern).matcher(sms);

        return (matcher.find())? matcher.group() : "";
    }

    private void resolveOtpFromSMS(String sms, int otpSize) {
        String otp = requestOtpFromSMS(sms, otpSize);

        JSObject result = new JSObject();

        result.put("sms", sms);

        if (!otp.isEmpty()) {
            result.put("status", "otpManagerSuccess");
            result.put("otp", otp);
        } else {
            result.put("status", "otpManagerEmpty");
        }

        notifyListeners("otpManagerEvent", result);
    }

    private void resetBroadcastReceiver() {
        if (broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    private boolean hasGoogleServicesAvailable() {
        GoogleApiAvailability services = GoogleApiAvailability.getInstance();
        int status = services.isGooglePlayServicesAvailable(getContext());

        return status == com.google.android.gms.common.ConnectionResult.SUCCESS;
    }

    private boolean hasHuaweiServicesAvailable() {
        HuaweiApiAvailability services = HuaweiApiAvailability.getInstance();
        int status = services.isHuaweiMobileServicesAvailable(getContext());

        return status == com.huawei.hms.api.ConnectionResult.SUCCESS;
    }
}
