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
  sendRequest(msg);
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
  sendRequest(msg);
}

/*---------------------------------------------------
 * send an udp broadcast request
 *-------------------------------------------------*/
void sendRequest(String msg) {
  Udp.beginPacket(getBroadcastIP().c_str(), UDP_PORT);
  Udp.write(msg.c_str());
  Udp.endPacket();
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
      Serial.println(F("got request registration packet"));
      sendRegister();
    }
  }
}

