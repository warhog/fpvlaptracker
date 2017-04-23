/*---------------------------------------------------
 * eeprom config loader
 *-------------------------------------------------*/
void loadConfig() { 
  if (EEPROM.read(CONFIG_START + 0) == CONFIG_VERSION[0] && EEPROM.read(CONFIG_START + 1) == CONFIG_VERSION[1] && EEPROM.read(CONFIG_START + 2) == CONFIG_VERSION[2]) {
    Serial.println(F("load values from eeprom"));
    for (unsigned int t = 0; t < sizeof(storage); t++) {
      *((char*)&storage + t) = EEPROM.read(CONFIG_START + t);
    }
  } else {
    Serial.println(F("load default values"));
    SPIFFS.begin();
    Serial.println(F("format spiffs, can take some time"));
    SPIFFS.format();
    saveConfig();
  }
}

/*---------------------------------------------------
 * eeprom config storage
 *-------------------------------------------------*/
void saveConfig() {
  Serial.println(F("store config to eeprom"));
  for (unsigned int t = 0; t < sizeof(storage); t++) {
    EEPROM.write(CONFIG_START + t, *((char*)&storage + t));
  }
  EEPROM.commit();
}

/*---------------------------------------------------
 * access point setup code
 *-------------------------------------------------*/
void setupAccessPoint() {
  String ssid = "flt-";
  ssid += ESP.getChipId();
  Serial.print(F("setting up access point with ssid "));
  Serial.println(ssid);
  WiFi.mode(WIFI_AP);
  boolean result = WiFi.softAP(ssid.c_str(), ssid.c_str());
  if (!result) {
    Serial.println(F("could not setup access point"));
    while (true) {
      digitalWrite(PIN_LED, HIGH);
      delay(150);
      digitalWrite(PIN_LED, LOW);
      delay(150);
    }
  }
}

/*---------------------------------------------------
 * build broadcast ip for current localIP (just replaces last group with 255
 * may be wrong on non trivial setups but for now it is good enough
 *-------------------------------------------------*/
String getBroadcastIP() {
  String localIP = WiFi.localIP().toString();
  if (standaloneMode) {
    localIP = WiFi.softAPIP().toString();
  }
  int pos = localIP.lastIndexOf('.');
  String broadcastIP = localIP.substring(0, pos);
  broadcastIP += ".255";
  return broadcastIP;
}

/*---------------------------------------------------
 * process led status, blink in standalone mode
 *-------------------------------------------------*/
void processLed() {
  static unsigned long ledTimerNextRun = 0L;
  static unsigned int ledStage = 0;
  if (ledTimerNextRun < millis()) {
    if (standaloneMode) {
      ledTimerNextRun = millis() + 1000;
      digitalWrite(PIN_LED, !digitalRead(PIN_LED));
    } else {
      if (ledStage == 0) {
        ledStage = 1;
        ledTimerNextRun = millis() + 50;
        digitalWrite(PIN_LED, HIGH);
      } else {
        ledStage = 0;
        ledTimerNextRun = millis() + 500;
        digitalWrite(PIN_LED, LOW);
      }
    }
  }
}

/*---------------------------------------------------
 * get current rssi strength
 *-------------------------------------------------*/
unsigned int getRssi() {
  // do multiple reads and calculate average value
  unsigned long sum = 0L;
  for (unsigned int i = 0; i < NUMBER_OF_RSSI_CYCLES; i++) {
    sum += analogRead(0);
  }
  sum /= NUMBER_OF_RSSI_CYCLES;
  return (unsigned int) sum;
}

/*---------------------------------------------------
 * process a lap
 *-------------------------------------------------*/
void processLapDetection() {
  static unsigned long currentLapStart = 0;
  static boolean isLocked = false;
  
  unsigned long tempLapTime = millis() - currentLapStart;
  if (tempLapTime < storage.minLapTime) {
/*    Serial.print(F("too fast lap, ignore for "));
    Serial.println(storage.minLapTime - tempLapTime);*/
  } else {
    if (isLocked && currentRssiStrength <= storage.rssiThresholdLow) {
      isLocked = false;
      Serial.println(F("unlock"));
      Serial.println(currentRssiStrength);
    } else if (!isLocked && currentRssiStrength >= storage.rssiThresholdHigh) {
      isLocked = true;
      Serial.println(F("detected lap, lock"));
      currentLapTime = millis() - currentLapStart;
      if (standaloneMode) {
        String output = "{\"t\":\"l\",\"d\":";
        output += currentLapTime;
        output += "}";
        webSocket.broadcastTXT(output.c_str());
      } else {
        sendLap(currentLapTime);
      }
      currentLapStart = millis();
    }
  }
}
