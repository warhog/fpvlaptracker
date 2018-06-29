

/*---------------------------------------------------
 * scan all channels
 *-------------------------------------------------*/
// String scanChannels() {
//   String result = "{\"channels\":[";
//   unsigned int channelIndexMax = 0;
//   unsigned int rssiMax = 0;
//   unsigned long timer = 0;
//   for (unsigned int i = 0; i < NR_OF_FREQUENCIES; i++) {
//     uint16_t channelData = pgm_read_word_near(channelTable + i);
//     RCV_FREQ(channelData);
//     uint16_t channelFreq = pgm_read_word_near(channelFreqTable + i);
//     delay(50);
//     rssi.process();
//     unsigned int rssiRaw = rssi.scan();
//     if (rssiRaw > rssiMax) {
//       rssiMax = rssiRaw;
//       channelIndexMax = i;
//     }
//     result += "{\"freq\":";
//     result += pgm_read_word_near(channelFreqTable + i);
//     result += ",\"rssi\":";
//     result += rssiRaw;
//     result += "}";
//     if (i < (NR_OF_FREQUENCIES - 1)) {
//       result += ",";
//     }
//   }
//   result += "],\"maxFreq\":";
//   result += pgm_read_word_near(channelFreqTable + channelIndexMax);
//   result += ",\"maxRssi\":";
//   result += rssiMax;
//   result += "}";
//   // restore stored channel
//   uint16_t channelData = pgm_read_word_near(channelTable + storage.channelIndex);
//   RCV_FREQ(channelData);
//   return result;
// }

