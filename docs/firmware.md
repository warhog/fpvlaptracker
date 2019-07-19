# firmware

## usage

### startup
the node tries to connect to its configured wifi first. if the wifi is available it starts into connected mode and connects and registeres to the web ui. if the wifi is not available it goes into standalone mode using bluetooth.

![flowchart startup](https://raw.githubusercontent.com/warhog/fpvlaptracker/master/docs/startup.png)

### leds
the leds show the state of the tracker node. the red led lights if 3.3v is available. the green led shows different states of the tracker.

after connecting the supply voltage the green led blinks the number of cells detected (2 blinks = 2s, 3 blinks = 3s, ...).

the green led blinks fast constantly when in standalone mode. in connected mode the led should be off.

the led can blink out error codes:

| number of blinks | reason |
|------------------|--------|
| 2 | bluetooth init failed |
| 3 | mDNS not started |
| 9 | shutdown voltage underrun |
| 10 | internal failure |


### buttons
there are 3 buttons on the node board:
* OTA
* RESET
* FLASH

![3d printed case opened](https://raw.githubusercontent.com/warhog/fpvlaptracker/master/hardware/fpvlaptrackerunit_open.jpg)

#### RESET
the RESET button does....a reset :>

#### FLASH
the FLASH button initiates the serial update mode of the ESP32. to initate serial update mode do:
1. press and hold FLASH button
2. press RESET button
3. stop pressing RESET button
4. wait 1s
5. stop pressing FLASH button
6. the esp32 is now in serial update mode and can be flashed using a serial adapter (3.3v!)

#### OTA
when the OTA button is pressed during startup of the node it enteres wifi access point mode. this is useful to do a OTA update when base station is not available. the username and password is `fltunit`, the ip address is fixed to `192.168.4.1`.

### modes
the node can operate in standalone or connected mode. in standalone mode bluetooth is used to communicate with the mobile app. in connected mode wifi is used to communicate with the web ui.

### webpage
when in connected mode the node is configurable und usable using the web ui. also it provides a simple webpage. using this webpage one can
* switch to bluetooth mode
* trigger ota (over-the-air) update
* load factory defaults
* start voltage calibration

TODO: screenshot

### bluetooth
when in standalone mode the node is configurable and usable using the mobile app over bluetooth

## flashing
**do not interrupt power during flashing**

### using serial adapter

### ota update
* start the node in connected mode or using the OTA button to start wifi ap (see OTA button description above)
* go to the webpage of the node (get ip from webui or 192.168.4.1 in wifi ap mode)
* select the .bin file you want to flash
* hit the update button
* wait some time, depending on the used browser it shows the upload state in bottom status bar
* after some time it should present a message with the status
* on success the node is restarted, verify the version using the webui or the node webpage

### voltage calibration
* start the node
* go to the node webpage
* hit voltage calibration link
* the node will output the voltage reference voltage on pin labeled VREF/J3
* connect a multimeter to the pin VREF/J3 on the pcb and ground
* get the voltage (should be between 999 and 1200 mV)
* reboot node
* goto webui node config or mobile app node config and set default voltage reference to the measured value

## building
the firmware uses the arduino stack. there is no restriction on IDE or anything. i used vscode with the arduino extension during development but use whatever is handy to you.

### prerequisites
install the esp32 sdk (i use 1.0.2)

install the following arduino libraries

|library|version|
|---|---|
|ArduinoJson|6.11.1|
|ResponsiveAnalogRead|1.2.1|

### target setup
* ESP32 dev module
* PSRAM: Disabled
* Partition Scheme: Minimal SPIFFS (1.9MB APP with OTA)
* CPU Frequency: 240 MHz
* Flash Mode: QIO
* Flash Frequency: 80 MHz
* Flash Size: 4MB (32Mb)
* Upload Speed: 921600
* Core Debug Level: None
