import { WebPlugin } from '@capacitor/core';
import { OtpManagerPlugin, ActivateOptions } from './definitions';

export class OtpManagerPluginWeb extends WebPlugin implements OtpManagerPlugin {
  public activate(_: ActivateOptions): Promise<void> {
    return Promise.reject('API services for SMS not available');
  }
}
