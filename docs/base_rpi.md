# running fpvlaptracker network gateway on a raspberry pi

## setup wifi access point

https://frillip.com/using-your-raspberry-pi-3-as-a-wifi-access-point-with-hostapd/

## copy fpvlaptracker host software to the pi
copy the fpvlaptracker.jar file, the application.properties and the audio folder to the raspberry pi (using scp, winscp, ...)

## make fpvlaptracker autostart
i appended the following to my /etc/rc.local file:

`cd /home/pi/`

`java -jar fpvlaptracker-0.1.jar`


## config file
using the [config file](/docs/configuration.md) best set the following options:

|option|value|description|
|---|---|---|
|shutdown.machine|true|set this to true so that the web ui shutdown command also shuts down the raspberry pi|
|audio.language||set this to your preferred language|
|admin.password|admin|set to your favourite admin password|
