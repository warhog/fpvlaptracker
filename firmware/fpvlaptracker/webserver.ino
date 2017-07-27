/*---------------------------------------------------
 * webserver functions
 *-------------------------------------------------*/
/*---------------------------------------------------
 * websocket event handler
 *-------------------------------------------------*/
void webSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t lenght) {
  switch (type) {
    case WStype_DISCONNECTED:
      Serial.printf("[%u] Disconnected!\n", num);
      break;
    case WStype_CONNECTED:
      {
        IPAddress ip = webSocket.remoteIP(num);
        Serial.printf("[%u] Connected from %d.%d.%d.%d url: %s\n", num, ip[0], ip[1], ip[2], ip[3], payload);
      }
      break;
    case WStype_TEXT:
      {
        Serial.printf("[%u] get Text: %s\n", num, payload);
        char buf[50] = { 0 };
        snprintf(buf, sizeof(buf), "%s", payload);
        if (strcmp(buf, "ping") == 0) {
          Serial.println(F("got ping, send pong"));
          webSocket.sendTXT(num, "pong");
        }
      }
      break;
    default:
      Serial.println(F("invalid event"));
  }

}

/*---------------------------------------------------
 * get the content type of a given filename
 *-------------------------------------------------*/
String getContentType(String filename) {
  if (server.hasArg("download")) return "application/octet-stream";
  else if (filename.endsWith(".htm")) return "text/html";
  else if (filename.endsWith(".html")) return "text/html";
  else if (filename.endsWith(".css")) return "text/css";
  else if (filename.endsWith(".js")) return "application/javascript";
  else if (filename.endsWith(".png")) return "image/png";
  else if (filename.endsWith(".gif")) return "image/gif";
  else if (filename.endsWith(".jpg")) return "image/jpeg";
  else if (filename.endsWith(".ico")) return "image/x-icon";
  else if (filename.endsWith(".xml")) return "text/xml";
  else if (filename.endsWith(".pdf")) return "application/x-pdf";
  else if (filename.endsWith(".zip")) return "application/x-zip";
  else if (filename.endsWith(".gz")) return "application/x-gzip";
  return "text/plain";
}

/*---------------------------------------------------
 * handle file read from spiffs file system for given path (uri)
 *-------------------------------------------------*/
bool handleFileRead(String path) {
  if (path.endsWith("/")) path += "index.html";
  String contentType = getContentType(path);
  Serial.print(F("get file for path: "));
  Serial.println(path);
  if (SPIFFS.exists(path)) {
    File file = SPIFFS.open(path, "r");
    size_t sent = server.streamFile(file, contentType);
    file.close();
    return true;
  }
  return false;
}

/*---------------------------------------------------
 * webservice data call
 *-------------------------------------------------*/
void handleData() {
  String output = "{\"thresholdLow\":";
  output += storage.rssiThresholdLow;
  output += ",\"thresholdHigh\":";
  output += storage.rssiThresholdHigh;
  output += ",\"minLapTime\":";
  output += storage.minLapTime;
  output += ",\"frequency\":";
  output += getFrequencyForChannelIndex(storage.channelIndex);
  output += ",\"rssi\":";
  output += currentRssiStrength;
  output += "}";
  server.send(200, "application/json", output);
}

/*---------------------------------------------------
 * webservice wifi call
 *-------------------------------------------------*/
void handleWifi() {
  String output = "{\"ssid\":\"";
  output += storage.ssid;
  output += "\",\"password\":\"";
  output += storage.password;
  output += "\"}";
  server.send(200, "application/json", output);
}

/*---------------------------------------------------
 * webservice measure call
 *-------------------------------------------------*/
void handleMeasure() {
  String result = "{\"channels\":[";
  unsigned int channelIndexMax = 0;
  unsigned int rssiMax = 0;
  unsigned long timer = 0;
  for (unsigned int i = 0; i < NR_OF_FREQUENCIES; i++) {
    uint16_t channelData = pgm_read_word_near(channelTable + i);
    RCV_FREQ(channelData);
    Serial.print(F("scanning channel "));
    uint16_t channelFreq = pgm_read_word_near(channelFreqTable + i);
    Serial.print(channelFreq);
    delay(50);
    unsigned int rssi = getRssi();
    if (rssi > rssiMax) {
      rssiMax = rssi;
      channelIndexMax = i;
    }
    Serial.print(", rssi: ");
    Serial.println(rssi);
    result += "{\"freq\":";
    result += pgm_read_word_near(channelFreqTable + i);
    result += ",\"rssi\":";
    result += rssi;
    result += "}";
    if (i < (NR_OF_FREQUENCIES - 1)) {
      result += ",";
    }
  }
  result += "],\"maxFreq\":";
  result += pgm_read_word_near(channelFreqTable + channelIndexMax);
  result += ",\"maxRssi\":";
  result += rssiMax;
  result += "}";
  Serial.print(F("maximum channel is "));
  Serial.print(pgm_read_word_near(channelFreqTable + channelIndexMax));
  Serial.print(F(" MHz with rssi "));
  Serial.println(rssiMax);
  // restore stored channel
  uint16_t channelData = pgm_read_word_near(channelTable + storage.channelIndex);
  RCV_FREQ(channelData);
  
  server.send(200, "application/json", result);
}

/*---------------------------------------------------
 * webservice write data call
 *-------------------------------------------------*/
void writeData() {
  Serial.println(F("write data"));
  if (server.args() <= 0) {
    server.send(500, "text/plain", "no args");
  } else {
    if (server.hasArg("minLapTime")) {
      storage.minLapTime = server.arg("minLapTime").toInt();
      Serial.print(F("minLapTime "));
      Serial.println(storage.minLapTime);
    }
    if (server.hasArg("thresholdhigh")) {
      storage.rssiThresholdHigh = server.arg("thresholdhigh").toInt();
      Serial.print(F("threshold high "));
      Serial.println(storage.rssiThresholdHigh);
    }
    if (server.hasArg("thresholdlow")) {
      storage.rssiThresholdLow = server.arg("thresholdlow").toInt();
      Serial.print(F("threshold low "));
      Serial.println(storage.rssiThresholdLow);
    }
    if (server.hasArg("ssid")) {
      strncpy(storage.ssid, server.arg("ssid").c_str(), sizeof(storage.ssid));
      Serial.print(F("ssid "));
      Serial.println(storage.ssid);
    }
    if (server.hasArg("password")) {
      strncpy(storage.password, server.arg("password").c_str(), sizeof(storage.password));
      Serial.print(F("password "));
      Serial.println(storage.password);
    }
    if (server.hasArg("frequency")) {
      unsigned int frequency = server.arg("frequency").toInt();
      Serial.print(F("frequency: "));
      Serial.println(frequency);
      unsigned int channelIndex = getChannelIndexForFrequency(frequency);
      if (channelIndex == -1) {
        Serial.println(F("invalid frequency"));
        server.send(500, "text/plain", "invalid frequency");
        return;
      } else {
        Serial.print(F("channel index "));
        Serial.println(channelIndex);
        uint16_t channelData = pgm_read_word_near(channelTable + channelIndex);
        RCV_FREQ(channelData);
        storage.channelIndex = channelIndex;
      }
    }
  }
  saveConfig();
  server.send(200, "application/json", "{\"status\":\"OK\"}");
}

/*---------------------------------------------------
 * webservice rssi call
 *-------------------------------------------------*/
void handleRssi() {
  String output = "{\"rssi\":";
  output += currentRssiStrength;
  output += "}";
  server.send(200, "application/json", output);
}

