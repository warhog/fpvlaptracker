#include "webupdate.h"

//#define DEBUG

void WebUpdate::begin() {

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

        this->_server.send(404, "text/plain", message);
    });

    this->_server.on("/", HTTP_GET, [&]() {
        this->_server.sendHeader("Connection", "close");
        String temp(this->_serverIndex);
        temp.replace("%VERSION%", this->_version);
        this->_server.send(200, "text/html", temp);
    });
    this->_server.on("/update", HTTP_POST, [&]() {
        this->_server.sendHeader("Connection", "close");
        this->_server.send(200, "text/html", (Update.hasError()) ? this->_failedResponse : this->_successResponse);
        this->_server.client().setNoDelay(true);
        delay(100);
        this->_server.client().stop();
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
    this->_server.begin();

}

void WebUpdate::run() {
    this->_server.handleClient();
}