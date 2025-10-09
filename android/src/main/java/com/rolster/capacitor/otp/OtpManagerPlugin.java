package com.rolster.capacitor.otp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CapacitorPlugin(name = "OtpManager")
public class OtpManagerPlugin extends Plugin implements OtpReceiveListener {
    private PluginCall pluginCall;

    private BroadcastReceiver broadcastReceiver;

    private OtpManagerResolver otpManagerResolver;

    @Override
    public void load() {
        try {
            String otpManagerResolverClass = BuildConfig.IS_HMS ?
                "com.rolster.capacitor.otp.huawei.HuaweiOtpManagerResolver" :
                "com.rolster.capacitor.otp.google.GoogleOtpManagerResolver";
            
            otpManagerResolver = (OtpManagerResolver) Class.forName(otpManagerResolverClass)
                    .getConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando StoreVerifyServices", e);
        }
    }

    @PluginMethod()
    public void activate(PluginCall call) {
        resetBroadcastReceiver();

        otpManagerResolver.execute(this, getActivity(), call);

        pluginCall = call;
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

    @Override()
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void registerReceiverSms(BroadcastReceiver receiver, IntentFilter intent) {
        broadcastReceiver = receiver;
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getActivity().registerReceiver(receiver, intent, Context.RECEIVER_EXPORTED);
        } else {
            getActivity().registerReceiver(receiver, intent);
        }
    }

    @ActivityCallback
    private void handlerGoogleSMS(PluginCall call, ActivityResult activityResult) {
        if (activityResult.getResultCode() == Activity.RESULT_OK) {
            String sms = activityResult.getData().getStringExtra("com.google.android.gms.auth.api.phone.EXTRA_SMS_MESSAGE");
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
}
