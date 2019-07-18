#include "frequency.h"

using namespace freq;

/*---------------------------------------------------
* channel mapping functions
*-------------------------------------------------*/
unsigned int Frequency::getChannelIndexForFrequency(unsigned int frequency) {
    unsigned int ci = -1;
    for (unsigned int i = 0; i < NR_OF_FREQUENCIES; i++) {
        unsigned int freq = pgm_read_word_near(CHANNEL_FREQ_TABLE + i);
        if (freq == frequency) {
            ci = i;
            break;
        }
    }
    return ci;
}

unsigned int Frequency::getFrequencyForChannelIndex(unsigned int channelIndex) {
    if (channelIndex >= (NR_OF_FREQUENCIES - 1)) {
        return pgm_read_word_near(CHANNEL_FREQ_TABLE + 0);
    }
    return pgm_read_word_near(CHANNEL_FREQ_TABLE + channelIndex);
}

String Frequency::getChannelNameForChannelIndex(unsigned int channelIndex) {
    if (channelIndex >= (NR_OF_FREQUENCIES - 1)) {
        return CHANNEL_NAMES[0];
    }
    return CHANNEL_NAMES[channelIndex];
}

String Frequency::getChannelNameForFrequency(unsigned int frequency) {
    int channelIndex = getChannelIndexForFrequency(frequency);
    if (channelIndex == -1) {
        return "-";
    }
    return CHANNEL_NAMES[channelIndex];
}
