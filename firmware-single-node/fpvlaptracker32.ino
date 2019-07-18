/*
 * fpvlaptracker32 firmware
 * fpv lap tracking software that uses rx5808 and esp32 to keep track of the fpv video signals
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
 * blinking 2 times - BT init failed
 * blinking 3 times - mdns not started
 * blinking 9 times - shutdown voltage
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
#include "batterymgr.h"
#include "wifiwebserver.h"
#include "wifiap.h"

// debug mode flags
//#define DEBUG
//#define MEASURE

#define VERSION "FLT32-R1.4"

// pin configurations
const unsigned int PIN_SPI_SLAVE_SELECT = 16;
const unsigned int PIN_SPI_CLOCK = 17;
const unsigned int PIN_SPI_DATA = 18;
const unsigned int PIN_LED = 19;
const unsigned int PIN_ANALOG_RSSI = 32;
const unsigned int PIN_WEB_UPDATE = 34;
const unsigned int PIN_ANALOG_BATTERY = 33;

lap::Rssi rssi(PIN_ANALOG_RSSI);
ledio::LedControl led(PIN_LED);
util::Storage storage;
lap::LapDetector lapDetector(&storage, &rssi);
radio::Rx5808 rx5808(PIN_SPI_CLOCK, PIN_SPI_DATA, PIN_SPI_SLAVE_SELECT, PIN_ANALOG_RSSI);
BluetoothSerial btSerial;
battery::BatteryMgr batteryMgr(PIN_ANALOG_BATTERY, &storage);
statemanagement::StateManager stateManager;
unsigned long loopTime = 0L;
unsigned long lastLoopTimeRun = 0L;
comm::WifiWebServer wifiWebServer(&storage, &rssi, &rx5808, &lapDetector, &batteryMgr, VERSION, &stateManager, &loopTime);
comm::WifiComm wifiComm(&storage, &rssi, &rx5808, &lapDetector, &batteryMgr, VERSION, &stateManager, &loopTime);
comm::BtComm btComm(&btSerial, &storage, &rssi, &rx5808, &lapDetector, &batteryMgr, VERSION, &stateManager, &wifiComm, &loopTime);
comm::WifiAp wifiAp;
unsigned long fastRssiTimeout = 0L;
unsigned long lowVoltageTimeout = 0L;

/*---------------------------------------------------
 * application setup
 *-------------------------------------------------*/
void setup() {
	led.off();
#if defined(DEBUG) || defined(MEASURE)
	Serial.begin(115200);
	Serial.println("");
	Serial.println("");
#ifdef DEBUG
	Serial.println(F("booting"));
    uint64_t chipId = ESP.getEfuseMac();
    char strChipId[15] = { 0 };
    sprintf(strChipId, "%u", chipId);
    String chipString = strChipId;
    Serial.printf("Chip ID: %s\n", chipString.c_str());
#endif
#ifdef MEASURE
	Serial.println(F("INFO: running measure mode"));
#endif
	Serial.flush();
#endif

	randomSeed(analogRead(PIN_ANALOG_BATTERY));

#ifdef DEBUG
	Serial.println(F("setting up ports"));
#endif
	rx5808.init();
	pinMode(PIN_WEB_UPDATE, INPUT_PULLUP);
	if (digitalRead(PIN_WEB_UPDATE) == LOW) {
#ifdef DEBUG
		Serial.println(F("force wifi ap mode"));
#endif
		wifiAp.connect();
	}

#ifdef DEBUG
	Serial.println(F("reading config from eeprom"));
#endif
	EEPROM.begin(512);
	storage.load();

#ifdef DEBUG
	Serial.println(F("setup batterymgr"));
#endif
	batteryMgr.detectCellsAndSetup();
#ifdef DEBUG
	Serial.printf("alarmVoltage: %f, shutdownVoltage: %f\n", batteryMgr.getAlarmVoltage(), batteryMgr.getShutdownVoltage());
#endif
	// blink <cell number> of times to give feedback on number of connected battery cells and signal startup
	led.mode(ledio::modes::BLINK_SEQUENCE);
	led.blinkSequence(batteryMgr.getCells(), 15, 250);
	for (unsigned int i = 0; i < ((250 + 15) * batteryMgr.getCells()); i++) {
		led.run();
		delay(1);
	}
	led.off();

	lapDetector.init();

#ifdef DEBUG
	Serial.println(F("setting radio frequency"));
#endif
	rx5808.freq(storage.getFrequency());
#ifdef DEBUG
	Serial.print(F("channel info: "));
	Serial.print(storage.getFrequency());
	Serial.print(F(" MHz, "));
	Serial.println(freq::Frequency::getChannelNameForFrequency(storage.getFrequency()));
#endif

	if (wifiAp.isConnected()) {
		wifiWebServer.begin();
	} else {
		// try to connect to wifi, if ssid not found, start bluetooth
		wifiConnect();
	}

	// no wifi found, start bluetooth
	if (!wifiComm.isConnected() && !wifiAp.isConnected()) {
		bluetoothConnect();
		if (btComm.isConnected()) {
			led.blinkSequence(UINT_MAX, 125, 2000);
			led.mode(ledio::modes::BLINK_SEQUENCE);
		}
	}

	if (wifiWebServer.isConnected()) {
#ifdef DEBUG
		Serial.println(F("setting up mdns"));
#endif
		MDNS.addService("http", "tcp", 80);
		if (!MDNS.begin("flt-unit")) {
#ifdef DEBUG
			Serial.println(F("error setting up MDNS responder!"));
#endif
			blinkError(3);
		}
	}

#ifdef DEBUG
	Serial.println(F("entering main loop"));
#endif
}

