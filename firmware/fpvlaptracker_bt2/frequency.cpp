#include "frequency.h"

using namespace freq;

/*---------------------------------------------------
* channel mapping functions
*-------------------------------------------------*/
unsigned int Frequency::getChannelIndexForFrequency(unsigned int frequency) {
    unsigned int ci = -1;
    for (unsigned int i = 0; i < NR_OF_FREQUENCIES; i++) {
        unsigned int freq = pgm_read_word_near(channelFreqTable + i);
        if (freq == frequency) {
            ci = i;
            break;
        }
    }
    return ci;
}

unsigned int Frequency::getFrequencyForChannelIndex(unsigned int channelIndex) {
    return pgm_read_word_near(channelFreqTable + channelIndex);
}

String Frequency::getChannelNameForChannelIndex(unsigned int channelIndex) {
     return channelNames[channelIndex];
}

unsigned int Frequency::getSPIFrequencyForChannelIndex(unsigned int channelIndex) {
    return pgm_read_word_near(channelTable + channelIndex);
}
