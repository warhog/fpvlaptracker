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
 * Copyright (c) 2016 warhog
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
 */
#include <EEPROM.h>
#include <ESP8266WiFi.h>
#include <WebSocketsServer.h>
#include <FS.h>
#include <ESP8266WebServer.h>
#include <WiFiClient.h>
#include <WiFiUdp.h>

// defines the interval for rssi measurings
const unsigned int RSSI_MEASURE_INTERVAL = 10;
// defines the number of rssi measure cycles (average is calculated over that number of cycles)
const unsigned int NUMBER_OF_RSSI_CYCLES = 3;

// udp port
const unsigned int UDP_PORT = 31337;

// pin configurations
const unsigned int PIN_SLAVE_SELECT = 12;
const unsigned int PIN_SPI_CLOCK = 13;
const unsigned int PIN_SPI_DATA = 14;
const unsigned int PIN_STANDALONE = 4;
const unsigned int PIN_LED = 5;

WebSocketsServer webSocket = WebSocketsServer(81);
ESP8266WebServer server(80);
WiFiUDP Udp;

bool standaloneMode = false;
unsigned int currentRssiStrength = 0;
unsigned long currentLapTime = 0L;

#define CONFIG_VERSION "001"
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
  4000,
  140,
  150,
  "flt-base",
  "flt-base"
};

/*---------------------------------------------------
 * application setup
 *-------------------------------------------------*/
void setup() {
  Serial.begin(115200);
  Serial.println("");
  Serial.println("");
  Serial.println(F("booting"));
  Serial.flush();

  pinMode(A0, INPUT);
  randomSeed(analogRead(0));

  Serial.println(F("setting up ports"));
  pinMode(PIN_SLAVE_SELECT, OUTPUT);
  pinMode(PIN_SPI_DATA, OUTPUT);
  pinMode(PIN_SPI_CLOCK, OUTPUT);
  pinMode(PIN_STANDALONE, INPUT_PULLUP);
  pinMode(PIN_LED, OUTPUT);
  digitalWrite(PIN_LED, HIGH);
  delay(250);
  digitalWrite(PIN_LED, LOW);
  
  Serial.println(F("reading config from eeprom"));
  EEPROM.begin(512);
  loadConfig();
  uint16_t channelData = getSPIFrequencyForChannelIndex(storage.channelIndex);
  RCV_FREQ(channelData);
  Serial.print(F("channel info: "));
  Serial.print(getFrequencyForChannelIndex(storage.channelIndex));
  Serial.print(F(" MHz, "));
  Serial.println(getChannelNameForChannelIndex(storage.channelIndex), HEX);

  Serial.println(F("starting spiffs"));
  SPIFFS.begin();
  Dir dir = SPIFFS.openDir("/");
  Serial.println(F("file list:"));
  while(dir.next()){
    File entry = dir.openFile("r");
    bool isDir = false;
    Serial.print(F("type: "));
    Serial.print((isDir)?"dir":"file");
    Serial.print(F(", name: "));
    Serial.println(entry.name());
    entry.close();
  }
  Serial.println("");

  if (digitalRead(PIN_STANDALONE) == LOW) {
    Serial.println(F("running in standalone mode"));
    standaloneMode = true;
    setupAccessPoint();
  } else {
    Serial.print(F("wifi connecting to ssid "));
    Serial.print(storage.ssid);
    WiFi.begin(storage.ssid, storage.password);
    unsigned int wait = 0;
    while (WiFi.status() != WL_CONNECTED && wait < 15000) {
      delay(500);
      wait += 500;
      Serial.print(F("."));
    }
    Serial.println("");
    if (WiFi.status() != WL_CONNECTED) {
      Serial.println(F("cannot connect to ssid, switching to standalone mode"));
      standaloneMode = true;
      setupAccessPoint();
    }
  }

  Serial.println(F("WiFi set up"));
  Serial.println(F("IP address: "));
  if (standaloneMode) {
    Serial.println(WiFi.softAPIP());
  } else {
    Serial.println(WiFi.localIP());
    Serial.print(F("broadcast IP is "));
    Serial.println(getBroadcastIP());
  }

  if (!standaloneMode) {
    Serial.println(F("starting udp"));
    Udp.begin(UDP_PORT);
  }
  
  if (standaloneMode) {
    Serial.println(F("starting websockets"));
    webSocket.begin();
    webSocket.onEvent(webSocketEvent);
  }

  Serial.println(F("registering webserver endpoints"));
  server.on("/data", HTTP_GET, handleData);
  server.on("/data", HTTP_POST, writeData);
  server.on("/rssi", HTTP_GET, handleRssi);
  server.on("/measure", HTTP_GET, handleMeasure);
  server.on("/wifi", HTTP_GET, handleWifi);
  server.on("/reboot", HTTP_POST, []() {
    server.send(200, "application/json", "{\"status\":\"OK\"}");
    Serial.println(F("restarting"));
    ESP.restart();
  });

  if (standaloneMode) {
    server.on("/", HTTP_GET, []() {
      Serial.println(F("forward from /"));
      if (!handleFileRead("/index.html")) {
         server.send(404, "text/plain", "File not found");
      }
    });
  
    server.onNotFound([]() {
      if (!handleFileRead(server.uri())) {
        server.send(404, "text/plain", "File not found");
      }
    });
  }

  Serial.println(F("starting webserver"));
  server.begin();

  if (!standaloneMode) {
    Serial.println(F("registering device"));
    sendRegister();
  }

}

/*---------------------------------------------------
 * application main loop
 *-------------------------------------------------*/
void loop() {

  if (standaloneMode) {
    webSocket.loop();
  }
  server.handleClient();

  if (!standaloneMode) {
    processUdp();
  }

  static unsigned long timerRssi = 0L;
  static unsigned int rssiOld = 0;
  
  if (timerRssi <= millis()) {
    unsigned int rssi = getRssi();
    currentRssiStrength = rssiOld * 0.25 + rssi * 0.75;
    rssiOld = rssi;
    
    timerRssi = millis() + RSSI_MEASURE_INTERVAL;
    processLapDetection();
  }

  processLed();

}

