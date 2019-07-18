#pragma once

#include <Arduino.h>

namespace freq {

    const unsigned int NR_OF_FREQUENCIES = 48;

    const unsigned int LOWEST_FREQUENCY = 5362;
    const unsigned int HIGHEST_FREQUENCY = 5945;

    // channels with their Mhz Values
    const unsigned int CHANNEL_FREQ_TABLE[] PROGMEM = {
        // Channel 1 - 8
        5865, 5845, 5825, 5805, 5785, 5765, 5745, 5725, // Band A
        5733, 5752, 5771, 5790, 5809, 5828, 5847, 5866, // Band B
        5705, 5685, 5665, 5645, 5885, 5905, 5925, 5945, // Band E
        5740, 5760, 5780, 5800, 5820, 5840, 5860, 5880, // Band F / Airwave
        5658, 5695, 5732, 5769, 5806, 5843, 5880, 5917, // Band C / Immersion Raceband
        5362, 5399, 5436, 5473, 5510, 5547, 5584, 5621  // Band L / 5.3
    };

    // the channel names
    const String CHANNEL_NAMES[NR_OF_FREQUENCIES] = {
            "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", // Band A
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", // Band B
            "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", // Band E
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", // Band F / airwave
            "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", // Band R / immersion raceband
            "L1", "L2", "L3", "L4", "L5", "L6", "L7", "L8", // Band L / 5.3
        };

    class Frequency {
    public:
        static unsigned int getChannelIndexForFrequency(unsigned int frequency);
        static unsigned int getFrequencyForChannelIndex(unsigned int channelIndex);
        static String getChannelNameForChannelIndex(unsigned int channelIndex);
        static String getChannelNameForFrequency(unsigned int frequency);
    private:
        Frequency() {};
    };


}