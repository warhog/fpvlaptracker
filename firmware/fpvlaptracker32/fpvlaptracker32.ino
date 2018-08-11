/*
 * fpvlaptracker32 firmware
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
 * Copyright (c) 2017-2018 warhog
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
 * blinking 2 times - BT name command not OK
 * blinking 3 times - mdns not started
 * blinking 10 times - internal failure
 * 
 */
#include <EEPROM.h>
#include <BluetoothSerial.h>
#include <ESPmDNS.h>
#include <WiFi.h>

#include "rssi.h"
#include "lapdetector.h"
#include "ledcontrol.h"
#include "storage.h"
#include "wificomm.h"
#include "btcomm.h"
#include "frequency.h"
#include "rx5808.h"
#include "statemanager.h"
#include "webupdate.h"

// debug mode flags
//#define DEBUG
//#define MEASURE

// pin configurations
const unsigned int PIN_SPI_SLAVE_SELECT = 16;
const unsigned int PIN_SPI_CLOCK = 17;
const unsigned int PIN_SPI_DATA = 18;
const unsigned int PIN_LED = 19;
const unsigned int PIN_ANALOG_RSSI = 32;
const unsigned int PIN_WEB_UPDATE = 21;
const unsigned int PIN_ANALOG_BATTERY = 33;

lap::Rssi rssi(PIN_ANALOG_RSSI);
ledio::LedControl led(PIN_LED);
util::Storage storage;
lap::LapDetector lapDetector(&storage, &rssi);
comm::WifiComm wifiComm(&storage);
radio::Rx5808 rx5808(PIN_SPI_CLOCK, PIN_SPI_DATA, PIN_SPI_SLAVE_SELECT, PIN_ANALOG_RSSI);
BluetoothSerial btSerial;
comm::BtComm btComm(&btSerial, &storage, &rssi, &rx5808);
statemanagement::StateManager stateManager;
unsigned long fastRssiTimeout = 0L;
bool webUpdateMode = false;
WebUpdate webUpdate;

void setState(statemanagement::state_enum state) {
	stateManager.setState(state);
	btComm.setState(stateManager.toString(stateManager.getState()));
}

/*---------------------------------------------------
 * application setup
 *-------------------------------------------------*/
void setup() {
#if defined(DEBUG) || defined(MEASURE)
	Serial.begin(115200);
	Serial.println("");
	Serial.println("");
#ifdef DEBUG
	Serial.println(F("booting"));
	uint64_t chipid = ESP.getEfuseMac();
	// upper 2 bytes
	Serial.printf("ESP32 Chip ID = %04X", static_cast<uint16_t>(chipid >> 32));
	// lower 4 bytes
	Serial.printf("%08X\n", static_cast<uint32_t>(chipid));
#endif
#ifdef MEASURE
	Serial.println(F("INFO: running measure mode"));
#endif
	Serial.flush();
#endif

	// blink led to show startup
	for (int i = 0; i < 20; i++) {
		led.toggle();
		delay(25);
	}
	led.off();

	btComm.addSubscriber(&stateManager);

	randomSeed(analogRead(0));
#ifdef DEBUG
	Serial.println(F("setting up ports"));
#endif
	rx5808.init();
	pinMode(PIN_WEB_UPDATE, INPUT_PULLDOWN);
	if (digitalRead(PIN_WEB_UPDATE) == HIGH) {
#ifdef DEBUG
		Serial.println(F("enabling webupdate mode"));
#endif		
		webUpdateMode = true;
	}

#ifdef DEBUG
	Serial.println(F("reading config from eeprom"));
#endif
	EEPROM.begin(512);
	storage.load();

	if (webUpdateMode) {
		// running in webupdate mode
#ifdef DEBUG
		Serial.println(F("starting webupdate mode"));
		Serial.println(F("setting up wifi ap"));
#endif
		WiFi.softAP("fltunit", "fltunit");
#ifdef DEBUG
		IPAddress myIP = WiFi.softAPIP();
		Serial.print(F("AP IP address: "));
		Serial.println(myIP);
#endif

#ifdef DEBUG
		Serial.println(F("setting up mdns"));
#endif
		if (!MDNS.begin("fltunit")) {
#ifdef DEBUG
			Serial.println(F("error setting up MDNS responder!"));
#endif
			blinkError(3);
		}
		webUpdate.setVersion("1.0");
		webUpdate.begin();
	    MDNS.addService("http", "tcp", 80);

		// blink 5 times to show end of setup() and start of rssi offset detection
		led.mode(ledio::modes::BLINK_SEQUENCE);
		led.blinkSequence(5, 500, 1000);
	} else {
#ifdef DEBUG
		Serial.println(F("starting in normal mode"));
#endif
		// non webupdate mode
		lapDetector.init();

#ifdef DEBUG
		Serial.println(F("setting radio frequency"));
#endif
		unsigned int channelData = freq::Frequency::getSPIFrequencyForChannelIndex(storage.getChannelIndex());
		rx5808.freq(channelData);
#ifdef DEBUG
		Serial.print(F("channel info: "));
		Serial.print(freq::Frequency::getFrequencyForChannelIndex(storage.getChannelIndex()));
		Serial.print(F(" MHz, "));
		Serial.println(freq::Frequency::getChannelNameForChannelIndex(storage.getChannelIndex()));
#endif

#ifdef DEBUG
		Serial.println(F("connecting wifi"));
#endif
		wifiComm.connect();
		if (wifiComm.isConnected()) {
			wifiComm.reg();
		}

#ifdef DEBUG
		Serial.println(F("connecting bluetooth"));
#endif
		int bterr = btComm.connect();
		if (bterr < 0) {
#ifdef DEBUG
			Serial.print(F("bt module error: "));
			Serial.println(bterr);
#endif
			if  (bterr == comm::btErrorCode::NAME_COMMAND_FAILED) {
				blinkError(2);
			}
		}

		// blink 5 times to show end of setup() and start of rssi offset detection
		led.mode(ledio::modes::BLINK_SEQUENCE);
		led.blinkSequence(5, 15, 250);
	}

#ifdef DEBUG
	Serial.println(F("entering main loop"));
#endif
}

