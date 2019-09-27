#pragma once

#include <Arduino.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <Update.h>

#include "storage.h"
#include "rssi.h"
#include "frequency.h"
#include "rx5808.h"
#include "statemanager.h"
#include "lapdetector.h"
#include "batterymgr.h"
#include "commtools.h"

namespace comm {
    class WifiWebServer {
        public:
            WifiWebServer(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector,
                battery::BatteryMgr *batteryMgr, const char *version, statemanagement::StateManager *stateManager,
                unsigned long *loopTime) :
                _storage(storage), _rssi(rssi), _rx5808(rx5808), _lapDetector(lapDetector), _batteryMgr(batteryMgr),
                _version(version), _stateManager(stateManager), _loopTime(loopTime), _jsonDocument(1024) {}
            void begin();
            void handle();
            bool isConnected() {
                return this->_connected;
            }
            void disconnect() {
                this->_connected = false;
                this->_server.stop();
            }
        private:
            void prepareJson();
            void sendJson();
            String concat(String text);

            WebServer _server;
            util::Storage *_storage;
            lap::Rssi *_rssi;
            radio::Rx5808 *_rx5808;
            lap::LapDetector *_lapDetector;
            battery::BatteryMgr *_batteryMgr;
            statemanagement::StateManager *_stateManager;
            DynamicJsonDocument _jsonDocument;
            const char *_version;
            bool _connected;
            unsigned long *_loopTime;

            char const *_header = R"(
                <html>
                <head>
                    <style>
                        body { font-family: Arial; background-color: #ffffff; } 
                        a { color: #b35000; } 
                        #content { margin: auto; border-radius: 5px; border: 1px solid #b6b6b6; padding: 20px; width: 600px;} 
                        #overlay { position: fixed; display: none; width: 100%; height: 100%; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0,0,0,0.5); z-index: 2; cursor:wait;}
                        #overlaydata { width: 500px; padding: 20px; border: 1px solid #005b74; border-radius: 5px; background-color: #bff1ff; position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); }
                        h1,h2,h3 { color: #0082a6; }
                        .button { font-size: 1.0em; border: none; border-radius: 5px; background-color: #80e3ff; color: #005b74; text-decoration: none; display: inline-block; margin: 5px; padding: 8px;}
                    </style>
                    <title>fpvlaptracker node</title>
                </head>
                <body>
                <div id='overlay'>
                    <div id='overlaydata'>
                        <div id='progressbar'>
                            firmware upload progress:<br />
                            <progress id='progress' style='margin-top: 10px; width: 90%;'></progress> <span id='percent'></span>
                        </div>
                        <div id='rebooting' style='display: none;'>
                            upload finished.<br />rebooting...please wait...
                        </div>
                    </div>
                </div>
                <div id='content'>
                <h1>fpvlaptracker32</h1>
            )";

            char const *_footer = R"(
                </div>
                <script>
                function overlay() {
                    document.getElementById('overlay').style.display = 'block';
                }
                if (document.getElementById('upload_form') !== null) {
                    document.getElementById('upload_form').onsubmit = function (evt) {
                        evt.preventDefault();
                        overlay();
                        let form = document.getElementById('upload_form');
                        let data = new FormData(form);
                        let xhr = new XMLHttpRequest();
                        let progress = document.getElementById('progress');
                        let percent = document.getElementById('percent');
                        progress.value = 0;
                        progress.max = 100;
                        percent.innerHTML = '0 %';
                        xhr.upload.addEventListener('progress', function(evt) {
                            if (evt.lengthComputable) {
                                let per = Math.round(evt.loaded / evt.total * 100);
                                progress.value = per;
                                percent.innerHTML = (per < 10 ? '0' : '') + per + ' %';
                            }
                        }, false);
                        xhr.upload.addEventListener('error', function(evt) {
                            alert('error during upload!');
                            console.log('error during upload', evt);
                        }, false);
                        xhr.upload.addEventListener('load', function(evt) {
                            document.getElementById('rebooting').style.display = 'block';
                            document.getElementById('progressbar').style.display = 'none';
                            window.setTimeout(function() {
                                location.reload();
                            }, 15000);
                    }, false);
                        xhr.open('POST', '/update');
                        xhr.send(data);
                    }
                }

                function uploadChange() {
                    let file = document.getElementById('update').files[0];
                    if (!file) {
                        console.log('no file selected');
                        return;
                    }

                    if (file.name.split('.').pop() != 'bin') {
                        alert('no .bin file selected. please select a .bin file.');
                    }
                }
                </script>
                </body></html>
            )";

            char const *_serverIndex = R"(
                chip id: %CHIPID%<br />
                current version: %VERSION%<br />
                build date: %DATETIME%<br /><br />
                <a class='button' href='/bluetooth'>switch to bluetooth</a> <a class='button' href='/reset'>restart node</a><br />
                <hr size='1'/>
                <h2>maintenance</h2>

                <h3>firmware update</h3>
                select .bin file to flash and press update to start the over the air update.<br />
                <b>attention:</b> do not interrupt power supply, else you might need to reflash using the serial adapter!<br />
                <form method='POST' action='#' enctype='multipart/form-data' id='upload_form'>
                    <input type='file' id='update' name='update' onchange='uploadChange();'/>
                    <input type='submit' class='button' value='update' />
                </form>

                <h3>voltage reference</h3>
                calibration of the voltage readings<br />
                <a class='button' href='/vref'>output voltage reference</a><br />

                <h3>factory defaults</h3>
                <b>attention:</b> all data is reset to factory defaults except voltage reference<br />
                <a class='button' href='/factorydefaults'>restore factory defaults</a>
            )";

    };
}
