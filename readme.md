# Rolster Capacitor OtpManager

Plugin for OTP manager in mobile Android, Huawei and iOS.

## Installation

Package only supports Capacitor 6

```
npm i @rolster/capacitor-otp-manager
```

### Android configuration

And register the plugin by adding it to you MainActivity's onCreate:

```java
import com.rolster.capacitor.otp.OtpManagerPlugin;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    registerPlugin(OtpManagerPlugin.class);
    // Others register plugins

    super.onCreate(savedInstanceState);
  }
}
```