/*---------------------------------------------------
 * application main loop
 *-------------------------------------------------*/
void loop() {

	led.run();
	if (webUpdateMode) {
		webUpdate.run();
	} else {
		rssi.process();
#ifdef MEASURE
		Serial.print(F("VAR: rssi="));
		Serial.println(rssi.getRssi());
#endif

		if (stateManager.isStateStartup()) {
#if defined(DEBUG) || defined(MEASURE)
			Serial.println(F("STATE: STARTUP"));
#endif
			// find the noise level
			rssi.setRssiOffset(0);
			unsigned long rssiRaw = 0L;
			// do 200 rounds = 2 sec
			for (unsigned int i = 0; i < 500; i++) {
				rssi.process();
				rssiRaw += rssi.getRssi();
				led.run();
				delay(10);
			}
			rssiRaw /= 200;
			rssi.setRssiOffset(static_cast<unsigned int>(rssiRaw));
#ifdef DEBUG
			Serial.print(F("rssi offset: "));
			Serial.println(rssiRaw);
#endif
#ifdef MEASURE
			Serial.print(F("VAR: rssi_offset="));
			Serial.println(rssiRaw);
#endif
			setState(statemanagement::state_enum::CALIBRATION);
			lapDetector.enableCalibrationMode();
#ifdef DEBUG
			Serial.println(F("switch to calibration mode"));
#endif
			led.interval(50);
			led.mode(ledio::modes::BLINK);
		} else if (stateManager.isStateScan()) {
			rx5808.scan();
			if (rx5808.isScanDone()) {
				// scan is done, start over
				unsigned int currentChannel = rx5808.getScanChannelIndex();
				btComm.sendScanData(freq::Frequency::getFrequencyForChannelIndex(currentChannel), rx5808.getScanResult());
				currentChannel++;
				if (currentChannel >= freq::NR_OF_FREQUENCIES) {
					currentChannel = 0;
				}
				rx5808.startScan(currentChannel);
			}
		} else if (stateManager.isStateRssi()) {
			if (millis() > fastRssiTimeout) {
				fastRssiTimeout = millis() + 100;
				btComm.sendFastRssiData(rssi.getRssi());
			}
		} else if (stateManager.isStateCalibration()) {
#ifdef MEASURE
			Serial.println(F("STATE: CALIBRATION"));
#endif
			if (lapDetector.process()) {
#ifdef MEASURE
				Serial.println(F("INFO: lap detected, calibration is done"));
#endif
				setState(statemanagement::state_enum::CALIBRATION_DONE);
			}
		} else if (stateManager.isStateCalibrationDone()) {
#if defined(DEBUG) || defined(MEASURE)
			Serial.println(F("STATE: CALIBRATION_DONE"));
#endif
			setState(statemanagement::state_enum::RACE);
			led.mode(ledio::modes::OFF);
		} else if (stateManager.isStateRace()) {
#ifdef MEASURE
			Serial.println(F("STATE: RACE"));
#endif
			if (lapDetector.process()) {
#ifdef MEASURE
				Serial.println(F("INFO: lap detected"));
				Serial.print(F("VAR: lastlaptime="));
				Serial.println(lapDetector.getLastLapTime());
				Serial.print(F("VAR: lastlaprssi="));
				Serial.println(lapDetector.getLastLapRssi());
#endif
				led.oneshot();
				if (wifiComm.isConnected()) {
					wifiComm.lap(lapDetector.getLastLapTime(), lapDetector.getLastLapRssi());
				}
				if (btComm.isConnected()) {
					btComm.lap(lapDetector.getLastLapTime(), lapDetector.getLastLapRssi());
				}
#ifdef DEBUG
				Serial.println(F("lap detected"));
				Serial.print(F("rssi="));
				Serial.println(lapDetector.getLastLapRssi());
				Serial.print(F("laptime="));
				Serial.println(lapDetector.getLastLapTime());
#endif
			}
		} else if (stateManager.isStateError()) {
#ifdef MEASURE
			Serial.println(F("STATE: ERROR"));
#endif
		} else {
			blinkError(10);
		}

		// if we are in network mode, process udp
		if (wifiComm.isConnected()) {
			wifiComm.processIncommingMessage();
		}

		if (btComm.isConnected()) {
			btComm.processIncommingMessage();
		}
	}
}

/*---------------------------------------------------
 * error code blinker
 *-------------------------------------------------*/
void blinkError(unsigned int errorCode) {
#ifdef DEBUG
	Serial.print(F("error code: "));
	Serial.println(errorCode);
	Serial.flush();
#endif
	led.mode(ledio::modes::STATIC);
	while (true) {
		for (int i = 0; i < errorCode; i++) {
			led.on();
			delay(100);
			led.off();
			delay(200);
		}
		delay(1000);
	}
}

