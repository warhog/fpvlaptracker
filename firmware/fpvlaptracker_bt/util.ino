/*---------------------------------------------------
 * error code blinker
 *-------------------------------------------------*/
void blinkError(unsigned int errorCode) {
#ifdef DEBUG
  Serial.print(F("error code: "));
  Serial.println(errorCode);
  Serial.flush();
#endif
  while (true) {
    for (int i = 0; i < errorCode; i++) {
      ledOn();
      delay(100);
      ledOff();
      delay(200);
    }
    delay(1000);
  }
}

/*---------------------------------------------------
 * led helpers
 *-------------------------------------------------*/
void ledOn() {
  digitalWrite(PIN_LED, HIGH);
}
void ledOff() {
  digitalWrite(PIN_LED, LOW);
}
void ledToggle() {
  digitalWrite(PIN_LED, !digitalRead(PIN_LED));
}

/*---------------------------------------------------
 * eeprom config loader
 *-------------------------------------------------*/
void loadConfig() { 
  if (EEPROM.read(CONFIG_START + 0) == CONFIG_VERSION[0] && EEPROM.read(CONFIG_START + 1) == CONFIG_VERSION[1] && EEPROM.read(CONFIG_START + 2) == CONFIG_VERSION[2]) {
#ifdef DEBUG
    Serial.println(F("load values from eeprom"));
#endif
    for (unsigned int t = 0; t < sizeof(storage); t++) {
      *((char*)&storage + t) = EEPROM.read(CONFIG_START + t);
    }
  } else {
#ifdef DEBUG
    Serial.println(F("load default values"));
#endif
    saveConfig();
  }
}

/*---------------------------------------------------
 * eeprom config storage
 *-------------------------------------------------*/
void saveConfig() {
#ifdef DEBUG
  Serial.println(F("store config to eeprom"));
#endif
  for (unsigned int t = 0; t < sizeof(storage); t++) {
    EEPROM.write(CONFIG_START + t, *((char*)&storage + t));
  }
  EEPROM.commit();
}

/*---------------------------------------------------
 * build broadcast ip for current localIP (just replaces last group with 255
 * may be wrong on non trivial setups but for now it is good enough
 *-------------------------------------------------*/
String getBroadcastIP() {
  String localIP = WiFi.localIP().toString();
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
    if (connectedMode) {
      if (ledStage == 0) {
        ledStage = 1;
        ledTimerNextRun = millis() + 50;
        digitalWrite(PIN_LED, HIGH);
      } else {
        ledStage = 0;
        ledTimerNextRun = millis() + 500;
        digitalWrite(PIN_LED, LOW);
      }
    } else {
      ledTimerNextRun = millis() + 1000;
      digitalWrite(PIN_LED, !digitalRead(PIN_LED));
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
#ifdef DEBUG
      Serial.println(F("unlock"));
      Serial.println(currentRssiStrength);
#endif
    } else if (!isLocked && currentRssiStrength >= storage.rssiThresholdHigh) {
      isLocked = true;
#ifdef DEBUG
      Serial.println(F("detected lap, lock"));
#endif
      currentLapTime = millis() - currentLapStart;
      sendLap(currentLapTime);
      currentLapStart = millis();
    }
  }
}
