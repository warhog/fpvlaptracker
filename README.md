# fpvlaptracker

licensed under [MIT](LICENSE.md)


simple open source diy fpv vtx lap tracking system

## build status
current status: ![build status image](https://travis-ci.org/warhog/fpvlaptracker.svg?branch=master)


## features
- standalone and networked mode available
- uses your existing analog vtx to do the lap detection
- cheap and easy to build (through hole pcb, non SMD)
- 48 channels supported
- networked mode supports up to 8 tracker units


## how does it work?
the tracker unit measures the video signal strength (rssi). when it is higher than the defined upper threshold it counts a lap. then the unit enters the wait period were it is locked for adjusted time. after that time the video signal strength must be lower than the lower threshold before the next lap can be detected.

the tracker unit has a stand alone mode for single participants or a networked mode supporting multiple participants and a much better web ui. in standalone mode the tracker unit serves a web page that is accessible using wifi network. the networked mode is used with any hardware that can run the java software backend.


## which hardware and software is used?
the hardware consists of a nodemcu (ESP8266) board with a boscam rx5808 receiver module connected. 
the nodemcu board runs the tracker unit firmware (written using Arduino for ESP8266).
the backend of the race software for networked mode was written in java using spring boot with jooq as database access 
library. the frontend was developed using angular 1. the race software can be run on almost
every platform that supports java se.

[detailed hardware instructions](docs/hardware.md)


## why you have chosen that technology stack?
arduino because of its easy handling, nodemcu to keep the hardware development as easy as
possible.
spring-boot because it allows rapid and powerful webservice development and easy to
set up (nothing needs to be installed).
angular because i always wanted to use and try it out. sadly i started development before angular 2 was released.
maybe i'll port it to a newer angular version someday.


## build instructions
you need gradle, npm and gulp installed. if these requirements are fulfilled you can easily
run "gradle build" from the command line to build the whole project.

[detailed build instructions](docs/build.md)


## tested devices
immersion rc vtx

blade inductrix fpv

boscam ts5828

eachine et526



## sound files used
_finishing:_ [freesound](https://www.freesound.org/people/jobro/sounds/60444/)

_lap:_ [freesound](https://www.freesound.org/people/StaneStane/sounds/73560/)

_participant register:_ [freesound](https://www.freesound.org/people/Zott820/sounds/209578/)

_invalid lap_: [freesound](https://www.freesound.org/people/Lalks/sounds/316841/)

_finish lap_: [freesound](https://www.freesound.org/people/wildweasel/sounds/39021/)
