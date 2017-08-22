/*---------------------------------------------------
 * send data to base station functions
 *-------------------------------------------------*/
/*---------------------------------------------------
 * send a new lap
 *-------------------------------------------------*/
void sendLap(unsigned long time) {
  String msg = "{\"type\":\"lap\",\"chipid\":";
  msg += ESP.getChipId();
  msg += ",\"duration\":";
  msg += time;
  msg += ",\"rssi\":";
  msg += currentRssiStrength;
  msg += "}";
  sendUdpMessage(msg);
  sendBtMessage("LAP: " + String(time), true);
}

/*---------------------------------------------------
 * send register message
 *-------------------------------------------------*/
void sendRegister() {
  String msg = "{\"type\":\"register\",\"chipid\":";
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
  if (connectedMode) {
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

