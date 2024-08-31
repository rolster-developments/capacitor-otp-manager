import { WebPlugin } from '@capacitor/core';
import {
  OtpManagerActivate,
  OtpManagerPlugin,
  OtpManagerProps
} from './definitions';

export class OtpManagerPluginWeb extends WebPlugin implements OtpManagerPlugin {
  public activate(_: OtpManagerProps): Promise<OtpManagerActivate> {
    return Promise.resolve({ status: 'unnecessary' });
  }
}
