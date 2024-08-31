package com.rolster.capacitor.otp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CapacitorPlugin(name = "OtpManager")
public class OtpManagerPlugin extends Plugin implements OtpReceiveListener {
  private final String receiveEvent = "otpReceivedEvent";

  private PluginCall lastPluginCall;

  private BroadcastReceiver broadcastReceiver;

  @PluginMethod()
  public void activate(PluginCall call) {
    if (hasHuaweiServices()) {
      activateForHuawei(call);
    } else if (hasGoogleServices()) {
      activateForGoogle(call);
    } else {
      call.reject("API services for SMS not available");
    }
  }
 
  @Override 
  public void onSMSReceivedSuccess(String message) {
    resolveOtpFromSMS(message);
    resetBroadcastReceiver();
  }

  @Override
  public void onSMSReceivedSuccess(Intent intent, String activityCallback) {
    startActivityForResult(lastPluginCall, intent, activityCallback);
  }

  @Override
  public void onSMSReceivedCancel() {
    JSObject cancel = new JSObject();
    cancel.put("status", "canceled");
    cancel.put("message", "User does not accept read permission");
    cancel.put("otp", "");
    
    notifyListeners(receiveEvent, cancel);
  }
  
  @Override
  public void onSMSReceivedTimeOut() {
    JSObject timeout = new JSObject();
    timeout.put("status", "timeout");
    timeout.put("message", "Sorry, the waiting time of 5 minutes for the arrival of the SMS has expired");
    timeout.put("otp", "");
    
    notifyListeners(receiveEvent, timeout);
  }
  
  @Override
  public void onSMSReceivedError(String errorMsg) {
    JSObject error = new JSObject();
    error.put("status", "error");
    error.put("message", errorMsg);
    error.put("otp", "");
    
    notifyListeners(receiveEvent, error);
  }
  
  @ActivityCallback
  private void handlerGoogleSMS(PluginCall call, ActivityResult result) {
    if (result.getResultCode() == Activity.RESULT_OK) {
      resolveOtpFromSMS(result.getData().getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE));
      resetBroadcastReceiver();
    } else {
      JSObject response = new JSObject();
      response.put("status", "canceled");
      response.put("message", "User does not accept read permission");
      response.put("otp", "");
      
      notifyListeners(receiveEvent, response);
    }
  }
  
  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  private void activateForGoogle(PluginCall call) {
    String senderCode = call.getString("senderCode");
    
    lastPluginCall = call;
    resetBroadcastReceiver();
    
    SmsRetriever.getClient(bridge.getActivity())
      .startSmsUserConsent(senderCode)
      .addOnSuccessListener(command -> {
        IntentFilter intent = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        broadcastReceiver = new GoogleBroadcastReceiver(this);
        bridge.getActivity().registerReceiver(broadcastReceiver, intent);
        
        JSObject result = new JSObject();
        result.put("status", "success");
        call.resolve(result);
      })
      .addOnFailureListener(error -> {
        call.reject(error.getMessage());
      });
  }
  
  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  private void activateForHuawei(PluginCall call) {
    String senderCode = call.getString("senderCode");
    
    lastPluginCall = call;
    resetBroadcastReceiver();
    
    ReadSmsManager.startConsent(getActivity(), senderCode)
      .addOnSuccessListener(command -> {
        IntentFilter intent = new IntentFilter(ReadSmsConstant.READ_SMS_BROADCAST_ACTION);
        broadcastReceiver = new HuaweiBroadcastReceiver(this);
        getActivity().registerReceiver(broadcastReceiver, intent);
        
        JSObject result = new JSObject();
        result.put("status", "success");
        call.resolve(result);
      })
      .addOnFailureListener(error -> {
        call.reject(error.getMessage());
      });
  }
  
  private String requestOtpFromSMS(String sms) {
    Matcher matcher = Pattern.compile("(\\d{8})").matcher(sms);
    
    return (matcher.find())? matcher.group() : "";
  }
  
  private void resolveOtpFromSMS(String sms) {
    String otp = requestOtpFromSMS(sms);
    
    JSObject response = new JSObject();

    if (!otp.isEmpty()) {
      response.put("status", "success");
      response.put("message", "");
      response.put("otp", otp);
    } else {
      response.put("status", "failed");
      response.put("message", "Error reading OTP from SMS");
      response.put("otp", "");
    }
    
    notifyListeners(receiveEvent, response);
  }
  
  private void resetBroadcastReceiver() {
    if (broadcastReceiver != null) {
      bridge.getActivity().unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

  private boolean hasGoogleServices() {
    int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

    return status == com.google.android.gms.common.ConnectionResult.SUCCESS;
  }

  private boolean hasHuaweiServices() {
    int status = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(getContext());

    return status == com.huawei.hms.api.ConnectionResult.SUCCESS;
  }
}
