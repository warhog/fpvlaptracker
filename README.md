# fpvlaptracker [![Build Status](https://travis-ci.org/warhog/fpvlaptracker.svg?branch=master)](https://travis-ci.org/warhog/fpvlaptracker)

licensed under [MIT](LICENSE.md)


open source diy fpv vtx lap tracking system


## features
- uses your analog vtx to do the lap detection
- 48 channels supported
- standalone (app) and connected mode available
- connected mode supports up to 8 tracker units and comfortable access using web ui

## how does it work?
the tracker unit measures the video signal strength (rssi) of the selected video channel to detect laps using peak detection. it has a stand alone mode for single participants or a connected mode supporting multiple participants and a web ui. in standalone mode the tracker unit is controlled using bluetooth and a mobile app for configuration and single user race (e.g. practicing). the connected mode is used with any hardware that can run the java software backend.

### app
the tracker unit is accessed using bluetooth and a mobile application (older version was creating an wifi ap and serving a webpage but that was horrible slow). using the app one can configure the unit or run single user races (e.g. practicing).

the application is available in the play store (only android right now, [play store](https://play.google.com/store/apps/details?id=de.warhog.fpvlaptracker)), the source is in the app folder).

### standalone mode
the tracker can be used in standalone mode for single user races.

### connected mode
running in the connected mode the tracker connects to a host software using wifi. races and particpants can be organized inside the host software. the host software includes a webserver that serves a web ui for watching / controlling races.

## tested devices
* immersion rc vtx
* blade inductrix fpv
* boscam ts5828
* eachine et526
* tbs unify pro hv
* betafpv 75s

## setup
[setup instructions](docs/setup.md)
    
## which hardware and software is used?
the hardware consists of a esp32 board with a boscam rx5808 receiver module connected. the esp32 runs the tracker firmware (written using Arduino software platform).
the backend of the race software for connected mode was written in java using spring boot 2. the frontend was developed using angular 8. the race software can be run on almost every platform that supports java se.
due to the low price, simplicity and size a raspberry pi (>= 2) powered by a power bank is the optimum solution for mobile usage.

# repository information
this repository contains everything needed to build your own tracker(s).

* app: the mobile app
* firmware-single-node: firmware for a single node
* hardware-single-node: hardware for a single node
* web: the java backend and web frontend

## sound files used
_lap:_ [freesound](https://www.freesound.org/people/StaneStane/sounds/73560/)

texts: [fromtexttospeech.com](http://www.fromtexttospeech.com/)
