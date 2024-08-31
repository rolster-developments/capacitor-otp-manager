import Foundation
import Capacitor

@objc(OtpManager)
public class OtpManager: CAPPlugin {
  @objc func activate(_ call: CAPPluginCall) {
    call.resolve([
      "status": "unnecessary"
    ]);
  }
}
