/*
 * fpv lap tracker
 * fpv lap tracking software that uses rx5808 to keep track of the fpv video signals
 * rssi for lap detection
 * 
 * 
 * the channel mapping tables are copied from shea iveys rx5808-pro-diversity project
 * https://github.com/sheaivey/rx5808-pro-diversity/blob/master/src/rx5808-pro-diversity/rx5808-pro-diversity.ino
 * 
 * the spi rx5808 code is taken from chickadee laptimer58 project
 * https://github.com/chickadee-tech/laptimer58
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2017 warhog
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * 
 * 
 * blink codes:
 * slow blinking - standalone mode
 * fast blinking - connected mode
 * blinking 2 times - BT version command not OK
 * blinking 3 times - BT name comamnd not OK
 * blinking 4 times - BT pin command not ok
 * 
 */
#include <EEPROM.h>
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <WiFiUdp.h>
#include <ArduinoJson.h>

// debug mode flag
//#define DEBUG

// udp port
const unsigned int UDP_PORT = 31337;

// defines the interval for rssi measurings
const unsigned int RSSI_MEASURE_INTERVAL = 10;
// defines the number of rssi measure cycles (average is calculated over that number of cycles)
const unsigned int NUMBER_OF_RSSI_CYCLES = 3;

// pin configurations
const unsigned int PIN_SLAVE_SELECT = 12;
const unsigned int PIN_SPI_CLOCK = 13;
const unsigned int PIN_SPI_DATA = 14;
const unsigned int PIN_LED = 5;

WiFiUDP Udp;

String serialString = "";
bool serialGotLine = false;
bool connectedMode = false;
unsigned int currentRssiStrength = 0;
unsigned long currentLapTime = 0L;

#define CONFIG_VERSION "007"
#define CONFIG_START 32
// eeprom storage structure and default values
struct StoreStruct {
  char version[4];
  unsigned int channelIndex;
  unsigned long minLapTime;
  unsigned int rssiThresholdLow;
  unsigned int rssiThresholdHigh;
  char ssid[64];
  char password[64];
} storage = {
  CONFIG_VERSION,
  0,
  8000,
  100,
  150,
  "flt-base",
  "flt-base"
};

/*---------------------------------------------------
 * application setup
 *-------------------------------------------------*/
void setup() {
#ifdef DEBUG
  Serial.begin(115200);
  Serial.println("");
  Serial.println("");
  Serial.println(F("booting"));
  Serial.flush();
#else
  Serial.begin(9600);
  delay(100);
  Serial.println();
  Serial.flush();
  delay(1000);
#endif

  pinMode(A0, INPUT);
  randomSeed(analogRead(0));

#ifdef DEBUG
  Serial.println(F("setting up ports"));
#endif
  pinMode(PIN_SLAVE_SELECT, OUTPUT);
  pinMode(PIN_SPI_DATA, OUTPUT);
  pinMode(PIN_SPI_CLOCK, OUTPUT);
  pinMode(PIN_LED, OUTPUT);
  for (int i = 0; i < 20; i++) {
    ledToggle();
    delay(25);
  }
  ledOff();

#ifdef DEBUG
  Serial.println(F("reading config from eeprom"));
#endif
  EEPROM.begin(512);
  loadConfig();
  uint16_t channelData = getSPIFrequencyForChannelIndex(storage.channelIndex);
  RCV_FREQ(channelData);
#ifdef DEBUG
  Serial.print(F("channel info: "));
  Serial.print(getFrequencyForChannelIndex(storage.channelIndex));
  Serial.print(F(" MHz, "));
  Serial.println(getChannelNameForChannelIndex(storage.channelIndex), HEX);
#endif

#ifdef DEBUG
  Serial.print(F("wifi connecting to ssid "));
  Serial.print(storage.ssid);
#endif
  WiFi.begin(storage.ssid, storage.password);
  unsigned int wait = 0;
  while (WiFi.status() != WL_CONNECTED && wait < 15000) {
    delay(500);
    wait += 500;
#ifdef DEBUG
    Serial.print(F("."));
#endif
  }
  if (WiFi.status() != WL_CONNECTED) {
#ifdef DEBUG
    Serial.println(F("cannot connect to ssid, switching to standalone mode"));
#endif
  } else if (WiFi.status() == WL_CONNECTED) {
#ifdef DEBUG
    Serial.println(F("connected"));
#endif
    connectedMode = true;
  }

#ifdef DEBUG
  if (connectedMode) {
    Serial.println(F("WiFi set up"));
    Serial.print(F("IP address: "));
    Serial.println(WiFi.localIP());
    Serial.print(F("broadcast IP is "));
    Serial.println(getBroadcastIP());
  }
#endif

  if (connectedMode) {
#ifdef DEBUG
    Serial.println(F("starting udp"));
#endif
    Udp.begin(UDP_PORT);

#ifdef DEBUG
    Serial.println(F("registering device"));
#endif
    sendRegister();
  }

  uint32_t chipId = ESP.getChipId();
  char strChipId[10];
  sprintf(strChipId, "%u", chipId);
  String chipString = strChipId;
  String name = "AT+NAMEFLT" + chipString;
  String pin = "AT+PIN" + chipString.substring(0, 4);

#ifdef DEBUG
  Serial.print(F("name command: "));
  Serial.println(name);
  Serial.print(F("pin command: "));
  Serial.println(pin);
#else
  uint8_t count = 5;
  while (count > 0) {
    count--;
    if (btSendAndWaitForOK(F("AT+VERSION"))) {
      break;
    }
  }
  if  (count == 0) {
    blinkError(2);
  }
/*  if (!btSendAndWaitForOK(F("AT+VERSION"))) {
    blinkError(2);
  }*/
  if (!btSendAndWaitForOK(name)) {
    blinkError(3);
  }
  if (!btSendAndWaitForOK(pin)) {
    blinkError(4);
  }
#endif

}

/*---------------------------------------------------
 * application main loop
 *-------------------------------------------------*/
void loop() {

  processSerialData();
    
  // process serial line
  if (serialGotLine) {
    if (serialString.length() >= 11 && serialString.substring(0, 11) == "GET version") {
      String v = F("VERSION: ");
      v += CONFIG_VERSION;
      sendBtMessage(v, true);
    } else if (serialString.length() >= 8 && serialString.substring(0, 8) == "GET rssi") {
      String r = F("RSSI: ");
      r += currentRssiStrength;
      sendBtMessage(r, true);
    } else if (serialString.length() >= 6 && serialString.substring(0, 6) == "REBOOT") {
      ESP.reset();
    } else if (serialString.length() >= 10 && serialString.substring(0, 10) == "GET config") {
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
        saveConfig();
        sendBtMessage(F("SETCONFIG: OK"), true);
      }
    } else {
      sendBtMessage(F("UNKNOWN_COMMAND"), true);
    }
    serialGotLine = false;
    serialString = "";
  }

  // if we are not in standalone mode, process udp
  if (connectedMode) {
    processUdp();
  }

  // test if we need to measure new rssi value
  static unsigned long timerRssi = 0L;
  static unsigned int rssiOld = 0;
  if (timerRssi <= millis()) {
    unsigned int rssi = getRssi();
    // smooth rssi (at least a bit)
    currentRssiStrength = rssiOld * 0.25 + rssi * 0.75;
    rssiOld = rssi;

    // do the lap detection
    processLapDetection();
    
    // restart rssi timer
    timerRssi = millis() + RSSI_MEASURE_INTERVAL;
  }

  processLed();

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

