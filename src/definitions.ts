import { PluginListenerHandle as Handle } from '@capacitor/core';

export type OtpManagerEvent = 'otpReceivedEvent';
export type OtpManagerActivateStatus = 'success' | 'unnecessary';
export type OtpManagerStatus =
  | 'success'
  | 'failed'
  | 'timeout'
  | 'error'
  | 'canceled';

export interface OtpManagerProps {
  senderCode: string;
}

export interface OtpManagerActivate {
  status: OtpManagerActivateStatus;
}

export interface OtpManagerResult {
  message: string;
  otp: string;
  status: OtpManagerStatus;
}

type OtpManagerCallback = (data: OtpManagerResult) => void;

export interface OtpManagerPlugin {
  activate(props: OtpManagerProps): Promise<OtpManagerActivate>;

  addListener(event: OtpManagerEvent, callback: OtpManagerCallback): Handle;
}
