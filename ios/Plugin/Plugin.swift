import Foundation
import Capacitor

@objc(OtpManagerPlugin)
public class OtpManagerPlugin: CAPPlugin {
    @objc func activate(_ call: CAPPluginCall) {
        call.reject("API services for SMS not available");
    }
}
