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
 * blinking 2 times - BT version command not OK
 * blinking 3 times - BT name comamnd not OK
 * blinking 4 times - BT pin command not ok
 * blinking 10 times - internal failure
 * 
 */
#include <EEPROM.h>
#include "rssi.h"
#include "lapdetector.h"
#include "ledcontrol.h"
#include "storage.h"
#include "wificomm.h"
#include "btcomm.h"
#include "frequency.h"
#include "rx5808.h"

// debug mode flag
//#define DEBUG
//#define MEASURE

// pin configurations
const unsigned int PIN_SPI_SLAVE_SELECT = 12;
const unsigned int PIN_SPI_CLOCK = 13;
const unsigned int PIN_SPI_DATA = 14;
const unsigned int PIN_LED = 5;

lap::Rssi rssi;
ledio::LedControl led(PIN_LED);
util::Storage storage;
lap::LapDetector lapDetector(&storage, &rssi);
freq::Frequency frequency;
comm::WifiComm wifiComm(&storage);
comm::BtComm btComm(&storage, &rssi);
radio::Rx5808 rx5808(PIN_SPI_CLOCK, PIN_SPI_DATA, PIN_SPI_SLAVE_SELECT);

enum class stateEnum {
	STARTUP,
	CALIBRATION,
	CALIBRATION_DONE,
	SCAN,
	RACE,
	ERROR
};
stateEnum state = stateEnum::STARTUP;

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
#endif
#ifdef MEASURE
	Serial.println(F("INFO: running measure mode"));
#endif
	Serial.flush();
#else
	Serial.begin(9600);
	delay(100);
	Serial.println();
	Serial.flush();
	delay(1000);
#endif
	// blink led to show startup
	for (int i = 0; i < 20; i++) {
		led.toggle();
		delay(25);
	}
	led.off();

	randomSeed(analogRead(0));
#ifdef DEBUG
	Serial.println(F("setting up ports"));
#endif
	rx5808.init();

#ifdef DEBUG
	Serial.println(F("reading config from eeprom"));
#endif
	EEPROM.begin(512);
	storage.load();
	lapDetector.init();
	
#ifdef DEBUG
	Serial.println(F("setting radio frequency"));
#endif
	unsigned int channelData = frequency.getSPIFrequencyForChannelIndex(storage.getChannelIndex());
	rx5808.freq(channelData);
#ifdef DEBUG
	Serial.print(F("channel info: "));
	Serial.print(frequency.getFrequencyForChannelIndex(storage.getChannelIndex()));
	Serial.print(F(" MHz, "));
	Serial.println(frequency.getChannelNameForChannelIndex(storage.getChannelIndex()));
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
#if not defined(DEBUG) && not defined(MEASURE)
	int bterr = btComm.connect();
	if (bterr < 0) {
#ifdef DEBUG
		Serial.print(F("bt module error: "));
		Serial.println(bterr);
#endif
		if  (bterr == comm::btErrorCode::MODULE_NOT_RESPONDING) {
			blinkError(2);
		}
		if  (bterr == comm::btErrorCode::NAME_COMMAND_FAILED) {
			blinkError(3);
		}
		if  (bterr == comm::btErrorCode::PIN_COMMAND_FAILED) {
			blinkError(4);
		}
	}
#endif

	// blink 5 times to show end of setup() and start of rssi offset detection
	led.mode(ledio::modes::BLINK_SEQUENCE);
	led.blinkSequence(5, 15, 250);
#ifdef DEBUG
	Serial.println(F("entering main loop"));
#endif
}

/*---------------------------------------------------
 * application main loop
 *-------------------------------------------------*/
void loop() {

	led.run();
	rssi.process();
#ifdef MEASURE
	Serial.print(F("VAR: rssi="));
	Serial.println(rssi.getRssi());
#endif

	if (state == stateEnum::STARTUP) {
#if defined(DEBUG) || defined(MEASURE)
		Serial.println(F("STATE: STARTUP"));
#endif
		// find the noise level
		rssi.setRssiOffset(0);
		unsigned long rssiRaw = 0L;
		// do 200 rounds = 2 sec
		for (unsigned int i = 0; i < 200; i++) {
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
		Serial.print("VAR: rssi_offset=");
		Serial.println(rssiRaw);
#endif
		state = stateEnum::CALIBRATION;
		lapDetector.enableCalibrationMode();
		led.interval(50);
		led.mode(ledio::modes::BLINK);
	} else if (state == stateEnum::CALIBRATION) {
#ifdef MEASURE
		Serial.println(F("STATE: CALIBRATION"));
#endif
		if (lapDetector.process()) {
#ifdef MEASURE
			Serial.println(F("INFO: lap detected, calibration is done"));
#endif
			state = stateEnum::CALIBRATION_DONE;
		}
	} else if (state == stateEnum::CALIBRATION_DONE) {
#if defined(DEBUG) || defined(MEASURE)
		Serial.println(F("STATE: CALIBRATION_DONE"));
#endif
		state = stateEnum::RACE;
		led.mode(ledio::modes::OFF);
	} else if (state == stateEnum::RACE) {
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
			Serial.println("lap detected");
			Serial.print("rssi=");
			Serial.println(lapDetector.getLastLapRssi());
			Serial.print(F("laptime="));
			Serial.println(lapDetector.getLastLapTime());
#endif
		}
	} else if (state == stateEnum::ERROR) {
#ifdef MEASURE
		Serial.println(F("STATE: ERROR"));
#endif

	} else {
		blinkError(10);
	}

	// if we are not in standalone mode, process udp
	if (wifiComm.isConnected()) {
		wifiComm.processIncommingMessage();
	}

	if (btComm.isConnected()) {
		btComm.processIncommingMessage();
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