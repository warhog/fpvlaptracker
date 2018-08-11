// the following code is taken directly from chickadee laptimer58 project
// the only change is putting it in a class and changing variable and function names
// scanner functionality was also added
// https://github.com/chickadee-tech/laptimer58
/*
 * SPI driver based on fs_skyrf_58g-main.c Written by Simon Chambers
 * TVOUT by Myles Metzel
 * Scanner by Johan Hermen
 * Inital 2 Button version by Peter (pete1990)
 * Refactored and GUI reworked by Marko Hoepken
 * Universal version my Marko Hoepken
 * Diversity Receiver Mode and GUI improvements by Shea Ivey
 * OLED Version by Shea Ivey
 * Seperating display concerns by Shea Ivey
 * 
The MIT License (MIT)
Copyright (c) 2015 Marko Hoepken
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
#pragma once

#include <Arduino.h>
#include "frequency.h"

namespace radio {

    enum class scan_state {
        SET,
        SCAN,
        DONE
    };

    class Rx5808 {
    public:
        Rx5808(unsigned int pinSpiClock, unsigned int pinSpiData, unsigned int pinSpiSlaveSelect, unsigned int pinRssi);
        void freq(unsigned int channelData);
        void init();
        void startScan(unsigned int channelIndex) {
            this->_scanState = scan_state::SET;
            this->_scanChannelIndex = channelIndex;
        }
        unsigned int getScanChannelIndex() {
            return this->_scanChannelIndex;
        }
        bool isScanDone() {
            return this->_scanState == scan_state::DONE;
        }
        void stopScan() {
            this->freq(this->_freq);
        }
        unsigned int getScanResult() {
            return this->_scanLastRssi;
        }
        void scan();

    private:
        unsigned int _pinSpiClock;;
        unsigned int _pinSpiData;
        unsigned int _pinSpiSlaveSelect;
        unsigned int _pinRssi;
        unsigned int _freq;
        
        unsigned long _scanLastRun;
        unsigned int _scanLastRssi;
        scan_state _scanState;
        unsigned int _scanChannelIndex;
        
        void serialSendBit0();
        void serialSendBit1();
        void serialEnableLow();
        void serialEnableHigh();
    };
}
