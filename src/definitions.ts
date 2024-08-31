import { PluginListenerHandle } from '@capacitor/core';

export type OtpManagerEvent = 'otpManagerEvent';

export interface ActivateOptions {
  otpSize: number;
  senderCode: string;
}

interface OtpManagerEventSuccess {
  otp: string;
  sms: string;
  status: 'otpManagerSuccess';
}

interface OtpManagerEventEmpty {
  sms: string;
  status: 'otpManagerEmpty';
}

type OtpManagerFailureStatus =
  | 'otpManagerCanceled'
  | 'otpManagerError'
  | 'otpManagerFailed'
  | 'otpManagerTimeout';

interface OtpManagerEventFailure {
  message: string;
  status: OtpManagerFailureStatus;
}

type OtpManagerEventResult =
  | OtpManagerEventSuccess
  | OtpManagerEventEmpty
  | OtpManagerEventFailure;

type OtpManagerCallback = (data: OtpManagerEventResult) => void;

export interface OtpManagerPlugin {
  activate(options: ActivateOptions): Promise<void>;

  addListener(
    event: OtpManagerEvent,
    callback: OtpManagerCallback
  ): Promise<PluginListenerHandle>;
}
