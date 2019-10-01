#include "wifiwebserver.h"

//#define DEBUG

using namespace comm;

void WifiWebServer::sendJson() {
    String result("");
    serializeJson(this->_jsonDocument, result);
    this->_server.send(200, "application/json", result);
}

String WifiWebServer::concat(String text) {
    String temp(comm::WifiWebServerFiles::header);
    temp.replace("%VERSION%", VERSION);
    temp.replace("%CHIPID%", comm::CommTools::getChipIdAsString());
    temp.replace("%DATETIME%", VERSION_DATETIME);
    String versionCommit = "";
    if (VERSION_COMMIT != "NO_TRAVIS_BUILD") {
        versionCommit = "(" VERSION_COMMIT ")";
    }
    temp.replace("%COMMIT%", versionCommit);
    return temp + text + comm::WifiWebServerFiles::footer;
}

void WifiWebServer::disconnectClients() {
    this->_server.client().setNoDelay(true);
    delay(100);
    this->_server.client().flush();
    this->_server.client().stop();
    this->_server.stop();
    delay(100);
}

void WifiWebServer::rebootNode() {
    this->_server.send(200, "text/html", this->concat("<script>window.setTimeout(function() { rebooting(); }, 50);</script>rebooting node..."));
    this->disconnectClients();
    ESP.restart();
}

