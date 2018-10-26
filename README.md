# fpvlaptracker

licensed under [MIT](LICENSE.md)


simple open source diy fpv vtx lap tracking system


## features
- standalone and network mode available
- uses your existing analog vtx to do the lap detection
- 48 channels supported
- networked mode supports up to 8 tracker units and comfortable access using web ui

## how does it work?
the tracker unit measures the video signal strength (rssi) of the selected video channel to detect laps. it has a stand alone mode for single participants or a network mode supporting multiple participants and a web ui. in standalone mode the tracker unit is controlled using bluetooth and a mobile app for configuration and single user race (e.g. practicing). the network mode is used with any hardware that can run the java software backend.

### app
the tracker unit is accessed using bluetooth and a mobile application (older version was creating an wifi ap and serving a webpage but that was horrible slow). using the app one can configure the unit or run single user races (e.g. practicing).

the application is available in the play store (only android right now, [play store](https://play.google.com/store/apps/details?id=de.warhog.fpvlaptracker)), the source is found [here](https://github.com/warhog/fpvlaptracker-app).

### standalone mode
the tracker can be used in standalone mode for single user races.

### network mode
in network mode the tracker connects to a host software using wifi. races and particpants can be organized inside the host software. the host software includes a webserver that serves a web ui for watching / controlling races. parts of the ui are only accessible using a password (defaults to Admin/admin).

the web ui has websocket support for instant updates on laps and also can play sounds and text using the html5 api.

## tested devices
immersion rc vtx

blade inductrix fpv

boscam ts5828

eachine et526

tbs unify pro hv

betafpv 75s


## setup
[setup instructions](docs/setup.md)

## which hardware and software is used?
the hardware consists of a esp32 board with a boscam rx5808 receiver module connected. the esp32 runs the tracker firmware (written using Arduino software platform).
the backend of the race software for network mode was written in java using spring boot with jooq as database access 
library. the frontend was developed using angularjs. the race software can be run on almost every platform that supports java se.

[detailed hardware instructions](docs/hardware.md)


### why you have chosen that technology stack?
arduino because of its easy handling. spring-boot because it allows rapid and powerful webservice development and easy to set up (nothing needs to be installed). angularjs because i always wanted to use and try it out. sadly i started development before angular 2 was released. maybe i'll port it to a newer angular version someday. the mobile app is developed using ionic framework.

# repository information
this reposity contains the network mode components. the [fpvlaptracker-firmware](https://github.com/warhog/fpvlaptracker-firmware) repository contains the firmware sources. the [fpvlaptracker-hardware](https://github.com/warhog/fpvlaptracker-hardware) repository is containing all the hardware stuff for building an own tracker unit. The app can be found in the [fpvlaptracker-app](https://github.com/warhog/fpvlaptracker-app) repository.

## build instructions
you need gradle, npm and gulp installed. if these requirements are fulfilled you can easily
run "gradle build" from the command line to build the whole project.

[detailed build instructions](docs/build.md)


## build status
current status: ![build status image](https://travis-ci.org/warhog/fpvlaptracker.svg?branch=master)

## sound files used
_lap:_ [freesound](https://www.freesound.org/people/StaneStane/sounds/73560/)

texts: [fromtexttospeech.com](http://www.fromtexttospeech.com/)
