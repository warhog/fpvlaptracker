/*---------------------------------------------------
 * send a new lap
 *-------------------------------------------------*/
void sendLap(unsigned long time) {
  String msg = "{\"type\":\"lap\",\"chipid\":";
  msg += ESP.getChipId();
  msg += ",\"duration\":";
  msg += time;
  msg += ",\"rssi\":";
  msg += rssi.getRssi();
  msg += "}";
  sendUdpMessage(msg);
  sendBtMessage("LAP: " + String(time), true);
}

/*---------------------------------------------------
 * send register message
 *-------------------------------------------------*/
void sendRegister() {
  String msg = "{\"type\":\"registerbt\",\"chipid\":";
  msg += ESP.getChipId();
  msg += ",\"ip\":";
  msg += WiFi.localIP();
  msg += "}";
  sendUdpMessage(msg);
}

/*---------------------------------------------------
 * send an udp broadcast message
 *-------------------------------------------------*/
void sendUdpMessage(String msg) {
  if (networkMode) {
    Udp.beginPacket(getBroadcastIP().c_str(), UDP_PORT);
    Udp.write(msg.c_str());
    Udp.endPacket();
  }
}

/*---------------------------------------------------
 * send a bluetooth message
 *-------------------------------------------------*/
void sendBtMessage(String msg) {
#ifndef DEBUG
  sendBtMessage(msg, false);
#endif
}
void sendBtMessage(String msg, boolean newLine) {
#ifndef DEBUG
  if (newLine) {
    msg += '\n';
  }
  Serial.write(msg.c_str());
#endif
}

/*---------------------------------------------------
 * incoming udp packet handler
 *-------------------------------------------------*/
void processUdp() {
  int packetSize = Udp.parsePacket();
  if (packetSize) {
    char incomingPacket[255];
    int len = Udp.read(incomingPacket, 255);
    if (len > 0) {
      incomingPacket[len] = 0;
    }
    if (strncmp(incomingPacket, "requestRegistration", 19) == 0) {
#ifdef DEBUG
      Serial.println(F("got request registration packet"));
#endif
      sendRegister();
    }
  }
}


/*---------------------------------------------------
 * event handler for incoming serial events (hc-06)
 *-------------------------------------------------*/
void processSerialData() {
  // process serial data
  while (Serial.available()) {
    char c = (char)Serial.read();
    serialString += c;
    if (c == '\n') {
      serialGotLine = true;
    }
  }
}

/*---------------------------------------------------
 * processor for incoming serial line
 *-------------------------------------------------*/
void processSerialLine() {
  // process serial line
  if (serialGotLine) {
    if (serialString.length() >= 11 && serialString.substring(0, 11) == "GET version") {
      // get the version
      String v = F("VERSION: ");
      v += CONFIG_VERSION;
      sendBtMessage(v, true);
    } else if (serialString.length() >= 8 && serialString.substring(0, 8) == "GET rssi") {
      // get the current rssi
      String r = F("RSSI: ");
      r += rssi.getRssi();
      sendBtMessage(r, true);
    } else if (serialString.length() >= 6 && serialString.substring(0, 6) == "REBOOT") {
      // reboot the device
      // currently the hc06 is not responding after a reboot
      ESP.reset();
    } else if (serialString.length() >= 10 && serialString.substring(0, 10) == "GET config") {
      // get the current config data
      DynamicJsonBuffer jsonBuffer(200);
      JsonObject& root = jsonBuffer.createObject();
      root["frequency"] = getFrequencyForChannelIndex(storage.channelIndex);
      root["minimumLapTime"] = storage.minLapTime;
      root["thresholdLow"] = storage.rssiThresholdLow;
      root["thresholdHigh"] = storage.rssiThresholdHigh;
      root["ssid"] = storage.ssid;
      root["password"] = storage.password;
      String c = F("CONFIG: ");
      root.printTo(c);
      sendBtMessage(c, true);
    } else if (serialString.length() >= 11 && serialString.substring(0, 11) == "PUT config ") {
      // store the given config data
      // received config
      DynamicJsonBuffer jsonBuffer(200);
      JsonObject& root = jsonBuffer.parseObject(serialString.substring(11));
      if (!root.success()) {
#ifdef DEBUG
        Serial.println(F("failed to parse config"));
#endif
        sendBtMessage(F("SETCONFIG: NOK"), true);
      } else {
        storage.minLapTime = root["minimumLapTime"];
        storage.rssiThresholdLow = root["thresholdLow"];
        storage.rssiThresholdHigh = root["thresholdHigh"];
        strncpy(storage.ssid, root["ssid"], sizeof(storage.ssid));
        strncpy(storage.password, root["password"], sizeof(storage.password));
        storage.channelIndex = getChannelIndexForFrequency(root["frequency"]);
        lap.setMinLapTime(storage.minLapTime);
        lap.setRssiThresholdLow(storage.rssiThresholdLow);
        lap.setRssiThresholdHigh(storage.rssiThresholdHigh);
        saveConfig();
        sendBtMessage(F("SETCONFIG: OK"), true);
      }
    } else {
      sendBtMessage(F("UNKNOWN_COMMAND"), true);
    }
    serialGotLine = false;
    serialString = "";
  }
}

/*---------------------------------------------------
 * serial config method for hc-06
 *-------------------------------------------------*/
bool btSendAndWaitForOK(String data) {
  sendBtMessage(data);
  unsigned int waitProtection = 200;
  while (Serial.available() == 0 && waitProtection > 0) {
    delay(10);
    waitProtection--;
  }
  if (waitProtection == 0) {
#ifdef DEBUG
    Serial.println(F("failed waiting for input"));
#endif
    return false;
  }
  // put short delay, hc06 takes some time for sending the config response
  delay(500);

  String result = "";
  while (Serial.available() > 0) {
    char c = (char)Serial.read();
    result += c;
  }

  if (result.substring(0, 2) == "OK") {
    return true;
  }
#ifdef DEBUG
  Serial.println(F("NOK"));
#endif
  return false;
}

