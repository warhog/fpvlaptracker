#include "wifiap.h"

using namespace comm;

void WifiAp::connect() {
#ifdef DEBUG
    Serial.println(F("setting up wifi ap"));
#endif
    WiFi.softAP("fltunit", "fltunit");
#ifdef DEBUG
    IPAddress myIP = WiFi.softAPIP();
    Serial.print(F("AP IP address: "));
    Serial.println(myIP);
#endif
    this->_connected = true;
}

void WifiAp::disconnect() {
    this->_connected = false;
    WiFi.softAPdisconnect(true);
}