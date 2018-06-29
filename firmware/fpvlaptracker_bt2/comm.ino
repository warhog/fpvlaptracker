/*---------------------------------------------------
 * processor for incoming serial line
 *-------------------------------------------------*/
// void processSerialLine() {
//   // process serial line
//   if (serialGotLine) {
//     if (serialString.length() >= 11 && serialString.substring(0, 11) == "GET version") {
//       // get the version
//       String v = F("VERSION: ");
//       v += CONFIG_VERSION;
//       sendBtMessageWithNewline(v);
//     } else if (serialString.length() >= 8 && serialString.substring(0, 8) == "GET rssi") {
//       // get the current rssi
//       String r = F("RSSI: ");
//       r += rssi.getRssi();
//       sendBtMessageWithNewline(r);
//     } else if (serialString.length() >= 6 && serialString.substring(0, 6) == "REBOOT") {
//       // reboot the device
//       // TODO currently the hc06 is not responding after a reboot
//       ESP.reset();
//     } else if (serialString.length() >= 10 && serialString.substring(0, 10) == "GET config") {
//       // get the current config data
//       processGetConfig();
//     } else if (serialString.length() >= 11 && serialString.substring(0, 11) == "PUT config ") {
//       // store the given config data
//       processStoreConfig();
//     } else if (serialString.length() >= 12 && serialString.substring(0, 12) == "GET channels") {
//       // scan all channels
//       processScanChannels();
//     } else {
//       sendBtMessageWithNewline(F("UNKNOWN_COMMAND"));
//     }
//     serialGotLine = false;
//     serialString = "";
//   }
// }

/*---------------------------------------------------
 * received get config message
 *-------------------------------------------------*/
// void processGetConfig() {
//   // send config as json
//   DynamicJsonBuffer jsonBuffer(200);
//   JsonObject& root = jsonBuffer.createObject();
//   root["frequency"] = getFrequencyForChannelIndex(storage.channelIndex);
//   root["minimumLapTime"] = storage.minLapTime;
//   root["thresholdLow"] = storage.rssiThresholdLow;
//   root["thresholdHigh"] = storage.rssiThresholdHigh;
//   root["ssid"] = storage.ssid;
//   root["password"] = storage.password;
//   uint16_t channelData = getSPIFrequencyForChannelIndex(storage.channelIndex);
//   RCV_FREQ(channelData);
//   String c = F("CONFIG: ");
//   root.printTo(c);
//   sendBtMessageWithNewline(c);
// }

/*---------------------------------------------------
 * received store config message
 *-------------------------------------------------*/
// void processStoreConfig() {
//   // received config as json
//   DynamicJsonBuffer jsonBuffer(200);
//   JsonObject& root = jsonBuffer.parseObject(serialString.substring(11));
//   if (!root.success()) {
// #ifdef DEBUG
//     Serial.println(F("failed to parse config"));
// #endif
//     sendBtMessageWithNewline(F("SETCONFIG: NOK"));
//   } else {
//     storage.minLapTime = root["minimumLapTime"];
//     storage.rssiThresholdLow = root["thresholdLow"];
//     storage.rssiThresholdHigh = root["thresholdHigh"];
//     strncpy(storage.ssid, root["ssid"], sizeof(storage.ssid));
//     strncpy(storage.password, root["password"], sizeof(storage.password));
//     storage.channelIndex = getChannelIndexForFrequency(root["frequency"]);
//     lap.setMinLapTime(storage.minLapTime);
//     lap.setRssiThresholdLow(storage.rssiThresholdLow);
//     lap.setRssiThresholdHigh(storage.rssiThresholdHigh);
//     saveConfig();
//     sendBtMessageWithNewline(F("SETCONFIG: OK"));
//   }
// }

/*---------------------------------------------------
 * received scan channels message
 *-------------------------------------------------*/
// void processScanChannels() {
//   String c = F("CHANNELS: ");
//   c += scanChannels();
//   sendBtMessageWithNewline(c);
// }