/*---------------------------------------------------
 * application main loop
 *-------------------------------------------------*/
void loop() {

	batteryMgr.measure();
	if (batteryMgr.isShutdown()) {
#ifdef DEBUG
		Serial.println(F("voltage isShutdown"));
#endif
		blinkError(9);
	} else if (batteryMgr.isAlarm() && millis() > lowVoltageTimeout) {
		// undervoltage warning
#ifdef DEBUG
		Serial.println(F("voltage isAlarm"));
#endif
		lowVoltageTimeout = millis() + (30 * 1000);
		if (btComm.isConnected() && btComm.hasClient()) {
#ifdef DEBUG
			Serial.println(F("bt voltage sendAlarm"));
#endif
			btComm.sendVoltageAlarm();
		}
		if (wifiComm.isConnected()) {
#ifdef DEBUG
			Serial.println(F("wifi voltage sendAlarm"));
#endif
			wifiComm.sendVoltageAlarm();
		}
	}

	led.run();
	
	rssi.process();
#ifdef MEASURE
	Serial.print(F("VAR: rssi="));
	Serial.println(rssi.getRssi());
#endif

	if (stateManager.isStateStartup()) {
#if defined(DEBUG) || defined(MEASURE)
		Serial.println(F("STATE: STARTUP"));
#endif
		stateManager.update(statemanagement::state_enum::CALIBRATION);
		lapDetector.enableCalibrationMode();
		rssi.setFilterRatio(storage.getFilterRatioCalibration());
#ifdef DEBUG
		Serial.println(F("switch to calibration mode"));
#endif
	} else if (stateManager.isStateDeepscan()) {
		rx5808.scan();
		if (rx5808.isScanDone()) {
			// scan is done, start over
			unsigned int currentFrequency = rx5808.getScanFrequency();
			if (btComm.isConnected()) {
				btComm.sendScanData(currentFrequency, rx5808.getScanResult());
			} else if (wifiComm.isConnected()) {
				wifiComm.sendScanData(currentFrequency, rx5808.getScanResult());
			}
			currentFrequency++;
			if (currentFrequency >= freq::HIGHEST_FREQUENCY) {
				currentFrequency = freq::LOWEST_FREQUENCY;
			}
			rx5808.startScan(currentFrequency);
		}
	} else if (stateManager.isStateScan()) {
		rx5808.scan();
		if (rx5808.isScanDone()) {
			// scan is done, start over
			int currentChannel = rx5808.getScanChannel();
			if (currentChannel != 65535) {
				if (btComm.isConnected()) {
					btComm.sendScanData(freq::Frequency::getFrequencyForChannelIndex(currentChannel), rx5808.getScanResult());
				} else if (wifiComm.isConnected()) {
					wifiComm.sendScanData(freq::Frequency::getFrequencyForChannelIndex(currentChannel), rx5808.getScanResult());
				}
			}
			currentChannel++;
			if (currentChannel >= freq::NR_OF_FREQUENCIES) {
				currentChannel = 0;
			}
			rx5808.setScanChannel(currentChannel);
			rx5808.startScan(freq::Frequency::getFrequencyForChannelIndex(currentChannel));
		}
	} else if (stateManager.isStateRssi()) {
		if (millis() > fastRssiTimeout) {
			fastRssiTimeout = millis() + 250;
			if (btComm.isConnected()) {
				btComm.sendFastRssiData(rssi.getRssi());
			} else if (wifiComm.isConnected()) {
				wifiComm.sendFastRssiData(rssi.getRssi());
			}
		}
	} else if (stateManager.isStateCalibration()) {
#ifdef MEASURE
		Serial.println(F("STATE: CALIBRATION"));
#endif
		if (lapDetector.process()) {
#ifdef MEASURE
			Serial.println(F("INFO: lap detected, calibration is done"));
#endif
			stateManager.update(statemanagement::state_enum::CALIBRATION_DONE);
		}
	} else if (stateManager.isStateCalibrationDone()) {
#if defined(DEBUG) || defined(MEASURE)
		Serial.println(F("STATE: CALIBRATION_DONE"));
#endif
		if (btComm.isConnected()) {
			btComm.sendCalibrationDone();
		}
		if (wifiComm.isConnected()) {
			wifiComm.sendCalibrationDone();
		}
		rssi.setFilterRatio(storage.getFilterRatio());
		stateManager.update(statemanagement::state_enum::RACE);
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
	} else if (stateManager.getState() == statemanagement::state_enum::SWITCH_TO_BLUETOOTH) {
		if (wifiWebServer.isConnected()) {
			wifiWebServer.disconnect();
		}
		if (wifiComm.isConnected()) {
			wifiComm.disconnect();
		}
		if (wifiAp.isConnected()) {
			wifiAp.disconnect();
		}
		bluetoothConnect();
		stateManager.update(statemanagement::state_enum::RESTORE_STATE);
	} else if (stateManager.getState() == statemanagement::state_enum::VREF_OUTPUT) {
		if (wifiWebServer.isConnected()) {
			wifiWebServer.disconnect();
		}
		if (wifiComm.isConnected()) {
			wifiComm.disconnect();
		}
		if (wifiAp.isConnected()) {
			wifiAp.disconnect();
		}
		if (btComm.isConnected()) {
			btComm.disconnect();
		}
		batteryMgr.enableVrefOutput();
	} else if (stateManager.isStateError()) {
#ifdef MEASURE
		Serial.println(F("STATE: ERROR"));
#endif
	} else {
		blinkError(10);
	}

	if (wifiComm.isConnected()) {
		// if we are in network mode, process udp
		wifiComm.processIncomingMessage();
	} else if (btComm.isConnected()) {
		// in bluetooth mode process the incoming bluetooth serial data
		btComm.processIncomingMessage();
	}

	if (wifiWebServer.isConnected()) {
		// handle the wifi webserver data
		wifiWebServer.handle();
	}

	loopTime = micros() - lastLoopTimeRun;
	lastLoopTimeRun = micros();
}

void bluetoothConnect() {
#ifdef DEBUG
	Serial.println(F("connecting bluetooth"));
#endif
	int bterr = btComm.connect();
	if (bterr != comm::btErrorCode::OK) {
#ifdef DEBUG
		Serial.print(F("bt module error: "));
		Serial.println(bterr);
#endif
		if (bterr == comm::btErrorCode::INIT_FAILED) {
			blinkError(2);
		}
	}
#ifdef DEBUG
	Serial.println(F("bluetooth connected"));
#endif
}

void wifiConnect() {
#ifdef DEBUG
	Serial.println(F("connecting wifi"));
#endif
	wifiComm.connect();
	if (wifiComm.isConnected()) {
#ifdef DEBUG
		Serial.println(F("wifi connected, starting node registration"));
#endif
		wifiComm.reg();
#ifdef DEBUG
		Serial.println(F("node registration done, starting webserver"));
#endif
		wifiWebServer.begin();
#ifdef DEBUG
		Serial.println(F("node registration done"));
	} else {
		Serial.println(F("wifi not connected"));
#endif
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