void WifiWebServer::begin() {

	this->_server.onNotFound([&]() {
        String message = "File Not Found\n\n";
        message += "URI: ";
        message += this->_server.uri();
        message += "\nMethod: ";
        message += (this->_server.method() == HTTP_GET) ? "GET" : "POST";
        message += "\nArguments: ";
        message += this->_server.args();
        message += "\n";

        for (uint8_t i = 0; i < this->_server.args(); i++) {
            message += " " + this->_server.argName(i) + ": " + this->_server.arg(i) + "\n";
        }

        this->_server.send(404, "text/plain", this->concat(message));
    });

    this->_server.on("/", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        String temp(comm::WifiWebServerFiles::index);
        temp.replace("%RSSI%", String(this->_rssi->getRssi()));
        this->_server.send(200, "text/html", this->concat(temp));
    });
    
    this->_server.on("/factorydefaults", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", this->concat("really load factory defaults?<br /><a class='button' href='/dofactorydefaults'>yes</a> <a class='button' href='/'>no</a>"));
    });
    this->_server.on("/dofactorydefaults", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_storage->loadFactoryDefaults();
        this->_storage->store();
        this->rebootNode();
    });

    this->_server.on("/wifi", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        String temp(comm::WifiWebServerFiles::wifi);
        temp.replace("%SSID%", this->_storage->getSsid());
        temp.replace("%PASSWORD%", this->_storage->getWifiPassword());
        String wifiList;
        int nrOfWifis = WiFi.scanNetworks(false, false, false, 500);
        for (unsigned int i = 0; i < nrOfWifis; i++) {
            wifiList += "<li>" + WiFi.SSID(i) + " <a href=\"#\" onclick=\"setWifi('" + WiFi.SSID(i) + "');\">use</a></li>";
        }

        temp.replace("%WIFIS%", wifiList);
        this->_server.send(200, "text/html", this->concat(temp));
    });
    this->_server.on("/setwifi", HTTP_POST, [&]() {
        this->_server.sendHeader("Connection", "close");
        if (this->_server.args() == 2) {
            this->_storage->setSsid(this->_server.arg("ssid"));
            this->_storage->setWifiPassword(this->_server.arg("password"));
            this->_storage->store();
            this->rebootNode();
        } else {
            this->_server.sendHeader("Connection", "close");
            this->_server.send(500, "text/html", "invalid arguments");
        }
    });

    // this reset is used for the node web ui
    this->_server.on("/reset", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", this->concat("really restart the tracker node?<br /><a class='button' href='/doreset'>yes</a> <a class='button' href='/'>no</a>"));
    });
    this->_server.on("/doreset", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->rebootNode();
    });

    this->_server.on("/vref", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", this->concat("really goto voltage reference output mode?<br /><a class='button' href='/dovref'>yes</a> <a class='button' href='/'>no</a>"));
    });
    this->_server.on("/dovref", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", this->concat("starting voltage reference output mode, wifi/bluetooth is now disabled! reboot node to exit this mode."));
        this->disconnectClients();
        this->_stateManager->update(statemanagement::state_enum::VREF_OUTPUT);
    });

    this->_server.on("/bluetooth", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", this->concat("really goto bluetooth mode? <b>attention:</b> wifi will not be available anymore till a reboot of the tracker node!<br /><a class='button' href='/dobluetooth'>yes</a> <a class='button' href='/'>no</a>"));
    });
    this->_server.on("/dobluetooth", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", this->concat("switching to bluetooth mode, wifi is now disabled!"));
        this->disconnectClients();
        this->_stateManager->update(statemanagement::state_enum::SWITCH_TO_BLUETOOTH);
    });

    this->_server.on("/update", HTTP_POST, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", this->concat((Update.hasError()) ? "update failed!\n" : "update successful! rebooting...<br /><a class='button' href='/'>back</a>"));
        this->disconnectClients();
        ESP.restart();
    }, [&]() {
        HTTPUpload& upload = this->_server.upload();
        if (upload.status == UPLOAD_FILE_START) {
#ifdef DEBUG
            Serial.setDebugOutput(true);
            Serial.printf("Update: %s\n", upload.filename.c_str());
#endif
            if (!Update.begin()) { //start with max available size
#ifdef DEBUG
                Update.printError(Serial);
#endif
            }
        } else if (upload.status == UPLOAD_FILE_WRITE) {
            if (Update.write(upload.buf, upload.currentSize) != upload.currentSize) {
#ifdef DEBUG
                Update.printError(Serial);
#endif
            }
        } else if (upload.status == UPLOAD_FILE_END) {
            if (Update.end(true)) { //true to set the size to the current progress
#ifdef DEBUG
                Serial.printf("Update Success: %u\nRebooting...\n", upload.totalSize);
#endif
            } else {
#ifdef DEBUG
                Update.printError(Serial);
#endif
            }
#ifdef DEBUG
            Serial.setDebugOutput(false);
#endif
        }
    });
    
    // the /api commands are for the webui
    this->_server.on("/api/rssi", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_jsonDocument.clear();
        this->_jsonDocument["rssi"] = this->_rssi->getRssi();
        this->sendJson();
    });

    // the /api commands are for the webui
    this->_server.on("/api/factorydefaults", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_storage->loadFactoryDefaults();
        this->_storage->store();
        this->_server.send(200, "text/html", "OK");
    });

    // the /api commands are for the webui
    this->_server.on("/api/reboot", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", "OK");
        this->disconnectClients();
        ESP.restart();
    });

    // the /api commands are for the webui
    this->_server.on("/api/devicedata", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "application/json", comm::CommTools::getDeviceDataAsJsonStringFromStorage(this->_storage, this->_stateManager, this->_lapDetector, this->_batteryMgr, *this->_loopTime, this->_rssi, VERSION));
    });

    // the /api commands are for the webui
    this->_server.on("/api/setstate", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_jsonDocument.clear();
        this->_jsonDocument["result"] = "NOK";
#ifdef DEBUG
        Serial.println(F("called /setstate"));
        for (uint8_t i = 0; i < this->_server.args(); i++) {
            Serial.printf("%s=%s\n", this->_server.argName(i).c_str(), this->_server.arg(i).c_str());
        }
#endif
        if (this->_server.args() > 0) {
            String newState = this->_server.arg("state");
#ifdef DEBUG
            Serial.printf("setstate argument: %s\n", newState.c_str());
#endif
            if (newState == "CALIBRATION_DONE") {
                this->_stateManager->setState(statemanagement::state_enum::CALIBRATION_DONE);
                this->_lapDetector->disableCalibrationMode();
                this->_jsonDocument["result"] = "OK";
            } else if (newState == "RESTORE_STATE") {
                this->_stateManager->restoreState();
                this->_jsonDocument["result"] = "OK";
            } else if (newState == "CALIBRATION") {
                // go to startup to make led flash, startup ends up in calibration
                this->_stateManager->setState(statemanagement::state_enum::STARTUP);
                this->_jsonDocument["result"] = "OK";
            } else if (newState == "SWITCH_TO_BLUETOOTH") {
                this->_stateManager->setState(statemanagement::state_enum::SWITCH_TO_BLUETOOTH);
                this->_jsonDocument["result"] = "OK";
            } else if (newState == "RACE") {
                this->_stateManager->setState(statemanagement::state_enum::RACE);
                this->_jsonDocument["result"] = "OK";
            } else if (newState == "SCAN") {
                this->_stateManager->setState(statemanagement::state_enum::SCAN);
                this->_rx5808->startScan(this->_storage->getFrequency());
                this->_jsonDocument["result"] = "OK";
            } else if (newState == "DEEPSCAN") {
                this->_stateManager->setState(statemanagement::state_enum::DEEPSCAN);
                this->_rx5808->startScan(this->_storage->getFrequency());
                this->_jsonDocument["result"] = "OK";
            } else if (newState == "VREF_OUTPUT") {
                this->_stateManager->setState(statemanagement::state_enum::VREF_OUTPUT);
                this->_jsonDocument["result"] = "OK";
#ifdef DEBUG
            } else {
                Serial.printf("invalid state: %s\n", newState.c_str());
#endif
            }
        }
        this->sendJson();
    });

    // the /api commands are for the webui
    this->_server.on("/api/devicedata", HTTP_POST, [&]() {
        this->_server.sendHeader("Connection", "close");
        if (this->_server.args() > 0) {
            DeserializationError error = deserializeJson(this->_jsonDocument, this->_server.arg(0));
            if (error) {
#ifdef DEBUG
                Serial.println(F("failed to parse config"));
                Serial.println(this->_server.arg(0));
                Serial.println(error.c_str());
#endif
                this->_server.send(500, "text/html", "failed to parse config: " + String(error.c_str()));
            } else {
#ifdef DEBUG
                Serial.printf("got config: %s\n", this->_server.arg(0).c_str());
#endif
                bool reboot = false;
                if (this->_jsonDocument["minimumLapTime"] > 1000 && this->_jsonDocument["minimumLapTime"] < 60000) {
                    this->_storage->setMinLapTime(this->_jsonDocument["minimumLapTime"]);
                }

                if (this->_jsonDocument["frequency"] >= 5325 && this->_jsonDocument["frequency"] <= 5945) {
                    if (this->_storage->getFrequency() != this->_jsonDocument["frequency"]) {
                        reboot = true;
                    }
                    this->_storage->setFrequency(this->_jsonDocument["frequency"]);
                }

                if (this->_jsonDocument["triggerThreshold"] > 10 && this->_jsonDocument["triggerThreshold"] < 1024) {
                    if (this->_storage->getTriggerThreshold() != this->_jsonDocument["triggerThreshold"]) {
                        reboot = true;
                    }
                    this->_storage->setTriggerThreshold(this->_jsonDocument["triggerThreshold"]);
                }

                if (this->_jsonDocument["triggerThresholdCalibration"] > 10 && this->_jsonDocument["triggerThresholdCalibration"] < 1024) {
                    if (this->_storage->getTriggerThresholdCalibration() != this->_jsonDocument["triggerThresholdCalibration"]) {
                        reboot = true;
                    }
                    this->_storage->setTriggerThresholdCalibration(_jsonDocument["triggerThresholdCalibration"]);
                }

                if (this->_jsonDocument["calibrationOffset"] > 1 && this->_jsonDocument["calibrationOffset"] < 256) {
                    this->_storage->setCalibrationOffset(this->_jsonDocument["calibrationOffset"]);
                }
                
                if (this->_jsonDocument["defaultVref"] > 999 && this->_jsonDocument["defaultVref"] < 1200) {
                    if (this->_storage->getDefaultVref() != this->_jsonDocument["defaultVref"]) {
                        reboot = true;
                    }
                    this->_storage->setDefaultVref(this->_jsonDocument["defaultVref"]);
                }

                if (this->_jsonDocument["filterRatio"] > 0.0 && this->_jsonDocument["filterRatio"] < 1.0) {
                    this->_storage->setFilterRatio(this->_jsonDocument["filterRatio"]);
                }
                if (this->_jsonDocument["filterRatioCalibration"] > 0.0 && this->_jsonDocument["filterRatioCalibration"] < 1.0) {
                    this->_storage->setFilterRatioCalibration(this->_jsonDocument["filterRatioCalibration"]);
                }
                if (this->_stateManager->isStateCalibration()) {
                    this->_rssi->setFilterRatio(this->_jsonDocument["filterRatioCalibration"]);
                } else {
                    this->_rssi->setFilterRatio(this->_jsonDocument["filterRatio"]);
                }

                if (this->_jsonDocument["triggerValue"] > 10 && this->_jsonDocument["triggerValue"] < 1024) {
                    // allow setting the trigger value outside of calibration mode
                    if (!this->_lapDetector->isCalibrating() && this->_jsonDocument["triggerValue"] != this->_lapDetector->getTriggerValue()) {
#ifdef DEBUG
                        Serial.printf("setting new trigger value: %d\n", this->_jsonDocument["triggerValue"]);
#endif
                        this->_lapDetector->setTriggerValue(this->_jsonDocument["triggerValue"]);
                    }
                }

                this->_storage->store();
                this->_lapDetector->init();
                
                String response = F("OK");
                if (reboot) {
                    response += " reboot";
                }
                this->_server.send(200, "text/html", response);
            }
        } else {
            this->_server.sendHeader("Connection", "close");
            this->_server.send(500, "text/html", "no arguments");
        }
    });

    this->_server.begin();
    this->_connected = true;
}

void WifiWebServer::handle() {
    this->_server.handleClient();
}