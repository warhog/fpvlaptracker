import { InjectableRxStompConfig } from '@stomp/ng2-stompjs';

const prepareBrokerURL = (path: string): string => {
  // Create a relative http(s) URL relative to current page
  const url = new URL(path, window.location.href);
  // Convert protocol http -> ws and https -> wss
  url.protocol = url.protocol.replace('http', 'ws');

  return url.href;
};

export const myRxStompConfig: InjectableRxStompConfig = {
  // Which server?
  //brokerURL: 'ws://localhost:8080/websocketEndpoint',
  brokerURL: prepareBrokerURL('/websocketEndpoint'),

  // Headers
  // Typical keys: login, passcode, host
//   connectHeaders: {
//     login: 'guest',
//     passcode: 'guest'
//   },

  // How often to heartbeat?
  // Interval in milliseconds, set to 0 to disable
  heartbeatIncoming: 0, // Typical value 0 - disabled
  heartbeatOutgoing: 20000, // Typical value 20000 - every 20 seconds

  // Wait in milliseconds before attempting auto reconnect
  // Set to 0 to disable
  // Typical value 500 (500 milli seconds)
  reconnectDelay: 500,

  // Will log diagnostics on console
  // It can be quite verbose, not recommended in production
  // Skip this key to stop logging to console
  // debug: (msg: string): void => {
  //   console.log(new Date(), msg);
  // }
};