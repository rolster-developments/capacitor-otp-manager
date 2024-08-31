import { registerPlugin } from '@capacitor/core';
import type { OtpManagerPlugin } from './definitions';

const OtpManager = registerPlugin<OtpManagerPlugin>('OtpManager', {
  web: () =>
    import('./web').then(({ OtpManagerPluginWeb }) => new OtpManagerPluginWeb())
});

export { OtpManager };
