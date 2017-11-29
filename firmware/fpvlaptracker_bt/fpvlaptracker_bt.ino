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
#include "rssi.h"
#include "lap.h"

// debug mode flag
//#define DEBUG

// udp port
const unsigned int UDP_PORT = 31337;

// pin configurations
const unsigned int PIN_SLAVE_SELECT = 12;
const unsigned int PIN_SPI_CLOCK = 13;
const unsigned int PIN_SPI_DATA = 14;
const unsigned int PIN_LED = 5;

WiFiUDP Udp;
Rssi rssi;
Lap lap;

String serialString = "";
bool serialGotLine = false;
bool networkMode = false;

#define CONFIG_VERSION "008"
#define CONFIG_START 32
// eeprom storage structure and default values
struct StoreStruct {
  char version[4];
  unsigned int channelIndex;
  unsigned long minLapTime;
  unsigned int rssiThresholdLow;
  unsigned int rssiThresholdHigh;
  unsigned int offset;
  char ssid[64];
  char password[64];
} storage = {
  CONFIG_VERSION,
  0,                  // channelIndex
  8000,               // minLapTime
  100,                // rssiThresholdLow
  150,                // rssiThresholdHigh
  55,                 // offset
  "flt-base",         // ssid
  "flt-base"          // password
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
  Serial.println(getChannelNameForChannelIndex(storage.channelIndex));
#endif

  bool wifiSsidFound = false;
  int nrOfWifis = WiFi.scanNetworks();
#ifdef DEBUG
  Serial.print(nrOfWifis);
  Serial.println(" network(s) found");
#endif
  for (unsigned int i = 0; i < nrOfWifis; i++) {
#ifdef DEBUG
    Serial.println(WiFi.SSID(i));
#endif
    if (WiFi.SSID(i) == storage.ssid) {
      wifiSsidFound = true;
#ifdef DEBUG
      Serial.println("found ssid, connecting");
#endif
    }
  }

  if (wifiSsidFound) {
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
      networkMode = true;
    }
  }

#ifdef DEBUG
  if (networkMode) {
    Serial.println(F("WiFi set up"));
    Serial.print(F("IP address: "));
    Serial.println(WiFi.localIP());
    Serial.print(F("broadcast IP is "));
    Serial.println(getBroadcastIP());
  }
#endif

  if (networkMode) {
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
  if (!btSendAndWaitForOK(name)) {
    blinkError(3);
  }
  if (!btSendAndWaitForOK(pin)) {
    blinkError(4);
  }
#endif

  lap.setMinLapTime(storage.minLapTime);
  lap.setRssiThresholdLow(storage.rssiThresholdLow);
  lap.setRssiThresholdHigh(storage.rssiThresholdHigh);

#ifdef DEBUG
  Serial.println("entering main loop");
#endif
}

/*---------------------------------------------------
 * application main loop
 *-------------------------------------------------*/
void loop() {

  processSerialData();
  processSerialLine();

  // if we are not in standalone mode, process udp
  if (networkMode) {
    processUdp();
  }

  rssi.process();
  if (lap.process(rssi.getRssi())) {
    sendLap(lap.getLastLapTime());
  }
  
  processLed();

}

